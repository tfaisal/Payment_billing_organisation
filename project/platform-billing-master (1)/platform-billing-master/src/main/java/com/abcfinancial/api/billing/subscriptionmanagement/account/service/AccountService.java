package com.abcfinancial.api.billing.subscriptionmanagement.account.service;

import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.domain.Type;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.payment.service.PaymentService;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodVO;
import com.abcfinancial.api.billing.generalledger.statements.domain.Statement;
import com.abcfinancial.api.billing.generalledger.statements.service.BalanceService;
import com.abcfinancial.api.billing.scheduler.EventScheduler;
import com.abcfinancial.api.billing.scheduler.schedules.Schedule;
import com.abcfinancial.api.billing.scheduler.schedules.StatementEventDetails;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.CompanyAddress;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccountID;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.LocationAccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationAccountResponseVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationAccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.MemberCreation;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.repository.MemberCreationRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountStatementRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.*;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.Address;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.helper.AddressServiceHelper;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.service.AvaOnboardingService;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.CanadaBankCode;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.abcfinancial.api.billing.utility.common.*;
import com.abcfinancial.api.billing.utility.exception.ConflictErrorResponse;
import com.abcfinancial.api.billing.utility.exception.CustomErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.common.domain.NotFoundResponseError;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.abcfinancial.api.billing.utility.common.MessageUtils.ACCOUNT_ROUTING_NUMBER_REGEX;

@Service
@Slf4j

public class AccountService
{
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private LocationAccountRepository locationAccountRepository;
    @Autowired
    private AccountStatementRepository accountStatementRepository;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private MemberCreationRepository memberCreationRepository;
    @Autowired
    private AvaOnboardingService avaOnboardingService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private EventScheduler eventScheduler;
    @Value( "${avalara.offer}" )
    private String avaOffer;
    @Value( "${generalLedger.scheduleTime.settlement}" )
    private String clientSchedulerTime;

    @Transactional( propagation = Propagation.REQUIRED )
    public LocationAccountResponseVO createLocationAccount( HttpHeaders headers, LocationAccountRequest locationAccountVO, boolean isAccountTest )
    {
        LocationAccountResponseVO locationResponseVO = null;
        UUID locationId = locationAccountVO.getLocationId();
        validateMandatoryFieldsForAccount( locationAccountVO );
        if( locationId == null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AccountService.class, "LocationID cannot be empty or blank" ) );
        }
        if( null == locationAccountVO.getAccount() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AccountService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_BLANK ) ) );
        }
        if( null == locationAccountVO.getAccount().getPaymentMethod() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AccountService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_METHOD_BLANK ) ) );
        }
        if( locationAccountVO.getAccount().getPaymentMethod().getTokenId() == null )
        {
            validatePaymentMethodFields( locationAccountVO );
        }
        else
        {
            if( null == locationAccountVO.getAccount().getPaymentMethod().getType() )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AccountService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_TYPE_BLANK ) ) );
            }
        }
        //Added condition to check if account already exist P3-1918 so that multiple location account are not created with same locationID
        List<LocationAccount> locationAccounts = locationAccountRepository.getAllByLocationIdAndDeActivate( locationAccountVO.getLocationId() );
        if( locationAccounts != null && !locationAccounts.isEmpty() )
        {
            //Use Custom ConflictErrorResponse class for Conflict Bug - P3-3409
            throw new ErrorResponse( new ConflictErrorResponse( HttpStatus.CONFLICT.value(), UpdateAccountDetailVO.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATION ) + locationAccountVO.getLocationId() ) );
        }
        locationAccountVO.getAccount().getPaymentMethod().setActive( true );
        Account account = ModelMapperUtils.map( locationAccountVO.getAccount(), Account.class );
        account.setLocation( locationId );
        PaymentMethod paymentMethod = ModelMapperUtils.map( locationAccountVO.getAccount().getPaymentMethod(), PaymentMethod.class );
        LocationAccountID locationAccountID = new LocationAccountID();
        LocationAccount locationAccount = ModelMapperUtils.map( locationAccountVO, LocationAccount.class );
        if( account.getPhone() != null )
        {
            String regex = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
            Pattern pattern = Pattern.compile( regex );
            String phoneNumber = locationAccountVO.getAccount().getPhone();
            Matcher matcher = pattern.matcher( phoneNumber );
            if( matcher.matches() )
            {
                locationAccountVO.getAccount().setPhone( phoneNumber );
            }
            else
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PHONE_VALID ) ) );
            }
        }
        log.debug( "Inserting account = {}", account );
        account = accountRepository.save( account );
        paymentMethod.setAccountId( account );
        paymentMethod.setLocationId( locationAccountVO.getLocationId() );
        log.debug( "Inserting paymentMethod = {}", paymentMethod );
        if( locationAccountVO.getAccount().getPaymentMethod().getTokenId() == null )
        {
            //Code change to add Alias in Payment Table
            paymentMethod.setAlias( locationAccountVO.getAccount().getPaymentMethod().getAlias() );
            paymentMethod.setDisplay( CommonUtil.getLastnCharacters( locationAccountVO.getAccount().getPaymentMethod().getAccountNumber(), 4 ) );
        }
        else
        {
            paymentMethod.setTokenId( locationAccountVO.getAccount().getPaymentMethod().getTokenId() );
        }
        paymentMethod = paymentMethodRepository.save( paymentMethod );
        locationAccountID.setAccount( account.getAccountId() );
        locationAccountID.setLocation( locationId );
        locationAccount.setLocaccId( locationAccountID );
        MerchantResponseVO merchantResponseVO = merchantService.createMerchant( headers, ( Objects.isNull( locationAccountVO.getLocationNumber() ) ) ?
                                                                                         new SimpleDateFormat( "yyyyMMddHHmmssSSS" ).format( new Date() ) :
                                                                                         locationAccountVO.getLocationNumber() ); //REL1-5206
        locationAccount.setMerchantId( merchantResponseVO.getId() ); //REL1-5206
        locationAccount = locationAccountRepository.save( locationAccount );
        log.debug( "location account saved {}", locationAccount );
        AccountVO accountVO = ModelMapperUtils.map( account, AccountVO.class );
        locationResponseVO = ModelMapperUtils.map( account, LocationAccountResponseVO.class );
        PaymentMethodVO paymentMethodVO = ModelMapperUtils.map( paymentMethod, PaymentMethodVO.class );
        //Changed to set the same phone number
        accountVO.setPhone( locationAccountVO.getAccount().getPhone() );
        locationResponseVO.setAccount( accountVO );
        locationResponseVO.getAccount().setPaymentMethod( paymentMethodVO );
        locationResponseVO.setLocationId( locationAccountVO.getLocationId() );
        locationResponseVO.setClientId( locationAccountVO.getClientId() );
        locationResponseVO.setMerchantId( merchantResponseVO.getId() );
        balanceService.createBalance( locationResponseVO.getAccount().getAccountId(), BigDecimal.ZERO );
        balanceService.createPaymentMethodBalance( locationResponseVO.getAccount().getAccountId(), paymentMethod.getId(), BigDecimal.ZERO );
        AvalaraAccountRequest avalaraAccountRequest = locationAccountVO.getAvalaraAccount();
        if( Objects.nonNull( avalaraAccountRequest ) )
        {
            validateAvalaraMandatoryFields( avalaraAccountRequest );
            AvalaraAccountVO avalaraAccountVO = ModelMapperUtils.map( avalaraAccountRequest, AvalaraAccountVO.class );
            avalaraAccountVO.setEmail( locationAccountVO.getAccount().getEmail() );
            avalaraAccountVO.setFirstName( locationAccountVO.getAccount().getName() );
            avalaraAccountVO.setLocationId( locationAccountVO.getLocationId() );
            avalaraAccountVO.setOffer( avaOffer );
            avalaraAccountVO.setCompanyCode( locationAccountVO.getAvalaraAccount().getAvaCompanyCode() );
            OnboardingAccountResponse onboardingAccountResponse = null;
            if( isAccountTest )
            {
                onboardingAccountResponse = CommonUtil.buildTestResponseObject( avalaraAccountVO );
            }
            else
            {
                onboardingAccountResponse = avaOnboardingService.onboardAccount( avalaraAccountVO );
                if( Objects.isNull( onboardingAccountResponse ) )
                {
                    throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVALARA_ACCOUNT_FAIL ) ) );
                }
            }
            if( Objects.nonNull( onboardingAccountResponse ) )
            {
                locationResponseVO.setOnboardingAccountResponse( onboardingAccountResponse );
            }
        }
        eventScheduler.scheduleSettlementEvent(
            Schedule.<StatementEventDetails>builder().start( account.getBillingDate().atTime( CommonUtil.convertTimeStringToLocalTime( clientSchedulerTime ) ) )
                                                     .repeating( false )
                                                     .properties( StatementEventDetails.builder()
                                                                                       .paymentMethodId( paymentMethodVO.getId() )
                                                                                       .netBalanceDue( BigDecimal.ZERO )
                                                                                       .build() )
                                                     .build() );
        return locationResponseVO;
    }

    private void validateMandatoryFieldsForAccount( LocationAccountRequest locationAccountVO )
    {
        if( null == locationAccountVO.getAccount().getBillingDate() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BILLING_DATE_BLANK ) ) );
        }
        if( locationAccountVO.getAccount().getBillingDate().isBefore( LocalDate.now() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BILLINGDATE_PASTDATECHECK ) ) );
        }
        if( Objects.isNull( locationAccountVO.getAccount().getSevaluation() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AccountService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_S_EVALUATION ) ) );
        }
        if( locationAccountVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.DAILY.toString() ) ||
            locationAccountVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.WEEKLY.toString() ) ||
            locationAccountVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.MONTHLY.toString() ) ||
            locationAccountVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.ANNUALLY.toString() ) ||
            locationAccountVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.QUARTERLY.toString() ) ||
            locationAccountVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.SEMIANNUALLY.toString() ) ||
            locationAccountVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.EVERY_OTHER_MONTH.toString() ) ||
            locationAccountVO.getAccount().getSevaluation().toUpperCase().trim().equals( Frequency.EVERY_OTHER_WEEK.toString() ) )
        {
            log.debug( "locationAccountVO.getAccount( )" + locationAccountVO.getAccount() );
        }
        else
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AccountService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CYCLE_CHECK ) ) );
        }
        /* Added for validation of paymentMethodTypes in creating Location*/

        log.debug( "Location account {}", locationAccountVO );
        if( locationAccountVO.getAccount() != null && null == locationAccountVO.getAccount().getPaymentMethod() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_NOT_FOUND_1 ) ) );
        }
        if( null == locationAccountVO.getLocationId() || locationAccountVO.getLocationId().toString().trim().length() <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATIONID_BLANK_1 ) ) );
        }
        if( Objects.nonNull( locationAccountVO.getClientId() ) )
        {
            LocationAccount locationAccount = locationAccountRepository.findByClientId( locationAccountVO.getClientId() );
            if( locationAccount != null )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AccountService.class,
                    "Account already exist for Client Id: " + locationAccountVO.getClientId() ) );
            }
        }
        if( null == locationAccountVO.getAccount() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_PAYLODE ) ) );
        }
        else
        {
            if( null == locationAccountVO.getAccount().getName() && null == locationAccountVO.getAccount().getEmail() && null == locationAccountVO.getAccount().getPhone() )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_BLANK ) ) );
            }
        }
        if( null == locationAccountVO.getAccount().getBillingDate() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BILLING_DATE_BLANK ) ) );
        }
        if( null == locationAccountVO.getAccount().getName() || locationAccountVO.getAccount().getName().trim().length() <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NAME_BLANK ) ) );
        }
        if( StringUtils.isBlank( locationAccountVO.getAccount().getSevaluation() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_S_EVALUATION ) ) );
        }
        if( StringUtils.isBlank( locationAccountVO.getAccount().getEmail() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EMAIL_BLANK ) ) );
        }
        else
        {
            if( locationAccountVO.getAccount().getEmail().length() > 254 || locationAccountVO.getAccount().getEmail().length() <= 0 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EMAIL_INVALID_LENGTH ) ) );
            }
        }
        if( locationAccountVO.getAccount().getSevaluation().length() > 100 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_SEVALUATION_SIZE ) ) );
        }
        //P3-1512
        if( Strings.isNotEmpty( locationAccountVO.getAccount().getName() ) )
        {   //P3-1922
            if( locationAccountVO.getAccount().getName().length() > 100 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NAME_SIZE ) ) );
            }
            if( !( locationAccountVO.getAccount().getName().trim().length() <= 100 && locationAccountVO.getAccount().getName().trim().length() >= 1 ) )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NAME_SIZE ) ) );
            }
        }
        if( locationAccountVO.getAccount().getEmail() != null )
        {
            boolean result = CommonUtil.isValidEmailAddress( locationAccountVO.getAccount().getEmail() );
            if( !result )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EMAIL_INVALID ) ) );
            }
        }
        if( null == locationAccountVO.getAccount() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_BLANK_1 ) ) );
        }
    }

    private void validatePaymentMethodFields( LocationAccountRequest locationAccountVO )
    {
        if( null == locationAccountVO.getAccount().getPaymentMethod() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_METHOD_BLANK ) ) );
        }
        locationAccountVO.getAccount().getPaymentMethod().setActive( true );
        /* Added for validation of paymentMethodTypes in creating Location*/

        log.debug( "Location account {}", locationAccountVO );
        if( null == locationAccountVO.getAccount().getPaymentMethod().getType() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_TYPE_BLANK ) ) );
        }
        if( locationAccountVO.getAccount().getPaymentMethod().getType() == Type.BANK_ACCOUNT )
        { //required if bank account
            if( locationAccountVO.getAccount().getPaymentMethod().getBankAccountType() == null )
            {
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class, "Bank account type must not be blank or null." ) );
            }
            String routingNumber;
            if( null == locationAccountVO.getAccount().getPaymentMethod().getRoutingNumber() ||
                locationAccountVO.getAccount().getPaymentMethod().getRoutingNumber().trim().length() <= 0 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccountVO.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER_BLANK ) ) );
            }
            else
            {
                routingNumber = locationAccountVO.getAccount().getPaymentMethod().getRoutingNumber().trim();
                if( CommonUtil.containsWhitespace( routingNumber ) )
                {
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SPACE_NOT_ALLOWED_BETWEEN_ROUTINGNUMBER ) ) );
                }
                if( routingNumber.length() < 9 || routingNumber.length() > 9 )
                {
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER_BETWEEN_1_10 ) ) );
                }
                try
                {
                    Integer.parseInt( routingNumber );
                    if( ABARoutingNumber.isValidRoutingNumber( routingNumber ) )
                    {
                        locationAccountVO.getAccount().getPaymentMethod().setRoutingNumber( routingNumber );
                    }
                    else
                    {
                        String inputBankCode = routingNumber.substring( 1, 4 );
                        CanadaBankCode bankCode = CanadaBankCode.forBankCode( inputBankCode );
                        if( null != bankCode )
                        {
                            locationAccountVO.getAccount().getPaymentMethod().setRoutingNumber( routingNumber );
                        }
                        else
                        {
                            throw new ErrorResponse(
                                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), CommonUtil.class, "Please enter a valid Routing number" ) );
                        }
                    }
                }
                catch( NumberFormatException exception )
                {
                    log.debug( "Unable to validate payment method fields", exception );
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccountVO.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER ) ) );
                }
            }
            if( null == locationAccountVO.getAccount().getPaymentMethod().getAccountNumber() ||
                locationAccountVO.getAccount().getPaymentMethod().getAccountNumber().trim().length() <= 0 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccountVO.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_BLANK_1 ) ) );
            }
            else
            {
                String accountNumber = locationAccountVO.getAccount().getPaymentMethod().getAccountNumber().trim();
                if( CommonUtil.containsWhitespace( accountNumber ) )
                {
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SPACE_NOT_ALLOWED_BETWEEN_ACCOUNTNUMBER ) ) );
                }
                if( ABARoutingNumber.isValidRoutingNumber( routingNumber ) )
                {
                    locationAccountVO.getAccount().getPaymentMethod().setAccountNumber( accountNumber );
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
                        locationAccountVO.getAccount().getPaymentMethod().setAccountNumber( canadaAccountNo );
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
            if( null != locationAccountVO.getAccount().getPaymentMethod().getProcessor() || null != locationAccountVO.getAccount().getPaymentMethod().getBrand() ||
                null != locationAccountVO.getAccount().getPaymentMethod().getToken() )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CARD_DETAILS ) ) );
            }
        }
        else if( locationAccountVO.getAccount().getPaymentMethod().getType() == Type.CREDIT_CARD )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CARD_DETAILS_NOT_ALLOW ) ) );
        }
        else if( locationAccountVO.getAccount().getPaymentMethod().getType() == Type.CASH )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CASH_DETAILS_NOT_ALLOW ) ) );
        }
    }

    private void validateAvalaraMandatoryFields( AvalaraAccountRequest avalaraAccountRequest )
    {
        Set<DataIntegrityViolationResponse> dataIntegrityViolationResponses = new HashSet<>();
        CompanyAddress companyAddress = avalaraAccountRequest.getCompanyAddress();
        if( Objects.isNull( companyAddress ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvalaraAccountRequest.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_COMPANY_ADDRESS_INVALID ) ) );
        }
        String accountName = avalaraAccountRequest.getAccountName();
        if( org.springframework.util.StringUtils.isEmpty( accountName ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvalaraAccountRequest.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVA_NAME_INVALID ) ) );
        }
        else if( accountName.length() > 50 || accountName.length() <= 0 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvalaraAccountRequest.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVA_NAME_INVALID_LENGTH ) ) );
        }
        String lastName = avalaraAccountRequest.getLastName();
        if( org.springframework.util.StringUtils.isEmpty( lastName ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvalaraAccountRequest.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVA_LNAME_INVALID ) ) );
        }
        else if( lastName.length() > 50 || lastName.length() <= 0 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvalaraAccountRequest.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVA_LNAME_INVALID_LENGTH ) ) );
        }
        String comapanyCode = avalaraAccountRequest.getAvaCompanyCode();
        if( org.springframework.util.StringUtils.isEmpty( comapanyCode ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvalaraAccountRequest.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVA_COMPCODE_INVALID ) ) );
        }
        else if( comapanyCode.length() > 50 || comapanyCode.length() <= 0 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvalaraAccountRequest.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVA_COMPCODE_INVALID_LENGTH ) ) );
        }
        if( Objects.isNull( avalaraAccountRequest.getOrganizationId() ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvalaraAccountRequest.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ORGID_INVALID ) ) );
        }
        if( Objects.isNull( companyAddress ) )
        {
            Address address = prepareAddress( companyAddress );
            Set<DataIntegrityViolationResponse> addressValidationErrorMSGs = AddressServiceHelper.getAllDataIntegrityViolationResponse( address );
            dataIntegrityViolationResponses.addAll( addressValidationErrorMSGs );
        }

        if( !dataIntegrityViolationResponses.isEmpty() )
        {
            throw new ErrorResponse( dataIntegrityViolationResponses.toArray( new DataIntegrityViolationResponse[dataIntegrityViolationResponses.size()] ) );
        }

    }

    private static final Address prepareAddress( CompanyAddress companyAddress )
    {
        Address address = new Address();
        address.setLine1( companyAddress.getLine() );
        address.setLine( companyAddress.getLine() );
        address.setRegion( companyAddress.getRegion() );
        address.setPostalCode( companyAddress.getPostalCode() );
        address.setCountry( companyAddress.getCountry() );
        address.setCity( companyAddress.getCity() );
        return address;
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public List<AccountVO> getAllAccounts( Pageable pageable )
    {
        log.debug( "get account by page = {}", pageable );
        Page<Account> accounts = accountRepository.findAll( pageable );
        return ModelMapperUtils.mapAll( accounts.getContent(), AccountVO.class );
    }

    @Transactional( readOnly = true )
    public LocationAccountVO getClientByLocation( UUID locationId )
    {
        PaymentMethodVO paymentMethodVO = null;
        LocationAccountVO locationAccountVO = null;
        log.debug( "trying to fetch Client account details By Location = {}", locationId );
        Optional<LocationAccount> locationAccountOptional = locationAccountRepository.getDetailsByLocationId( locationId );
        if( !locationAccountOptional.isPresent() )
        {
            throw new ErrorResponse( new NotFoundResponseError( LocationAccount.class, locationId ) );
        }
        Optional<Account> accountOptional = accountRepository.getDetailsByAccountId( locationAccountOptional.get().getAccountId().getAccountId() );
        if( accountOptional.isPresent() )
        {
            Account account = accountOptional.get();
            log.debug( "trying to fetch payment Methods By  account = {}", account );
            PaymentMethod paymentMethod = paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( account.getAccountId(), Boolean.TRUE );
            if( !Objects.isNull( paymentMethod ) )
            {
                paymentMethodVO = ModelMapperUtils.map( paymentMethod, PaymentMethodVO.class );
            }
            AccountVO accountVO = ModelMapperUtils.map( account, AccountVO.class );
            locationAccountVO = ModelMapperUtils.map( locationAccountOptional, LocationAccountVO.class );
            locationAccountVO.setLocationId( account.getLocation() );
            locationAccountVO.setClientId( locationAccountOptional.get().getClientId() );
            locationAccountVO.setMerchantId( UUID.randomUUID() );
            accountVO.setPaymentMethod( paymentMethodVO );
            locationAccountVO.setAccount( accountVO );
        }
        return locationAccountVO;
    }

    public AccountStatementVO getStatementByStatementId( UUID statementId )
    {
        log.trace( "Inside the AccountStatementVO getStatementByStatementId( UUID statementId ) !!!." );
        AccountStatementVO accountStatementVO = new AccountStatementVO();
        Optional<Statement> statementOptional = accountStatementRepository.findById( statementId );
        if( statementOptional.isPresent() )
        {
            Statement statement = accountStatementRepository.getStatementDataByStatementId( statementId );
            Account account = statement.getAccountId();
            accountStatementVO.setStatementId( statement.getStatementId() );
            accountStatementVO.setLocationId( statement.getLocationId() );
            accountStatementVO.setAccountId( account.getAccountId() );
            accountStatementVO.setStatementAmount( statement.getTotalAmount() );
            accountStatementVO.setStatementDate( statement.getStmtDate() );
            accountStatementVO.setStatementCreated( statement.getCreated() );
            accountStatementVO.setStatementModified( statement.getModified() );
            accountStatementVO.setStatementDeactivated( statement.getDeactivated() );
        }
        else
        {
            log.debug( "Statement ID is not exist {}", statementId );
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Statement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_STATEMENTID_NOT_EXIST ) + statementId ) );
        }
        return accountStatementVO;
    }

    @Transactional( readOnly = true )
    public List<AccountStatementVO> getStatementByAccountId( UUID accountId, Optional<String> fromDate, Optional<String> toDate, Pageable pageable )
    {
        log.trace( "Inside the List<AccountStatementVO> getStatementByAccountId( UUID accountId, Optional<String> fromDate, Optional<String> toDate, Pageable pageable ) !!!." );
        validateFromDateToDateFieldsForAccountStatement( fromDate, toDate );
        List<AccountStatementVO> accountStatementVO2 = new ArrayList<>();
        LocalDate fromDateForStatement = null;
        LocalDate toDateForStatement = null;
        if( fromDate.isPresent() && toDate.isPresent() )
        {
            fromDateForStatement = CommonUtil.convertToDateTime( fromDate.get() );
            toDateForStatement = CommonUtil.convertToDateTime( toDate.get() );
        }
        Optional<MemberCreation> memberCreation = memberCreationRepository.getDetailsByAccountId( accountId );
        if( !memberCreation.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Account.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + accountId ) );
        }
        Optional<Account> account = accountRepository.findById( accountId );
        if( account.isPresent() && fromDate.isPresent() && toDate.isPresent() )
        {
            List<Statement> statementsList = accountStatementRepository
                .findByAccountIdAccountIdAndStmtDateBetween( accountId, fromDateForStatement.atStartOfDay(), toDateForStatement.atTime( LocalTime.MAX ), pageable );
            for( Statement statement : statementsList )
            {
                AccountStatementVO accountStatementVO = new AccountStatementVO();
                accountStatementVO.setStatementId( statement.getStatementId() );
                accountStatementVO.setLocationId( statement.getLocationId() );
                accountStatementVO.setAccountId( statement.getAccountId().getAccountId() );
                accountStatementVO.setStatementAmount( statement.getTotalAmount() );
                accountStatementVO.setStatementDate( statement.getStmtDate() );
                accountStatementVO.setStatementCreated( statement.getCreated() );
                accountStatementVO.setStatementModified( statement.getModified() );
                accountStatementVO.setStatementDeactivated( statement.getDeactivated() );
                accountStatementVO2.add( accountStatementVO );
            }
        }
        else if( account.isPresent() && !fromDate.isPresent() && !toDate.isPresent() )
        {
            List<Statement> statementsList = accountStatementRepository.findByAccountId_AccountId( accountId, pageable );
            for( Statement statement : statementsList )
            {
                AccountStatementVO accountStatementVO = new AccountStatementVO();
                accountStatementVO.setStatementId( statement.getStatementId() );
                accountStatementVO.setLocationId( statement.getLocationId() );
                accountStatementVO.setAccountId( statement.getAccountId().getAccountId() );
                accountStatementVO.setStatementAmount( statement.getTotalAmount() );
                accountStatementVO.setStatementDate( statement.getStmtDate() );
                accountStatementVO.setStatementCreated( statement.getCreated() );
                accountStatementVO.setStatementModified( statement.getModified() );
                accountStatementVO.setStatementDeactivated( statement.getDeactivated() );
                accountStatementVO2.add( accountStatementVO );
            }
        }
        else
        {
            log.debug( "Account ID is not exist {}", accountId );
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Statement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NOT_EXIST ) + accountId ) );
        }
        if( accountStatementVO2.isEmpty() )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RECORD_NOT_FOUND ) ) );
        }
        else
        {
            return accountStatementVO2;
        }
    }

    private void validateFromDateToDateFieldsForAccountStatement( Optional<String> fromDate, Optional<String> toDate )
    {
        if( fromDate.isPresent() && !toDate.isPresent() )
        {
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    "FromDate is Mandatory with toDate. Expected format is  MM-DD-YYYY" ) );
        }
        if( !fromDate.isPresent() && toDate.isPresent() )
        {
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    "ToDate is Mandatory with fromDate . Expected format is  MM-DD-YYYY" ) );
        }
    }
}
