package com.abcfinancial.api.billing.subscriptionmanagement.subscription.service;

import com.abcfinancial.api.billing.generalledger.invoice.service.InvoiceService;
import com.abcfinancial.api.billing.scheduler.EventScheduler;
import com.abcfinancial.api.billing.scheduler.schedules.Schedule;
import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionDetails;
import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionExpired;
import com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer.FreezeSubscriptionGenerator;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.MemberSubscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.SubscriptionItem;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.SubscriptionTypeEnum;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionMembersRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.*;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.CustomDate;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import com.abcfinancial.api.billing.utility.exception.CustomErrorResponse;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.google.i18n.phonenumbers.NumberParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service

public class FreezeSubscriptionService
{
    @Autowired
    CustomDate customDate;
    @Autowired
    InvoiceService invoiceService;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private EventScheduler eventScheduler;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private SubscriptionMembersRepository membersRepository;
    @Autowired
    private FreezeSubscriptionGenerator freezeSubscriptionGenerator;

    public FreezeSubscriptionVO freezeSubscription( UUID subscriptionId, FreezeSubscriptionVO freezeSubscriptionVO ) throws NumberParseException, CloneNotSupportedException
    {
        Subscription subscription = subscriptionRepository.findById( subscriptionId ).orElseThrow( () -> new ErrorResponse(
            new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB ) ) ) );
        FreezeSubscriptionVO subscriptionVO = null;

        if( Objects.isNull( freezeSubscriptionVO.getFreezeStartDate() ) || Objects.isNull( freezeSubscriptionVO.getFreezeEndDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_START_DATE_AND_END_DATE ) ) );
        }

        if( ( Objects.nonNull( freezeSubscriptionVO.getRenewStartDate() ) &&
              Objects.nonNull( freezeSubscriptionVO.getRenewExpirationDate() ) &&
              Objects.nonNull( freezeSubscriptionVO.getRenewInvoiceDate() ) &&
              Objects.nonNull( freezeSubscriptionVO.getSubExpirationDate() )
        ) )
        {
            validateRenewSubscription( freezeSubscriptionVO );
        }
        if( subscription.getSubscriptionTypeEnum().equals( SubscriptionTypeEnum.RENEW )
            && ( Objects.nonNull( freezeSubscriptionVO.getRenewStartDate() ) ||
                 Objects.nonNull( freezeSubscriptionVO.getRenewExpirationDate() ) ||
                 Objects.nonNull( freezeSubscriptionVO.getRenewInvoiceDate() )
            ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_FOR_RENEW_SUBSCRIPTION ) ) );
        }
        if( subscription.isAutoRenew() && ( Objects.isNull( freezeSubscriptionVO.getRenewStartDate() ) ||
                                            Objects.isNull( freezeSubscriptionVO.getRenewExpirationDate() ) ||
                                            Objects.isNull( freezeSubscriptionVO.getRenewInvoiceDate() )
        ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DETAILS_NEEDED_FOR_RENEW ) ) );
        }
        if( !subscription.isAutoRenew() && ( Objects.nonNull( freezeSubscriptionVO.getRenewStartDate() ) ||
                                             Objects.nonNull( freezeSubscriptionVO.getRenewExpirationDate() ) ||
                                             Objects.nonNull( freezeSubscriptionVO.getRenewInvoiceDate() )
        ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEWAL_OPTION_FALSE ) ) );
        }
        if( freezeSubscriptionVO.getFreezeStartDate().isBefore( LocalDate.now() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FREEZE_START_DATE_NOT_VALID ) ) );
        }
        if( null == subscription.getExpirationDate() )
        {
            subscriptionVO = null;
        }
        else
        {
            subscriptionVO = freezeSubscriptionForTerm( subscription, freezeSubscriptionVO );
        }
        return subscriptionVO;
    }

    private void validateRenewSubscription( FreezeSubscriptionVO freezeSubscriptionVO )
    {
        if( freezeSubscriptionVO.getRenewStartDate().isBefore( freezeSubscriptionVO.getSubExpirationDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEWDATE_BEFORE_EXPIRE ) ) );
        }
        if( freezeSubscriptionVO.getRenewInvoiceDate().isBefore( freezeSubscriptionVO.getRenewStartDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_INVOICE_DATE_BEFORE_RENEW_START ) ) );
        }
        if( freezeSubscriptionVO.getRenewInvoiceDate().isAfter( freezeSubscriptionVO.getRenewExpirationDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_INVOICE_DATE_AFTER_RENEW_EXP ) ) );
        }
        if( freezeSubscriptionVO.getRenewExpirationDate().isBefore( freezeSubscriptionVO.getRenewStartDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_END_DATE_BEFORE_START_DATE ) ) );
        }

    }

    public FreezeSubscriptionVO freezeSubscriptionForTerm( Subscription subscription, FreezeSubscriptionVO freezeSubscriptionVO ) throws CloneNotSupportedException
    {
        SubscriptionVO subscriptionVO = ModelMapperUtils.map( subscription, SubscriptionVO.class );
        BigDecimal totalAmount = new BigDecimal( 0 );
        List<MemberSubscription> memberSubscriptionList = membersRepository.findListBySubId( subscription.getSubId() );
        Subscription subscriptionRes = subscriptionRepository.findSubscription( subscription.getLocationId(), subscription.getSubId() );
        UUID renewSubId;
        if( null != subscriptionRes && Objects.nonNull( subscriptionRes.getSubNextRefId() ) )
        {
            UUID freezeId = subscriptionRes.getSubNextRefId();
            Optional<Subscription> freezeSubscription = subscriptionRepository.findById( freezeId );
            if( freezeSubscription.isPresent() && freezeSubscription.get().getSubscriptionTypeEnum().equals( SubscriptionTypeEnum.FREEZE ) )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CAN_NOT_FREEZE ) ) );
            }
        }
        if( subscription.getSubscriptionTypeEnum().equals( SubscriptionTypeEnum.RENEW ) &&
            ( Objects.nonNull( freezeSubscriptionVO.getRenewStartDate() ) || Objects.nonNull( freezeSubscriptionVO.getRenewInvoiceDate() ) ||
              Objects.nonNull( freezeSubscriptionVO.getRenewExpirationDate() ) ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_FOR_RENEW_SUBSCRIPTION ) ) );
        }

        if( Objects.isNull( freezeSubscriptionVO.getSubExpirationDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EXPIRATION_DATE ) ) );
        }
        if( freezeSubscriptionVO.getFreezeEndDate().isAfter( freezeSubscriptionVO.getSubExpirationDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FREEZE_END_DATE_AFTER_SUB_EXPIRE_DATE ) ) );
        }

        if( subscription.isActive() )
        {
            if( ( null != freezeSubscriptionVO.getFreezeStartDate() ) && ( null != freezeSubscriptionVO.getFreezeEndDate() ) )
            {
                if( freezeSubscriptionVO.getFreezeStartDate().isBefore( subscription.getExpirationDate() ) )
                {

                    if( freezeSubscriptionVO.getFreezeEndDate().isAfter( freezeSubscriptionVO.getFreezeStartDate() ) )
                    {
                        if( null == subscription.getFreezeStartDate() && null == subscription.getFreezeEndDate() )
                        {
                            renewSubId = subscription.getSubNextRefId();
                            subscription.setFreezeStartDate( freezeSubscriptionVO.getFreezeStartDate().atStartOfDay() );
                            subscription.setExpirationDate( freezeSubscriptionVO.getFreezeStartDate() );
                            subscription.setFreezeEndDate( freezeSubscriptionVO.getFreezeEndDate().atStartOfDay() );
                            if( null == freezeSubscriptionVO.getFreezeAmount() )
                            {
                                freezeSubscriptionVO.setFreezeAmount( new BigDecimal( 0 ) );
                                subscription.setFreezeAmount( new BigDecimal( 0 ) );
                            }
                            else if( freezeSubscriptionVO.getFreezeAmount().compareTo( new BigDecimal( 0 ) ) >= 0 )
                            {
                                subscription.setFreezeAmount( freezeSubscriptionVO.getFreezeAmount() );
                            }
                            else
                            {
                                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PRICE ) ) );
                            }
                            subscription.setMemberSubscriptionList( memberSubscriptionList );
                            originalSubscriptionCancelExpireEvent( subscription );
                            Optional<UUID> expiredEventId = originalSubscriptionNewExpireEvent( subscription );
                            if( expiredEventId.isPresent() )
                            {
                                subscription.setSubExpiredEventId( expiredEventId.get() );
                            }

                            if( subscription.getExpirationDate().isEqual( LocalDate.now() ) || subscription.getExpirationDate().isBefore( LocalDate.now() ) )
                            {
                                subscription.setActive( false );
                            }
                            else
                            {
                                subscription.setActive( true );
                            }

                            subscriptionRepository.save( subscription );
                            Subscription freezeSubscription = (Subscription) subscription.clone();
                            freezeSubscription.setSubId( null );
                            List<SubscriptionItem> subscriptionItems = subscription.getItems();
                            for( int index = 0; index < subscriptionItems.size(); index++ )
                            {
                                totalAmount = totalAmount.add( subscriptionItems.get( index ).getPrice() );
                            }
                            if( null != totalAmount )
                            {
                                if( totalAmount.compareTo( freezeSubscriptionVO.getFreezeAmount() ) < 0 )
                                {
                                    throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PRICE_CHECK ) ) );
                                }
                            }
                            for( int index = 0; index < subscriptionItems.size(); index++ )
                            {
                                subscriptionItems.get( index ).setId( null );
                                BigDecimal freeAmount = subscription.getFreezeAmount();
                                if( !new BigDecimal( 0 ).equals( freeAmount ) )
                                {
                                    if( index == 0 )
                                    {
                                        subscriptionItems.get( index ).setPrice( freeAmount );
                                    }
                                    else
                                    {
                                        subscriptionItems.get( index ).setPrice( new BigDecimal( 0 ) );
                                    }
                                }
                                else
                                {
                                    subscriptionItems.get( index ).setPrice( new BigDecimal( 0 ) );
                                }
                            }
                            freezeSubscription.setItems( subscriptionItems );
                            freezeSubscription.setStart( freezeSubscriptionVO.getFreezeStartDate() );
                            freezeSubscription.setExpirationDate( freezeSubscriptionVO.getFreezeEndDate() );
                            freezeSubscription.setMemberSubscriptionList( memberSubscriptionList );

                            freezeSubscription.setSubPrevRefId( subscription.getSubId() );
                            freezeSubscription.setSubscriptionTypeEnum( SubscriptionTypeEnum.FREEZE );

                            Subscription freezesubscription = subscriptionRepository.save( freezeSubscription );

                            log.info( "subscription pointer adding" );
                            subscription.setSubNextRefId( freezesubscription.getSubId() );
                            log.info( "subscription pointer added" );
                            subscriptionRepository.save( subscription );
                            Optional<UUID> freezeExpiredEventId = freezeSubscriptionExpireEvent( freezeSubscription );
                            if( freezeExpiredEventId.isPresent() )
                            {
                                freezeSubscription.setSubExpiredEventId( freezeExpiredEventId.get() );
                            }
                            subscriptionRepository.save( freezeSubscription );
                            freezeSubscriptionGenerator.send( freezesubscription );
                            freezeSubscriptionVO.setId( freezesubscription.getSubId() );
                            Optional<UUID> freezeInvoiceEventId = createSubscriptionTriggerEvent( freezesubscription, subscriptionVO );
                            if( freezeInvoiceEventId.isPresent() )
                            {
                                freezeSubscription.setScheduleInvoicesId( freezeInvoiceEventId.get() );
                            }

                            if( freezeSubscription.getStart().isEqual( LocalDate.now() ) )
                            {
                                freezeSubscription.setActive( true );
                            }
                            else
                            {
                                freezeSubscription.setActive( false );
                            }

                            subscriptionRepository.save( freezeSubscription );

                            Subscription clonesubscription = (Subscription) subscription.clone();
                            Subscription originalSubscription = ModelMapperUtils.map( subscriptionVO, Subscription.class );
                            List<SubscriptionItem> cloneItems = originalSubscription.getItems();
                            clonesubscription.setSubId( null );
                            for( int index = 0; index < cloneItems.size(); index++ )
                            {
                                cloneItems.get( index ).setId( null );
                            }

                            clonesubscription.setSubPrevRefId( freezesubscription.getSubId() );
                            clonesubscription.setSubNextRefId( renewSubId );
                            clonesubscription.setItems( cloneItems );
                            clonesubscription.setAccount( subscription.getAccount() );
                            clonesubscription.setMemberSubscriptionList( subscription.getMemberSubscriptionList() );
                            clonesubscription.setStart( freezeSubscriptionVO.getFreezeEndDate() );
                            clonesubscription.setExpirationDate( freezeSubscriptionVO.getSubExpirationDate() );
                            clonesubscription.setSubscriptionTypeEnum( SubscriptionTypeEnum.CLONE );
                            Subscription cloneSubscription = subscriptionRepository.save( clonesubscription );
                            cloneSubscription.setInvoiceDate( freezeSubscriptionVO.getFreezeEndDate() );
                            SubscriptionVO cloneSubscriptionVO = ModelMapperUtils.map( cloneSubscription, SubscriptionVO.class );
                            Optional<UUID> cloneExpireEventId = subscriptionService.invoiceExpireEvent( cloneSubscriptionVO );
                            Optional<UUID> cloneInvoiceEventId = createCloneSubscriptionTriggerEvent( cloneSubscription, subscriptionVO );
                            if( cloneExpireEventId.isPresent() )
                            {
                                clonesubscription.setSubExpiredEventId( cloneExpireEventId.get() );
                            }
                            if( cloneInvoiceEventId.isPresent() )
                            {
                                clonesubscription.setScheduleInvoicesId( cloneInvoiceEventId.get() );
                            }

                            clonesubscription.setActive( false );
                            Subscription savedCloneSubscription = subscriptionRepository.save( clonesubscription );
                            freezeSubscription.setSubNextRefId( savedCloneSubscription.getSubId() );
                            subscriptionRepository.save( freezeSubscription );
                            if( Objects.nonNull( renewSubId ) )
                            {
                                Optional<Subscription> subscriptionRenew = subscriptionRepository.findById( renewSubId );
                                if( subscriptionRenew.isPresent() )
                                {
                                    Subscription renewSubscription = subscriptionRenew.get();
                                    if( subscription.getSubscriptionTypeEnum().equals( SubscriptionTypeEnum.SECONDARY ) &&
                                        renewSubscription.getSubscriptionTypeEnum().equals( SubscriptionTypeEnum.RENEW ) &&
                                        ( Objects.nonNull( freezeSubscriptionVO.getRenewStartDate() ) && Objects.nonNull( freezeSubscriptionVO.getRenewInvoiceDate() ) &&
                                          Objects.nonNull( freezeSubscriptionVO.getRenewExpirationDate() ) ) )
                                    {
                                        freezeSubscriptionVO = updateRenewSubscriptionForTerm( subscription, freezeSubscriptionVO, savedCloneSubscription, renewSubscription );
                                    }
                                }
                            }
                        }
                        else
                        {
                            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_START_CANT_BEFORE_FREEZE ) ) );
                        }
                    }
                    else
                    {
                        throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_END_DATE_BEFORE_START_DATE ) ) );
                    }

                }
                else
                {
                    throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_START_CANT_BEFORE_FREEZE ) ) );
                }
            }
            else
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_START_DATE_AND_END_DATE ) ) );
            }
        }
        else
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_SUBSCRIPTION_CANT_FREEZE_ALRAEDY_EXPIRING ) ) );
        }
        return freezeSubscriptionVO;
    }

    public boolean originalSubscriptionCancelExpireEvent( Subscription subscription )
    {
        return eventScheduler.deleteSubscriptionExpireJob( subscription.getSubExpiredEventId() );
    }

    public Optional<UUID> originalSubscriptionNewExpireEvent( Subscription subscriptionVO )
    {
        return eventScheduler.scheduleSubscriptionExpire( Schedule.<SubscriptionExpired>builder().expire( subscriptionVO.getExpirationDate() )
                                                                                                 .repeating( false )
                                                                                                 .properties( SubscriptionExpired.builder()
                                                                                                                                 .locationId( subscriptionVO.getLocationId() )
                                                                                                                                 .subscriptionId( subscriptionVO.getSubId() )
                                                                                                                                 .freezeSubscriptionRequest( true )
                                                                                                                                 .subExpDate( subscriptionVO.getExpirationDate()
                                                                                                                                 )
                                                                                                                                 .build() )
                                                                                                 .build() );
    }

    public Optional<UUID> freezeSubscriptionExpireEvent( Subscription subscriptionVO )
    {
        return eventScheduler.scheduleSubscriptionExpire( Schedule.<SubscriptionExpired>builder().expire( subscriptionVO.getExpirationDate() )
                                                                                                 .repeating( false )
                                                                                                 .properties( SubscriptionExpired.builder()
                                                                                                                                 .locationId( subscriptionVO.getLocationId() )
                                                                                                                                 .subscriptionId( subscriptionVO.getSubId() )
                                                                                                                                 .freezeSubscriptionRequest( true )
                                                                                                                                 .subExpDate( subscriptionVO.getExpirationDate() )
                                                                                                                                 .build() )
                                                                                                 .build() );
    }

    public Optional<UUID> createSubscriptionTriggerEvent( Subscription subscription, SubscriptionVO subscriptionVO )
    {
        subscriptionVO.setSubId( subscription.getSubId() );
        return eventScheduler.scheduleInvoices( Schedule.<SubscriptionDetails>builder().start( subscription.getStart().atStartOfDay() )
                                                                                       .repeating( false )
                                                                                       .frequency( Period.ofDays( 1 ) )
                                                                                       .duration( Period.ofDays( (int) ChronoUnit.DAYS
                                                                                           .between( subscription.getStart(), subscription.getExpirationDate() ) ) )
                                                                                       // .duration( Period.ofDays( subscription.getFreezeEndDate( ).compareTo( subscription
                                                                                       // .getFreezeStartDate( ) ) ) )
                                                                                       .properties( SubscriptionDetails.builder()
                                                                                                                       .locationId( subscription.getLocationId() )
                                                                                                                       .subscriptionId( subscription.getSubId() )
                                                                                                                       .build() )
                                                                                       .build() );
    }

    public Optional<UUID> createCloneSubscriptionTriggerEvent( Subscription subscription, SubscriptionVO subscriptionVO )
    {
        subscriptionVO.setSubId( subscription.getSubId() );
        return eventScheduler.scheduleInvoices( Schedule.<SubscriptionDetails>builder().start( subscription.getStart().atTime( LocalTime.now() ) )
                                                                                       .repeating( false )
                                                                                       .frequency( subscriptionVO.getFrequency().getPeriod() )
                                                                                       .duration( subscriptionService
                                                                                           .calculateDuration( subscriptionVO.getFrequency(), subscriptionVO.getDuration() ) )
                                                                                       .properties( SubscriptionDetails.builder()
                                                                                                                       .locationId( subscription.getLocationId() )
                                                                                                                       .subscriptionId( subscription.getSubId() )
                                                                                                                       .build() )
                                                                                       .build() );
    }

    public FreezeSubscriptionVO updateRenewSubscriptionForTerm( Subscription subscription, FreezeSubscriptionVO freezeSubscriptionVO,
        Subscription savedCloneSubscription, Subscription renewSubscription )
    {
        SubscriptionVO subscriptionVO = ModelMapperUtils.map( subscription, SubscriptionVO.class );

        renewSubscription.setStart( freezeSubscriptionVO.getRenewStartDate() );
        renewSubscription.setExpirationDate( freezeSubscriptionVO.getRenewExpirationDate() );
        renewSubscription.setInvoiceDate( freezeSubscriptionVO.getRenewInvoiceDate() );
        renewSubscription.setSubPrevRefId( savedCloneSubscription.getSubId() );

        SubscriptionVO renewSubscriptionVO = ModelMapperUtils.map( renewSubscription, SubscriptionVO.class );
        Optional<UUID> renewExpireEventId = subscriptionService.invoiceExpireEvent( renewSubscriptionVO );
        Optional<UUID> cloneInvoiceEventId = createCloneSubscriptionTriggerEvent( renewSubscription, subscriptionVO );
        if( renewExpireEventId.isPresent() )
        {
            renewSubscription.setSubExpiredEventId( renewExpireEventId.get() );
        }
        if( cloneInvoiceEventId.isPresent() )
        {
            renewSubscription.setScheduleInvoicesId( cloneInvoiceEventId.get() );
        }

        originalSubscriptionCancelExpireEvent( renewSubscription );
        Optional<UUID> expiredEventId = originalSubscriptionNewExpireEvent( renewSubscription );
        if( expiredEventId.isPresent() )
        {
            renewSubscription.setSubExpiredEventId( expiredEventId.get() );
        }
        subscriptionRepository.save( renewSubscription );
        freezeSubscriptionVO.setRenewSubId( renewSubscription.getSubId() );

        return freezeSubscriptionVO;
    }

    public RemoveFreezeSubscriptionVO removeFreeze( UUID subFreezeId, RemoveFreezeSubscriptionVO freezeSubscriptionVO )
    {

        Subscription freezeSubscription = subscriptionRepository.findById( subFreezeId ).orElseThrow( () -> new ErrorResponse(
            new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB ) ) ) );
        freezeSubscriptionVO.setId( subFreezeId );
        validateRenewSubscriptionForRemove( freezeSubscriptionVO );
        if( Objects.isNull( freezeSubscriptionVO.getSubExpirationDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EXPIRATION_DATE ) ) );
        }
        if( freezeSubscription.getSubscriptionTypeEnum().equals( SubscriptionTypeEnum.FREEZE ) )
        {
            if( null != freezeSubscription.getStart() && ( freezeSubscription.getStart().isEqual( LocalDate.now() ) ) ||
                freezeSubscription.getStart().isBefore( LocalDate.now() ) )
            {
                freezeSubscription.setExpirationDate( LocalDate.now() );
                freezeSubscriptionVO.setFreezeStartDate( freezeSubscription.getStart() );
                freezeSubscriptionVO.setFreezeEndDate( freezeSubscription.getExpirationDate() );
                if( null != freezeSubscription.getSubExpiredEventId() )
                {
                    eventScheduler.deleteSubscriptionExpireJob( freezeSubscription.getSubExpiredEventId() );
                    Optional<UUID> freezeExpiredEventId = freezeSubscriptionExpireEvent( freezeSubscription );
                    if( freezeExpiredEventId.isPresent() )
                    {
                        freezeSubscription.setSubExpiredEventId( freezeExpiredEventId.get() );
                    }
                    if( null != freezeSubscription.getFreezeAmount() )
                    {
                        freezeSubscriptionVO.setFreezeAmount( freezeSubscription.getFreezeAmount() );
                    }
                }
                subscriptionRepository.save( freezeSubscription );

                UUID renewSubId = null;
                if( Objects.nonNull( freezeSubscriptionVO.getSubExpirationDate() ) )
                {
                    UUID cloneSubid = freezeSubscription.getSubNextRefId();
                    Optional<Subscription> cloneSubs = subscriptionRepository.findById( cloneSubid );

                    if( cloneSubs.isPresent() )
                    {
                        Subscription cloneSubscription = cloneSubs.get();

                        SubscriptionVO subscriptionVO = ModelMapperUtils.map( cloneSubscription, SubscriptionVO.class );
                        cloneSubscription.setStart( LocalDate.now( Clock.systemUTC() ) );
                        if( null != cloneSubscription.getScheduleInvoicesId() )
                        {
                            eventScheduler.cancelScheduledInvoices( cloneSubscription.getScheduleInvoicesId() );
                            Optional<UUID> cloneInvoiceEventId = createCloneSubscriptionTriggerEvent( cloneSubscription, subscriptionVO );
                            if( cloneInvoiceEventId.isPresent() )
                            {
                                cloneSubscription.setScheduleInvoicesId( cloneInvoiceEventId.get() );
                            }
                        }
                        if( null != cloneSubscription.getSubExpiredEventId() )
                        {
                            eventScheduler.deleteSubscriptionExpireJob( cloneSubscription.getSubExpiredEventId() );
                            SubscriptionVO cloneSubscriptionVO = ModelMapperUtils.map( cloneSubscription, SubscriptionVO.class );
                            Optional<UUID> cloneExpireEventId = subscriptionService.invoiceExpireEvent( cloneSubscriptionVO );
                            if( cloneExpireEventId.isPresent() )
                            {
                                cloneSubscription.setSubExpiredEventId( cloneExpireEventId.get() );
                            }
                        }
                        cloneSubscription.setInvoiceDate( LocalDate.now( Clock.systemUTC() ) );
                        cloneSubscription.setExpirationDate( freezeSubscriptionVO.getSubExpirationDate() );
                        renewSubId = cloneSubscription.getSubNextRefId();

                        subscriptionRepository.save( cloneSubscription );
                    }
                }

                if( Objects.nonNull( renewSubId ) )
                {
                    Optional<Subscription> renewSubOptional = subscriptionRepository.findById( renewSubId );
                    if( renewSubOptional.isPresent() )
                    {
                        Subscription renewSubscription = renewSubOptional.get();
                        if( renewSubscription.getSubscriptionTypeEnum().equals( SubscriptionTypeEnum.RENEW ) && Objects.nonNull( freezeSubscriptionVO.getRenewStartDate() ) &&
                            Objects.nonNull( freezeSubscriptionVO.getRenewExpirationDate() ) &&
                            Objects.nonNull( freezeSubscriptionVO.getRenewInvoiceDate() ) )
                        {
                            SubscriptionVO subscriptionVO = ModelMapperUtils.map( renewSubscription, SubscriptionVO.class );
                            renewSubscription.setStart( freezeSubscriptionVO.getRenewStartDate() );
                            if( null != renewSubscription.getScheduleInvoicesId() )
                            {
                                eventScheduler.cancelScheduledInvoices( renewSubscription.getScheduleInvoicesId() );
                                Optional<UUID> cloneInvoiceEventId = createCloneSubscriptionTriggerEvent( renewSubscription, subscriptionVO );
                                if( cloneInvoiceEventId.isPresent() )
                                {
                                    renewSubscription.setScheduleInvoicesId( cloneInvoiceEventId.get() );
                                }
                            }
                            if( null != renewSubscription.getSubExpiredEventId() )
                            {
                                eventScheduler.deleteSubscriptionExpireJob( renewSubscription.getSubExpiredEventId() );
                                SubscriptionVO cloneSubscriptionVO = ModelMapperUtils.map( renewSubscription, SubscriptionVO.class );
                                Optional<UUID> cloneExpireEventId = subscriptionService.invoiceExpireEvent( cloneSubscriptionVO );
                                if( cloneExpireEventId.isPresent() )
                                {
                                    renewSubscription.setSubExpiredEventId( cloneExpireEventId.get() );
                                }
                            }
                            renewSubscription.setExpirationDate( freezeSubscriptionVO.getRenewExpirationDate() );
                            subscriptionRepository.save( renewSubscription );
                        }
                    }
                }

            }
            else
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NOT_IN_FREEZE_STATE ) ) );
            }

        }
        else
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NOT_IN_FREEZE_STATE ) ) );
        }
        RemoveFreezeSubscriptionVO removeFreezeSubscriptionVO = ModelMapperUtils.map( freezeSubscriptionVO, RemoveFreezeSubscriptionVO.class );
        return removeFreezeSubscriptionVO;
    }

    private void validateRenewSubscriptionForRemove( RemoveFreezeSubscriptionVO freezeSubscriptionVO )
    {

        if( Objects.nonNull( freezeSubscriptionVO.getRenewStartDate() ) && freezeSubscriptionVO.getRenewStartDate().isBefore( freezeSubscriptionVO.getSubExpirationDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEWDATE_BEFORE_EXPIRE ) ) );
        }
        if( Objects.nonNull( freezeSubscriptionVO.getRenewStartDate() ) && Objects.nonNull( freezeSubscriptionVO.getRenewInvoiceDate() ) &&
            freezeSubscriptionVO.getRenewInvoiceDate().isBefore( freezeSubscriptionVO.getRenewStartDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_INVOICE_DATE_BEFORE_RENEW_START ) ) );
        }
        if( Objects.nonNull( freezeSubscriptionVO.getRenewExpirationDate() ) && Objects.nonNull( freezeSubscriptionVO.getRenewInvoiceDate() ) &&
            freezeSubscriptionVO.getRenewInvoiceDate().isAfter( freezeSubscriptionVO.getRenewExpirationDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RENEW_INVOICE_DATE_AFTER_RENEW_EXP ) ) );
        }
        if( Objects.nonNull( freezeSubscriptionVO.getRenewExpirationDate() ) && Objects.nonNull( freezeSubscriptionVO.getRenewStartDate() ) &&
            freezeSubscriptionVO.getRenewExpirationDate().isBefore( freezeSubscriptionVO.getRenewStartDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_END_DATE_BEFORE_START_DATE ) ) );
        }

    }

    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public UpdateFreezeSubscriptionVO updateFreezeSubscription( UpdateFreezeSubscriptionRequestVO freezeSubscriptionVO )
    {
        UpdateFreezeSubscriptionVO updateFreezeSubscriptionVO = null;
        BigDecimal totalAmount = new BigDecimal( 0 );
        FreezeSubscriptionVO updatedFreezeSubscriptionVO = null;

        validateUpdateFreezeSubscription( freezeSubscriptionVO );

        Optional<Subscription> freezeSubscriptionDtl = subscriptionRepository.findById( freezeSubscriptionVO.getSubId() );
        Subscription freezeSubscription = freezeSubscriptionDtl.get();

        Optional<Subscription> subsOptional = subscriptionRepository.findById( freezeSubscription.getSubId() );
        if( !subsOptional.isPresent() )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_NOT_EXIST ) ) );
        }

        Subscription originalFreezeSubscription = subsOptional.get();

        List<SubscriptionItem> subscriptionItems = originalFreezeSubscription.getItems();

        for( int index = 0; index < subscriptionItems.size(); index++ )
        {
            totalAmount = totalAmount.add( subscriptionItems.get( index ).getPrice() );
        }
        if( null != totalAmount && Objects.nonNull( freezeSubscriptionVO.getFreezeAmount() ) && totalAmount.compareTo( freezeSubscriptionVO.getFreezeAmount() ) < 0 )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PRICE_CHECK ) ) );
        }

        //FOR FREEZE DATE UPDATE
        if( Objects.nonNull( freezeSubscriptionVO.getFreezeStartDate() ) )
        {
            originalFreezeSubscription.setStart( originalFreezeSubscription.getStart() );
        }
        if( Objects.nonNull( freezeSubscriptionVO.getFreezeEndDate() ) )
        {
            originalFreezeSubscription.setExpirationDate( freezeSubscriptionVO.getFreezeEndDate() );
        }
        if( null != freezeSubscriptionVO.getFreezeAmount() && freezeSubscriptionVO.getFreezeAmount() != freezeSubscription.getFreezeAmount() )
        {
            originalFreezeSubscription.setFreezeAmount( freezeSubscriptionVO.getFreezeAmount() );
        }

        // for both scenario in end date
        // we have to schedule freeze expire on requested date by canceling original expire of freeze subscription event
        SubscriptionVO freezeSubscriptionVO1 = ModelMapperUtils.map( originalFreezeSubscription, SubscriptionVO.class );
        Optional<UUID> freezeExpiredEventId = freezeSubscriptionExpireEvent( originalFreezeSubscription );
        if( freezeExpiredEventId.isPresent() )
        {
            originalFreezeSubscription.setSubExpiredEventId( freezeExpiredEventId.get() );
        }
        Optional<UUID> freezeInvoiceEventId = createSubscriptionTriggerEvent( originalFreezeSubscription, freezeSubscriptionVO1 );
        if( freezeInvoiceEventId.isPresent() )
        {
            freezeSubscription.setScheduleInvoicesId( freezeInvoiceEventId.get() );
        }

        //FOR UPDATING THE CLONE SUBSCRIPTION AFTER FREEZE UPDATE
        UUID cloneRefId = originalFreezeSubscription.getSubNextRefId();
        Optional<Subscription> cloneSubscriptionOptional = subscriptionRepository.findById( cloneRefId );
        Subscription cloneSubscription = null;
        if( cloneSubscriptionOptional.isPresent() &&
            ( Objects.nonNull( freezeSubscriptionVO.getFreezeEndDate() ) || Objects.nonNull( freezeSubscriptionVO.getSubExpirationDate() ) ) )
        {
            cloneSubscription = cloneSubscriptionOptional.get();
            if( Objects.nonNull( freezeSubscriptionVO.getSubExpirationDate() ) && ( freezeSubscriptionVO.getSubExpirationDate().isBefore( cloneSubscription.getExpirationDate() ) ||
                                                                                    freezeSubscriptionVO.getSubExpirationDate().isBefore( cloneSubscription.getStart() ) ) )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_START_CANT_BEFORE_FREEZE ) ) );
            }

            if( Objects.nonNull( freezeSubscriptionVO.getSubExpirationDate() ) )
            {
                cloneSubscription.setStart( originalFreezeSubscription.getExpirationDate() );
                cloneSubscription.setInvoiceDate( cloneSubscription.getStart() );
            }
            if( Objects.nonNull( freezeSubscriptionVO.getSubExpirationDate() ) )
            {
                cloneSubscription.setExpirationDate( freezeSubscriptionVO.getSubExpirationDate() );
            }

            // we have to schedule expire of cloned subscription on new requested date by canceling old clone expire event
            SubscriptionVO cloneSubscriptionVO = ModelMapperUtils.map( cloneSubscription, SubscriptionVO.class );
            Optional<UUID> cloneExpireEventId = subscriptionService.invoiceExpireEvent( cloneSubscriptionVO );
            if( cloneExpireEventId.isPresent() )
            {
                cloneSubscription.setSubExpiredEventId( cloneExpireEventId.get() );
            }
            Optional<UUID> cloneInvoiceEventId = createCloneSubscriptionTriggerEvent( cloneSubscription, cloneSubscriptionVO );
            if( cloneInvoiceEventId.isPresent() )
            {
                cloneSubscription.setScheduleInvoicesId( cloneInvoiceEventId.get() );
            }
            subscriptionRepository.save( cloneSubscription );
        }

        subscriptionRepository.save( originalFreezeSubscription );
        updateFreezeSubscriptionVO = ModelMapperUtils.map( originalFreezeSubscription, UpdateFreezeSubscriptionVO.class );
        if( Objects.nonNull( freezeSubscriptionVO.getRenewStartDate() ) && Objects.nonNull( freezeSubscriptionVO.getRenewExpirationDate() ) &&
            Objects.nonNull( freezeSubscriptionVO.getRenewInvoiceDate() ) )
        {
            updateFreezeSubscriptionVO = updateRenewSubscriptiondetails( freezeSubscriptionVO, cloneSubscription );
        }
        //freezeSubscriptionVO = ModelMapperUtils.map( originalFreezeSubscription, FreezeSubscriptionVO.class );

        return updateFreezeSubscriptionVO;
    }

    private void validateUpdateFreezeSubscription( UpdateFreezeSubscriptionRequestVO freezeSubscriptionVO )
    {

        if( Objects.isNull( freezeSubscriptionVO.getSubId() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FREEZE_ID_NULL ) ) );
        }
        if( !subscriptionRepository.existsById( freezeSubscriptionVO.getSubId() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_NOT_EXIST ) ) );
        }

        if( Objects.isNull( freezeSubscriptionVO.getFreezeStartDate() ) && Objects.isNull( freezeSubscriptionVO.getFreezeEndDate() ) &&
            Objects.isNull( freezeSubscriptionVO.getSubExpirationDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NO_FIELD_PROVIDED ) ) );
        }

        Optional<Subscription> freezeSubscriptionDtl = subscriptionRepository.findById( freezeSubscriptionVO.getSubId() );
        Subscription freezeSubscription = freezeSubscriptionDtl.get();

        if( !freezeSubscription.getSubscriptionTypeEnum().equals( SubscriptionTypeEnum.FREEZE ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NOT_A_FREEZE_SUBSCRIPTION ) ) );
        }

        if( Objects.nonNull( freezeSubscriptionVO.getFreezeStartDate() ) && freezeSubscriptionVO.getFreezeStartDate().isBefore( LocalDate.now() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FREEZE_START_DATE_NOT_VALID ) ) );
        }

        if( Objects.nonNull( freezeSubscriptionVO.getSubExpirationDate() ) && Objects.nonNull( freezeSubscriptionVO.getFreezeStartDate() ) &&
            freezeSubscriptionVO.getFreezeStartDate().isAfter( freezeSubscriptionVO.getSubExpirationDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_START_CANT_BEFORE_FREEZE ) ) );
        }

        if( Objects.nonNull( freezeSubscriptionVO.getFreezeEndDate() ) && freezeSubscriptionVO.getFreezeEndDate().isBefore( LocalDate.now() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FREEZE_END_DATE_NOT_VALID ) ) );
        }

        if( Objects.nonNull( freezeSubscriptionVO.getFreezeEndDate() ) && Objects.nonNull( freezeSubscriptionVO.getFreezeStartDate() ) &&
            freezeSubscriptionVO.getFreezeEndDate().isBefore( freezeSubscriptionVO.getFreezeStartDate() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_END_DATE_BEFORE_START_DATE ) ) );
        }

        if( Objects.nonNull( freezeSubscriptionVO.getFreezeAmount() ) && freezeSubscriptionVO.getFreezeAmount().compareTo( new BigDecimal( 0 ) ) < 0 )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PRICE ) ) );

        }

    }

    private UpdateFreezeSubscriptionVO updateRenewSubscriptiondetails( UpdateFreezeSubscriptionRequestVO freezeSubscriptionVO, Subscription cloneSubscription )
    {
        UpdateFreezeSubscriptionVO updateFreezeSubscriptionVO = null;
        UUID renewSubsid = cloneSubscription.getSubNextRefId();
        Optional<Subscription> renewSubsOptioanal = subscriptionRepository.findById( renewSubsid );

        if( renewSubsOptioanal.isPresent() && renewSubsOptioanal.get().getSubscriptionTypeEnum().equals( SubscriptionTypeEnum.RENEW ) )
        {
            Subscription renewSubscription = renewSubsOptioanal.get();
            renewSubscription.setStart( freezeSubscriptionVO.getRenewStartDate() );
            renewSubscription.setInvoiceDate( freezeSubscriptionVO.getRenewInvoiceDate() );
            renewSubscription.setExpirationDate( freezeSubscriptionVO.getRenewExpirationDate() );

            SubscriptionVO renewSubscriptionVO = ModelMapperUtils.map( renewSubscription, SubscriptionVO.class );
            Optional<UUID> renewExpireEventId = subscriptionService.invoiceExpireEvent( renewSubscriptionVO );
            Optional<UUID> renewInvoiceEventId = createCloneSubscriptionTriggerEvent( renewSubscription, renewSubscriptionVO );
            if( renewExpireEventId.isPresent() )
            {
                renewSubscription.setSubExpiredEventId( renewExpireEventId.get() );
            }
            if( renewInvoiceEventId.isPresent() )
            {
                renewSubscription.setScheduleInvoicesId( renewInvoiceEventId.get() );
            }

            originalSubscriptionCancelExpireEvent( renewSubscription );
            Optional<UUID> expiredEventId = originalSubscriptionNewExpireEvent( renewSubscription );
            if( expiredEventId.isPresent() )
            {
                renewSubscription.setSubExpiredEventId( expiredEventId.get() );
            }
            subscriptionRepository.save( renewSubscription );
            updateFreezeSubscriptionVO = ModelMapperUtils.map( renewSubscription, UpdateFreezeSubscriptionVO.class );
            updateFreezeSubscriptionVO.setRenewSubId( renewSubscription.getSubId() );

            return updateFreezeSubscriptionVO;
        }
        return updateFreezeSubscriptionVO;
    }

    private CustomDate differenceBetweenLocalDateTime( LocalDateTime fromDateTime, LocalDateTime toDateTime )
    {
        long years;
        long months;
        long days;
        long hours;
        long minutes;
        long seconds;
        LocalDateTime tempDateTime = LocalDateTime.from( fromDateTime );
        years = tempDateTime.until( toDateTime, ChronoUnit.YEARS );
        tempDateTime = tempDateTime.plusYears( years );
        months = tempDateTime.until( toDateTime, ChronoUnit.MONTHS );
        tempDateTime = tempDateTime.plusMonths( months );
        days = tempDateTime.until( toDateTime, ChronoUnit.DAYS );
        tempDateTime = tempDateTime.plusDays( days );
        hours = tempDateTime.until( toDateTime, ChronoUnit.HOURS );
        tempDateTime = tempDateTime.plusHours( hours );
        minutes = tempDateTime.until( toDateTime, ChronoUnit.MINUTES );
        tempDateTime = tempDateTime.plusMinutes( minutes );
        seconds = tempDateTime.until( toDateTime, ChronoUnit.SECONDS );
        customDate.setYears( years );
        customDate.setMonths( months );
        customDate.setDays( days );
        customDate.setHours( hours );
        customDate.setMinutes( minutes );
        customDate.setSeconds( seconds );

        return customDate;
    }

    @Transactional
    public FreezeSubscriptionResponseVo getFreezeSubscriptionById( UUID subscriptionId, UUID locationId )
    {
        FreezeSubscriptionResponseVo freezeSubscriptionVO = null;
        Subscription subscription = subscriptionRepository.findSubscription( locationId, subscriptionId );
        if( null != subscription )
        {
            if( Objects.nonNull( subscription.getSubNextRefId() ) )
            {
                Optional<Subscription> freezeSubscriptionOptional = subscriptionRepository.findBySubId( subscription.getSubNextRefId() );

                if( freezeSubscriptionOptional.isPresent() && freezeSubscriptionOptional.get().getSubscriptionTypeEnum().equals( SubscriptionTypeEnum.FREEZE ) )
                {

                    freezeSubscriptionVO = ModelMapperUtils.map( freezeSubscriptionOptional.get(), FreezeSubscriptionResponseVo.class );
                    return freezeSubscriptionVO;
                }
                else
                {
                    throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NO_FREEZE_SUBSCRIPTION ) ) );
                }
            }
            else
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NO_FREEZE_SUBSCRIPTION ) ) );
            }
        }
        else
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_ID_NOT_EXIST_WITH_LOCATION ) ) );
        }
    }
}

