package com.abcfinancial.api.billing.subscriptionmanagement.subscription.service;

import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.abcfinancial.api.billing.generalledger.invoice.repository.InvoiceRepository;
import com.abcfinancial.api.billing.generalledger.invoice.service.InvoiceService;
import com.abcfinancial.api.billing.generalledger.kafka.consumer.InvoiceListener;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.scheduler.EventScheduler;
import com.abcfinancial.api.billing.scheduler.schedules.Schedule;
import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionCancel;
import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionDetails;
import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionExpired;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.MemberCreation;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.repository.MemberCreationRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer.SubscriptionAutoRenewGenerator;
import com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer.SubscriptionExpireGenerator;
import com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer.SubscriptionGenerator;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.service.PricingService;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject.PricingDetailsVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.MemberSubscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.MemberSubscriptionId;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.SubscriptionItem;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemExpirationStart;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.RenewType;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.SubscriptionTypeEnum;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionMembersRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.*;
import com.abcfinancial.api.billing.utility.common.AppConstants;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import com.abcfinancial.api.billing.utility.exception.CustomErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import com.abcfinancial.api.common.domain.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.Months;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.abcfinancial.api.billing.utility.common.MessageUtils.ERROR_MESSAGE_INVALID_DURATION;

@Slf4j
@Service

public class SubscriptionService
{
    @Autowired
    private SubscriptionGenerator subscriptionGenerator;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MemberCreationRepository memberCreationRepository;
    @Autowired
    private SubscriptionMembersRepository subscriptionMembersRepository;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private KafkaOperations<String, Object> kafkaOperations;
    @Autowired
    private EventScheduler eventScheduler;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private InvoiceListener invoiceListner;
    @Autowired
    private SubscriptionAutoRenewGenerator subscriptionAutoRenewGenerator;
    @Autowired
    private SubscriptionExpireGenerator subscriptionExpireGenerator;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private PricingService pricingService;

    private static org.joda.time.LocalDate localDateToJodaLocalDate( LocalDate localDate )
    {
        return new org.joda.time.LocalDate( localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth() );
    }

    private static boolean distingtMemberId( List<UUID> memberIdList )
    {
        Set<UUID> accountIdSet = new HashSet<>();
        for( UUID memberId : memberIdList )
        {
            if( accountIdSet.contains( memberId ) )
            {
                return false;
            }
            accountIdSet.add( memberId );
        }
        return true;
    }

    @Transactional
    public SubscriptionVO createSubscription( SubscriptionVO subscriptionVO, boolean agreementFlag )
    {
        List<UUID> memberIdListStore = subscriptionVO.getMemberIdList();
        if( null != subscriptionVO.getMemberId() )
        {
            List<UUID> memberIdList = new ArrayList<>();
            memberIdList.add( subscriptionVO.getMemberId() );
            subscriptionVO.setMemberIdList( memberIdList );
        }
        validateMandatoryFieldsForSubscription( subscriptionVO, memberIdListStore, agreementFlag );
        log.debug( "Create Subscription: {}", subscriptionVO.getSubId() );
        LocalDate invoiceDateForResponse = subscriptionVO.getInvoiceDate();
        Subscription subscription = subscriptionCreation( subscriptionVO );
        if( subscriptionVO.isAutoRenew() )
        {
            subscriptionVO.setRenewable( true );
            subscription.setRenewable( true );
            if( null == subscriptionVO.getSubscriptionTypeEnum() )
            {
                subscription.setSubscriptionTypeEnum( SubscriptionTypeEnum.SECONDARY );
            }
        }
        else
        {
            //Added the condition as P3-3016
            subscription.setSubscriptionTypeEnum( SubscriptionTypeEnum.SECONDARY );
        }

        if( subscriptionVO.isOpenEnded() )
        {
            if( subscriptionVO.getInvoiceDate().isBefore( subscriptionVO.getStart() ) )
            {
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVOICE_DATE ) ) );
            }
            List dateList = new ArrayList();
            LocalDate nextDate = subscription.getInvoiceDate();
            while( nextDate.isBefore( LocalDate.now( Clock.systemUTC() ) ) )
            {
                dateList.add( nextDate );
                nextDate = calculateNextSubBillingDate( subscriptionVO ).toLocalDate();
                subscriptionVO.setInvoiceDate( nextDate );
            }
            dateList.forEach( dateInList -> {
                subscription.setInvoiceDate( (LocalDate) dateInList );
                createInvoice( subscription );
            } );

            subscription.setInvoiceDate( nextDate );
            Optional<UUID> scheduleInvoicesId = invoiceTriggerEventOpenEnded( subscription, subscriptionVO );
            if( scheduleInvoicesId.isPresent() )
            {
                subscription.setScheduleInvoicesId( scheduleInvoicesId.get() );
            }
            subscription.setOpenEnded( subscriptionVO.isOpenEnded() );
            //to updating scheduleInvoicesId in subscription
            subscriptionVO.setSubId( subscription.getSubId() );
        }
        else
        {
            if( subscriptionVO.getInvoiceDate().isBefore( subscriptionVO.getStart() ) ||
                ( subscriptionVO.getInvoiceDate().isAfter( subscriptionVO.getExpirationDate() ) ) ||
                subscriptionVO.getInvoiceDate().isBefore( LocalDate.now( Clock.systemUTC() ).minusDays( 60 ) ) )
            {
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_INVOICE_DATE ) ) );
            }
            subscriptionVO.setSubId( subscription.getSubId() );
            Optional<UUID> invoiceExpireId = invoiceExpireEvent( subscriptionVO );
            if( subscriptionVO.getFrequency() == Frequency.DAILY || subscriptionVO.getFrequency() == Frequency.WEEKLY ||
                subscriptionVO.getFrequency() == Frequency.EVERY_OTHER_WEEK || subscriptionVO.getFrequency() == Frequency.MONTHLY ||
                subscriptionVO.getFrequency() == Frequency.EVERY_OTHER_MONTH || subscriptionVO.getFrequency() == Frequency.ANNUALLY ||
                subscriptionVO.getFrequency() == Frequency.QUARTERLY || subscriptionVO.getFrequency() == Frequency.SEMIANNUALLY )
            {
                List dateList = new ArrayList();
                LocalDate nextDate = subscription.getInvoiceDate();
                if( null != subscriptionVO.getRenewalOptions() && subscriptionVO.isAutoRenew() &&
                    !subscriptionVO.getInvoiceDate().isEqual( subscriptionVO.getRenewalOptions().getRenewDate() ) )
                {
                    pastInvoiceData( nextDate, subscriptionVO, dateList );
                    long days = ChronoUnit.DAYS.between( invoiceDateForResponse, subscriptionVO.getRenewalOptions().getRenewDate() );

                    if( days <= subscriptionVO.getDuration() && !dateList.isEmpty() && ( subscriptionVO.getDuration() <= dateList.size() ) )
                    {
                        dateList = dateList.subList( 0, (int) days );
                    }

                }
                else if( !subscriptionVO.isAutoRenew() )
                {
                    pastInvoiceData( nextDate, subscriptionVO, dateList );
                    log.debug( "Create invoice for past base subscription" );

                }
                if( dateList.size() >= subscriptionVO.getDuration() )
                {
                    List durationDateList = dateList.subList( 0, ( subscriptionVO.getDuration() ) );

                    durationDateList.forEach( durationDateInList -> {
                        subscription.setInvoiceDate( (LocalDate) durationDateInList );
                        createInvoice( subscription );
                    } );
                }
                else
                {
                    dateList.forEach( dateInList -> {
                        subscription.setInvoiceDate( (LocalDate) dateInList );
                        createInvoice( subscription );
                    } );
                    subscription.setInvoiceDate( nextDate );
                    if( null != subscriptionVO.getRenewalOptions() && subscriptionVO.isAutoRenew() &&
                        !subscriptionVO.getInvoiceDate().isEqual( subscriptionVO.getRenewalOptions().getRenewDate() ) )
                    {
                        invoiceSchedule( subscriptionVO, subscription );

                    }
                    else if( !subscriptionVO.isAutoRenew() )
                    {
                        invoiceSchedule( subscriptionVO, subscription );
                        log.debug( "Invoice Schedule for base subscription" );
                    }

                    if( invoiceExpireId.isPresent() )
                    {
                        subscription.setSubExpiredEventId( invoiceExpireId.get() );
                    }
                }
                subscription.setInvoiceDate( invoiceDateForResponse );
            }
        }

        if( subscriptionVO.getStart().isAfter( LocalDate.now( Clock.systemUTC() ) ) )
        {
            activeTriggerEvent( subscription, subscriptionVO );
        }
        //to updating scheduleInvoicesId in subscription
        subscriptionGenerator.send( subscription, subscriptionVO );
        subscriptionRepository.save( subscription );
        if( null != subscriptionVO.getRenewalOptions() && null == subscriptionVO.getSubRefferalId() )
        {
            if( subscription.getExpirationDate().isAfter( subscriptionVO.getRenewalOptions().getRenewDate() ) )
            {
                subscription.setExpirationDate( subscriptionVO.getRenewalOptions().getRenewDate() );
            }
            subscriptionRepository.save( subscription );
            subscriptionVO = renewSubscription( subscriptionVO, subscription );
        }
        subscriptionVO.setInvoiceDate( invoiceDateForResponse );
        if( null != subscriptionVO.getMemberId() )
        {
            subscriptionVO.getMemberId();
            subscriptionVO.setMemberIdList( null );
        }

        return subscriptionVO;
    }

    private void invoiceSchedule( SubscriptionVO subscriptionVO, Subscription subscription )
    {
        Optional<UUID> scheduleInvoicesId = invoiceTriggerEvent( subscription, subscriptionVO );
        if( scheduleInvoicesId.isPresent() )
        {
            subscription.setScheduleInvoicesId( scheduleInvoicesId.get() );
        }
    }

    private void pastInvoiceData( LocalDate nextDate, SubscriptionVO subscriptionVO, List dateList )
    {
        while( ( nextDate.isBefore( LocalDate.now( Clock.systemUTC() ) ) ) && nextDate.isBefore( subscriptionVO.getExpirationDate() ) )
        {
            dateList.add( nextDate );
            nextDate = calculateNextSubBillingDate( subscriptionVO ).toLocalDate();
            subscriptionVO.setInvoiceDate( nextDate );
        }
    }

    public SubscriptionVO renewSubscription( SubscriptionVO subscriptionVO, Subscription subscription )
    {
        Subscription renewSubscription = new Subscription();
        renewSubscription.setStart( subscriptionVO.getRenewalOptions().getRenewDate() );
        renewSubscription.setLocationId( subscriptionVO.getLocationId() );
        renewSubscription.setPlanId( null );
        renewSubscription.setSalesEmployeeId( subscriptionVO.getSalesEmployeeId() );
        renewSubscription.setPlanVersion( 0L );
        renewSubscription.setName( null );
        List<SubscriptionItem> items = subscriptionVO.getItems().stream().map( temp -> {
            SubscriptionItem subscriptionItem = new SubscriptionItem();
            subscriptionItem.setLocId( temp.getLocId() );
            subscriptionItem.setItemId( temp.getItemId() );
            subscriptionItem.setVersion( temp.getVersion() );
            subscriptionItem.setItemName( temp.getItemName() );
            subscriptionItem.setPrice( subscriptionVO.getRenewalOptions().getRenewAmount() );
            subscriptionItem.setQuantity( temp.getQuantity() );
            subscriptionItem.setType( temp.getType() );
            subscriptionItem.setExpirationStart( temp.getExpirationStart() );
            subscriptionItem.setItemCategoryId( temp.getItemCategoryId() );
            return subscriptionItem;
        } ).collect( Collectors.toList() );
        renewSubscription.setItems( items );
        renewSubscription.setRenewInvoiceDate( subscriptionVO.getRenewalOptions().getRenewDate() );
        renewSubscription.setSubscriptionTypeEnum( SubscriptionTypeEnum.RENEW );
        if( subscriptionVO.getRenewalOptions().getRenewType() == RenewType.OPEN )
        {
            renewSubscription.setRenewType( subscriptionVO.getRenewalOptions().getRenewType() );
        }
        else
        {
            renewSubscription.setDuration( subscriptionVO.getRenewalOptions().getRenewDuration() );
            renewSubscription.setExpirationDate( subscriptionVO.getRenewalOptions().getRenewExpireDate() );
            renewSubscription.setRenewType( subscriptionVO.getRenewalOptions().getRenewType() );
        }
        renewSubscription.setAccount( subscription.getAccount() );
        renewSubscription.setFrequency( subscriptionVO.getRenewalOptions().getRenewFrequency() );
        renewSubscription.setRenewInvoiceDate( subscriptionVO.getRenewalOptions().getRenewInvoiceDate() );
        Subscription subscriptionResponse = subscriptionRepository.save( renewSubscription );
        RenewalOptionsVO renewalOptions = new RenewalOptionsVO();
        renewalOptions.setRenewSubId( subscriptionResponse.getSubId() );
        renewalOptions.setRenewDate( subscriptionVO.getRenewalOptions().getRenewDate() );
        renewalOptions.setRenewInvoiceDate( subscriptionResponse.getRenewInvoiceDate() );
        if( subscriptionVO.getRenewalOptions().getRenewType() == RenewType.TERM )
        {
            renewalOptions.setRenewDuration( subscriptionVO.getRenewalOptions().getRenewDuration() );
            renewalOptions.setRenewExpireDate( subscriptionVO.getRenewalOptions().getRenewExpireDate() );
        }
        renewalOptions.setRenewFrequency( subscriptionVO.getRenewalOptions().getRenewFrequency() );
        renewalOptions.setRenewType( subscriptionVO.getRenewalOptions().getRenewType() );
        renewalOptions.setRenewAmount( subscriptionVO.getRenewalOptions().getRenewAmount() );
        subscriptionVO.setRenewalOptions( renewalOptions );
        subscriptionMemberCreation( subscriptionVO, subscriptionResponse );
        if( ( subscriptionVO.getRenewalOptions().getRenewDate().isBefore( subscription.getExpirationDate() )
              || subscriptionVO.getRenewalOptions().getRenewDate().isEqual( subscription.getExpirationDate() ) )
            && ( subscriptionVO.getRenewalOptions().getRenewDate().isBefore( LocalDate.now() )
                 || subscriptionVO.getRenewalOptions().getRenewDate().equals( LocalDate.now() ) ) )
        {
            subscription.setExpirationDate( subscriptionVO.getRenewalOptions().getRenewDate() );
            boolean cancelScheduledInvoicesStatus = false;
            if( null != subscription.getScheduleInvoicesId() )
            {
                cancelScheduledInvoicesStatus = eventScheduler.cancelScheduledInvoices( subscription.getScheduleInvoicesId() );
                subscription.setScheduleInvoicesId( null );
            }
            log.debug( "****canceled the scheduled quartz job id {} for remaining invoices - Status {}", subscription.getScheduleInvoicesId(), cancelScheduledInvoicesStatus );
            subscription.setSubExpiredEventId( null );
            subscriptionVO.setActive( false );
            SubscriptionExpired subscriptionExpired =
                new SubscriptionExpired( subscriptionVO.getLocationId(), subscriptionVO.getAccountId(), subscription.getSubId(), subscriptionVO.getExpirationDate(),
                    AppConstants.ISFALSE );
            subscription.setActive( false );
            subscriptionExpireGenerator.send( subscriptionExpired );
        }
        //  renewSubscription.setRenewRefId( subscription.getSubId() );
        renewSubscription.setSubPrevRefId( subscription.getSubId() );
        Subscription renewSubscriptionResponse = subscriptionRepository.save( renewSubscription );
        subscription.setSubNextRefId( renewSubscriptionResponse.getSubId() );
        subscriptionRepository.save( subscription );
        renewSubscription.setRenewInvoiceDate( subscriptionVO.getRenewalOptions().getRenewInvoiceDate() );
        if( subscriptionVO.getRenewalOptions().getRenewDate().isBefore( LocalDate.now() ) )
        {
            renewSubscription.setPameIdAccount( subscriptionVO.isPameIdAccount() );
            subscriptionAutoRenewGenerator.send( renewSubscription );
        }
        else
        {
            Optional<UUID> subscriptionAutoRenewId = renewScheduler( renewSubscription, subscriptionVO );
            if( subscriptionAutoRenewId.isPresent() )
            {
                log.debug( "subscriptionAutoRenewId : {} ", subscriptionAutoRenewId.get() );
                subscription.setSubScheduleRenewalId( subscriptionAutoRenewId.get() );
                subscriptionRepository.save( renewSubscription );
            }
        }
        return subscriptionVO;
    }

    private void validateRenew( SubscriptionVO subscriptionVO )
    {
        Optional<LocalDate> renewDate = Optional.ofNullable( subscriptionVO.getRenewalOptions().getRenewDate() );
        if( !renewDate.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_DATE ) ) );
        }
        Optional<LocalDate> renewInvoiceDate = Optional.ofNullable( subscriptionVO.getRenewalOptions().getRenewInvoiceDate() );
        if( !renewInvoiceDate.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_INVOICE_DATE ) ) );
        }
        Optional<Frequency> renewFrequency = Optional.ofNullable( subscriptionVO.getRenewalOptions().getRenewFrequency() );
        if( !renewFrequency.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_FREQUENCY_VALUE ) ) );
        }
        Optional<RenewType> renewType = Optional.ofNullable( subscriptionVO.getRenewalOptions().getRenewType() );
        if( !renewType.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEWTYPE_VALUE ) ) );
        }

        if( subscriptionVO.getRenewalOptions().getRenewDate().isBefore( LocalDate.now() ) || subscriptionVO.getRenewalOptions().getRenewDate().isAfter( LocalDate.now() ) )
        {
            if( subscriptionVO.getRenewalOptions().getRenewDate().isBefore( subscriptionVO.getStart() ) )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEWDATE_BEFORE_SUB_START ) ) );
            }

            if( subscriptionVO.getRenewalOptions().getRenewDate() != null &&
                subscriptionVO.getRenewalOptions().getRenewDate().isBefore( subscriptionVO.getExpirationDate().minusDays( 90 ) ) )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_START_DATE ) ) );
            }
            if( subscriptionVO.getRenewalOptions().getRenewInvoiceDate().isBefore( subscriptionVO.getExpirationDate().minusDays( 60 ) ) )
            {
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_INVALID_INVOICE_DATE ) ) );
            }

        }
        validateRenews( subscriptionVO );
    }

    private void validateRenews( SubscriptionVO subscriptionVO )
    {
        if( subscriptionVO.getRenewalOptions().getRenewInvoiceDate().isBefore( subscriptionVO.getRenewalOptions().getRenewDate() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_INVOICE_DATE_BEFORE_RENEW_START ) ) );
        }

        if( RenewType.OPEN == subscriptionVO.getRenewalOptions().getRenewType() && 0 != subscriptionVO.getRenewalOptions().getRenewDuration() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DURATIO_RENEWTYPE ) ) );
        }
        Optional<BigDecimal> renewAmount = Optional.ofNullable( subscriptionVO.getRenewalOptions().getRenewAmount() );
        if( !renewAmount.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_AMOUNT ) ) );
        }
        if( 0 > subscriptionVO.getRenewalOptions().getRenewAmount().compareTo( BigDecimal.ZERO ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(),
                Subscription.class, applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_AMOUNT_NEGATIVE ) ) );
        }
        if( null == subscriptionVO.getRenewalOptions().getRenewExpireDate() && RenewType.TERM == subscriptionVO.getRenewalOptions().getRenewType() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(),
                Subscription.class, applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_EXPIRATION ) ) );
        }
        validateRenewSub( subscriptionVO );
    }

    private void validateRenewSub( SubscriptionVO subscriptionVO )
    {
        if( null != subscriptionVO.getRenewalOptions().getRenewExpireDate() && RenewType.OPEN == subscriptionVO.getRenewalOptions().getRenewType() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(),
                Subscription.class, applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_EXPIRATION ) ) );
        }
        if( subscriptionVO.getRenewalOptions().getRenewType() == RenewType.TERM &&
            subscriptionVO.getRenewalOptions().getRenewExpireDate().isBefore( subscriptionVO.getRenewalOptions().getRenewDate() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(),
                Subscription.class, applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_EXPIRATION_BEFORE ) ) );
        }
        Integer renewalDuration = subscriptionVO.getRenewalOptions().getRenewDuration();
        if( ( subscriptionVO.getRenewalOptions().getRenewType() == RenewType.TERM ) )
        {
            if( subscriptionVO.getRenewalOptions().getRenewInvoiceDate().isAfter( subscriptionVO.getRenewalOptions().getRenewExpireDate() ) )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_INVOICE_DATE_AFTER_RENEW_EXP ) ) );
            }
            if( renewalDuration <= 0 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_DURATION ) ) );
            }
        }
    }

    public Optional<UUID> renewScheduler( Subscription renewSubscription, SubscriptionVO subscriptionVO )
    {
        return eventScheduler.subscriptionAutoRenew( Schedule.<SubscriptionDetails>builder()
            .start( subscriptionVO.getRenewalOptions().getRenewDate().atTime( 1, 30 ) ).repeating( false )
            .properties( SubscriptionDetails.builder().locationId( renewSubscription.getLocationId() )
                                            .subscriptionId( renewSubscription.getSubId() ).memberIdList( subscriptionVO.getMemberIdList() )
                                            .isPameIdAccount( subscriptionVO.isPameIdAccount() ).build() )
            .build() );
    }

    private void validateMandatoryFieldsForSubscription( SubscriptionVO subscriptionVO, List<UUID> memberIdListStore, boolean agreementflag )
    {
        Optional<LocalDate> startDate = Optional.ofNullable( subscriptionVO.getStart() );
        if( !startDate.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_START_DATE ) ) );
        }
        Optional<LocalDate> invoiceDate = Optional.ofNullable( subscriptionVO.getInvoiceDate() );
        if( !invoiceDate.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVOICE_DATE ) ) );
        }
        if( subscriptionVO.getExpirationDate() == null && ( 0 <= subscriptionVO.getDuration() ) && !subscriptionVO.isOpenEnded() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DURATION_VALUE ) ) );
        }
        if( ( subscriptionVO.getExpirationDate() != null && ( subscriptionVO.isOpenEnded() == AppConstants.ISTRUE ) ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EXPIRATION_DATE_NOT_ALLOWED ) ) );
        }

        if( subscriptionVO.isOpenEnded() == AppConstants.ISFALSE && subscriptionVO.getExpirationDate().isBefore( subscriptionVO.getStart() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_START_DATE_EXP_DATE ) ) );
        }
        validateSub( subscriptionVO, memberIdListStore, agreementflag );
    }

    private void validateSub( SubscriptionVO subscriptionVO, List<UUID> memberIdListStore, boolean agreementflag )
    {
        UUID locationId = subscriptionVO.getLocationId();
        Integer duration = subscriptionVO.getDuration();
        Frequency frequency = subscriptionVO.getFrequency();

        if( duration <= 0 && !subscriptionVO.isOpenEnded() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DURATION_VALUE ) ) );
        }
        if( null == frequency )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FREQUENCY_VALUE ) ) );
        }
        if( !subscriptionVO.isOpenEnded() )
        {
            validatePeriod( subscriptionVO );
        }
        if( null == locationId )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATION_ID ) ) );
        }
        if( null == subscriptionVO.getMemberIdList() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ID ) ) );
        }

        subscriptionVO.getMemberIdList().forEach( memberId -> {
            if( null == memberId )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ID ) ) );
            }
        } );

        if( null == subscriptionVO.getAccountId() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID ) ) );
        }

        if( subscriptionVO.getStart().isBefore( LocalDate.now( Clock.systemUTC() ).minusDays( 90 ) ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_START_DATE ) ) );
        }

        validateSubs( subscriptionVO, memberIdListStore, agreementflag );
    }

    private void validateSubs( SubscriptionVO subscriptionVO, List<UUID> memberIdListStore, boolean agreementflag )
    {
        List<SubscriptionItemVO> items = subscriptionVO.getItems();
        if( items.isEmpty() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_COMPLETE_ITEM ) ) );
        }
        items.forEach( item -> {
            if( Strings.isEmpty( item.getItemName() ) )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ITEM_NAME ) ) );
            }
            if( null == item.getItemId() || new UUID( 0L, 0L ).equals( item.getItemId() ) )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ITEM_ID ) ) );
            }
            Optional<BigDecimal> itemPrice = Optional.ofNullable( item.getPrice() );
            if( !itemPrice.isPresent() )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PRICE ) ) );
            }
            if( item.getPrice().compareTo( BigDecimal.ZERO ) < 0 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(),
                    Subscription.class, applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PRICE ) ) );
            }
            if( null == item.getType() )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TYPE_NOT_NULL ) ) );
            }
            BigDecimal roundOff = item.getPrice();
            item.setPrice( roundOff.setScale( 2, BigDecimal.ROUND_HALF_UP ) );
        } );
        validateSubscription( subscriptionVO, memberIdListStore, agreementflag );
    }

    private void validateSubscription( SubscriptionVO subscriptionVO, List<UUID> memberIdListStore, boolean agreementflag )
    {
        if( subscriptionVO.isOpenEnded() == AppConstants.ISTRUE && ( 0 != subscriptionVO.getDuration() || subscriptionVO.getExpirationDate() != null ||
                                                                     null != subscriptionVO.getRenewalOptions() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERRORMESSAGE_INVALID_DURATION_OPENENDED ) ) );
        }
        else if( subscriptionVO.isOpenEnded() == AppConstants.ISFALSE && ( 0 == subscriptionVO.getDuration() ) && subscriptionVO.getExpirationDate() == null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_DURATION ) ) );
        }
        if( ( subscriptionVO.isAutoRenew() == AppConstants.ISFALSE && null != subscriptionVO.getRenewalOptions() ) ||
            ( subscriptionVO.isAutoRenew() && null == subscriptionVO.getRenewalOptions() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEWALOPTION ) ) );
        }
        if( subscriptionVO.isAutoRenew() && subscriptionVO.isOpenEnded() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_OPEN_ENDED_AUTO_RENEW ) ) );
        }
        if( !distingtMemberId( subscriptionVO.getMemberIdList() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ID_DISTINGT_IN_LIST ) ) );
        }
        validateSubscriptions( subscriptionVO, memberIdListStore, agreementflag );
    }

    private void validateSubscriptions( SubscriptionVO subscriptionVO, List<UUID> memberIdListStore, boolean agreementflag )
    {
        if( null == subscriptionVO.getMemberIdList() && ( null == subscriptionVO.getMemberId() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ID ) ) );

        }
        if( agreementflag == AppConstants.ISFALSE && ( null != memberIdListStore && null != subscriptionVO.getMemberId() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ID_OR_MEMBERLIST ) ) );
        }

        if( null != subscriptionVO.getRenewalOptions() )
        {
            validateRenew( subscriptionVO );
        }
    }

    @Transactional
    public Subscription subscriptionCreation( SubscriptionVO subscriptionVO )
    {
        Account account = null;
        PaymentMethod paymentMethod = null;
        log.debug( "Finding account by account pameId: {}", subscriptionVO.getAccountId() );
        Optional<Account> accountOptional = accountRepository.getDetailsByAccountId( subscriptionVO.getAccountId() );
        if( !accountOptional.isPresent() )
        {
            account = accountRepository.getDetailsByPaymentMethodId( subscriptionVO.getAccountId() );
            subscriptionVO.setPameIdAccount( true );
        }
        else
        {
            account = accountOptional.get();
        }
        paymentMethod = paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( subscriptionVO.getAccountId(), Boolean.TRUE );
        if( Objects.isNull( paymentMethod ) )
        {
            paymentMethod = paymentMethodRepository.findByIdAndActive( subscriptionVO.getAccountId(), Boolean.TRUE );
        }
        if( Objects.isNull( paymentMethod ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_METHOD_NOT_ACTIVE ) ) );
        }

        if( !( paymentMethod.getActive() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_METHOD_NOT_ACTIVE ) ) );
        }
        subscriptionVO.getItems().parallelStream().forEach( subscriptionItem -> {
            subscriptionItem.setLocId( subscriptionVO.getLocationId() );
            if( subscriptionItem.getExpirationStart() == null )
            {
                subscriptionItem.setExpirationStart( ItemExpirationStart.PURCHASE );
            }
            if( subscriptionItem.isUnlimited() )
            {
                subscriptionItem.setQuantity( 1 );
            }
        } );
        return createSubscriptions( subscriptionVO, account );
    }

    private Subscription createSubscriptions( SubscriptionVO subscriptionVO, Account account )
    {
        Subscription subscription = ModelMapperUtils.map( subscriptionVO, Subscription.class );
        subscription.setAccount( account );
        subscription.setPlanId( null );
        subscription.setPlanVersion( 0L );
        subscription.setName( null );
        if( subscriptionVO.getStart().isEqual( LocalDate.now( Clock.systemUTC() ) ) )
        {
            subscription.setActive( true );
            subscriptionVO.setActive( true );
        }
        else
        {
            subscription.setActive( false );
            subscriptionVO.setActive( false );
        }
        if( !subscriptionVO.isOpenEnded() )
        {
            if( subscriptionVO.getStart().isBefore( LocalDate.now( Clock.systemUTC() ) ) && subscriptionVO.getExpirationDate().isBefore( LocalDate.now( Clock.systemUTC() ) ) )
            {
                subscription.setActive( false );
                subscriptionVO.setActive( false );
            }
            if( subscriptionVO.getStart().isBefore( LocalDate.now( Clock.systemUTC() ) ) && ( subscriptionVO.getExpirationDate().isAfter( LocalDate.now( Clock.systemUTC() ) ) ) )
            {
                subscription.setActive( true );
                subscriptionVO.setActive( true );
            }
        }
        else
        {
            subscription.setActive( true );
            subscriptionVO.setActive( true );
        }
        Subscription subscriptionResponse = subscriptionRepository.save( subscription );
        if( subscriptionVO.isPameIdAccount() )
        {
            subscriptionResponse.setPameIdAccount( true );
        }
        log.trace( "-------------Creating Subscription member------------------" );
        subscriptionMemberCreation( subscriptionVO, subscriptionResponse );
        return subscriptionResponse;
    }

    /**
     * @param subscriptionVO
     * @return
     * @deprecated no use in subscription
     */
    @Deprecated
    public LocalDate calculateSubExpirationDate( SubscriptionVO subscriptionVO )
    {
        LocalDate expDate = null;
        LocalDate startDate = subscriptionVO.getStart();
        switch( subscriptionVO.getFrequency() )
        {
            case DAILY:
                expDate = startDate.plusDays( subscriptionVO.getDuration() );
                break;
            case WEEKLY:
                expDate = startDate.plusWeeks( subscriptionVO.getDuration() );
                break;
            case EVERY_OTHER_WEEK:
                expDate = startDate.plusWeeks( 2L * subscriptionVO.getDuration() );
                break;
            case MONTHLY:
                expDate = startDate.plusMonths( subscriptionVO.getDuration() );
                break;
            case EVERY_OTHER_MONTH:
                expDate = startDate.plusMonths( 2L * subscriptionVO.getDuration() );
                break;
            case ANNUALLY:
                expDate = startDate.plusYears( subscriptionVO.getDuration() );
                break;
            case QUARTERLY:
                expDate = startDate.plusMonths( 3L * subscriptionVO.getDuration() );
                break;
            case SEMIANNUALLY:
                expDate = startDate.plusMonths( 6L * subscriptionVO.getDuration() );
                break;
        }
        return expDate;
    }
    //Logic for P3-863( "calculateSubExpirationDate" )

    public LocalDateTime calculateNextSubBillingDate( SubscriptionVO subscriptionVO )
    {
        LocalDateTime subBillingDate = subscriptionVO.getInvoiceDate().atStartOfDay();
        subBillingDate = getSubscriptionBillingDate( subscriptionVO, subBillingDate );
        return subBillingDate;
    }

    private LocalDateTime getSubscriptionBillingDate( SubscriptionVO subscriptionVO, LocalDateTime subBillingDate )
    {

        switch( subscriptionVO.getFrequency() )
        {
            case DAILY:
                subBillingDate = subscriptionVO.getInvoiceDate().plusDays( 1 ).atStartOfDay();
                break;
            case WEEKLY:
                subBillingDate = subscriptionVO.getInvoiceDate().plusWeeks( 1 ).atStartOfDay();
                break;
            case EVERY_OTHER_WEEK:
                subBillingDate = subscriptionVO.getInvoiceDate().plusWeeks( 2 ).atStartOfDay();
                break;
            case MONTHLY:
                subBillingDate = subscriptionVO.getInvoiceDate().plusMonths( 1 ).atStartOfDay();
                break;
            case EVERY_OTHER_MONTH:
                subBillingDate = subscriptionVO.getInvoiceDate().plusMonths( 2 ).atStartOfDay();
                break;
            case ANNUALLY:
                subBillingDate = subscriptionVO.getInvoiceDate().plusYears( 1 ).atStartOfDay();
                break;
            case QUARTERLY:
                subBillingDate = subscriptionVO.getInvoiceDate().plusMonths( 3 ).atStartOfDay();
                break;
            case SEMIANNUALLY:
                subBillingDate = subscriptionVO.getInvoiceDate().plusMonths( 6 ).atStartOfDay();
                break;
        }
        return subBillingDate;
    }

    private void validatePeriod( SubscriptionVO subscriptionVO )
    {
        LocalDate startDate = ( subscriptionVO.getStart() == null ? LocalDate.now( Clock.systemUTC() ) : subscriptionVO.getStart() );
        LocalDate endDate = startDate;
        if( null != subscriptionVO.getFrequency() )
        {
            switch( subscriptionVO.getFrequency() )
            {
                case DAILY:
                    endDate = endDate.plusDays( subscriptionVO.getDuration() );
                    break;
                case WEEKLY:
                    endDate = endDate.plusWeeks( subscriptionVO.getDuration() );
                    break;
                case EVERY_OTHER_WEEK:
                    endDate = endDate.plusWeeks( 2L * subscriptionVO.getDuration() );
                    break;
                case MONTHLY:
                    endDate = endDate.plusMonths( subscriptionVO.getDuration() );
                    break;
                case EVERY_OTHER_MONTH:
                    endDate = endDate.plusMonths( 2L * subscriptionVO.getDuration() );
                    break;
                case QUARTERLY:
                    endDate = endDate.plusMonths( 3L * subscriptionVO.getDuration() );
                    break;
                case SEMIANNUALLY:
                    endDate = endDate.plusMonths( 6L * subscriptionVO.getDuration() );
                    break;
                case ANNUALLY:
                    endDate = endDate.plusYears( subscriptionVO.getDuration() );
                    break;
            }

            validatePeriod( subscriptionVO, endDate, startDate );
        }
    }

    private void validatePeriod( SubscriptionVO subscriptionVO, LocalDate endDate, LocalDate startDate )
    {
        switch( subscriptionVO.getFrequency() )
        {
            case DAILY:
                break;
            case WEEKLY:
                if( ChronoUnit.WEEKS.between( startDate, endDate ) <= 0 )
                {
                    log.debug( "WEEKLY WEEKS wrong" );
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                            applicationConfiguration.getValue( ERROR_MESSAGE_INVALID_DURATION ) ) );
                }
                break;
            case MONTHLY:
                if( getMonthDiff( startDate, endDate ) <= 0 )
                {
                    log.debug( "MONTHLY MONTHS wrong" );
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                            applicationConfiguration.getValue( ERROR_MESSAGE_INVALID_DURATION ) ) );
                }
                break;
            case ANNUALLY:
                if( getYearDiff( startDate, endDate ) <= 0 )
                {
                    log.debug( "ANNUALLY YEARS wrong" );
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                            applicationConfiguration.getValue( ERROR_MESSAGE_INVALID_DURATION ) ) );
                }
                break;
            case EVERY_OTHER_WEEK:
                if( ChronoUnit.WEEKS.between( startDate, endDate ) < 2 )
                {
                    log.debug( "EVERY_OTHER_WEEK WEEKS wrong" );
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_DURATION ) ) );
                }
                break;
            case EVERY_OTHER_MONTH:
                if( getMonthDiff( startDate, endDate ) < 2 )
                {
                    log.debug( "EVERY_OTHER_MONTH MONTHS wrong" );
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_DURATION ) ) );
                }
                break;
            case QUARTERLY:
                if( ChronoUnit.MONTHS.between( startDate, endDate ) < 3 )
                {
                    log.debug( "QUARTERLY MONTHS wrong" );
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                            applicationConfiguration.getValue( ERROR_MESSAGE_INVALID_DURATION ) ) );
                }
                break;
            case SEMIANNUALLY:
                if( ChronoUnit.MONTHS.between( startDate, endDate ) < 6 )
                {
                    log.debug( "QUARTERLY MONTHS wrong" );
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                            applicationConfiguration.getValue( ERROR_MESSAGE_INVALID_DURATION ) ) );
                }
                break;
        }
    }

    private void subscriptionMemberCreation( SubscriptionVO subscriptionVO, Subscription subscription )
    {
        log.debug( "Creating subscription member with List of memberId: {} and subscriptionId: {}", subscriptionVO.getMemberIdList(), subscription.getSubId() );
        MemberSubscription memberSubscription = null;
        List<MemberSubscription> memberSubscriptionList = new ArrayList<>();
        if( null != subscriptionVO.getMemberIdList() )
        {
            for( UUID memberId : subscriptionVO.getMemberIdList() )
            {
                MemberSubscriptionId memberSubscriptionId = new MemberSubscriptionId();
                memberSubscription = new MemberSubscription();
                memberSubscriptionId.setMemId( memberId );
                memberSubscriptionId.setSubId( subscription.getSubId() );
                memberSubscription.setId( memberSubscriptionId );
                memberSubscription.setLocId( subscription.getLocationId() );
                memberSubscription = subscriptionMembersRepository.save( memberSubscription );
                memberSubscriptionList.add( memberSubscription );
                subscriptionMembersRepository.save( memberSubscription );
            }
        }
        subscription.setMemberSubscriptionList( memberSubscriptionList );
    }

    @Transactional( propagation = Propagation.REQUIRED, readOnly = true )
    public List<SubscriptionVO> getMemberSubscriptions( UUID memberId )
    {
        List<MemberSubscription> memberSubscriptionList = subscriptionMembersRepository.findAll( memberId );
        if( memberSubscriptionList.isEmpty() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError(
                HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ACCOUNT_NOT_FOUND ) + memberId ) );
        }
        List<SubscriptionVO> subscriptionVOList = new ArrayList<>();
        memberSubscriptionList.forEach( memberSubscriptionObj -> {
            SubscriptionVO subscriptionVO = ModelMapperUtils.map( memberSubscriptionObj.getSubscription(), SubscriptionVO.class );
            //2173
            subscriptionVO.setLocationId( memberSubscriptionObj.getLocId() );
            subscriptionVO.setAccountId( memberSubscriptionObj.getSubscription().getAccount().getAccountId() );
            subscriptionVOList.add( subscriptionVO );
        } );
        log.info( "*** subscription vo list data : {}", subscriptionVOList );
        return subscriptionVOList;
    }

    @Transactional
    public CancelSubscriptionVO cancelSubscription( UUID subscriptionId, SubscriptionCancelVO subscriptionCancelVO )
    {
        log.debug( "**** Cancellation of Subscription for subscriptionId {} ", subscriptionId );
        LocalDate cancellationDate = subscriptionCancelVO.getSubCancellationDate();
        validateSubCancellationDate( cancellationDate );
        CancelSubscriptionVO cancellationVOResponse = null;
        List<MemberSubscription> memberSubscriptionList;
        Subscription subscription = subscriptionRepository.findById( subscriptionId )
                                                          .orElseThrow( () -> new ErrorResponse(
                                                              new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                                                                  applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB + "" + subscriptionId ) ) ) );
        if( cancellationDate.equals( LocalDate.now( Clock.systemUTC() ) )
            || cancellationDate.isAfter( LocalDate.now( Clock.systemUTC() ) ) )
        {
            LocalDate subExpirationDate = subscription.getExpirationDate();
            if( null == subExpirationDate || cancellationDate.isBefore( subExpirationDate )
                || cancellationDate.equals( subExpirationDate ) )
            {
                if( null == subscription.getSubCancellationDate() )
                {
                    subscription.setSubCancellationDate( cancellationDate.atTime( LocalTime.now() ) );
                    Subscription updateSubscription;
                    memberSubscriptionList = subscriptionMembersRepository.findListBySubId( subscription.getSubId() );
                    List<UUID> memIdlist = new ArrayList<>();
                    if( null != memberSubscriptionList )
                    {
                        for( MemberSubscription memberSubscription : memberSubscriptionList )
                        {
                            memIdlist.add( memberSubscription.getId().getMemId() );
                        }
                    }
                    if( cancellationDate.isAfter( LocalDate.now( Clock.systemUTC() ) ) )
                    {
                        Optional<UUID> cancelEventId = cancelSubscription( subscription, memIdlist, cancellationDate );
                        if( cancelEventId.isPresent() )
                        {
                            subscription.setSubCancelEventId( cancelEventId.get() );
                        }
                        updateSubscription = subscriptionRepository.save( subscription );
                    }
                    else
                    {
                        subscription.setActive( false );
                        Optional<UUID> cancelEventId = cancelSubscription( subscription, memIdlist, cancellationDate );
                        if( cancelEventId.isPresent() )
                        {
                            subscription.setSubCancelEventId( cancelEventId.get() );
                        }
                        updateSubscription = subscriptionRepository.save( subscription );
                        SubscriptionCancel subscriptionCancel = new SubscriptionCancel( memIdlist, subscription.getSubId(),
                            subscription.getScheduleInvoicesId(), subscriptionCancelVO.getSubCancellationDate() );
                        log.debug( "****subscription-pending-cancelled  called eventId {}", subscriptionCancel );
                        kafkaOperations.send( "subscription-pending-canceled", subscriptionCancel );
                    }
                    cancellationVOResponse = ModelMapperUtils.map( updateSubscription, CancelSubscriptionVO.class );
                    cancellationVOResponse.setAccountId( updateSubscription.getAccount().getAccountId() );
                }
                else
                {
                    throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST,
                        HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB_CANCEL ) ) );
                }
            }
            else
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(),
                    SubscriptionService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB_CANCEL_EXPIRED ) ) );
            }
        }
        else
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(),
                SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB_CANCEL_FUTURE ) ) );
        }
        log.debug( "cancellation Response {} ", cancellationVOResponse );
        return cancellationVOResponse;
    }

    public void validateSubCancellationDate( LocalDate cancellationDate )
    {
        if( null == cancellationDate )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUB_CANCELLATION_DATE ) ) );
        }
    }

    public Optional<UUID> cancelSubscription( Subscription subscription, List<UUID> memIdList, LocalDate cancellationDate )
    {
        return eventScheduler.scheduleSubscriptionCancelation( Schedule.<SubscriptionCancel>builder().cancel( cancellationDate ).properties(
            SubscriptionCancel.builder().memberIdList( memIdList ).subscriptionId( subscription.getSubId() ).scheduleInvoicesId( subscription.getScheduleInvoicesId() )
                              .subCancelDate( cancellationDate ).build() ).build() );
    }

    @Transactional
    public SubscriptionExpireVO expireSubscription( UUID subscriptionId, SubscriptionExpireVO subscriptionExpireVO )
    {
        log.debug( "****Expire Subscription****** subscriptionId {} Expiration date{}",
            subscriptionId, subscriptionExpireVO.getExpirationDate() );
        Optional<UUID> subId = Optional.ofNullable( subscriptionId );
        if( !subId.isPresent() )
        {
            throw new ErrorResponse(
                new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTIONID_NOT_NULL ) ) );
        }
        Subscription subscription = subscriptionRepository.findById( subscriptionId ).orElseThrow( () -> new ErrorResponse(
            new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB + "" + subscriptionId ) ) ) );
        if( subscription != null )
        {
            if( !subscription.isActive() )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_ALREADY_EXPIRED ) ) );
            }
            LocalDate expirationDate = subscriptionExpireVO.getExpirationDate();
            if( ( subscriptionExpireVO.getExpirationDate().isAfter( LocalDate.now( Clock.systemUTC() ) )
                  || ( subscriptionExpireVO.getExpirationDate().equals( LocalDate.now( Clock.systemUTC() ) ) ) ) )
            {
                if( ( subscriptionExpireVO.getExpirationDate().isBefore( subscription.getExpirationDate() ) )
                    || ( subscriptionExpireVO.getExpirationDate().equals( subscription.getExpirationDate() ) ) )
                {
                    subscription.setExpirationDate( expirationDate );
                    subscription.setActive( false );
                    subscription = subscriptionRepository.save( subscription );
                    eventScheduler.scheduleSubscriptionCancelation( Schedule.<SubscriptionCancel>builder().expire( expirationDate )
                                                                                                          .properties( SubscriptionCancel.builder()
                                                                                                                                         .subscriptionId(
                                                                                                                                             subscription
                                                                                                                                                 .getSubId() )
                                                                                                                                         .scheduleInvoicesId(
                                                                                                                                             subscription
                                                                                                                                                 .getScheduleInvoicesId() )
                                                                                                                                         .subCancelDate(
                                                                                                                                             expirationDate )
                                                                                                                                         .build() )
                                                                                                          .build() );
                    //create the subscription-expired event
                    Optional<UUID> eventId = eventScheduler.scheduleSubscriptionExpire( Schedule.<SubscriptionExpired>builder().start( expirationDate.atTime( LocalTime.now() ) )
                                                                                                                               .repeating( false )
                                                                                                                               .properties( SubscriptionExpired.builder()
                                                                                                                                                               .locationId(
                                                                                                                                                                   subscription
                                                                                                                                                                       .getLocationId() )
                                                                                                                                                               .subscriptionId(
                                                                                                                                                                   subscription
                                                                                                                                                                       .getSubId() )
                                                                                                                                                               .build() )
                                                                                                                               .build() );
                    log.debug( "****subscription-expired called eventId {}", eventId );
                    if( eventId.isPresent() )
                    {
                        subscriptionExpireVO.setExpired( true );
                        subscriptionExpireVO.setLocationId( subscription.getLocationId() );
                        List<MemberSubscription> memberSubscriptionList = subscriptionMembersRepository.findListBySubId( subscription.getSubId() );
                        if( null != memberSubscriptionList )
                        {
                            for( MemberSubscription memberSubscription : memberSubscriptionList )
                            {
                                if( null != subscriptionExpireVO.getMemberId() && subscriptionExpireVO.getMemberId().equals( memberSubscription.getId().getMemId() ) )
                                {
                                    subscriptionExpireVO.setMemberId( memberSubscription.getId().getMemId() );
                                }
                                else
                                {
                                    subscriptionExpireVO.setMemberId( memberSubscription.getId().getMemId() );
                                }
                            }
                        }
                        subscriptionExpireVO.setSubId( subscription.getSubId() );
                    }
                }
                else
                {
                    throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_EXPIRATION ) ) );
                }
            }
            else
            {
                throw new DateTimeException( applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_FUTURE ) );
            }
        }
        else
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB + "" + subscriptionId ) ) );
        }
        return subscriptionExpireVO;
    }

    @Transactional
    public SubscriptionVO renewSubscription( UUID subscriptionId, SubscriptionVO subscriptionVO, boolean agreementFlag )
    {
        log.debug( "****Auto renew Subscription ", subscriptionId );
        Subscription subscription = subscriptionRepository.findById( subscriptionId )
                                                          .orElseThrow( () -> new ErrorResponse(
                                                              new DataIntegrityViolationResponse( HttpStatus.NOT_FOUND.value(), Subscription.class,
                                                                  applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB + "" + subscriptionId ) ) ) );
        validateMandatoryFieldsForSubscription( subscriptionVO, null, agreementFlag );
        Subscription renewedSubscription = ModelMapperUtils.map( subscriptionVO, Subscription.class );
        List<MemberSubscription> memberSubscriptionList = subscriptionMembersRepository.findListBySubId( subscription.getSubId() );
        subscription.setMemberSubscriptionList( memberSubscriptionList );
        //Setting the old subscription to false
        subscription.setActive( false );
        subscription.setExpirationDate( LocalDate.now( Clock.systemUTC() ) );
        subscriptionRepository.saveAndFlush( subscription );
        renewedSubscription.setRenewable( true );
        renewedSubscription.setActive( true );
        //renewedSubscription.setRenewDate( LocalDate.now( Clock.systemUTC() ) );
        renewedSubscription.setSubscriptionTypeEnum( SubscriptionTypeEnum.RENEW );
        SubscriptionVO updatedSubscriptionVO = ModelMapperUtils.map( renewedSubscription, SubscriptionVO.class );
        if( null != subscriptionVO.getMemberIdList() )
        {

            updatedSubscriptionVO.setMemberIdList( subscriptionVO.getMemberIdList() );
        }
        updatedSubscriptionVO.setAccountId( subscription.getAccount().getAccountId() );
        return createSubscription( updatedSubscriptionVO, false );
    }

    public Subscription getSubscriptionByLocId( UUID locID, UUID subscriptionId )
    {
        return subscriptionRepository.findSubscription( locID, subscriptionId );
    }

    @Transactional
    public SubscriptionUpdateVO updateSubscription( UUID subscriptionId, SubscriptionUpdateVO subscriptionUpdateVO )
    {
        log.debug( "****Update Subscription****** subscriptionId {} ", subscriptionId );
        SubscriptionUpdateVO subscriptionUpdateVOResponse = null;
        Subscription subscription = null;
        subscription = subscriptionRepository.findById( subscriptionId )
                                             .orElseThrow( () -> new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND,
                                                 HttpStatus.NOT_FOUND.value(), Subscription.class,
                                                 applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB ) + subscriptionId ) ) );
        if( subscriptionUpdateVO.getSalesEmployeeId() != null )
        {
            subscription.setSalesEmployeeId( subscriptionUpdateVO.getSalesEmployeeId() );
            subscription.setFrequency( subscription.getFrequency() );
        }
        subscription = subscriptionRepository.save( subscription );
        subscriptionUpdateVOResponse = ModelMapperUtils.map( subscription, SubscriptionUpdateVO.class );
        log.debug( "****Update Subscription******Response {} ", subscriptionUpdateVO );
        subscriptionUpdateVOResponse.setAccountId( subscription.getAccount().getAccountId() );
        return subscriptionUpdateVOResponse;
    }

    @Transactional
    public List<CancelSubscriptionVO> getCanceledSubscriptions( UUID accountId )
    {
        log.debug( "****get Canceled Subscriptions ****** account Id {} ", accountId );
        List<CancelSubscriptionVO> canceledSubscriptionVOResponse = null;
        Optional<List<Subscription>> canceledSubscriptions = subscriptionRepository.findCanceledSubscriptionsByAccountId( accountId );
        if( !canceledSubscriptions.isPresent() )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RECORD_NOT_FOUND ) ) );
        }
        else
        {
            canceledSubscriptionVOResponse = ModelMapperUtils.mapAll( canceledSubscriptions.get(), CancelSubscriptionVO.class );
        }
        log.debug( "****get Cancellation Date Subscription****** cancellation Response {} ", canceledSubscriptionVOResponse );
        return canceledSubscriptionVOResponse;
    }

    @Transactional
    public RemovedCancelSubscriptionVO removeSubscriptionCancel( UUID subscriptionId )
    {
        log.debug( "****remove Cancellation Date Subscription****** subscriptionId {} ", subscriptionId );
        RemovedCancelSubscriptionVO removedCancelSubscriptionVO = null;
        Subscription subscription = subscriptionRepository.findById( subscriptionId )
                                                          .orElseThrow( () -> new ErrorResponse(
                                                              new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                                                                  applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB + "" + subscriptionId ) ) ) );
        if( !subscription.isActive() )
        {
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CAN_NOT_CANCEL ) ) );
        }
        if( null != subscription.getSubCancellationDate() )
        {
            if( null != subscription.getSubCancelEventId() )
            {
                eventScheduler.cancelSub( subscription.getSubCancelEventId() );
            }
            subscription.setSubCancellationDate( null );
            if( !subscription.isActive() )
            {
                subscription.setActive( true );
                SubscriptionVO subscriptionVO = ModelMapperUtils.map( subscription, SubscriptionVO.class );
                Optional<UUID> scheduleInvoicesId = invoiceTriggerEvent( subscription, subscriptionVO );
                if( scheduleInvoicesId.isPresent() )
                {
                    subscription.setScheduleInvoicesId( scheduleInvoicesId.get() );
                }
            }
            Subscription subscriptionResponse = subscriptionRepository.save( subscription );
            removedCancelSubscriptionVO = ModelMapperUtils.map( subscriptionResponse, RemovedCancelSubscriptionVO.class );
            removedCancelSubscriptionVO.setAccountId( subscriptionResponse.getAccount().getAccountId() );
        }
        else
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB_NOTCANCELSUB ) ) );
        }
        log.debug( "****remove Cancellation Date Subscription****** cancellation Response {} ", removedCancelSubscriptionVO );
        return removedCancelSubscriptionVO;
    }

    public Optional<UUID> invoiceTriggerEvent( Subscription subscription, SubscriptionVO subscriptionVO )
    {
        Optional<UUID> invoiceScheduleID;
        if( subscriptionVO.getDuration() == 1 )
        {
            invoiceScheduleID = eventScheduler.scheduleInvoices(
                Schedule.<SubscriptionDetails>builder().start( subscriptionVO.getInvoiceDate().atTime( LocalTime.now() ) )
                                                       .repeating( false ).frequency( subscriptionVO.getFrequency().getPeriod() )
                                                       .duration( calculateDuration( subscriptionVO.getFrequency(), subscriptionVO.getDuration() ) )
                                                       .properties( SubscriptionDetails.builder().locationId( subscription.getLocationId() )
                                                                                       .subscriptionId( subscription.getSubId() ).memberIdList( subscriptionVO.getMemberIdList() )
                                                                                       .isPameIdAccount( subscriptionVO.isPameIdAccount() )
                                                                                       .build() )
                                                       .build() );
        }
        else
        {

            invoiceScheduleID = eventScheduler.scheduleInvoices(
                Schedule.<SubscriptionDetails>builder().start( subscriptionVO.getInvoiceDate().atTime( LocalTime.now() ) )
                                                       .repeating( true ).frequency( subscriptionVO.getFrequency().getPeriod() )
                                                       .duration( calculateDuration( subscriptionVO.getFrequency(), subscriptionVO.getDuration() ) )
                                                       .properties( SubscriptionDetails.builder().locationId( subscription.getLocationId() )
                                                                                       .subscriptionId( subscription.getSubId() ).memberIdList( subscriptionVO.getMemberIdList() )
                                                                                       .isPameIdAccount( subscriptionVO.isPameIdAccount() )
                                                                                       .build() )
                                                       .build() );
        }
        return invoiceScheduleID;
    }

    public Period calculateDuration( Frequency frequency, int duration )
    {
        Period period = Period.ofDays( 0 );
        switch( frequency )
        {
            case DAILY:
                period = Period.ofDays( duration );
                break;
            case WEEKLY:
                period = Period.ofWeeks( duration );
                break;
            case EVERY_OTHER_WEEK:
                period = Period.ofWeeks( duration );
                break;
            case MONTHLY:
                period = Period.ofMonths( duration );
                break;
            case EVERY_OTHER_MONTH:
                period = Period.ofMonths( 2 * duration );
                break;
            case ANNUALLY:
                period = Period.ofYears( duration );
                break;
            case QUARTERLY:
                period = Period.ofMonths( 3 * duration );
                break;
            case SEMIANNUALLY:
                period = Period.ofMonths( 6 * duration );
                break;
        }
        return period;
    }

    @Transactional
    public CancelSubscriptionVO updateCancelSubscription( UUID subscriptionId, SubscriptionCancelVO subscriptionUpdateVO )
    {
        log.debug( "****Update CancelSubscription****** subscriptionId {} ", subscriptionId );
        LocalDate cancellationDate = subscriptionUpdateVO.getSubCancellationDate();
        validateSubCancellationDate( cancellationDate );
        CancelSubscriptionVO cancellationVOResponse = null;
        List<MemberSubscription> memberSubscriptionList;
        Subscription subscription = subscriptionRepository.findById( subscriptionId )
                                                          .orElseThrow( () -> new ErrorResponse(
                                                              new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                                                                  applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB + "" + subscriptionId ) ) ) );

        if( !subscription.isActive() && cancellationDate.isAfter( LocalDate.now( Clock.systemUTC() ) ) )
        {
            subscription.setSubCancellationDate( cancellationDate.atStartOfDay() );
        }
        if( !subscription.isActive() && ( cancellationDate.isEqual( LocalDate.now( Clock.systemUTC() ) )
                                          || cancellationDate.isBefore( LocalDate.now( Clock.systemUTC() ) ) ) )
        {
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CAN_NOT_CANCEL ) ) );
        }
        if( cancellationDate.isAfter( LocalDate.now( Clock.systemUTC() ) ) || cancellationDate.isEqual( LocalDate.now( Clock.systemUTC() ) ) )
        {
            LocalDate subExpirationDate = subscription.getExpirationDate();
            if( null == subExpirationDate || cancellationDate.isBefore( subExpirationDate ) )
            {
                if( null != subscription.getSubCancellationDate() )
                {
                    if( subscription.isActive() || subscription.getStart().isAfter( LocalDate.now() ) )
                    {
                        subscription.setSubCancellationDate( cancellationDate.atTime( LocalTime.now() ) );
                        Subscription updateSubscription;
                        memberSubscriptionList = subscriptionMembersRepository.findListBySubId( subscription.getSubId() );
                        List<UUID> memIdlist = new ArrayList<>();
                        if( null != memberSubscriptionList )
                        {
                            for( MemberSubscription memberSubscription : memberSubscriptionList )
                            {
                                memIdlist.add( memberSubscription.getId().getMemId() );
                            }
                        }

                        UUID subCancelID = subscription.getSubCancelEventId();
                        boolean cancelScheduledInvoicesStatus = false;
                        if( null != subCancelID )
                        {
                            cancelScheduledInvoicesStatus = eventScheduler.cancelSub( subCancelID );
                            subscription.setSubCancelEventId( null );
                        }
                        else
                        {
                            Optional<UUID> cancelEventId = eventScheduler.scheduleSubscriptionCancelation(
                                Schedule.<SubscriptionCancel>builder().cancel( cancellationDate )
                                                                      .properties( SubscriptionCancel.builder().memberIdList( memIdlist )
                                                                                                     .subscriptionId( subscription.getSubId() )
                                                                                                     .scheduleInvoicesId( subscription.getScheduleInvoicesId() )
                                                                                                     .subCancelDate( cancellationDate )
                                                                                                     .build() )
                                                                      .build() );
                            if( cancelEventId.isPresent() )
                            {
                                subscription.setSubCancelEventId( cancelEventId.get() );
                                SubscriptionCancel subscriptionCancel = new SubscriptionCancel( memIdlist,
                                    subscription.getSubId(), subscription.getScheduleInvoicesId(),
                                    subscriptionUpdateVO.getSubCancellationDate() );
                                log.debug( "****subscription-pending-cancelled  called eventId {}", subscriptionCancel );
                                kafkaOperations.send( "subscription-pending-canceled", subscriptionCancel );
                            }
                        }
                        log.debug( "****canceled the scheduled quartz job id {} for cancel - Status {}", subCancelID, cancelScheduledInvoicesStatus );
                        if( cancellationDate.isEqual( LocalDate.now( Clock.systemUTC() ) ) )
                        {
                            subscription.setActive( false );
                        }
                        updateSubscription = subscriptionRepository.save( subscription );
                        cancellationVOResponse = ModelMapperUtils.map( updateSubscription, CancelSubscriptionVO.class );
                        cancellationVOResponse.setAccountId( updateSubscription.getAccount().getAccountId() );
                    }
                    else
                    {
                        throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST,
                            HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB_CANCELED ) ) );
                    }
                }
                else
                {
                    throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST,
                        HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB_NOTCANCELSUB ) ) );
                }
            }
            else
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(),
                    SubscriptionService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB_CANCEL_EXPIRED ) ) );
            }
        }
        else
        {
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB_CANCEL_FUTURE ) ) );
        }
        log.debug( "****Update Cancellation Date Subscription****** cancellation Response {} ", cancellationVOResponse );
        return cancellationVOResponse;
    }

    private int getMonthDiff( LocalDate startDate, LocalDate endDate )
    {
        return Months.monthsBetween( localDateToJodaLocalDate( startDate ), localDateToJodaLocalDate( endDate ) ).getMonths();
    }

    private int getYearDiff( LocalDate startDate, LocalDate endDate )
    {
        return Years.yearsBetween( localDateToJodaLocalDate( startDate ), localDateToJodaLocalDate( endDate ) ).getYears();
    }

    public Optional<UUID> activeTriggerEvent( Subscription subscription, SubscriptionVO subscriptionVO )
    {
        return eventScheduler.scheduleActive(
            Schedule.<SubscriptionDetails>builder().start( subscription.getStart().atTime( LocalTime.now() ) )
                                                   .repeating( true ).frequency( subscriptionVO.getFrequency().getPeriod() )
                                                   .duration( calculateDuration( subscriptionVO.getFrequency(), subscriptionVO.getDuration() ) )
                                                   .properties( SubscriptionDetails.builder().locationId( subscription.getLocationId() )
                                                                                   .subscriptionId( subscription.getSubId() ).memberIdList( subscriptionVO.getMemberIdList() )
                                                                                   .build() )
                                                   .build() );
    }

    public void createRenewSubscription( Subscription subscription )
    {
        subscription.setActive( true );
        List<MemberSubscription> memberSubscriptionList = subscriptionMembersRepository.findListBySubId( subscription.getSubId() );
        List<UUID> memberList = new ArrayList<>();
        for( MemberSubscription memberSubscription : memberSubscriptionList )
        {
            memberList.add( memberSubscription.getId().getMemId() );
        }
        SubscriptionVO subscriptionVO = ModelMapperUtils.map( subscription, SubscriptionVO.class );
        RenewalOptionsVO renewalOptionsVO = new RenewalOptionsVO();
        //renewalOptionsVO.setRenewDate( subscription.getRenewDate() );
        renewalOptionsVO.setRenewInvoiceDate( subscription.getRenewInvoiceDate() );
        renewalOptionsVO.setRenewAmount( subscription.getTotalAmount() );
        renewalOptionsVO.setRenewDuration( subscription.getDuration() );
        renewalOptionsVO.setRenewType( subscription.getRenewType() );
        renewalOptionsVO.setRenewFrequency( subscription.getFrequency() );
        renewalOptionsVO.setRenewExpireDate( subscription.getExpirationDate() );
        subscriptionVO.setRenewalOptions( renewalOptionsVO );
        if( subscription.getRenewType() == RenewType.OPEN )
        {
            List dateList = new ArrayList();
            LocalDate nextDate = subscription.getRenewInvoiceDate();
            while( nextDate.isBefore( LocalDate.now( Clock.systemUTC() ) ) )
            {
                dateList.add( nextDate );
                nextDate = calculateNextRenewSubBillingDate( subscriptionVO ).toLocalDate();
                subscriptionVO.getRenewalOptions().setRenewInvoiceDate( nextDate );
            }
            dateList.forEach( dateInList -> {
                subscription.setRenewInvoiceDate( (LocalDate) dateInList );
                createInvoice( subscription );
            } );
            subscription.setRenewInvoiceDate( nextDate );
            Optional<UUID> scheduleInvoicesId = invoiceRenewEventOpenEnded( subscription, memberList );
            subscription.setOpenEnded( true );
            if( scheduleInvoicesId.isPresent() )
            {
                subscription.setScheduleInvoicesId( scheduleInvoicesId.get() );
                subscriptionRepository.save( subscription );
            }
        }
        else
        {
            List dateList = new ArrayList();
            LocalDate nextDate = subscription.getRenewInvoiceDate();
            subscriptionVO.getRenewalOptions().setRenewInvoiceDate( subscription.getRenewInvoiceDate() );
            while( ( nextDate.isBefore( LocalDate.now( Clock.systemUTC() ) ) ) && nextDate.isBefore( subscription.getExpirationDate() ) )
            {
                dateList.add( nextDate );
                nextDate = calculateNextRenewSubBillingDate( subscriptionVO ).toLocalDate();
                subscriptionVO.getRenewalOptions().setRenewInvoiceDate( nextDate );
            }
            if( dateList.size() >= subscriptionVO.getRenewalOptions().getRenewDuration() )
            {
                List durationDateList = dateList.subList( 0, ( subscriptionVO.getRenewalOptions().getRenewDuration() ) );
                durationDateList.forEach( dateInList -> {
                    subscription.setRenewInvoiceDate( (LocalDate) dateInList );
                    createInvoice( subscription );
                } );
            }
            else
            {
                dateList.forEach( dateInList -> {
                    subscription.setRenewInvoiceDate( (LocalDate) dateInList );
                    createInvoice( subscription );
                } );
                subscription.setRenewInvoiceDate( nextDate );

                Optional<UUID> invoiceExpireId = invoiceRenewExpireEvent( subscription );
                Optional<UUID> scheduleInvoicesId = invoiceRenewEvent( subscription, memberList );
                if( scheduleInvoicesId.isPresent() )
                {
                    subscription.setScheduleInvoicesId( scheduleInvoicesId.get() );
                }
                if( invoiceExpireId.isPresent() )
                {
                    subscription.setSubExpiredEventId( invoiceExpireId.get() );
                }
            }
            subscriptionRepository.save( subscription );
        }
    }

    public LocalDateTime calculateNextRenewSubBillingDate( SubscriptionVO subscriptionVO )
    {
        LocalDateTime subBillingDate = subscriptionVO.getRenewalOptions().getRenewInvoiceDate().atStartOfDay();
        subBillingDate = getRenewSubscriptionBillingDate( subscriptionVO, subBillingDate );
        return subBillingDate;
    }

    private void createInvoice( Subscription subscription )
    {
        Invoice invoice = null;
        try
        {
            invoice = invoiceService.createInvoice( subscription, subscription.isPameIdAccount() );
            invoiceListner.createPayorInvoice( invoice );
        }
        catch( IOException exception )
        {
            log.warn( "Unable to set date for invoices " + exception.getMessage() );
        }
    }

    private Optional<UUID> invoiceRenewEventOpenEnded( Subscription subscription, List<UUID> memberList )
    {
        return eventScheduler.scheduleInvoices(
            Schedule.<SubscriptionDetails>builder().start( subscription.getRenewInvoiceDate().atTime( LocalTime.now() ) )
                                                   .repeating( true ).frequency( subscription.getFrequency().getPeriod() )
                                                   .properties( SubscriptionDetails.builder().locationId( subscription.getLocationId() )
                                                                                   .subscriptionId( subscription.getSubId() ).memberIdList( memberList )
                                                                                   .isPameIdAccount( subscription.isPameIdAccount() ).build() )
                                                   .build() );
    }

    private Optional<UUID> invoiceRenewExpireEvent( Subscription subscription )
    {
        return eventScheduler.scheduleSubscriptionExpire( Schedule.<SubscriptionExpired>builder()
            .expire( subscription.getExpirationDate().atStartOfDay().toLocalDate() ).repeating( false )
            .properties( SubscriptionExpired.builder().locationId( subscription.getLocationId() )
                                            .subscriptionId( subscription.getSubId() )
                                            .subExpDate( subscription.getExpirationDate().atStartOfDay().toLocalDate() ).build() )
            .build() );
    }

    private Optional<UUID> invoiceRenewEvent( Subscription subscription, List<UUID> memberList )
    {
        Optional<UUID> renewInvoiceScheduleID;
        if( subscription.getDuration() == 1 )
        {
            renewInvoiceScheduleID = eventScheduler.scheduleInvoices( Schedule.<SubscriptionDetails>builder()
                .start( subscription.getRenewInvoiceDate().atTime( LocalTime.now() ).plusMinutes( 1L ) ).repeating( false )
                .frequency( subscription.getFrequency().getPeriod() )
                .duration( calculateDuration( subscription.getFrequency(),
                    subscription.getDuration() ) )
                .properties( SubscriptionDetails.builder().locationId( subscription.getLocationId() )
                                                .subscriptionId( subscription.getSubId() ).memberIdList( memberList ).isPameIdAccount( subscription.isPameIdAccount() ).build() )

                .build() );
        }
        else
        {
            renewInvoiceScheduleID = eventScheduler.scheduleInvoices( Schedule.<SubscriptionDetails>builder()
                .start( subscription.getRenewInvoiceDate().atTime( LocalTime.now() ).plusMinutes( 1L ) ).repeating( true )
                .frequency( subscription.getFrequency().getPeriod() )
                .duration( calculateDuration( subscription.getFrequency(),
                    subscription.getDuration() ) )
                .properties( SubscriptionDetails.builder().locationId( subscription.getLocationId() )
                                                .subscriptionId( subscription.getSubId() ).memberIdList( memberList ).isPameIdAccount( subscription.isPameIdAccount() ).build() )

                .build() );
        }
        return renewInvoiceScheduleID;
    }

    private LocalDateTime getRenewSubscriptionBillingDate( SubscriptionVO subscriptionVO, LocalDateTime subBillingDate )
    {

        switch( subscriptionVO.getRenewalOptions().getRenewFrequency() )
        {
            case DAILY:
                subBillingDate = subscriptionVO.getRenewalOptions().getRenewInvoiceDate().plusDays( 1 ).atStartOfDay();
                break;
            case WEEKLY:
                subBillingDate = subscriptionVO.getRenewalOptions().getRenewInvoiceDate().plusWeeks( 1 ).atStartOfDay();
                break;
            case EVERY_OTHER_WEEK:
                subBillingDate = subscriptionVO.getRenewalOptions().getRenewInvoiceDate().plusWeeks( 2 ).atStartOfDay();
                break;
            case MONTHLY:
                subBillingDate = subscriptionVO.getRenewalOptions().getRenewInvoiceDate().plusMonths( 1 ).atStartOfDay();
                break;
            case EVERY_OTHER_MONTH:
                subBillingDate = subscriptionVO.getRenewalOptions().getRenewInvoiceDate().plusMonths( 2 ).atStartOfDay();
                break;
            case ANNUALLY:
                subBillingDate = subscriptionVO.getRenewalOptions().getRenewInvoiceDate().plusYears( 1 ).atStartOfDay();
                break;
            case QUARTERLY:
                subBillingDate = subscriptionVO.getRenewalOptions().getRenewInvoiceDate().plusMonths( 3 ).atStartOfDay();
                break;
            case SEMIANNUALLY:
                subBillingDate = subscriptionVO.getRenewalOptions().getRenewInvoiceDate().plusMonths( 6 ).atStartOfDay();
                break;
        }
        return subBillingDate;
    }

    public Optional<UUID> invoiceTriggerEventOpenEnded( Subscription subscription, SubscriptionVO subscriptionVO )
    {
        return eventScheduler.scheduleInvoices(
            Schedule.<SubscriptionDetails>builder().start( subscription.getInvoiceDate().atTime( LocalTime.now() ) )
                                                   .repeating( true ).frequency( subscriptionVO.getFrequency().getPeriod() )
                                                   .properties( SubscriptionDetails.builder().locationId( subscription.getLocationId() )
                                                                                   .subscriptionId( subscription.getSubId() ).memberIdList( subscriptionVO.getMemberIdList() )
                                                                                   .isPameIdAccount( subscriptionVO.isPameIdAccount() )
                                                                                   .build() )
                                                   .build() );
    }

    public Optional<UUID> invoiceExpireEvent( SubscriptionVO subscriptionVO )
    {
        return eventScheduler.scheduleSubscriptionExpire( Schedule.<SubscriptionExpired>builder()
            .expire( subscriptionVO.getExpirationDate().atStartOfDay().toLocalDate() ).repeating( false )
            .properties( SubscriptionExpired.builder().locationId( subscriptionVO.getLocationId() )
                                            .subscriptionId( subscriptionVO.getSubId() )
                                            .subExpDate( subscriptionVO.getExpirationDate().atStartOfDay().toLocalDate() ).build() )
            .build() );
    }

    @Transactional
    public SubscriptionVO addMember( UpdateMemberSubscriptionVO updateMemberSubscriptionVO, UUID subscriptionId )
    {
        log.debug( "**** Add member ****** memberId {}, subscriptionId {} ", updateMemberSubscriptionVO.getMemberId(), subscriptionId );
        MemberSubscription memberSubscription;
        SubscriptionVO subscriptionVO = getSubscription( subscriptionId );
        if( subscriptionVO == null )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_NOT_EXIST ) ) );
        }
        if( subscriptionVO.isActive() == AppConstants.ISFALSE )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_ALREADY_CANCEL ) ) );
        }
        if( !subscriptionVO.isOpenEnded() && subscriptionVO.getExpirationDate().isBefore( LocalDate.now() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EXPIRE_SUBSCRIPTION ) ) );
        }

        List<UUID> memberIdList = subscriptionVO.getMemberIdList();
        if( memberIdList.contains( updateMemberSubscriptionVO.getMemberId() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ACCOUNT_ALREADY_ADDED ) ) );
        }
        memberIdList.add( updateMemberSubscriptionVO.getMemberId() );
        for( UUID memberId : memberIdList )
        {
            MemberSubscriptionId memberSubscriptionId = new MemberSubscriptionId();
            memberSubscription = new MemberSubscription();
            memberSubscriptionId.setMemId( memberId );
            memberSubscriptionId.setSubId( subscriptionId );
            memberSubscription.setId( memberSubscriptionId );
            memberSubscription.setLocId( subscriptionVO.getLocationId() );
            subscriptionMembersRepository.save( memberSubscription );
        }
        subscriptionVO.setMemberIdList( memberIdList );
        log.debug( "**** member added successfully  ****** ", subscriptionVO );
        return subscriptionVO;
    }

    @Transactional
    public SubscriptionVO getSubscription( UUID subscriptionId )
    {

        log.debug( "finding Subscription details By Id = {}", subscriptionId );

        Subscription subscription = subscriptionRepository.findById( subscriptionId ).orElseThrow( () -> new ErrorResponse(
            new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUBSCRIPTION ) + subscriptionId ) ) );

        List<MemberSubscription> memberSubscriptionList = subscriptionMembersRepository.findListBySubId( subscription.getSubId() );
        List<UUID> memberIdList = new ArrayList<>();
        for( MemberSubscription memberSubscription : memberSubscriptionList )
        {
            if( memberSubscription.getDeactivated() == null )
            {
                memberIdList.add( memberSubscription.getId().getMemId() );
            }
        }
        SubscriptionVO subscriptionVO = ModelMapperUtils.map( subscription, SubscriptionVO.class );
        subscriptionVO.setAccountId( subscription.getAccount().getAccountId() );
        if( !memberIdList.isEmpty() )
        {
            subscriptionVO.setMemberIdList( memberIdList );
        }
        //Optional<Subscription> renewSubscription = subscriptionRepository.findByRenewRefId( subscriptionId );
        Optional<Subscription> renewSubscription = subscriptionRepository.findBySubPrevRefId( subscriptionId );
        if( renewSubscription.isPresent() )
        {
            RenewalOptionsVO renewalOptionsVO = new RenewalOptionsVO();
            renewalOptionsVO.setRenewDate( renewSubscription.get().getStart() );
            renewalOptionsVO.setRenewInvoiceDate( renewSubscription.get().getRenewInvoiceDate() );
            renewalOptionsVO.setRenewDuration( renewSubscription.get().getDuration() );
            renewalOptionsVO.setRenewExpireDate( renewSubscription.get().getExpirationDate() );
            renewalOptionsVO.setRenewFrequency( renewSubscription.get().getFrequency() );
            renewalOptionsVO.setRenewType( renewSubscription.get().getRenewType() );
            renewalOptionsVO.setRenewSubId( renewSubscription.get().getSubId() );
            List<Subscription> subscriptions = subscriptionRepository.findPriceBySubId( renewSubscription.get().getSubId() );
            renewalOptionsVO.setRenewAmount( subscriptions.get( 0 ).getItems().get( 0 ).getPrice() );
            subscriptionVO.setRenewalOptions( renewalOptionsVO );
        }
        return subscriptionVO;
    }

    @Transactional
    public SubscriptionVO removeMember( UUID subscriptionId, UUID memberId, boolean flag )
    {
        log.debug( "**** Remove member ****** memberId {}, subscriptionId {}  ", memberId, subscriptionId );

        SubscriptionVO subscriptionVO = getSubscription( subscriptionId );
        if( subscriptionVO == null )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_NOT_EXIST ) ) );
        }
        if( Objects.nonNull( subscriptionVO.getExpirationDate() ) && subscriptionVO.getExpirationDate().isBefore( LocalDate.now() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EXPIRE_SUBSCRIPTION ) ) );
        }
        List<UUID> memberIdList = subscriptionVO.getMemberIdList();
        if( !memberIdList.contains( memberId ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ACCOUNT_NOT_ASSOCIATED ) ) );
        }
        if( memberIdList.size() == 1 && flag )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DELETE_MEMBER_ACCOUNT ) ) );
        }

        MemberSubscription memberSubscription = subscriptionMembersRepository.findMemberSubscriptionBySubIdAndMemId( subscriptionId, memberId );
        if( memberSubscription == null )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST.value(), MemberSubscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ACCOUNT_NOT_ASSOCIATED ) ) );
        }

        if( memberSubscription.getDeactivated() != null )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), MemberSubscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ALREADY_DELETED ) ) );
        }
        memberSubscription.setDeactivated( LocalDateTime.now() );
        subscriptionMembersRepository.save( memberSubscription );

        memberIdList.remove( memberId );
        subscriptionVO.setMemberIdList( memberIdList );
        log.info( "**** member remove successfully  ****** ", subscriptionVO );
        return subscriptionVO;
    }

    public SubscriptionDue getRemainingSubscriptionValue( UUID subscriptonId )
    {
        log.debug( "****get Remaining Subscriptions ****** subscriptionId {} ", subscriptonId );
        SubscriptionDue subscriptionDueResponse = new SubscriptionDue();
        int subDuration = 0;
        BigDecimal dueAmount = null;
        int invoiceCountDue = 0;
        Subscription subscription = subscriptionRepository.findById( subscriptonId ).orElseThrow( () -> new ErrorResponse(
            new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB ) ) ) );
        if( subscription.isOpenEnded() )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), MemberSubscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_OPEN_SUB ) ) );
        }
        List<Invoice> invoice = invoiceRepository.findBySubId( subscriptonId );
        if( ( subscription.getSubscriptionTypeEnum().equals( SubscriptionTypeEnum.RENEW ) ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_FOR_RENEW_SUBSCRIPTION ) ) );
        }

        if( invoice.size() == subscription.getDuration() )
        {
            subDuration = subscription.getDuration();
            dueAmount = new BigDecimal( 0 );
            invoiceCountDue = 0;
        }
        else if( Objects.nonNull( subscription.getExpirationDate() ) &&
                 ( Objects.nonNull( subscription.getSubCancellationDate() ) && subscription.getSubCancellationDate().isAfter( LocalDateTime.now( Clock.systemUTC() ) ) ) &&
                 ( subscription.getSubCancellationDate().toLocalDate() ).isBefore( subscription.getExpirationDate() ) )
        {
            if( ( subscription.getFrequency().equals( Frequency.DAILY ) &&
                  subscription.getDuration() > ChronoUnit.DAYS.between( subscription.getInvoiceDate(), subscription.getSubCancellationDate() ) ) ||
                ( subscription.getFrequency().equals( Frequency.MONTHLY ) &&
                  subscription.getDuration() > ChronoUnit.MONTHS.between( subscription.getInvoiceDate(), subscription.getSubCancellationDate() ) ) ||
                ( subscription.getFrequency().equals( Frequency.ANNUALLY ) &&
                  subscription.getDuration() > ChronoUnit.YEARS.between( subscription.getInvoiceDate(), subscription.getSubCancellationDate() ) ) ||
                ( subscription.getFrequency().equals( Frequency.EVERY_OTHER_MONTH ) &&
                  subscription.getDuration() > ChronoUnit.MONTHS.between( subscription.getInvoiceDate(), subscription.getSubCancellationDate() ) / 2 ) ||
                ( subscription.getFrequency().equals( Frequency.QUARTERLY ) &&
                  subscription.getDuration() > ChronoUnit.YEARS.between( subscription.getInvoiceDate(), subscription.getSubCancellationDate() ) + 3 ) ||
                ( subscription.getFrequency().equals( Frequency.SEMIANNUALLY ) &&
                  subscription.getDuration() > ChronoUnit.YEARS.between( subscription.getInvoiceDate(), subscription.getSubCancellationDate() ) * 2 ) ||
                ( subscription.getFrequency().equals( Frequency.WEEKLY ) &&
                  subscription.getDuration() > ChronoUnit.WEEKS.between( subscription.getInvoiceDate(), subscription.getSubCancellationDate() ) ) ||
                ( subscription.getFrequency().equals( Frequency.EVERY_OTHER_WEEK ) &&
                  subscription.getDuration() > ChronoUnit.WEEKS.between( subscription.getInvoiceDate(), subscription.getSubCancellationDate() ) / 2 ) )
            {
                subDuration = getRemainingDurationByFrequency( subscription );
            }
            else
            {
                subDuration = subscription.getDuration();
            }
        }
        else if( Objects.nonNull( subscription.getExpirationDate() ) &&
                 ( subscription.getFrequency().equals( Frequency.DAILY ) &&
                   subscription.getDuration() > ChronoUnit.DAYS.between( subscription.getInvoiceDate(), subscription.getExpirationDate() ) ) ||
                 ( subscription.getFrequency().equals( Frequency.MONTHLY ) &&
                   subscription.getDuration() > ChronoUnit.MONTHS.between( subscription.getInvoiceDate(), subscription.getExpirationDate() ) ) ||
                 ( subscription.getFrequency().equals( Frequency.ANNUALLY ) &&
                   subscription.getDuration() > ChronoUnit.YEARS.between( subscription.getInvoiceDate(), subscription.getExpirationDate() ) ) ||
                 ( subscription.getFrequency().equals( Frequency.EVERY_OTHER_MONTH ) &&
                   subscription.getDuration() > ChronoUnit.MONTHS.between( subscription.getInvoiceDate(), subscription.getExpirationDate() ) / 2 ) ||
                 ( subscription.getFrequency().equals( Frequency.QUARTERLY ) &&
                   subscription.getDuration() > ChronoUnit.YEARS.between( subscription.getInvoiceDate(), subscription.getExpirationDate() ) + 3 ) ||
                 ( subscription.getFrequency().equals( Frequency.SEMIANNUALLY ) &&
                   subscription.getDuration() > ChronoUnit.YEARS.between( subscription.getInvoiceDate(), subscription.getExpirationDate() ) * 2 ) ||
                 ( subscription.getFrequency().equals( Frequency.WEEKLY ) &&
                   subscription.getDuration() > ChronoUnit.WEEKS.between( subscription.getInvoiceDate(), subscription.getExpirationDate() ) ) ||
                 ( subscription.getFrequency().equals( Frequency.EVERY_OTHER_WEEK ) &&
                   subscription.getDuration() > ChronoUnit.WEEKS.between( subscription.getInvoiceDate(), subscription.getExpirationDate() ) / 2 ) )
        {
            subDuration = getRemainingDurationByFrequency( subscription );
        }
        else
        {
            subDuration = subscription.getDuration();
        }
        if( invoice.size() == subDuration )
        {
            dueAmount = new BigDecimal( 0 );
            invoiceCountDue = 0;
        }
        else
        {
            if( invoice.size() > 0 )
            {
                invoiceCountDue = subDuration - invoice.size();
            }
            else
            {
                invoiceCountDue = subDuration;
            }
            PricingDetailsVO pricingDetailsVO = invoiceService.calculateTaxPricing( subscription, subscription.getLocationId() );
            dueAmount = new BigDecimal( subDuration - invoice.size() ).multiply( pricingDetailsVO.getTotalAmount() );
        }
        if( Objects.nonNull( subscription.getSubCancellationDate() ) && ( subscription.getSubCancellationDate().isBefore(
            LocalDateTime.now( Clock.systemUTC() ) ) || subscription.getSubCancellationDate().isEqual( LocalDateTime.now( Clock.systemUTC() ) ) ) ||
            Objects.nonNull( subscription.getExpirationDate() ) && ( subscription.getExpirationDate().isBefore(
                LocalDate.now( Clock.systemUTC() ) ) || subscription.getExpirationDate().isEqual( LocalDate.now( Clock.systemUTC() ) ) ) )
        {
            dueAmount = new BigDecimal( 0 );
            invoiceCountDue = 0;
        }
        subscriptionDueResponse.setInvoiceAmountDue( dueAmount );
        subscriptionDueResponse.setInvoiceCountDue( invoiceCountDue );
        return subscriptionDueResponse;
    }

    private int getRemainingDurationByFrequency( Subscription subscription )
    {
        Subscription subLocal = new Subscription();
        subLocal.setSubCancellationDate( subscription.getSubCancellationDate() );
        subLocal.setInvoiceDate( subscription.getInvoiceDate() );
        subLocal.setExpirationDate( subscription.getExpirationDate() );
        int subDuration = 1;
        if( Objects.nonNull( subLocal.getSubCancellationDate() ) && subLocal.getSubCancellationDate().isAfter( LocalDateTime.now( Clock.systemUTC() ) ) &&
            ( subLocal.getSubCancellationDate().toLocalDate() ).isBefore( subLocal.getExpirationDate() ) )
        {
            subLocal.setExpirationDate( subLocal.getSubCancellationDate().toLocalDate() );
        }
        switch( subscription.getFrequency() )
        {
            case DAILY:
                subDuration = (int) ChronoUnit.DAYS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() );
                break;
            case WEEKLY:
                if( ( subLocal.getInvoiceDate().getDayOfMonth() ) - ( subLocal.getExpirationDate().getDayOfMonth() ) % 7 != 0 )
                {
                    subDuration = subDuration + (int) ChronoUnit.WEEKS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() );
                }
                else
                {
                    subDuration = (int) ChronoUnit.WEEKS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() );
                }
                break;
            case EVERY_OTHER_WEEK:
                if( ( subLocal.getInvoiceDate().getDayOfMonth() ) - ( subLocal.getExpirationDate().getDayOfMonth() ) % 7 != 0 )
                {
                    subDuration = subDuration + (int) ChronoUnit.WEEKS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() ) / 2;
                }
                else
                {
                    subDuration = (int) ChronoUnit.WEEKS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() ) / 2;
                }

                break;
            case MONTHLY:
                if( subLocal.getInvoiceDate().getDayOfMonth() != subLocal.getExpirationDate().getDayOfMonth() )
                {
                    subDuration = subDuration + (int) ChronoUnit.MONTHS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() );
                }
                else
                {
                    subDuration = (int) ChronoUnit.MONTHS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() );
                }
                break;
            case EVERY_OTHER_MONTH:
                if( subLocal.getInvoiceDate().getDayOfMonth() != subLocal.getExpirationDate().getDayOfMonth() )
                {
                    subDuration = subDuration + (int) ChronoUnit.MONTHS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() ) / 2;
                }
                else
                {
                    subDuration = (int) ChronoUnit.MONTHS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() ) / 2;
                }
                break;
            case ANNUALLY:
                if( subLocal.getInvoiceDate().getDayOfMonth() != subLocal.getExpirationDate().getDayOfMonth() )
                {
                    subDuration = subDuration + (int) ChronoUnit.YEARS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() );
                }
                else
                {
                    subDuration = (int) ChronoUnit.YEARS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() );
                }
                break;
            case QUARTERLY:
                subDuration = (int) ChronoUnit.YEARS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() ) + 3;
                break;
            case SEMIANNUALLY:
                subDuration = (int) ChronoUnit.YEARS.between( subLocal.getInvoiceDate(), subLocal.getExpirationDate() ) + 1;
                break;
        }
        return subDuration;
    }

}
