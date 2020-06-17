package com.abcfinancial.api.billing.subscriptionmanagement.account.member.service;

import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.abcfinancial.api.billing.generalledger.invoice.repository.InvoiceRepository;
import com.abcfinancial.api.billing.generalledger.payment.domain.Brand;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.domain.Type;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.payment.service.PaymentService;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodResponseVO;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodVO;
import com.abcfinancial.api.billing.generalledger.statements.service.BalanceService;
import com.abcfinancial.api.billing.scheduler.EventScheduler;
import com.abcfinancial.api.billing.scheduler.schedules.Schedule;
import com.abcfinancial.api.billing.scheduler.schedules.StatementEventDetails;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.LocationAccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.MemberAccountID;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.MemberCreation;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.repository.MemberCreationRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.valueobject.GetPayorResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.valueobject.MemberCreationVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.valueobject.MemberVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.AccountService;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AccountResponseVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Payment;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.CanadaBankCode;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.InvoiceTypeEnum;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.PaymentRepository;
import com.abcfinancial.api.billing.utility.common.*;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.common.domain.NotFoundResponseError;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.abcfinancial.api.billing.utility.common.MessageUtils.ACCOUNT_ROUTING_NUMBER_REGEX;

@Service

@Slf4j

public class MemberAccountService
{
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private MemberCreationRepository memberCreationRepository;
    @Autowired
    private LocationAccountRepository clientAccountRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private EventScheduler eventScheduler;
    @Value( "${generalLedger.scheduleTime.statement}" )
    private String payorSchedulerTime;

    @Transactional( propagation = Propagation.REQUIRED )
    public MemberCreationVO createMember( MemberCreationVO memberCreationVO ) throws NumberParseException
    {
        log.debug( "Member account {}", memberCreationVO );
        validateMandatoryFieldsForMemberCreation( memberCreationVO );
        Optional<LocationAccount> clientAccount = clientAccountRepository.findByLocaccIdLocation( memberCreationVO.getLocationId() );
        if( !clientAccount.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberAccountService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INCORRECT_LOCATION_ID ) ) );
        }
        if( null == memberCreationVO.getAccount() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberAccountService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_BLANK ) ) );
        }
        if( null == memberCreationVO.getAccount().getPaymentMethod() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberAccountService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_METHOD_BLANK ) ) );
        }
        if( memberCreationVO.getAccount().getPaymentMethod().getTokenId() == null )
        {
            validatePaymentMethod( memberCreationVO );
        }
        else
        {
            if( null == memberCreationVO.getAccount().getPaymentMethod().getType() )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_TYPE_BLANK ) ) );
            }
        }
        MemberCreation memberCreation = ModelMapperUtils.map( memberCreationVO, MemberCreation.class );
        log.trace( "printing token" + memberCreationVO.getAccount().getPaymentMethod().getToken() );
        PaymentMethod paymentMethod = ModelMapperUtils.map( memberCreationVO.getAccount().getPaymentMethod(), PaymentMethod.class );
        Account account = ModelMapperUtils.map( memberCreationVO.getAccount(), Account.class );
        account.setLocation( memberCreationVO.getLocationId() );
        if( StringUtils.isNotBlank( account.getPhone() ) )
        {
            if( !StringUtils.isNumeric( account.getPhone() ) )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PHONE_INVALID ) ) );
            }

            Phonenumber.PhoneNumber phoneNumber;
            phoneNumber = CommonUtil.toGoogleLibphone( account.getPhone() );
            account.setPhone( String.valueOf( phoneNumber.getCountryCode() + "" + phoneNumber.getNationalNumber() ) );
        }
        log.debug( "account entity {}", account );
        Account dbAccountObj = accountRepository.save( account );
        paymentMethod.setAccountId( dbAccountObj );
        paymentMethod.setLocationId( memberCreationVO.getLocationId() );
        paymentMethod.setActive( Boolean.TRUE );
        if( memberCreationVO.getAccount().getPaymentMethod().getTokenId() == null )
        {
            if( memberCreationVO.getAccount().getPaymentMethod().getType() == Type.CREDIT_CARD )
            {
                paymentMethod.setBrand( Brand.valueOf( "VISA" ) );
                paymentMethod.setDisplay( "8787" );
                paymentMethod.setExpiryMonth( 12 );
                paymentMethod.setExpiryYear( 18 );
            }
            else if( memberCreationVO.getAccount().getPaymentMethod().getType() == Type.BANK_ACCOUNT )
            {
                paymentMethod.setDisplay( CommonUtil.getLastnCharacters( memberCreationVO.getAccount().getPaymentMethod().getAccountNumber(), 4 ) );
                //Changes to add Alias field
                paymentMethod.setAlias( memberCreationVO.getAccount().getPaymentMethod().getAlias() );
            }
        }
        else
        {
            paymentMethod.setTokenId( memberCreationVO.getAccount().getPaymentMethod().getTokenId() );
        }
        log.debug( "Inserting paymentMethod = {}", paymentMethod );
        PaymentMethod paymentMethodEntity = paymentMethodRepository.save( paymentMethod );
        PaymentMethodVO paymentMethodVO = ModelMapperUtils.map( paymentMethodEntity, PaymentMethodVO.class );
        MemberAccountID memberAccountID = new MemberAccountID();
        memberAccountID.setAccountId( dbAccountObj );
        memberCreation.setMemberId( memberCreationVO.getMemberId() );
        memberAccountID.setPayorId( memberCreationVO.getPayorId() );
        memberCreation.setMemberAccountID( memberAccountID );
        memberCreation.setLocId( memberCreationVO.getLocationId() );
        log.debug( "Inserting memberCreation = {}", memberCreation );
        memberCreationRepository.save( memberCreation );
        AccountVO accountVO = ModelMapperUtils.map( dbAccountObj, AccountVO.class );
        accountVO.setAccountId( dbAccountObj.getAccountId() );
        accountVO.setPaymentMethod( paymentMethodVO );
        memberCreationVO.setAccount( accountVO );
        balanceService.createBalance( memberCreationVO.getAccount().getAccountId(), BigDecimal.ZERO );
        balanceService.createPaymentMethodBalance( memberCreationVO.getAccount().getAccountId(), paymentMethodEntity.getId(), BigDecimal.ZERO );
        eventScheduler.scheduleAccountLedgerEvent(
            Schedule.<StatementEventDetails>builder().start( account.getBillingDate().atTime( CommonUtil.convertTimeStringToLocalTime( payorSchedulerTime ) ) )
                                                     .repeating( false )
                                                     .properties( StatementEventDetails.builder()
                                                                                       .paymentMethodId( paymentMethodVO.getId() )
                                                                                       .build() )
                                                     .build() );
        return memberCreationVO;
    }

    private void validateMandatoryFieldsForMemberCreation( MemberCreationVO memberCreationVO )
    {
        UUID locationId = memberCreationVO.getLocationId();
        UUID memberId = memberCreationVO.getMemberId();
        log.debug( "member id {}", memberId );
        UUID payorId = memberCreationVO.getPayorId();
        if( null == memberCreationVO.getAccount().getBillingDate() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BILLING_DATE_BLANK ) ) );
        }
        if( memberCreationVO.getAccount().getBillingDate().isBefore( LocalDate.now() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BILLINGDATE_PASTDATECHECK ) ) );
        }
        if( Objects.isNull( memberCreationVO.getAccount().getSevaluation() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AccountService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_S_EVALUATION ) ) );
        }
        if( memberCreationVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.DAILY.toString() ) ||
            memberCreationVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.WEEKLY.toString() ) ||
            memberCreationVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.MONTHLY.toString() ) ||
            memberCreationVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.ANNUALLY.toString() ) ||
            memberCreationVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.QUARTERLY.toString() ) ||
            memberCreationVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.SEMIANNUALLY.toString() ) ||
            memberCreationVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.EVERY_OTHER_MONTH.toString() ) ||
            memberCreationVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.EVERY_OTHER_WEEK.toString() ) )
        {
            log.debug( "memberCreationVO.getAccount( )" + memberCreationVO.getAccount() );
        }
        else
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberAccountService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CYCLE_CHECK ) ) );
        }
        if( null == locationId )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATION_ID ) ) );
        }
        //P3-1512
        if( Strings.isNotEmpty( memberCreationVO.getAccount().getName() ) )
        {
            //New condition added as P3-1921
            if( memberCreationVO.getAccount().getName().length() > 100 || memberCreationVO.getAccount().getName().length() < 1 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NAME_SIZE ) ) );
            }
        }
        if( StringUtils.isBlank( memberCreationVO.getAccount().getSevaluation() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_S_EVALUATION ) ) );
        }
        if( memberCreationVO.getAccount().getSevaluation().length() > 100 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_SEVALUATION_SIZE ) ) );
        }
        if( null == payorId )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYOR_ID ) ) );
        }
        MemberCreation memberCreation = memberCreationRepository.findByMemberAccountIDPayorId( payorId );
        if( memberCreation != null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class, "Account already exist for Payor Id: " + payorId ) );
        }
        if( null == memberCreationVO.getAccount() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_VALUE ) ) );
        }
        if( null == memberCreationVO.getAccount().getName() || memberCreationVO.getAccount().getName().trim().length() <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NAME_SIZE ) ) );
        }
    }

    private void validatePaymentMethod( MemberCreationVO memberCreationVO )
    {
        if( null == memberCreationVO.getAccount().getPaymentMethod().getType() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_TYPE_BLANK ) ) );
        }
        if( null == memberCreationVO.getAccount().getPaymentMethod() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_PAYLOAD ) ) );
        }
        if( memberCreationVO.getAccount().getPaymentMethod().getType() == Type.BANK_ACCOUNT )
        { //required if bank account
            if( memberCreationVO.getAccount().getPaymentMethod().getBankAccountType() == null )
            {
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class, "Bank account type must not be blank or null." ) );
            }
            String routingNumber;
            if( null == memberCreationVO.getAccount().getPaymentMethod().getRoutingNumber() ||
                memberCreationVO.getAccount().getPaymentMethod().getRoutingNumber().trim().length() <= 0 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER_BLANK ) ) );
            }
            else
            {
                routingNumber = memberCreationVO.getAccount().getPaymentMethod().getRoutingNumber().trim();
                if( CommonUtil.containsWhitespace( routingNumber ) )
                {
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SPACE_NOT_ALLOWED_BETWEEN_ROUTINGNUMBER ) ) );
                }
                if( routingNumber != null && !StringUtils.isNumeric( routingNumber ) )
                {
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER_INTEGER ) ) );
                }
                if( ( routingNumber != null && routingNumber.length() < 9 || routingNumber.length() > 9 ) )
                {
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreationVO.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER_BETWEEN_1_10 ) ) );
                }
                try
                {
                    Integer.parseInt( routingNumber );
                    if( ABARoutingNumber.isValidRoutingNumber( routingNumber ) )
                    {
                        memberCreationVO.getAccount().getPaymentMethod().setRoutingNumber( routingNumber );
                    }
                    else
                    {
                        String inputBankCode = routingNumber.substring( 1, 4 );
                        CanadaBankCode bankCode = CanadaBankCode.forBankCode( inputBankCode );
                        if( null != bankCode )
                        {
                            memberCreationVO.getAccount().getPaymentMethod().setRoutingNumber( routingNumber );
                        }
                        else
                        {
                            throw new ErrorResponse(
                                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), CommonUtil.class, " Please enter a valid Routing number" ) );
                        }
                    }
                }
                catch( NumberFormatException exception )
                {
                    log.debug( "Unable to validate payment method", exception );
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER_INTEGER ) ) );
                }
            }
            if( null == memberCreationVO.getAccount().getPaymentMethod().getAccountNumber() ||
                memberCreationVO.getAccount().getPaymentMethod().getAccountNumber().trim().length() <= 0 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NUMBER_BLANK ) ) );
            }
            else
            {
                String accountNumber = memberCreationVO.getAccount().getPaymentMethod().getAccountNumber().trim();
                if( CommonUtil.containsWhitespace( accountNumber ) )
                {
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SPACE_NOT_ALLOWED_BETWEEN_ACCOUNTNUMBER ) ) );
                }
                if( ABARoutingNumber.isValidRoutingNumber( routingNumber ) )
                {
                    memberCreationVO.getAccount().getPaymentMethod().setAccountNumber( accountNumber );
                    if( accountNumber.matches( ACCOUNT_ROUTING_NUMBER_REGEX ) )
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNTNUMBER_ALL_ZERO ) ) );
                    }
                    if( !StringUtils.isNumeric( accountNumber ) )
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NUMBER_INTEGER ) ) );
                    }
                    if( accountNumber.length() < 4 || accountNumber.length() > 12 )
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NUMBER_SIZE ) ) );
                    }
                }
                else
                {
                    String inputBankCode = routingNumber.substring( 1, 4 );
                    String transitNumber = routingNumber.substring( 4, 9 );
                    CanadaBankCode bankCode = CanadaBankCode.forBankCode( inputBankCode );
                    if( null != bankCode )
                    {
                        String canadaAccountNo = bankCode.getNumber() + "" + transitNumber + "" + accountNumber;
                        memberCreationVO.getAccount().getPaymentMethod().setAccountNumber( canadaAccountNo );
                    }
                    else
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_BANK_CODE ) ) );
                    }
                    if( !StringUtils.isNumeric( accountNumber ) )
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NUMBER_INTEGER ) ) );
                    }
                    if( accountNumber.length() < 12 || accountNumber.length() > 12 )
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NUMBER_SIZE_FOR_CANADA ) ) );
                    }
                }
            }
            if( null != memberCreationVO.getAccount().getPaymentMethod().getProcessor() || null != memberCreationVO.getAccount().getPaymentMethod().getBrand() ||
                null != memberCreationVO.getAccount().getPaymentMethod().getToken() || memberCreationVO.getAccount().getPaymentMethod().getExpiryYear() != 0 ||
                memberCreationVO.getAccount().getPaymentMethod().getExpiryMonth() != 0 )
            {
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CARD_DETAILS_NOT_ALLOW ) ) );
            }
            if( memberCreationVO.getAccount().getEmail() != null )
            {
                boolean result = CommonUtil.isValidEmailAddress( memberCreationVO.getAccount().getEmail() );
                if( !result )
                {
                    throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EMAIL_INVALID ) ) );
                }
            }
        }
        else if( memberCreationVO.getAccount().getPaymentMethod().getType() == Type.CREDIT_CARD )
        {
            //New condition for Alias
            if( memberCreationVO.getAccount().getPaymentMethod().getAlias() != null )
            {
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Payment.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_DETAIL_NOT_ALLOW ) ) );
            }
            if( null == memberCreationVO.getAccount().getPaymentMethod().getToken() || memberCreationVO.getAccount().getPaymentMethod().getToken().trim().length() <= 0 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOKEN_BLANK ) ) );
            }
            if( memberCreationVO.getAccount().getEmail() != null )
            {
                boolean result = CommonUtil.isValidEmailAddress( memberCreationVO.getAccount().getEmail() );
                if( !result )
                {
                    throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EMAIL_INVALID ) ) );
                }
            }
            if( null != memberCreationVO.getAccount().getPaymentMethod().getRoutingNumber() || null != memberCreationVO.getAccount().getPaymentMethod().getAccountNumber() )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BANK_DETAIL ) ) );
            }
        }
        else if( memberCreationVO.getAccount().getPaymentMethod().getType() == Type.CASH )
        {
            if( null != memberCreationVO.getAccount().getPaymentMethod().getToken() || null != memberCreationVO.getAccount().getPaymentMethod().getRoutingNumber()
                || null != memberCreationVO.getAccount().getPaymentMethod().getAccountNumber() || null != memberCreationVO.getAccount().getPaymentMethod().getBrand() ||
                null != memberCreationVO.getAccount().getPaymentMethod().getAlias() || 0 > memberCreationVO.getAccount().getPaymentMethod().getExpiryMonth() ||
                0 > memberCreationVO.getAccount().getPaymentMethod().getExpiryYear() )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BANK_CREDIT_DETAIL ) ) );
            }
        }
        else
        {
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BANK_OR_CREDIT_CARD ) ) );
        }
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public MemberCreationVO getMember( UUID memberId )
    {
        PaymentMethodVO paymentMethodVO = null;
        Optional<MemberCreation> memberAccount = Optional.ofNullable( memberCreationRepository.findMemberById( memberId ) );
        if( !memberAccount.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ACCOUNT_NOT_FOUND ) + memberId ) );
        }
        Account account = memberAccount.get().getMemberAccountID().getAccountId();
        PaymentMethod paymentMethod = paymentMethodRepository
            .findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( memberAccount.get().getMemberAccountID().getAccountId().getAccountId(), Boolean.TRUE );
        if( paymentMethod != null )
        {
            paymentMethodVO = ModelMapperUtils.map( paymentMethod, PaymentMethodVO.class );
        }
        else
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.NOT_FOUND.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_NOT_FOUND ) ) );
        }
        AccountVO accountVO = ModelMapperUtils.map( account, AccountVO.class );
        accountVO.setPaymentMethod( paymentMethodVO );
        MemberCreationVO memberCreationVO = ModelMapperUtils.map( memberAccount, MemberCreationVO.class );
        memberCreationVO.setMemberId( memberAccount.get().getMemberId() );
        memberCreationVO.setAccount( accountVO );
        memberCreationVO.setLocationId( accountVO.getPaymentMethod().getLocationId() );
        return memberCreationVO;
    }

    @Transactional( readOnly = true )
    public MemberCreationVO reviewPayor( UUID accountId )
    {
        PaymentMethodVO paymentMethodVO = null;
        Optional<MemberCreation> memberAccount = Optional.ofNullable( memberCreationRepository.findByMemberAccountIDAccountIdAccountId( accountId ) );
        if( !memberAccount.isPresent() )
        {
            throw new ErrorResponse( new NotFoundResponseError( MemberCreation.class, accountId ) );
        }
        Account account = memberAccount.get().getMemberAccountID().getAccountId();
        PaymentMethod paymentMethod = paymentMethodRepository
            .findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( memberAccount.get().getMemberAccountID().getAccountId().getAccountId(), Boolean.TRUE );
        if( !Objects.isNull( paymentMethod ) )
        {
            paymentMethodVO = ModelMapperUtils.map( paymentMethod, PaymentMethodVO.class );
        }
        AccountVO accountVO = ModelMapperUtils.map( account, AccountVO.class );
        accountVO.setPaymentMethod( paymentMethodVO );
        MemberCreationVO memberCreationVO = ModelMapperUtils.map( memberAccount, MemberCreationVO.class );
        memberCreationVO.setMemberId( memberAccount.get().getMemberId() );
        memberCreationVO.setAccount( accountVO );
        memberCreationVO.setLocationId( accountVO.getPaymentMethod().getLocationId() );
        memberCreationVO.setPayorId( memberAccount.get().getMemberAccountID().getPayorId() );
        return memberCreationVO;
    }

    @Transactional( readOnly = true )
    public GetPayorResponse getPayor( UUID accountId )
    {
        List<PaymentMethodResponseVO> paymentMethodResponseVO = null;
        Optional<MemberCreation> memberAccount = Optional.ofNullable( memberCreationRepository.findByMemberAccountIDAccountIdAccountId( accountId ) );
        if( !memberAccount.isPresent() )
        {
            throw new ErrorResponse( new NotFoundResponseError( MemberCreation.class, accountId ) );
        }
        Account account = memberAccount.get().getMemberAccountID().getAccountId();
        List<PaymentMethod> paymentMethod = paymentMethodRepository.findByAccountIdAccountId( memberAccount.get().getMemberAccountID().getAccountId().getAccountId() );
        if( !Objects.isNull( paymentMethod ) )
        {
            paymentMethodResponseVO = ModelMapperUtils.mapAll( paymentMethod, PaymentMethodResponseVO.class );
        }
        AccountResponseVO accountVO = ModelMapperUtils.map( account, AccountResponseVO.class );
        accountVO.setPaymentMethod( paymentMethodResponseVO );
        GetPayorResponse getPayorResponse = ModelMapperUtils.map( memberAccount, GetPayorResponse.class );
        getPayorResponse.setMemberId( memberAccount.get().getMemberId() );
        getPayorResponse.setAccount( accountVO );
        getPayorResponse.setLocationId( accountVO.getPaymentMethod().get( 0 ).getLocationId() );
        getPayorResponse.setPayorId( memberAccount.get().getMemberAccountID().getPayorId() );
        return getPayorResponse;
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public Account updateAccountByAccountId( UUID accountId, MemberCreationVO memberVO )
    {
        log.debug( "trying to fetch Account details by accountId Id = {}", accountId );
        Account account = accountRepository.findById( accountId ).orElseThrow(
            () -> new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NO_CONTENT.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ACCOUNT_NOT_FOUND ) ) ) );
        log.debug( "trying to fetch member details by member Id = {}", accountId );
        account.setName( memberVO.getAccount().getName() );
        account = accountRepository.save( account );
        return account;
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public MemberVO updateMemberLocationId( UUID memberId, MemberVO memberVO )
    {
        log.debug( "Inside the updateMemberLocationId( UUID memberId, MemberCreationVO memberVO )" );
        validateMandatoryFieldForUpdateMemeberLocation( memberId, memberVO );
        // Update location in payor_account table
        MemberCreation memberCreation = memberCreationRepository.findLocationByMemberId( memberId );
        memberCreation.setLocId( memberVO.getLocationId() );
        memberCreationRepository.save( memberCreation );
        // Update location and invoiceNo in invoice table
        List<Invoice> invoice = invoiceRepository.findLocationByMemberId2( memberId );
        IntStream.range( 0, invoice.size() ).forEach( i -> {
            invoice.get( i ).setLocationId( memberVO.getLocationId() );
            invoice.get( i ).setInvoiceType( InvoiceTypeEnum.T );
            invoice.get( i ).setTransferDate( LocalDateTime.now( Clock.systemUTC() ) );
        } );
        invoiceRepository.saveAll( invoice );
        Invoice invoice2 = invoiceRepository.findLocationByMemberId( memberId );
        List<Payment> payment = paymentRepository.findByAccountIdByLocation2( invoice2.getAccountId() );
        IntStream.range( 0, payment.size() ).forEach( i -> {
            payment.get( i ).setLocationId( memberVO.getLocationId() );
            payment.get( i ).setInvoiceType( InvoiceTypeEnum.T );
            payment.get( i ).setTransferDate( LocalDateTime.now( Clock.systemUTC() ) );
        } );
        paymentRepository.saveAll( payment );
        MemberVO memberCVO = ModelMapperUtils.map( memberCreation, MemberVO.class );
        memberCVO.setLocationId( memberCreation.getLocId() );
        return memberCVO;
    }

    private void validateMandatoryFieldForUpdateMemeberLocation( UUID memberId, MemberVO memberVO )
    {
        log.trace( "Inside the validateMandatoryFieldForUpdateMemeberLocation( UUID memberId, MemberVO memberVO )" );
        if( null == memberId )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ACCOUNT_NOT_FOUND ) ) );
        }
        if( null == memberVO.getLocationId() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATION_ID_NULL ) ) );
        }
    }

    @Transactional( readOnly = true )
    public List<MemberCreationVO> reviewPayorAccounts( String name, Pageable pageable )
    {
        if( name == null )
        {
            name = Strings.EMPTY;
        }
        else if( name.length() < 4 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberAccountService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NAME_LENGTH ) ) );
        }
        List<MemberCreation> memberCreations = memberCreationRepository.getAllPayorAccountsByName( "%" + name + "%", pageable );
        List<MemberCreationVO> memberCreationVOList = ModelMapperUtils.mapAll( memberCreations, MemberCreationVO.class );
        IntStream.range( 0, memberCreationVOList.size() ).forEach( i -> {
            MemberCreationVO memberCreationVO = memberCreationVOList.get( i );
            MemberCreation memberCreation = memberCreations.get( i );
            memberCreationVO.setLocationId( memberCreation.getLocId() );
            memberCreationVO.setPayorId( memberCreation.getMemberAccountID().getPayorId() );
            memberCreationVO.setMemberId( memberCreation.getMemberId() );
            memberCreationVO.setAccount( ModelMapperUtils.map( memberCreation.getMemberAccountID().getAccountId(), AccountVO.class ) );
            PaymentMethod paymentMethod = paymentMethodRepository
                .findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( memberCreation.getMemberAccountID().getAccountId().getAccountId(), Boolean.TRUE );
            PaymentMethodVO paymentMethodVO = ModelMapperUtils.map( paymentMethod, PaymentMethodVO.class );
            memberCreationVO.getAccount().setPaymentMethod( paymentMethodVO );
        } );
        if( memberCreations.isEmpty() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), MemberAccountService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RECORD_NOT_FOUND ) ) );
        }
        else
        {
            return memberCreationVOList;
        }
    }
}
