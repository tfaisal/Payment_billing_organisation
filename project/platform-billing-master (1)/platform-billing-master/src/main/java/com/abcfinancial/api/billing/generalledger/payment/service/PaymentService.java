package com.abcfinancial.api.billing.generalledger.payment.service;

import com.abcfinancial.api.billing.generalledger.adjustment.domain.Adjustment;
import com.abcfinancial.api.billing.generalledger.adjustment.enums.AdjustmentType;
import com.abcfinancial.api.billing.generalledger.adjustment.repository.AdjustmentRepository;
import com.abcfinancial.api.billing.generalledger.adjustment.service.AdjustmentService;
import com.abcfinancial.api.billing.generalledger.adjustment.valueobject.AdjustmentRequestVO;
import com.abcfinancial.api.billing.generalledger.adjustment.valueobject.AdjustmentResponseVO;
import com.abcfinancial.api.billing.generalledger.common.validations.CommonValidation;
import com.abcfinancial.api.billing.generalledger.enums.PaymentType;
import com.abcfinancial.api.billing.generalledger.enums.Status;
import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.generalledger.fee.service.FeeService;
import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.abcfinancial.api.billing.generalledger.invoice.domain.InvoiceItem;
import com.abcfinancial.api.billing.generalledger.invoice.repository.InvoiceItemRepository;
import com.abcfinancial.api.billing.generalledger.invoice.repository.InvoiceRepository;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.InvoiceItemVO;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.InvoiceVO;
import com.abcfinancial.api.billing.generalledger.kafka.consumer.PaymentStatusListener;
import com.abcfinancial.api.billing.generalledger.kafka.producer.PaymentProcessEvent;
import com.abcfinancial.api.billing.generalledger.kafka.producer.PaymentQueuedGenerator;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.domain.Type;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.payment.repository.ProcessorRepository;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.*;
import com.abcfinancial.api.billing.generalledger.settlement.domain.Settlement;
import com.abcfinancial.api.billing.generalledger.settlement.repository.SettlementRepository;
import com.abcfinancial.api.billing.generalledger.statements.domain.Balance;
import com.abcfinancial.api.billing.generalledger.statements.domain.PaymentMethodAccount;
import com.abcfinancial.api.billing.generalledger.statements.domain.Statement;
import com.abcfinancial.api.billing.generalledger.statements.domain.Summary;
import com.abcfinancial.api.billing.generalledger.statements.produce.StatementProduce;
import com.abcfinancial.api.billing.generalledger.statements.repository.AccountSummaryRepository;
import com.abcfinancial.api.billing.generalledger.statements.repository.BalanceRepository;
import com.abcfinancial.api.billing.generalledger.statements.repository.PaymentMethodAccountRepository;
import com.abcfinancial.api.billing.generalledger.statements.repository.StatementRepository;
import com.abcfinancial.api.billing.generalledger.statements.service.BalanceService;
import com.abcfinancial.api.billing.generalledger.statements.service.StatementService;
import com.abcfinancial.api.billing.generalledger.transaction.valueobject.ClientAccountTransactionResponseVO;
import com.abcfinancial.api.billing.scheduler.EventScheduler;
import com.abcfinancial.api.billing.scheduler.schedules.Schedule;
import com.abcfinancial.api.billing.scheduler.schedules.StatementEventDetails;
import com.abcfinancial.api.billing.scheduler.service.JobDetailsService;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.AccountingRefund;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.AccountingRevenue;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.AccountingTransaction;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.LocationAccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.MemberCreation;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.repository.MemberCreationRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRefundRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountingRevenueRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.AccountTransactionService;
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.AccountingRevenueService;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.*;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.service.PricingService;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject.PricingDetailsVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Payment;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.PaymentHistory;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.*;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.PaymentHistoryRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.PaymentRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.restutil.HttpBillingService;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.SubscriptionService;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.DimeboxCardTransactionResponseVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentQueuedVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentVO;
import com.abcfinancial.api.billing.utility.common.*;
import com.abcfinancial.api.billing.utility.exception.CustomErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.common.domain.NotFoundResponseError;
import com.abcfinancial.api.common.domain.ValidationError;
import com.google.i18n.phonenumbers.NumberParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.abcfinancial.api.billing.generalledger.statements.domain.Type.Cr;
import static com.abcfinancial.api.billing.generalledger.statements.domain.Type.Dr;
import static com.abcfinancial.api.billing.utility.common.AppConstants.*;
import static com.abcfinancial.api.billing.utility.common.MessageUtils.*;

@Service
@Slf4j
public class PaymentService
{
    @Autowired
    PaymentProcessEvent paymentProcessEvent;
    @Autowired
    SubscriptionService subscriptionService;
    @Autowired
    SubscriptionRepository subscriptionRepository;
    @Autowired
    PaymentQueuedGenerator paymentQueuedGenerator;
    @Autowired
    InvoiceItemRepository invoiceItemRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private LocationAccountRepository locationAccountRepository;
    @Autowired
    private MemberCreationRepository memberCreationRepository;
    @Autowired
    private HttpBillingService httpService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;
    @Autowired
    private ProcessorRepository processorRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Value( "${dimebox.uri.createtransaction}" )
    private String getTransactionURI;
    @Autowired
    private EventScheduler eventScheduler;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private AccountTransactionService accountTransactionService;
    @Autowired
    private AccountingRevenueService accountingRevenueService;
    @Autowired
    private AccountingRevenueRepository accountingRevenueRepository;
    @Autowired
    private AccountRefundRepository accountRefundRepository;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private AccountSummaryRepository accountSummaryRepository;
    @Autowired
    private PaymentMethodAccountRepository paymentMethodAccountRepository;
    @Autowired
    private StatementRepository statementRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private StatementService statementService;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private LocationAccountRepository clientAccountRepository;
    @Value( "${paymentGateway.uri.paymentRequest}" )
    private String paymentRequestURL;
    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private AdjustmentService adjustmentService;
    @Autowired
    private AdjustmentRepository adjustmentRepository;
    @Autowired
    private FeeService feeService;

    @Autowired
    private CommonValidation commonValidation;
    @Autowired
    private JobDetailsService jobDetailsService;
    @Value( "${generalLedger.scheduleTime.settlement}" )
    private String clientSchedulerTime;
    @Value( "${generalLedger.scheduleTime.statement}" )
    private String payorSchedulerTime;

    @Transactional( propagation = Propagation.REQUIRED )
    public ResponseEntity<UpdateAccountDetailVO> updateAccountDetails( UpdateAccountDetailVO updateAccountDetailVO, UUID accountId ) throws NumberParseException
    {
        log.debug( "Update Account: " + accountId );
        Optional<Account> accountOptional = accountRepository.getDetailsByAccountId( accountId );
        if( !accountOptional.isPresent() )
        {
            throw new ErrorResponse( new NotFoundResponseError( Account.class, accountId ) );
        }
        UpdateAccountVO updateAccountVO = updateAccountDetailVO.getAccount();
        if( ( !Objects.isNull( updateAccountDetailVO.getAccount().getName() ) ) && updateAccountDetailVO.getAccount().getName().isEmpty() )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NAME_BLANK ) );
        }
        if( ( !Objects.isNull( updateAccountDetailVO.getAccount().getSevaluation() ) ) && StringUtils.isWhitespace( updateAccountDetailVO.getAccount().getSevaluation() ) )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_S_EVALUATION ) );
        }
        if( !Objects.isNull( updateAccountDetailVO.getAccount().getEmail() ) )
        {
            boolean result = CommonUtil.isValidEmailAddress( updateAccountDetailVO.getAccount().getEmail() );
            if( !result )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_EMAIL_INVALID ) );
            }
        }
        if( null != updateAccountVO.getPhone() )
        {
            String regex = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
            Pattern pattern = Pattern.compile( regex );
            String phoneNumber = updateAccountVO.getPhone();
            Matcher matcher = pattern.matcher( phoneNumber );
            if( matcher.matches() )
            {
                updateAccountVO.setPhone( phoneNumber );
            }
            else
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_PHONE_VALID ) );
            }
        }
        Optional<LocationAccount> locationAccount = locationAccountRepository.getDetailsByAccountId( accountId );
        if( locationAccount.isPresent() )
        {
            PaymentMethodVO paymentMethodVO = updateLocationAccount( updateAccountVO, accountId );
            updateAccountDetailVO.getAccount().setPaymentMethod( paymentMethodVO );
        }
        Optional<MemberCreation> memberCreation = memberCreationRepository.getDetailsByAccountId( accountId );
        if( memberCreation.isPresent() )
        {
            PaymentMethodVO paymentMethodVO = updateMemberAccount( updateAccountVO, accountId );
            updateAccountDetailVO.getAccount().setPaymentMethod( paymentMethodVO );
        }
        updateAccountDetailVO.getAccount().setAccountId( accountId );
        updateAccountDetailVO.getAccount().setEmail( accountOptional.get().getEmail() );
        updateAccountDetailVO.getAccount().setName( accountOptional.get().getName() );
        updateAccountDetailVO.getAccount().setPhone( accountOptional.get().getPhone() );
        updateAccountDetailVO.getAccount().setSevaluation( accountOptional.get().getSevaluation() );
        return ResponseEntity.ok().body( updateAccountDetailVO );
    }

    private PaymentMethodVO updateLocationAccount( UpdateAccountVO updateAccountVO, UUID accountId )
    {
        log.debug( "update location account start." );
        validateUpdateLocationAccountDetail( updateAccountVO );
        log.debug( "getting detail of account: " + accountId );
        Optional<Account> accountOptional = accountRepository.getDetailsByAccountId( accountId );
        if( accountOptional.isPresent() )
        {
            Account account = accountOptional.get();
            if( Strings.isNotEmpty( updateAccountVO.getName() ) )
            {
                account.setName( updateAccountVO.getName() );
            }
            if( Strings.isNotEmpty( updateAccountVO.getEmail() ) )
            {
                account.setEmail( updateAccountVO.getEmail() );
            }
            if( Strings.isNotEmpty( updateAccountVO.getPhone() ) )
            {
                account.setPhone( updateAccountVO.getPhone() );
            }
            if( StringUtils.isNotBlank( updateAccountVO.getSevaluation() ) )
            {
                account.setSevaluation( updateAccountVO.getSevaluation() );
            }
            log.debug( UPDATE_CHANGES_IN_ACCOUNT_TABLE );
            accountRepository.save( account );
            log.debug( "Getting detail from payment_method table by accountId: {} and active true", accountId );
            PaymentMethod paymentMethod = paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( account.getAccountId(), Boolean.TRUE );
            if( paymentMethod == null )
            {
                log.error( "Payment method detail not found." );
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_METHOD_BLANK ) ) );
            }
            PaymentMethodVO paymentMethodVO = ModelMapperUtils.map( paymentMethod, PaymentMethodVO.class );
            PaymentMethod paymentMethodClone = ModelMapperUtils.map( paymentMethodVO, PaymentMethod.class );
            setBankFields( updateAccountVO, paymentMethodClone );
            paymentMethodClone.setAccountId( account );
            if( null != updateAccountVO.getPaymentMethod() && !updateAccountVO.getPaymentMethod().isEmpty() )
            {
                paymentMethodRepository.saveAndFlush( paymentMethodClone );
                paymentMethod.setActive( Boolean.FALSE );
                paymentMethod.setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
                paymentMethodRepository.saveAndFlush( paymentMethod );
            }
            return ModelMapperUtils.map( paymentMethodClone, PaymentMethodVO.class );
        }
        return null;
    }

    private PaymentMethodVO updateMemberAccount( UpdateAccountVO updateAccountVO, UUID accountId ) throws NumberParseException
    {
        log.debug( "update member account start." );
        Optional<Account> accountOptional = accountRepository.getDetailsByAccountId( accountId );
        if( accountOptional.isPresent() )
        {
            Account account = setAccountFields( updateAccountVO, accountOptional.get() );
            log.debug( UPDATE_CHANGES_IN_ACCOUNT_TABLE );
            accountRepository.save( account );
            log.debug( "Getting detail from payment_method table by accountId: {} and active true", accountId );
            PaymentMethod paymentMethod = paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( account.getAccountId(), Boolean.TRUE );
            if( paymentMethod == null )
            {
                log.error( "Payment method detail not found." );
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_NOT_FOUND_1 ) ) );
            }
            validateUpdateMemberAccountDetail( updateAccountVO, paymentMethod.getType() );
            PaymentMethodVO paymentMethodVO = ModelMapperUtils.map( paymentMethod, PaymentMethodVO.class );
            PaymentMethod paymentMethodClone = ModelMapperUtils.map( paymentMethodVO, PaymentMethod.class );
            if( updateAccountVO.getPaymentMethod() != null && paymentMethod.getType() == updateAccountVO.getPaymentMethod().getType() )
            {
                if( paymentMethod.getType() == Type.BANK_ACCOUNT )
                {
                    setBankFields( updateAccountVO, paymentMethodClone );
                }
                if( paymentMethod.getType() == Type.CREDIT_CARD )
                {
                    throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), SubscriptionService.class, "credit card not supported." ) );

                }
            }
            else if( updateAccountVO.getPaymentMethod() != null && updateAccountVO.getPaymentMethod().getType() == Type.BANK_ACCOUNT )
            {
                if( updateAccountVO.getPaymentMethod().getBankAccountType() == null )
                {
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class, "Bank account type must not be blank or null." ) );
                }
                clearCardDetail( paymentMethodClone );
                paymentMethodClone.setType( Type.BANK_ACCOUNT );
                paymentMethodClone.setBankAccountType( updateAccountVO.getPaymentMethod().getBankAccountType() );
                setBankFields( updateAccountVO, paymentMethodClone );
            }
            else if( updateAccountVO.getPaymentMethod() != null && updateAccountVO.getPaymentMethod().getType() == Type.CREDIT_CARD )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), SubscriptionService.class, "credit card not supported." ) );
            }
            else if( ( updateAccountVO.getPaymentMethod() != null && updateAccountVO.getPaymentMethod().getType() == Type.CASH ) )
            {
                clearDetail( paymentMethodClone );
                paymentMethodClone.setType( Type.CASH );
            }
            paymentMethodClone.setAccountId( account );
            if( updateAccountVO.getPaymentMethod() != null && !updateAccountVO.getPaymentMethod().isEmpty() )
            {
                log.debug( "Save new payment method detail with updated fields." );
                paymentMethodRepository.saveAndFlush( paymentMethodClone );
                paymentMethod.setActive( Boolean.FALSE );
                paymentMethod.setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
                log.debug( "Update active false and dectivated date in payment method table." );
                paymentMethodRepository.saveAndFlush( paymentMethod );
            }
            return ModelMapperUtils.map( paymentMethodClone, PaymentMethodVO.class );
        }
        return null;
    }

    private void validateUpdateLocationAccountDetail( UpdateAccountVO updateAccountVO )
    {
        PaymentMethodVO paymentMethodVO = updateAccountVO.getPaymentMethod();
        if( paymentMethodVO != null )
        {
            if( paymentMethodVO.getType() == null || paymentMethodVO.getType().name().trim().length() <= 0 )
            {
                log.error( "Type is mandatory." );
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_TYPE_BLANK ) ) );
            }
            if( paymentMethodVO.getType() != null && paymentMethodVO.getType() != Type.BANK_ACCOUNT )
            {
                log.error( "Type of payment method is not BANK_ACCOUNT for update location" );
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CARD_DETAILS_LOCATION_ACCOUNT ) ) );
            }
            if( Strings.isNotEmpty( paymentMethodVO.getToken() ) )
            {
                log.error( "Token is not for update location account." );
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOKEN_NOT_REQUIRED ) ) );
            }
            if( paymentMethodVO.getType() == Type.BANK_ACCOUNT )
            { //required if bank account
                String routingNumber = paymentMethodVO.getRoutingNumber();
                String accountNumber = paymentMethodVO.getAccountNumber();
                //rounting number update
                if( routingNumber != null )
                {
                    routingNumber = routingNumber.trim();
                    if( CommonUtil.containsWhitespace( routingNumber ) )
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SPACE_NOT_ALLOWED_BETWEEN_ROUTINGNUMBER ) ) );
                    }
                    if( !StringUtils.isNumeric( routingNumber ) )
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER_INTEGER ) ) );
                    }
                    if( routingNumber.length() < 9 || routingNumber.length() > 9 )
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER ) ) );
                    }

                    if( ABARoutingNumber.isValidRoutingNumber( routingNumber ) )
                    {
                        paymentMethodVO.setRoutingNumber( routingNumber );
                    }
                    else
                    {
                        String inputBankCode = routingNumber.substring( 1, 4 );
                        CanadaBankCode bankCode = CanadaBankCode.forBankCode( inputBankCode );
                        if( null != bankCode )
                        {
                            paymentMethodVO.setRoutingNumber( routingNumber );
                        }
                        else
                        {
                            throw new ErrorResponse(
                                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), CommonUtil.class, ERROR_MESSAGE_ROUTING_NUMBER_BLANK ) );
                        }
                    }
                }
                //account number update
                if( accountNumber != null )
                {
                    accountNumber = accountNumber.trim();
                    if( CommonUtil.containsWhitespace( accountNumber ) )
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SPACE_NOT_ALLOWED_BETWEEN_ACCOUNTNUMBER ) ) );
                    }
                    if( ABARoutingNumber.isValidRoutingNumber( routingNumber ) )
                    {
                        paymentMethodVO.setAccountNumber( accountNumber );
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
                        String inputBankCode = "";
                        String transitNumber = "";
                        if( Objects.nonNull( routingNumber ) )
                        {
                            inputBankCode = routingNumber.substring( 1, 4 );
                            transitNumber = routingNumber.substring( 4, 9 );
                        }
                        CanadaBankCode bankCode = CanadaBankCode.forBankCode( inputBankCode );
                        if( null != bankCode )
                        {
                            String canadaAccountNo = bankCode.getNumber() + "" + transitNumber + "" + accountNumber;
                            paymentMethodVO.setAccountNumber( canadaAccountNo );
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
            }
        }
    }

    private void setBankFields( UpdateAccountVO updateAccountVO, PaymentMethod paymentMethodClone )
    {
        if( updateAccountVO.getPaymentMethod() != null )
        {
            if( Strings.isNotEmpty( updateAccountVO.getPaymentMethod().getAccountNumber() ) )
            {
                String accountNumber = updateAccountVO.getPaymentMethod().getAccountNumber();
                paymentMethodClone.setAccountNumber( accountNumber );
                paymentMethodClone.setDisplay( accountNumber.substring( accountNumber.length() - 4 ) );
            }
            if( Strings.isNotEmpty( updateAccountVO.getPaymentMethod().getRoutingNumber() ) )
            {
                paymentMethodClone.setRoutingNumber( updateAccountVO.getPaymentMethod().getRoutingNumber() );
            }
            if( Strings.isNotEmpty( updateAccountVO.getPaymentMethod().getAlias() ) )
            {
                paymentMethodClone.setAlias( updateAccountVO.getPaymentMethod().getAlias() );
            }
            if( updateAccountVO.getPaymentMethod().getBankAccountType() != null )
            {
                paymentMethodClone.setBankAccountType( updateAccountVO.getPaymentMethod().getBankAccountType() );
            }
        }
    }

    private Account setAccountFields( UpdateAccountVO updateAccountVO, Account account )
    {
        if( Strings.isNotEmpty( updateAccountVO.getName() ) )
        {
            account.setName( updateAccountVO.getName() );
        }
        if( Strings.isNotEmpty( updateAccountVO.getPhone() ) )
        {
            account.setPhone( updateAccountVO.getPhone() );
        }
        if( Strings.isNotEmpty( updateAccountVO.getEmail() ) )
        {
            account.setEmail( updateAccountVO.getEmail() );
        }
        if( Strings.isNotEmpty( updateAccountVO.getSevaluation() ) )
        {
            account.setSevaluation( updateAccountVO.getSevaluation() );
        }
        return account;
    }

    private void validateUpdateMemberAccountDetail( UpdateAccountVO updateAccountVO, Type existType )
    {
        PaymentMethodVO paymentMethodVO = updateAccountVO.getPaymentMethod();
        if( updateAccountVO == null || updateAccountVO.isValid() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_BLANK ) ) );
        }
        if( paymentMethodVO != null )
        {
            if( paymentMethodVO.getType() == null || paymentMethodVO.getType().name().trim().length() <= 0 )
            {
                log.error( "Type is mandatory." );
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_METHOD_BLANK ) ) );
            }
            if( paymentMethodVO.getType() == Type.BANK_ACCOUNT && existType == Type.CREDIT_CARD )
            {
                validateNotCreditcard( paymentMethodVO );
                commonValidation( paymentMethodVO );
            }
            if( paymentMethodVO.getType() == Type.CASH && ( null != paymentMethodVO.getToken() || null != paymentMethodVO.getRoutingNumber()
                                                            || null != paymentMethodVO.getAccountNumber() || null != paymentMethodVO.getBrand() ||
                                                            null != paymentMethodVO.getAlias() || 0 > paymentMethodVO.getExpiryMonth() ||
                                                            0 > paymentMethodVO.getExpiryYear() ) )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BANK_CREDIT_DETAIL ) ) );
            }
            if( paymentMethodVO.getType() == Type.CREDIT_CARD && existType == Type.BANK_ACCOUNT )
            {
                validateNotBank( paymentMethodVO );
                commonValidationCredit( paymentMethodVO );
            }
            if( paymentMethodVO.getType() == Type.CASH && existType == Type.BANK_ACCOUNT )
            {
                validateNotBank( paymentMethodVO );
            }
            if( paymentMethodVO.getType() == Type.CASH && existType == Type.CREDIT_CARD )
            {
                validateNotCreditcard( paymentMethodVO );
            }
            if( paymentMethodVO.getType() == Type.CREDIT_CARD && existType == Type.CASH )
            {
                validateNotBank( paymentMethodVO );
                commonValidationCredit( paymentMethodVO );
            }
            if( paymentMethodVO.getType() == Type.BANK_ACCOUNT && existType == Type.CASH )
            {
                validateNotCreditcard( paymentMethodVO );
                commonValidation( paymentMethodVO );
            }
            if( paymentMethodVO.getType() == existType )
            { //required if bank account
                if( Type.BANK_ACCOUNT == existType )
                {
                    String routingNumber = paymentMethodVO.getRoutingNumber();
                    String accountNumber = paymentMethodVO.getAccountNumber();
                    validateNotCreditcard( paymentMethodVO );
                    //rounting number update
                    if( routingNumber != null )
                    {
                        routingNumber = routingNumber.trim();
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
                                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER ) ) );
                        }
                        if( routingNumber != null && !StringUtils.isNumeric( routingNumber ) )
                        {
                            throw new ErrorResponse(
                                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER_INTEGER ) ) );
                        }
                        if( ABARoutingNumber.isValidRoutingNumber( routingNumber ) )
                        {
                            paymentMethodVO.setRoutingNumber( routingNumber );
                        }
                        else
                        {
                            String inputBankCode = routingNumber.substring( 1, 4 );
                            CanadaBankCode bankCode = CanadaBankCode.forBankCode( inputBankCode );
                            if( null != bankCode )
                            {
                                paymentMethodVO.setRoutingNumber( routingNumber );
                            }
                            else
                            {
                                throw new ErrorResponse(
                                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), CommonUtil.class, " Please enter a valid Routing number" ) );
                            }
                        }
                    }
                    //account number update
                    if( accountNumber != null )
                    {
                        accountNumber = accountNumber.trim();
                        if( CommonUtil.containsWhitespace( accountNumber ) )
                        {
                            throw new ErrorResponse(
                                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SPACE_NOT_ALLOWED_BETWEEN_ACCOUNTNUMBER ) ) );
                        }
                        if( ABARoutingNumber.isValidRoutingNumber( routingNumber ) )
                        {
                            paymentMethodVO.setAccountNumber( accountNumber );
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
                            String inputBankCode = "";
                            String transitNumber = "";
                            if( Objects.nonNull( routingNumber ) )
                            {
                                inputBankCode = routingNumber.substring( 1, 4 );
                                transitNumber = routingNumber.substring( 4, 9 );
                            }
                            CanadaBankCode bankCode = CanadaBankCode.forBankCode( inputBankCode );
                            if( null != bankCode )
                            {
                                String canadaAccountNo = bankCode.getNumber() + "" + transitNumber + "" + accountNumber;
                                paymentMethodVO.setAccountNumber( canadaAccountNo );
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
                }
                if( Type.CREDIT_CARD == existType )
                {
                    validateNotBank( paymentMethodVO );
                    if( Strings.isEmpty( paymentMethodVO.getToken() ) )
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOKEN_MANDATORY ) ) );
                    }
                }
                if( Type.CASH == existType )
                {
                    validateNotBank( paymentMethodVO );
                    validateNotCreditcard( paymentMethodVO );
                }
            }
        }
    }

    private void clearCardDetail( PaymentMethod paymentMethodClone )
    {
        paymentMethodClone.setToken( null );
        paymentMethodClone.setBrand( null );
        paymentMethodClone.setProcessor( null );
        paymentMethodClone.setDisplay( "" );
        paymentMethodClone.setExpiryMonth( 0 );
        paymentMethodClone.setExpiryYear( 0 );
    }

    private void clearDetail( PaymentMethod paymentMethodClone )
    {
        paymentMethodClone.setToken( null );
        paymentMethodClone.setBrand( null );
        paymentMethodClone.setProcessor( null );
        paymentMethodClone.setDisplay( null );
        paymentMethodClone.setExpiryMonth( 0 );
        paymentMethodClone.setExpiryYear( 0 );
        paymentMethodClone.setBankAccountType( null );
        paymentMethodClone.setAccountNumber( null );
        paymentMethodClone.setBankAccountType( null );
        paymentMethodClone.setRoutingNumber( null );
        paymentMethodClone.setAlias( null );
    }

    private void validateNotCreditcard( PaymentMethodVO paymentMethodVO )
    {
        if( Strings.isNotEmpty( paymentMethodVO.getToken() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOKEN_NOT_REQUIRED ) ) );
        }
        if( paymentMethodVO.getExpiryMonth() > 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EXPIRATION_MONTH ) ) );
        }
        if( paymentMethodVO.getExpiryYear() > 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EXPIRATION_YEAR ) ) );
        }
        if( paymentMethodVO.getBrand() != null )
        {
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class, applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BRAND ) ) );
        }
        if( paymentMethodVO.getProcessor() != null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PROCESSOR ) ) );
        }
        if( paymentMethodVO.getDisplay() != null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DISPLAY ) ) );
        }
    }

    private void commonValidation( PaymentMethodVO paymentMethodVO )
    {
        if( Strings.isEmpty( paymentMethodVO.getAccountNumber() ) )
        {
            log.error( "Account number should not be blank." );
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_BLANK ) ) );
        }
        if( Strings.isEmpty( paymentMethodVO.getRoutingNumber() ) )
        {
            log.error( "Routing number should not be blank." );
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER ) ) );
        }
    }

    private void validateNotBank( PaymentMethodVO paymentMethodVO )
    {
        if( Strings.isNotEmpty( paymentMethodVO.getRoutingNumber() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER_NOT_REQUIRED ) ) );
        }
        if( Strings.isNotEmpty( paymentMethodVO.getAccountNumber() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NUMBER_NOT_REQUIRED ) ) );
        }
        if( Strings.isNotEmpty( paymentMethodVO.getAlias() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NAME_ALIAS ) ) );
        }
    }

    private void commonValidationCredit( PaymentMethodVO paymentMethodVO )
    {
        if( Strings.isEmpty( paymentMethodVO.getToken() ) )
        {
            log.error( "Token should not be blank." );
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOKEN_BLANK ) ) );
        }
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public Payment getPaymentById( UUID paymentId )
    {
        return paymentRepository.findById( paymentId )
                                .orElseThrow( () -> new EntityNotFoundException( applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NAME_ALIAS ) ) );
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public PaymentVO updatePayment( PaymentQueuedVO paymentQueuedVO )
    {
        Payment paymentResult = paymentRepository.findById( paymentQueuedVO.getPaymentId() )
                                                 .orElseThrow( () -> new EntityNotFoundException( applicationConfiguration.getValue( ERROR_MESSAGE_PAYMENT_DETAIL_NOT_EXIST ) ) );
        paymentResult.setPayStatus( PayStatus.PENDING );
        paymentResult = paymentRepository.saveAndFlush( paymentResult );
        log.debug( "Marked payment {} as pending", paymentResult.getId() );
        PaymentVO paymentVOResult = ModelMapperUtils.map( paymentResult, PaymentVO.class );
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setPayment( paymentResult );
        paymentHistory.setPaymhAmount( paymentResult.getPayAmount() );
        paymentHistory.setPaymhSettlementStatus( paymentResult.getPaySettlementStatus() );
        paymentHistory.setPaymhStatus( paymentResult.getPayStatus() );
        paymentHistory.setPaymhCreated( paymentResult.getCreated() );
        paymentHistory = paymentHistoryRepository.saveAndFlush( paymentHistory );
        log.debug( "Created payment history {} for payment {}", paymentHistory.getId(), paymentResult.getId() );
        log.warn( "Would have called dimebox, but Mark Vander Lugt deleted it with gusto" ); //https://media.makeameme.org/created/my-dearlet-me.jpg
        return paymentVOResult;
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public PaymentVO savePayment( PaymentVO paymentVO, StatementProduce statementProduce )
    {
        Payment payment = ModelMapperUtils.map( paymentVO, Payment.class );
        Invoice invoiceOriginal = invoiceRepository.findById( statementProduce.getInvoiceId() ).orElseThrow(
            () -> new EntityNotFoundException( applicationConfiguration.getValue( ERROR_MESSAGE_INVOICE_NOT_EXIST ) ) );
        payment.setInvoiceType( invoiceOriginal.getInvoiceType() );
        payment.setInvoiceNumber( invoiceOriginal.getInvoiceNumber() );
        payment = paymentRepository.saveAndFlush( payment );
        log.debug( "Created payment {} for {} against invoice #{}",
            payment.getId(), NumberFormat.getCurrencyInstance().format( payment.getPayAmount() ), invoiceOriginal.getInvoiceNumber() );
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setPayment( payment );
        paymentHistory.setPaymhStatus( paymentVO.getPayStatus() );
        paymentHistory.setPaymhAmount( paymentVO.getPayAmount() );
        paymentHistoryRepository.saveAndFlush( paymentHistory );

        paymentVO = ModelMapperUtils.map( payment, PaymentVO.class );
        paymentQueuedGenerator.paymentQueuedSend( paymentVO, statementProduce );
        return paymentVO;
    }

    public List<PaymentVO> getPaymentByAccount( UUID accountId, Pageable pageable )
    {
        log.debug( "get all Payment of by account id : {}", accountId );
        List<PaymentVO> paymentVOList = new ArrayList<>();
        Optional<Account> account = accountRepository.findById( accountId );
        if( account.isPresent() )
        {
            List<Payment> paymentList = paymentRepository.findByAccountAccountId( account.get().getAccountId(), pageable );
            paymentList.forEach( payment ->
                {
                    PaymentVO paymentVO = new PaymentVO();
                    paymentVO.setId( payment.getId() );
                    paymentVO.setPayAmount( payment.getPayAmount() );
                    paymentVO.setPaySettlementStatus( payment.getPaySettlementStatus() );
                    paymentVO.setPayStatus( payment.getPayStatus() );
                    paymentVO.setLocationId( payment.getLocationId() );
                    paymentVO.setPameId( payment.getPameId() );
                    paymentVO.setPayReceivedDate( payment.getPayReceivedDate() );
                    List<InvoiceVO> invoiceVOList = new ArrayList<>();
                    payment.getInvoices().forEach( invoice -> {
                            InvoiceVO invoiceVO = new InvoiceVO();
                            invoiceVO.setId( invoice.getId() );
                            invoiceVO.setTotalAmount( invoice.getTotalAmount() );
                            invoiceVO.setSalesEmployeeId( invoice.getSalesEmployeeId() );
                            invoiceVO.setMemberId( invoice.getMemberId() );
                            invoiceVO.setLocationId( invoice.getLocationId() );
                            invoiceVO.setTotalTax( invoice.getTotalTax() );
                            invoiceVO.setTotalNetPrice( invoice.getTotalNetPrice() );
                            List<InvoiceItem> invoiceItemList = invoiceItemRepository.findByInvoiceId( invoice.getId() );
                            List<InvoiceItemVO> invoiceItemVOList = new ArrayList<>();
                            invoiceItemList.forEach( invoiceItem -> {
                                    InvoiceItemVO invoiceItemVO = new InvoiceItemVO();
                                    invoiceItemVO.setId( invoiceItem.getId() );
                                    invoiceItemVO.setItemName( invoiceItem.getItemName() );
                                    invoiceItemVO.setItemId( invoiceItem.getItemId() );
                                    invoiceItemVO.setVersion( invoiceItem.getVersion() );
                                    invoiceItemVO.setQuantity( invoiceItem.getQuantity() );
                                    invoiceItemVO.setItemId( invoiceItem.getItemId() );
                                    invoiceItemVO.setAmountRemaining( invoiceItem.getAmountRemaining() );
                                    invoiceItemVO.setDiscountAmount( invoiceItem.getDiscountAmount() );
                                    invoiceItemVO.setPrice( invoiceItem.getPrice() );
                                    invoiceItemVO.setType( invoiceItem.getType() );
                                    invoiceItemVOList.add( invoiceItemVO );
                                    invoiceVO.setItems( invoiceItemVOList );
                                }
                            );
                            invoiceVOList.add( invoiceVO );
                            paymentVO.setInvoices( invoiceVOList );
                        }
                    );
                    paymentVOList.add( paymentVO );
                }
            );
        }
        else
        {
            log.debug( "Account ID is not exist {}", accountId );
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Payment.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NOT_EXIST ) + accountId ) );
        }
        log.debug( "list of payment {} ", paymentVOList.toString() );
        return paymentVOList;
    }

    /**
     * @param paymentId
     * @param refundPaymentVO
     * @return
     * @deprecated No in use
     */

    @Deprecated
    @Transactional
    public RefundPaymentVO refundPayment( UUID paymentId, RefundPaymentVO refundPaymentVO ) //todo MarkV kill dimebox
    {
        log.trace( "Refund Payment start." );
        log.debug( "Getting Payment detail of payment id: " + paymentId );
        Payment payment = paymentRepository.findById( paymentId ).orElseThrow( () -> new ErrorResponse(
            new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentService.class, applicationConfiguration.getValue( ERROR_MESSAGE_PAYMENT_DETAIL_NOT_EXIST ) ) ) );
        if( refundPaymentVO.getAmount() != null && refundPaymentVO.getAmount().compareTo( payment.getPayAmount() ) >= 1 &&
            payment.getPaySettlementStatus() == PaySettlementStatus.SETTLEMENT_COMPLETED )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( ERROR_MESSAGE_REFUND_PAYMENT_AMOUNT ) ) );
        }
        if( payment.getInvoices().isEmpty() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( ERROR_MESSAGE_INVOICE_NOT_EXIST ) ) );
        }

        if( refundPaymentVO.getAmount() == null )
        {
            refundPaymentVO.setAmount( payment.getPayAmount() );
        }
        String getCardApiURL = getTransactionURI + "/" + payment.getPayProcessorId();
        DimeboxCardTransactionResponseVO dimeboxGetTransactionResponse = httpService.callGetApi( getCardApiURL, DimeboxCardTransactionResponseVO.class ); //todo MarkV kill dimebox
        log.trace( "Setting Dimbox detail request." );
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put( "TransID", payment.getPayProcessorId() );
        if( dimeboxGetTransactionResponse.getStatus() == StatusTypes.SETTLEMENT_CANCELLED )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                "Transaction is already cancelled for transaction id: " + payment.getPayProcessorId() ) );
        }
        if( dimeboxGetTransactionResponse.getStatus() != StatusTypes.SETTLEMENT_REQUESTED && dimeboxGetTransactionResponse.getStatus() != StatusTypes.SETTLEMENT_COMPLETED )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                "Status for transaction is: " + dimeboxGetTransactionResponse.getStatus() ) );
        }
        BigDecimal refAmount = refundPaymentVO.getAmount();
        log.debug( "Invoices: {}", payment.getInvoices() );
        Invoice invoice = payment.getInvoices().get( 0 );
        BigDecimal refundAmount = BigDecimal.ZERO.subtract( payment.getPayAmount() );
        Payment refundPayment = new Payment();
        refundPayment.setLocationId( payment.getLocationId() );
        refundPayment.setPayReceivedDate( payment.getPayReceivedDate() );
        refundPayment.setPayStatus( payment.getPayStatus() );
        refundPayment.setPaySettlementStatus( payment.getPaySettlementStatus() );
        refundPayment.setPameId( payment.getPameId() );
        refundPayment.setPayProcessorId( payment.getPayProcessorId() );
        refundPayment.setAccount( payment.getAccount() );
        refundPayment.setInvoiceType( InvoiceTypeEnum.O );
        List<Invoice> paymentList = new ArrayList<>();
        BeanUtils.copyProperties( paymentList, payment.getInvoices() );
        refundPayment.setInvoices( paymentList );
        refundPayment.setPaymentIdRefund( payment.getId() );
        refundPayment.setPayAmount( refundAmount );
        log.debug( "Finding Account transaction for invoice id: " + invoice.getId() );
        List<AccountingTransaction> accountingTransactions = accountTransactionService.findAccountingTransactionByInvoiceId( invoice.getId() );
        log.debug( "Finding Account revenue for invoice id: " + invoice.getId() );
        List<AccountingRevenue> accountingRevenues = accountingRevenueService.findByInvoiceId( invoice.getId() );

        for( int i = 0; i < invoice.getItems().size(); i++ )
        {
            AccountingRefund accountingRefund = new AccountingRefund();
            InvoiceItem invoiceItem = invoice.getItems().get( i );
            AccountingTransaction accountingTransaction = accountingTransactions.get( i );
            AccountingRevenue accountingRevenue1 = accountingRevenues.get( i );
            AccountingRevenue accountingRevenue = accountingRevenue1.clone();
            BigDecimal revenueNetPrice = BigDecimal.ZERO;
            if( dimeboxGetTransactionResponse.getStatus() == StatusTypes.SETTLEMENT_REQUESTED )
            {
                revenueNetPrice = BigDecimal.ZERO.subtract( invoiceItem.getPrice() );
            }
            PricingDetailsVO pricingDtlItem = pricingService.calculatePricingForOneItem( refundPaymentVO.getAmount(), invoiceItem.getLocId() );
            PricingDetailsVO pricingDtlRef = pricingService.calculatePricingForOneItem( invoiceItem.getPrice(), invoiceItem.getLocId() );
            BigDecimal refNetPrice = ( dimeboxGetTransactionResponse.getStatus() == StatusTypes.SETTLEMENT_REQUESTED ) ? pricingDtlRef.getTotalNetAmount() : BigDecimal.ZERO;
            BigDecimal refTax = ( dimeboxGetTransactionResponse.getStatus() == StatusTypes.SETTLEMENT_REQUESTED ) ? pricingDtlRef.getTotalTax() : BigDecimal.ZERO;
            BigDecimal totalInvoiceItemPrice = invoiceItem.getPrice().add( invoiceItem.getTaxAmount() );
            if( totalInvoiceItemPrice.compareTo( BigDecimal.ZERO ) > 0 && refundPaymentVO.getAmount().compareTo( BigDecimal.ZERO ) > 0 &&
                dimeboxGetTransactionResponse.getStatus() == StatusTypes.SETTLEMENT_COMPLETED && ( totalInvoiceItemPrice.compareTo( refundPaymentVO.getAmount() ) > 0 ) )
            {
                refNetPrice = pricingDtlItem.getTotalNetAmount();
                refTax = pricingDtlItem.getTotalTax();
                revenueNetPrice = BigDecimal.ZERO.subtract( refNetPrice );
            }
            accountingRevenue.setId( null );
            accountingRevenue.setNetPrice( revenueNetPrice );
            accountingRevenue = accountingRevenueRepository.save( accountingRevenue );
            log.debug( "**** accounting Revenue saved {} ***", accountingRevenue );
            accountingRefund.setLocationId( invoiceItem.getLocId() );
            accountingRefund.setRefNetPrice( refNetPrice );
            accountingRefund.setRefTax( refTax );
            accountingRefund.setMemberId( invoice.getMemberId() );
            accountingRefund.setInvoiceId( invoice.getId() );
            accountingRefund.setAccountId( invoice.getAccountId() );
            accountingRefund.setAccountingTransactionId( accountingTransaction.getId() );
            accountingRefund.setInvoiceItemId( accountingRevenue.getInviId() );
            accountingRefund = accountRefundRepository.save( accountingRefund );
            log.debug( "**** accounting Refund saved {} ***", accountingRefund );
            refundPaymentVO.setAmount( refundPaymentVO.getAmount().subtract( refNetPrice.add( refTax ) ) );
        }
        refundPaymentVO.setAmount( refAmount.setScale( 2, RoundingMode.HALF_UP ) );
        refundPayment = paymentRepository.save( refundPayment );
        log.debug( "**** Refund Payment details saved {} ***", refundPayment );
        return refundPaymentVO;
    }

    @Transactional
    public ResponseEntity<AccountSummaryResponseVO> updateAccountSummaryDetails( UUID paymentId, AccountSummaryVO accountSummaryVO )
    {
        log.trace( " Update Account Summary start. " );
        Optional<Payment> payment = paymentRepository.findById( paymentId );
        AccountSummaryResponseVO accountSummaryResponseVO = new AccountSummaryResponseVO();
        if( !payment.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_ID_NOT_EXIST ) ) );
        }
        Optional<Invoice> invoiceOptional = Optional.empty();
        Optional<Statement> statementOptional = Optional.empty();
        log.debug( " Getting account details" );
        Account account = payment.get().getAccount();
        if( account == null )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NOT_EXIST ) ) );
        }
        if( accountSummaryVO.getInvoiceId() != null )
        {
            invoiceOptional = invoiceRepository.findByIdAndAccountId( accountSummaryVO.getInvoiceId(), account.getAccountId() );
            if( !invoiceOptional.isPresent() )
            {
                throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentService.class,
                    applicationConfiguration.getValue( ERROR_MESSAGE_INVOICE_NOT_EXIST ) ) );
            }
        }
        if( accountSummaryVO.getStatementId() != null )
        {
            statementOptional = statementRepository.findByStatementIdAndAccountId( accountSummaryVO.getStatementId(), account );
            if( !statementOptional.isPresent() )
            {
                throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_STATEMENT_NOT_EXIST ) ) );
            }
        }
        Optional<Summary> summaryOptional = accountSummaryRepository.findByPayment( payment.get() );
        log.debug( " Update Account Summary " );
        if( summaryOptional.isPresent() )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.CONFLICT, HttpStatus.CONFLICT.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_ID_ALREADY_EXIST ) ) );
        }
        Summary summary = new Summary();
        summary.setPayment( payment.get() );
        summary.setAccountId( account.getAccountId() );
        summary.setInvoice( invoiceOptional.isPresent() ? invoiceOptional.get() : null );
        summary.setStatement( statementOptional.isPresent() ? statementOptional.get() : null );
        if( payment.get().getPayAmount().compareTo( BigDecimal.ZERO ) < 0 )
        {
            summary.setType( com.abcfinancial.api.billing.generalledger.statements.domain.Type.Dr );
        }
        else
        {
            summary.setType( com.abcfinancial.api.billing.generalledger.statements.domain.Type.Cr );
        }
        summary.setSummaryDate( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setCreated( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setTransactionType( TransactionType.PAYMENT );
        accountSummaryRepository.save( summary );
        PaymentMethod paymentMethod = paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( account.getAccountId(), Boolean.TRUE );
        PaymentMethodAccount paymentMethodAccount = ModelMapperUtils.map( summary, PaymentMethodAccount.class );
        paymentMethodAccount.setPaymentMethodId( paymentMethod.getId() );
        paymentMethodAccount.setTransactionType( TransactionType.PAYMENT );
        paymentMethodAccountRepository.save( paymentMethodAccount );
        log.debug( AppConstants.ACCOUNT_SUMMARY );
        accountSummaryResponseVO.setPayStatus( payment.get().getPayStatus() );
        Balance balance = balanceRepository.findByAccountIdAndDeactivatedAndPaymentMethodIdNull( account.getAccountId(), null );
        statementOptional.ifPresent( statement -> {
            if( statement.getTotalAmount().compareTo( payment.get().getPayAmount() ) <= 0 )
            {
                balance.setAmount( payment.get().getPayAmount().subtract( statement.getTotalAmount() ) );
                statement.setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
            }
            else
            {
                BigDecimal leftAmount = statement.getTotalAmount().subtract( payment.get().getPayAmount() );
                log.debug( " Difference between total amount and payAmount is {} ", leftAmount );
            }
            statementRepository.save( statement );
        } );

        return ResponseEntity.status( HttpStatus.CREATED ).body( accountSummaryResponseVO );
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public ResponseEntity<PaymentMethodPaymentResponseVO> updatePaymentMethodDetails( PaymentMethodPaymentVO paymentMethodVO, UUID paymentId ) throws NumberParseException
    {
        log.trace( "Inside the ResponseEntity<UpdateAccountDetailVO> updatePaymentMethodDetails( UpdateAccountDetailVO updateAccountDetailVO, UUID paymentId ) :::" + paymentId );
        // Code added start
        Optional<PaymentMethod> paymentMethod = paymentMethodRepository.findPaymentMethodByIdAndActive( paymentId, Boolean.TRUE );
        Optional<PaymentMethod> paymentMethodClone = paymentMethodRepository.findPaymentMethodByIdAndActive( paymentId, Boolean.TRUE );
        if( paymentMethod.isPresent() && paymentMethodClone.isPresent() )
        {
            @NotNull( message = "type cannot be null" )
            Type paymentMethodType = paymentMethod.get().getType();
            validateUpdatePaymentMethodDetails( paymentMethodVO, paymentMethodType );
            if( paymentMethod.get().getType() == Type.BANK_ACCOUNT )
            {
                log.debug( "Update BankAccount Info in payment_method table !!!.." );
                if( paymentMethodVO.getAccountNumber() != null && !paymentMethodVO.getAccountNumber().trim().equals( "" ) )
                {
                    paymentMethodClone.get().setAccountNumber( paymentMethodVO.getAccountNumber() );
                    paymentMethodClone.get().setDisplay( paymentMethodVO.getAccountNumber().substring( paymentMethodVO.getAccountNumber().length() - 4 ) );
                }
                if( paymentMethodVO.getRoutingNumber() != null && !paymentMethodVO.getRoutingNumber().trim().equals( "" ) )
                {
                    paymentMethodClone.get().setRoutingNumber( paymentMethodVO.getRoutingNumber() );
                }
                if( paymentMethodVO.getBankAccountType() != null && !paymentMethodVO.getBankAccountType().toString().trim().equals( "" ) )
                {
                    paymentMethodClone.get().setBankAccountType( paymentMethodVO.getBankAccountType() );
                }
                if( paymentMethodVO.getAlias() != null && !paymentMethodVO.getAlias().trim().equals( "" ) )
                {
                    paymentMethodClone.get().setAlias( paymentMethodVO.getAlias() );
                }
                paymentMethodRepository.saveAndFlush( paymentMethodClone.get() );
            }
            else if( paymentMethod.get().getType() == Type.CASH )
            {
                clearDetail( paymentMethodClone.get() );
                paymentMethodClone.get().setType( Type.CASH );
            }
        }
        else
        {
            log.debug( "Payment ID is not exist {}", paymentId );
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentMethod.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_NOT_FOUND ) + " " + paymentId ) );
        }
        PaymentMethodPaymentResponseVO paymentMethodPaymentResponseVO = ModelMapperUtils.map( paymentMethodClone.get(), PaymentMethodPaymentResponseVO.class );
        return ResponseEntity.ok().body( paymentMethodPaymentResponseVO );
    }

    void validateUpdatePaymentMethodDetails( PaymentMethodPaymentVO paymentMethodVO, Type existingType )
    {
        log.trace( "Inside the validateUpdatePaymentMethodDetails( PaymentMethodPaymentVO paymentMethodVO, Type existingType ) !!!.." );
        if( Type.BANK_ACCOUNT == existingType )
        {
            if( paymentMethodVO.getRoutingNumber() == null && paymentMethodVO.getAccountNumber() != null )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NO_NOT_NULL_FOR_ACCOUNT_NO ) ) );
            }
            if( paymentMethodVO.getRoutingNumber() != null && paymentMethodVO.getAccountNumber() == null )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NO_NOT_NULL_FOR_ACCOUNT_NO ) ) );
            }
            if( paymentMethodVO.getType() == null || paymentMethodVO.getType().name().trim().length() <= 0 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TYPE_BLANK ) ) );
            }
            if( paymentMethodVO.getType() != existingType )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentMethod.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_METHOD_INVALID ) + " " + existingType ) );
            }
            if( paymentMethodVO.getRoutingNumber() != null && paymentMethodVO.getAccountNumber() != null )
            {
                String routingNumber = paymentMethodVO.getRoutingNumber();
                String accountNumber = paymentMethodVO.getAccountNumber();
                //RoutingNo Validation
                routingNumber = routingNumber.trim();
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
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER ) ) );
                }
                if( !StringUtils.isNumeric( routingNumber ) )
                {
                    throw new ErrorResponse(
                        new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER_INTEGER ) ) );
                }
                if( ABARoutingNumber.isValidRoutingNumber( routingNumber ) )
                {
                    paymentMethodVO.setRoutingNumber( routingNumber );
                }
                else
                {
                    String inputBankCode = routingNumber.substring( 1, 4 );
                    CanadaBankCode bankCode = CanadaBankCode.forBankCode( inputBankCode );
                    if( null != bankCode )
                    {
                        paymentMethodVO.setRoutingNumber( routingNumber );
                    }
                    else
                    {
                        throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), CommonUtil.class, " Please enter a valid Routing number" ) );
                    }
                }

                // End routing number
                //AccountNo Validation
                if( accountNumber != null )
                {
                    accountNumber = accountNumber.trim();
                    if( CommonUtil.containsWhitespace( accountNumber ) )
                    {
                        throw new ErrorResponse(
                            new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SPACE_NOT_ALLOWED_BETWEEN_ACCOUNTNUMBER ) ) );
                    }
                    if( Objects.nonNull( routingNumber ) )
                    {
                        if( ABARoutingNumber.isValidRoutingNumber( routingNumber ) )
                        {
                            paymentMethodVO.setAccountNumber( accountNumber );
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
                    }
                    else
                    {
                        String inputBankCode = routingNumber.substring( 1, 4 );
                        String transitNumber = routingNumber.substring( 4, 9 );
                        CanadaBankCode bankCode = CanadaBankCode.forBankCode( inputBankCode );
                        if( null != bankCode )
                        {
                            String canadaAccountNo = bankCode.getNumber() + "" + transitNumber + "" + accountNumber;
                            paymentMethodVO.setAccountNumber( canadaAccountNo );
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
            }
            validateNotCreditcardPaymentMethod( paymentMethodVO );
            // End Account validation
        }
        else if( Type.CASH == existingType )
        {
            if( paymentMethodVO.getType() == null || paymentMethodVO.getType().name().trim().length() <= 0 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TYPE_BLANK ) ) );
            }
            if( paymentMethodVO.getType() != existingType )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentMethod.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_METHOD_INVALID ) + " " + existingType ) );
            }
            validateNotBankPaymentMethod( paymentMethodVO );
            validateNotCreditcardPaymentMethod( paymentMethodVO );
        }
        else if( Type.CREDIT_CARD == existingType )
        {
            if( paymentMethodVO.getType() == null || paymentMethodVO.getType().name().trim().length() <= 0 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TYPE_BLANK ) ) );
            }
            if( paymentMethodVO.getType() != existingType )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentMethod.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_METHOD_INVALID ) + " " + existingType ) );
            }
            validateNotBankPaymentMethod( paymentMethodVO );
        }
    }

    private void validateNotCreditcardPaymentMethod( PaymentMethodPaymentVO paymentMethodVO )
    {
        if( Strings.isNotEmpty( paymentMethodVO.getToken() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOKEN_NOT_REQUIRED ) ) );
        }
        if( paymentMethodVO.getExpiryMonth() > 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EXPIRATION_MONTH ) ) );
        }
        if( paymentMethodVO.getExpiryYear() > 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EXPIRATION_YEAR ) ) );
        }
        if( paymentMethodVO.getBrand() != null )
        {
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class, applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BRAND ) ) );
        }
        if( paymentMethodVO.getProcessor() != null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PROCESSOR ) ) );
        }
        if( paymentMethodVO.getDisplay() != null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DISPLAY ) ) );
        }
    }

    private void validateNotBankPaymentMethod( PaymentMethodPaymentVO paymentMethodVO )
    {
        if( Strings.isNotEmpty( paymentMethodVO.getRoutingNumber() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ROUTING_NUMBER_NOT_REQUIRED ) ) );
        }
        if( Strings.isNotEmpty( paymentMethodVO.getAccountNumber() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NUMBER_NOT_REQUIRED ) ) );
        }

        if( paymentMethodVO.getBankAccountType() != null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NAME_BANK_ACCOUNT_TYPE ) ) );
        }

        if( Strings.isNotEmpty( paymentMethodVO.getAlias() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NAME_ALIAS ) ) );
        }
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public ResponseEntity<UpdateAccountResponseVO> updateAccountWithStatementDate( UpdateAccountRequestVO updateAccountRequestVO, UUID accountId ) throws NumberParseException
    {
        log.trace( "Update Account: {}", accountId );
        UpdateAccountResponseVO updateAccountResponseVO = new UpdateAccountResponseVO();
        return ResponseEntity.ok().body( updateAccountResponseVO );
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public ResponseEntity<UpdateAccountResponseVO> updateAccount( UpdateAccountRequestVO updateAccountRequestVO, UUID accountId ) throws NumberParseException
    {
        log.trace( "Update Account: {}", accountId );
        UpdateAccountResponseVO updateAccountResponseVO = new UpdateAccountResponseVO();
        Optional<Account> accountOptional = accountRepository.getDetailsByAccountId( accountId );
        if( !accountOptional.isPresent() )
        {
            throw new ErrorResponse( new NotFoundResponseError( Account.class, accountId ) );
        }
        UpdateAccountInfoVO updateAccountInfoVo = updateAccountRequestVO.getAccount();
        commonValidation.validateName( updateAccountInfoVo.getName() );
        commonValidation.validateSevaluation( updateAccountInfoVo.getSevaluation() );
        commonValidation.validateEmail( updateAccountInfoVo.getEmail() );
        commonValidation.validatePhone( updateAccountInfoVo.getPhone() );
        Optional<LocationAccount> locationAccount = locationAccountRepository.getDetailsByAccountId( accountId );
        if( locationAccount.isPresent() )
        {
            updateLocationAccountInfo( updateAccountInfoVo, accountId );
        }
        Optional<MemberCreation> memberCreation = memberCreationRepository.getDetailsByAccountId( accountId );
        if( memberCreation.isPresent() )
        {
            updateMemberAccountInfo( updateAccountInfoVo, accountId );
        }
        UpdateAccountInfoVO updateAccountInfo = new UpdateAccountInfoVO();
        updateAccountResponseVO.setAccountId( accountId );
        updateAccountInfo.setEmail( accountOptional.get().getEmail() );
        updateAccountInfo.setName( accountOptional.get().getName() );
        updateAccountInfo.setPhone( accountOptional.get().getPhone() );
        updateAccountInfo.setSevaluation( accountOptional.get().getSevaluation() );
        updateAccountInfo.setBillingDate( accountOptional.get().getBillingDate() );

        if( !Objects.isNull( updateAccountRequestVO.getAccount().getBillingDate() ) )
        {
            updateAccountInfo.setBillingDate( updateAccountRequestVO.getAccount().getBillingDate() );
        }
        updateAccountResponseVO.setAccount( updateAccountInfo );

        if( updateAccountRequestVO.getAccount().getBillingDate() != null || updateAccountRequestVO.getAccount().getSevaluation() != null )
        {
            updateScheduler( updateAccountRequestVO.getAccount().getBillingDate(), accountId );
        }

        return ResponseEntity.ok().body( updateAccountResponseVO );
    }

    private void updateLocationAccountInfo( UpdateAccountInfoVO updateAccountInfoVO, UUID accountId )
    {
        log.trace( "update location account start." );
        Optional<Account> accountOptional = accountRepository.getDetailsByAccountId( accountId );
        if( accountOptional.isPresent() )
        {
            if( !Objects.isNull( updateAccountInfoVO.getBillingDate() ) )
            {
                commonValidation.validateBillingDateForClient( updateAccountInfoVO.getBillingDate() );
            }
            Account account = setAccountFieldsInfo( updateAccountInfoVO, accountOptional.get() );
            log.debug( UPDATE_CHANGES_IN_ACCOUNT_TABLE );
            accountRepository.save( account );
        }
    }

    private void updateMemberAccountInfo( UpdateAccountInfoVO updateAccountInfoVO, UUID accountId ) throws NumberParseException
    {
        log.trace( "update member account start." );
        Optional<Account> accountOptional = accountRepository.getDetailsByAccountId( accountId );
        if( accountOptional.isPresent() )
        {
            if( !Objects.isNull( updateAccountInfoVO.getBillingDate() ) )
            {
                validateDateAndCycleForPayor( updateAccountInfoVO, accountOptional );
            }
            Account account = setAccountFieldsInfo( updateAccountInfoVO, accountOptional.get() );
            log.debug( UPDATE_CHANGES_IN_ACCOUNT_TABLE );
            accountRepository.save( account );
        }
    }

    private void updateScheduler( LocalDate requestedBillingDate, UUID accountId )
    {
        try
        {
            log.info( "Update Scheduler for {}", accountId );
            PaymentMethod initialPaymentMethod = paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( accountId, Boolean.TRUE );

            UUID existingJobId = jobDetailsService.getJobActiveId( initialPaymentMethod.getId() );
            boolean isStatementDelete = false;
            boolean isSettlementDelete = false;
            if( Objects.isNull( existingJobId ) ) {
                isStatementDelete = eventScheduler.deleteAccountLedgerJob( existingJobId );
                isSettlementDelete = eventScheduler.deleteSettlementJob( existingJobId );
            }
            log.info( "Delete statement scheeduler {}", isStatementDelete );
            log.info( "Delete settlement scheeduler {}", isSettlementDelete );
            LocalDate billing = requestedBillingDate != null ? requestedBillingDate : LocalDate.now();

            if( isSettlementDelete )
            {
                eventScheduler
                    .scheduleSettlementEvent( Schedule.<StatementEventDetails>builder().start( billing.atTime( CommonUtil.convertTimeStringToLocalTime( clientSchedulerTime ) ) )
                                                                                       .repeating( false )
                                                                                       .properties( StatementEventDetails.builder()
                                                                                                                         .paymentMethodId( initialPaymentMethod.getId() )
                                                                                                                         .netBalanceDue( BigDecimal.ZERO )
                                                                                                                         .build() )
                                                                                       .build() );
            }
            if( isStatementDelete )
            {
                eventScheduler
                    .scheduleAccountLedgerEvent( Schedule.<StatementEventDetails>builder().start( billing.atTime( CommonUtil.convertTimeStringToLocalTime( payorSchedulerTime ) ) )
                                                                                          .repeating( false )
                                                                                          .properties( StatementEventDetails.builder()
                                                                                                                            .paymentMethodId( initialPaymentMethod.getId() )
                                                                                                                            .build() )
                                                                                          .build() );
            }
        }
        catch( Exception exep )
        {
            log.error( "Update Scheduler not processed for {} due to {}", accountId, exep );
        }
    }

    private Account setAccountFieldsInfo( UpdateAccountInfoVO updateAccountInfoVO, Account account )
    {
        if( Strings.isNotEmpty( updateAccountInfoVO.getName() ) )
        {
            account.setName( updateAccountInfoVO.getName() );
        }
        if( Strings.isNotEmpty( updateAccountInfoVO.getPhone() ) )
        {
            account.setPhone( updateAccountInfoVO.getPhone() );
        }
        if( Strings.isNotEmpty( updateAccountInfoVO.getEmail() ) )
        {
            account.setEmail( updateAccountInfoVO.getEmail() );
        }
        if( Strings.isNotEmpty( updateAccountInfoVO.getSevaluation() ) )
        {
            account.setSevaluation( updateAccountInfoVO.getSevaluation() );
        }
        if( !Objects.isNull( updateAccountInfoVO.getBillingDate() ) )
        {
            account.setBillingDate( updateAccountInfoVO.getBillingDate() );
        }

        return account;
    }

    private void validateDateAndCycleForPayor( UpdateAccountInfoVO updateAccountInfoVO, Optional<Account> accountOptional )
    {
        log.trace( "Inside the validateStatementDateAndCycle( UpdateAccountInfoVO updateAccountInfoVO, Optional<Account> accountOptional ) !!!.." );
        if( accountOptional.isPresent() )
        {
            commonValidation.validateBillingDateForPayor( updateAccountInfoVO.getBillingDate(), accountOptional.get().getBillingDate(), updateAccountInfoVO.getSevaluation() );
        }

    }

    @Transactional
    public ResponseEntity<ApplyPaymentResponseVO> updateAccountSummaryInfo( HttpHeaders headers, UUID accountId, ApplyPaymentRequestVO applyPaymentRequestVO )
    {
        log.debug( " PaymentService :: updateAccountSummaryInfo method start. " );
        Optional<PaymentMethod> paymentMethodOptional = paymentMethodRepository.findPaymentMethodByIdAndActive( accountId, Boolean.TRUE );
        validateApplyPaymentRequest( applyPaymentRequestVO, paymentMethodOptional );
        Optional<Account> accountOptional = accountRepository.getDetailsByAccountId( accountId );

        if( !accountOptional.isPresent() && !paymentMethodOptional.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NOT_EXIST ) ) );
        }

        Optional<Invoice> invoice = Optional.empty();
        log.debug( " PaymentService :: updateAccountSummaryInfo :: Validating InvoiceId. " );
        if( !Objects.isNull( applyPaymentRequestVO.getInvoiceId() ) )
        {
            invoice = invoiceRepository.findById( applyPaymentRequestVO.getInvoiceId() );

            if( !invoice.isPresent() )
            {
                throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentService.class,
                    applicationConfiguration.getValue( ERROR_MESSAGE_INVOICE_NOT_EXIST ) ) );
            }

            Optional<Payment> invoicePaymentOptional = paymentRepository.findByInvoiceId( applyPaymentRequestVO.getInvoiceId() );
            if( invoicePaymentOptional.isPresent() )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.CONFLICT, HttpStatus.CONFLICT.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_ALREADY_EXIST_FOR_INVOICE ) + applyPaymentRequestVO.getInvoiceId() ) );
            }

            Optional<Summary> summary = accountSummaryRepository.findByInvoiceIdAndPaymentNullAndStatementStatementIdNotNull( applyPaymentRequestVO.getInvoiceId() );
            if( summary.isPresent() )
            {
                throw new ErrorResponse( new ValidationError( "400", "Statement already evaluated the invoice", MessageUtils.ERROR_MESSAGE_APPLYPAYMENT_INVOICE ) );
            }
        }

        log.debug( " PaymentService :: updateAccountSummaryInfo :: checking  statementId exist :" + applyPaymentRequestVO.getStatementId() );
        Optional<Statement> statementOptional = Optional.empty();
        if( !Objects.isNull( applyPaymentRequestVO.getStatementId() ) )
        {
            statementOptional = statementRepository.findByStatementId( applyPaymentRequestVO.getStatementId() );

            if( !statementOptional.isPresent() )
            {
                throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_STATEMENT ) ) );
            }

            log.debug( " PaymentService :: updateAccountSummaryInfo :: checking  statement payment request or already done:" );
            Optional<Payment> paymentStatementOptional = paymentRepository.findTopByStatementId( statementOptional.get().getStatementId() );
            if( paymentStatementOptional.isPresent() )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.CONFLICT, HttpStatus.CONFLICT.value(), PaymentService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_STATEMENT_ALEREADY_EXIST ) + statementOptional.get().getStatementId() ) );
            }
        }

        Payment paymentObj = null;

        if( accountOptional.isPresent() )
        {
            log.debug( " PaymentService :: updateAccountSummaryInfo :: Apply Payment for main account: " );
            paymentObj = applyPaymentMainAccount( accountOptional.get(), invoice, statementOptional, applyPaymentRequestVO.getPayAmount() );
        }

        if( paymentMethodOptional.isPresent() )
        {
            log.debug( " PaymentService :: updateAccountSummaryInfo :: Apply Payment for payment method account. " );
            paymentObj = applyPaymentPaymentMethodAccount( paymentMethodOptional.get(), invoice, statementOptional );
        }
        log.debug( " PaymentService :: updateAccountSummaryInfo :: creating response for apply payment " );
        ApplyPaymentResponseVO applyPaymentResponseVO = new ApplyPaymentResponseVO();
        applyPaymentResponseVO.setPayStatus( paymentObj.getPayStatus() );
        applyPaymentResponseVO.setMessage( "Payment is successfully applied" );
        log.debug( " PaymentService :: updateAccountSummaryInfo :: Payment is successfully applied " );
        return ResponseEntity.status( HttpStatus.CREATED ).body( applyPaymentResponseVO );
    }

    private void validateApplyPaymentRequest( ApplyPaymentRequestVO applyPaymentRequestVO, Optional<PaymentMethod> paymentMethodOptional )
    {

        log.debug( "PaymentService :: validateApplyPaymentRequest method start. " );

        if( !paymentMethodOptional.isPresent() && Objects.isNull( applyPaymentRequestVO.getPayAmount() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AMOUNT_NOT_NULL ) ) );
        }

        if( paymentMethodOptional.isPresent() && Objects.isNull( applyPaymentRequestVO.getInvoiceId() ) && Objects.isNull( applyPaymentRequestVO.getStatementId() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_STATEMENT_OR_INVOICE_REQUIRED ) ) );
        }

        if( paymentMethodOptional.isPresent() && !Objects.isNull( applyPaymentRequestVO.getPayAmount() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAY_AMOUNT_NOT_ALLOWED ) ) );
        }

        if( !paymentMethodOptional.isPresent() && applyPaymentRequestVO.getPayAmount().compareTo( BigDecimal.ZERO ) <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AMOUNT_INVALID ) ) );
        }

        if( !paymentMethodOptional.isPresent() && applyPaymentRequestVO.getPayAmount().compareTo( BigDecimal.valueOf( PAY_AMOUNT ) ) > 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAY_AMOUNT_EXCEEDS ) ) );
        }

        if( !Objects.isNull( applyPaymentRequestVO.getStatementId() ) && !Objects.isNull( applyPaymentRequestVO.getInvoiceId() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_STATEMENT_INVOICE_INVALID ) ) );
        }

        if( !Objects.isNull( applyPaymentRequestVO.getStatementId() ) && !Objects.isNull( applyPaymentRequestVO.getPayAmount() ) && paymentMethodOptional.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), PaymentService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_STATEMENT_PAY_AMOUNT_NOT_ALLOWED ) ) );
        }

        log.debug( "PaymentService :: validateApplyPaymentRequest method end. " );
    }

    public Payment applyPaymentMainAccount( Account account, Optional<Invoice> invoiceOptional, Optional<Statement> statementOptional,
        BigDecimal payAmount )
    {
        log.debug( " applyPaymentMainAccount start. " );
        final BigDecimal invoiceAmount;
        final Payment paymentObj;
        BigDecimal extraAmount;
        BigDecimal totalBalAmt;

        Payment payment = new Payment();
        Summary summary = new Summary();
        Balance balance = balanceRepository.findByAccountIdAndDeactivatedAndPaymentMethodIdNull( account.getAccountId(), null );
        if( Objects.isNull( balance ) )
        {
            balance = balanceService.createBalance( account.getAccountId(), BigDecimal.ZERO );
        }
        log.debug( " checking invoice. " );
        if( invoiceOptional.isPresent() )
        {
            invoiceAmount = invoiceOptional.get().getTotalAmount();

            if( payAmount.compareTo( invoiceAmount ) > 0 )
            {
                extraAmount = payAmount.subtract( invoiceAmount );
                totalBalAmt = extraAmount.add( balance.getAmount() );
                validateBalanceAmount( totalBalAmt );
                balance.setAmount( totalBalAmt );
                balanceRepository.save( balance );
            }

            if( payAmount.compareTo( invoiceAmount ) < 0 )
            {
                totalBalAmt = payAmount.add( balance.getAmount() );
                validateBalanceAmount( totalBalAmt );
                balance.setAmount( totalBalAmt );
                balanceRepository.save( balance );
            }

            if( payAmount.compareTo( invoiceAmount ) >= 0 )
            {
                payment.setInvoiceId( invoiceOptional.isPresent() ? invoiceOptional.get().getId() : null );
                payment.setInvoiceType( invoiceOptional.isPresent() ? InvoiceTypeEnum.O : null );
                summary.setInvoice( invoiceOptional.isPresent() ? invoiceOptional.get() : null );
                invoiceOptional.get().setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
                invoiceRepository.save( invoiceOptional.get() );
                log.debug( " invoice processed " );
            }
        }
        log.debug( " checking statement. " );
        if( statementOptional.isPresent() )
        {
            final BigDecimal statementAmount = statementOptional.get().getTotalAmount();

            if( statementAmount.compareTo( BigDecimal.ZERO ) >= 0 )
            {
                if( payAmount.compareTo( statementAmount ) > 0 )
                {
                    extraAmount = payAmount.subtract( statementAmount );
                    totalBalAmt = extraAmount.add( balance.getAmount() );
                    validateBalanceAmount( totalBalAmt );
                    balance.setAmount( totalBalAmt );
                    balanceRepository.save( balance );
                }

                if( payAmount.compareTo( statementAmount ) < 0 )
                {
                    totalBalAmt = payAmount.add( balance.getAmount() );
                    validateBalanceAmount( totalBalAmt );
                    validateBalanceAmount( totalBalAmt );
                    balance.setAmount( totalBalAmt );
                    balanceRepository.save( balance );
                }

                if( payAmount.compareTo( statementAmount ) >= 0 )
                {
                    payment.setStatementId( statementOptional.isPresent() ? statementOptional.get().getStatementId() : null );
                    summary.setStatement( statementOptional.isPresent() ? statementOptional.get() : null );
                    statementOptional.get().setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
                    statementRepository.save( statementOptional.get() );
                    log.debug( " statement processed " );

                }
            }
            else
            {
                totalBalAmt = payAmount.add( balance.getAmount() );
                validateBalanceAmount( totalBalAmt );
                balance.setAmount( totalBalAmt );
                balanceRepository.save( balance );
                payment.setStatementId( statementOptional.isPresent() ? statementOptional.get().getStatementId() : null );
                summary.setStatement( statementOptional.isPresent() ? statementOptional.get() : null );
                statementOptional.get().setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
                statementRepository.save( statementOptional.get() );
                log.debug( " statement processed " );
            }
        }
        log.debug( " amount only exist. " );
        if( !Objects.isNull( payAmount ) && !invoiceOptional.isPresent() && !statementOptional.isPresent() )
        {
            totalBalAmt = payAmount.add( balance.getAmount() );
            validateBalanceAmount( totalBalAmt );
            balance.setAmount( totalBalAmt );
            balanceRepository.save( balance );
        }

        log.debug( "Updating payment table." );
        payment.setLocationId( account.getLocation() );
        payment.setAccount( account );
        payment.setPayAmount( payAmount );
        payment.setCreated( LocalDateTime.now( Clock.systemUTC() ) );
        payment.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        payment.setPayStatus( PayStatus.APPROVED );
        payment.setPameId( UUID.fromString( TEST_UUID ) );
        paymentObj = paymentRepository.save( payment );

        log.debug( " Updating Account Summary " );

        summary.setPayment( paymentObj );
        summary.setAccountId( account.getAccountId() );
        summary.setSummaryDate( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setType( Cr );
        summary.setCreated( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setTransactionType( TransactionType.PAYMENT );
        accountSummaryRepository.save( summary );
        log.debug( AppConstants.ACCOUNT_SUMMARY );

        return paymentObj;
    }

    public Payment applyPaymentPaymentMethodAccount( PaymentMethod paymentMethod, Optional<Invoice> invoiceOptional, Optional<Statement> statementOptional )
    {
        log.debug( " PaymentService :: applyPaymentPaymentMethodAccount :: start" );
        Payment paymentObj;

        Payment payment = new Payment();
        if( statementOptional.isPresent() )
        {
            BigDecimal statementAmount = statementOptional.get().getTotalAmount();
            statementOptional.get().setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
            payment.setPayAmount( statementAmount );
            log.debug( " PaymentService :: applyPaymentPaymentMethodAccount :: Statement processed. " );
        }

        if( invoiceOptional.isPresent() )
        {
            BigDecimal invoiceAmount = invoiceOptional.get().getTotalAmount();
            invoiceOptional.get().setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
            invoiceRepository.save( invoiceOptional.get() );
            log.debug( "  PaymentService :: applyPaymentPaymentMethodAccount :: invoice processed " );
            payment.setPayAmount( invoiceAmount );
        }

        log.debug( " PaymentService :: applyPaymentPaymentMethodAccount :: Updating payment table." );
        payment.setLocationId( paymentMethod.getLocationId() );
        payment.setAccount( paymentMethod.getAccountId() );
        payment.setInvoiceId( invoiceOptional.isPresent() ? invoiceOptional.get().getId() : null );
        payment.setInvoiceType( invoiceOptional.isPresent() ? InvoiceTypeEnum.O : null );
        payment.setStatementId( statementOptional.isPresent() ? statementOptional.get().getStatementId() : null );
        payment.setCreated( LocalDateTime.now( Clock.systemUTC() ) );
        payment.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        payment.setPayStatus( PayStatus.APPROVED );
        payment.setPameId( paymentMethod.getId() );
        paymentObj = paymentRepository.save( payment );
        log.debug( "PaymentService :: applyPaymentPaymentMethodAccount :: Payment updated." + paymentObj );

        PaymentMethodAccount paymentMethodAccount = new PaymentMethodAccount();
        paymentMethodAccount.setAccountId( paymentMethod.getAccountId().getAccountId() );
        paymentMethodAccount.setInvoice( invoiceOptional.isPresent() ? invoiceOptional.get() : null );
        paymentMethodAccount.setStatement( statementOptional.isPresent() ? statementOptional.get() : null );
        paymentMethodAccount.setPayment( paymentObj );
        paymentMethodAccount.setType( Cr );
        paymentMethodAccount.setSummaryDate( LocalDateTime.now( Clock.systemUTC() ) );
        paymentMethodAccount.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        paymentMethodAccount.setPaymentMethodId( paymentMethod.getId() );
        paymentMethodAccount.setTransactionType( TransactionType.PAYMENT );
        paymentMethodAccountRepository.save( paymentMethodAccount );
        log.debug( " PaymentService :: applyPaymentPaymentMethodAccount :: Payment Method Account updated." + paymentMethodAccount );

        return paymentObj;
    }

    private void validateBalanceAmount( BigDecimal balanceAmount )
    {
        log.debug( "PaymentService :: validateBalanceAmount method start. " );
        if( balanceAmount.compareTo( BigDecimal.valueOf( BALANCE_AMOUNT ) ) > 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_BALANCE_AMOUNT_EXCEEDS ) ) );
        }
        log.debug( "PaymentService :: validateBalanceAmount method end. " );
    }

    @Transactional
    public WebhookPayloadResponseVO createPaymentStatus( WebhookPayloadRequestVO webhookPayloadRequestVO )
    {
        log.info( "PaymentService createPaymentStatus WebhookPayloadResponseVO{}", webhookPayloadRequestVO );

        validateMandatoryFieldsForPaymentStatus( webhookPayloadRequestVO );

        List<Payload> payloadList = webhookPayloadRequestVO.getPayloadList();

        for( Payload payload : payloadList )
        {
            if( payload.getStatus().equals( Status.SUCCESS ) && payload.getTransactionType().equals( PaymentType.DEBIT ) )
            {
                consumePaymentStatus( payload.getReferencedId() );
            }
            if( payload.getStatus().equals( Status.SUCCESS ) && payload.getTransactionType().equals( PaymentType.DEPOSIT ) )
            {
                paymentStatusDeposit( payload.getReferencedId() );
            }
        }
        log.info( " END PaymentService createPaymentStatus WebhookPayloadResponseVO{}", webhookPayloadRequestVO );
        return ModelMapperUtils.map( webhookPayloadRequestVO, WebhookPayloadResponseVO.class );
    }

    private void validateMandatoryFieldsForPaymentStatus( WebhookPayloadRequestVO webhookPayloadRequestVO )
    {
        log.info( " PaymentService :: validateMandatoryFieldsForPaymentStatus {} ", webhookPayloadRequestVO );
        List<Payload> payloadList = webhookPayloadRequestVO.getPayloadList();
        if( Objects.isNull( payloadList ) )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_PAYLOAD_EMPTY ) );
        }
        if( !payloadList.isEmpty() )
        {
            payloadList.forEach( payload -> {
                try
                {
                    UUID.fromString( payload.getReferencedId() );
                }
                catch( IllegalArgumentException uuidException )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_PAYLOAD_REFRENCEDID_INVALID ) );
                }
            } );
        }

    }

    @Transactional
    public void consumePaymentStatus( String referencedId )
    {
        log.info( "PaymentService consumePaymentStatus referencedId{}", referencedId );
        Optional<Statement> statementOptional = statementRepository.findByStatementId( UUID.fromString( referencedId ) );
        if( !statementOptional.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentStatusListener.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_STATEMENT_NOT_EXIST ) ) );
        }

        applyPaymentApprovedStatus( statementOptional.get() );

    }

    @Transactional
    public void paymentStatusDeposit( String referencedId )
    {
        Settlement settlement = commonValidation.validateSettlement( UUID.fromString( referencedId ) );
        Payment payment = commonValidation.validateSettlementAccount( settlement.getSettlementId(), settlement.getAccountId().getAccountId() );

        if( payment.getPayStatus().equals( PayStatus.APPROVED ) )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_PAYLOAD_ALREADY_SUCCESS ) );
        }

        payment.setPayStatus( PayStatus.APPROVED );
        Payment paymentObj = paymentRepository.save( payment );
        Summary summary = new Summary();
        summary.setPayment( paymentObj );
        summary.setAccountId( settlement.getAccountId().getAccountId() );
        summary.setSettlement( settlement );
        summary.setType( Cr );
        summary.setLocationId( payment.getLocationId() );
        summary.setSummaryDate( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setCreated( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setTransactionType( TransactionType.DEPOSIT );
        accountSummaryRepository.save( summary );
    }

    @Transactional
    public void applyPaymentApprovedStatus( Statement statement )
    {
        log.info( "PaymentService applyPaymentApprovedStatus Statement{}", statement );
        Optional<Payment> paymentOptional = paymentRepository.findByStatementIdAndAccountAccountId( statement.getStatementId(), statement.getAccountId().getAccountId() );
        if( !paymentOptional.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentService.class,
                applicationConfiguration.getValue( ERROR_MESSAGE_PAYMENT_DETAIL_NOT_EXIST ) ) );
        }
        if( paymentOptional.get().getPayStatus().equals( PayStatus.APPROVED ) )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_PAYLOAD_ALREADY_SUCCESS ) );
        }
        log.info( "Updating payment status..." );
        paymentOptional.get().setPayStatus( PayStatus.APPROVED );
        Payment paymentObj = paymentRepository.save( paymentOptional.get() );
        log.info( " Update applyPaymentApprovedStatus Payment{}", paymentObj );
        Summary summary = new Summary();
        summary.setPayment( paymentObj );
        summary.setAccountId( statement.getAccountId().getAccountId() );
        summary.setStatement( statement );
        summary.setType( Dr );
        summary.setLocationId( paymentOptional.get().getLocationId() );
        summary.setSummaryDate( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setCreated( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setTransactionType( TransactionType.PAYMENT );
        accountSummaryRepository.save( summary );

        Optional<LocationAccount> locationAccountOptional = locationAccountRepository.findByLocaccIdLocation( statement.getLocationId() );
        if( locationAccountOptional.isPresent() )
        {
            List<BigDecimal> feeList = feeService.calculateDeductionFee( statement.getTotalAmount(), locationAccountOptional.get().getAccountId().getAccountId() );

            feeList.forEach( fee -> {

                AdjustmentRequestVO adjustmentRequestVO = new AdjustmentRequestVO();
                adjustmentRequestVO.setAmount( fee );
                adjustmentRequestVO.setAccountId( statement.getAccountId() );
                adjustmentRequestVO.setAdjustmentType( AdjustmentType.SERVICE_FEE );
                adjustmentRequestVO.setAdjustmentField( "Payment Deduction" );
                adjustmentRequestVO.setLocationId( statement.getLocationId() );

                AdjustmentResponseVO adjustmentResponseVO = adjustmentService.createAdjustment( adjustmentRequestVO );
                Adjustment adjustment = adjustmentRepository.findByAdjustmentId( adjustmentResponseVO.getAdjustmentId() );

                Summary summaryFee = new Summary();
                summaryFee.setPayment( paymentObj );
                summaryFee.setAccountId( statement.getAccountId().getAccountId() );
                summaryFee.setStatement( statement );
                summaryFee.setType( Cr );
                summaryFee.setLocationId( statement.getLocationId() );
                summaryFee.setAdjustment( adjustment );
                summaryFee.setSummaryDate( LocalDateTime.now( Clock.systemUTC() ) );
                summaryFee.setCreated( LocalDateTime.now( Clock.systemUTC() ) );
                summaryFee.setModified( LocalDateTime.now( Clock.systemUTC() ) );
                summaryFee.setTransactionType( TransactionType.ADJUSTMENT );
                accountSummaryRepository.save( summaryFee );
            } );
        }
        log.debug( AppConstants.ACCOUNT_SUMMARY );
        PaymentMethod paymentMethodObj =
            paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( statement.getAccountId().getAccountId(), Boolean.TRUE );
        PaymentMethodAccount paymentMethodAccount = ModelMapperUtils.map( summary, PaymentMethodAccount.class );
        paymentMethodAccount.setPaymentMethodId( paymentMethodObj.getId() );
        paymentMethodAccount.setType( Cr );
        paymentMethodAccount.setTransactionType( TransactionType.PAYMENT );
        paymentMethodAccountRepository.save( paymentMethodAccount );
        log.debug( " Payment method account updated. " );
    }

    @Transactional( readOnly = true )
    public List<PayorTransactionVO> getPayorTransactions( UUID accountId, Optional<String> startDate, Optional<String> endDate, Optional<String> type, Pageable pageable )
    {
        Optional<Account> accountOptional = accountRepository.getDetailsByAccountId( accountId );
        Optional<PaymentMethod> paymentMethodOptional = paymentMethodRepository.findById( accountId );

        commonValidation.validateAccountId( accountOptional, paymentMethodOptional, accountId );

        PayorTransactionVO payorTransactionVO = null;

        LocalDate transStartDate = null;
        LocalDate transEndDate = null;
        TransactionType transType = null;

        log.info( " Checking Transaction Type presense " );
        if( type.isPresent() )
        {
            transType = commonValidation.validateTransactionType( type.get() );
        }

        log.info( " Checking startDate and  endDate presense " );
        if( startDate.isPresent() && endDate.isPresent() )
        {
            transStartDate = CommonUtil.convertToDateTime( startDate.get() );
            transEndDate = CommonUtil.convertToDateTime( endDate.get() );

        }

        log.info( " Checking startDate and  endDate is not present " );
        if( startDate.isPresent() && !endDate.isPresent() )
        {
            transStartDate = CommonUtil.convertToDateTime( startDate.get() );
            transEndDate = LocalDate.now();
        }

        log.info( " Checking startDate is not present and  endDate is present " );
        if( !startDate.isPresent() && endDate.isPresent() )
        {
            transEndDate = CommonUtil.convertToDateTime( endDate.get() );

            if( accountOptional.isPresent() )
            {
                transStartDate = accountOptional.get().getCreated().toLocalDate();
            }
            if( paymentMethodOptional.isPresent() )
            {
                transStartDate = paymentMethodOptional.get().getCreated().toLocalDate();
            }
        }

        log.info( " Checking transStartDate and transEndDate " );
        if( Objects.nonNull( transStartDate ) && Objects.nonNull( transEndDate ) )
        {
            commonValidation.validateDateRange( transStartDate, transEndDate );
        }

        log.info( " Checking account id is only present in request" );
        if( accountOptional.isPresent() && !startDate.isPresent() && !endDate.isPresent() && !type.isPresent() )
        {
            payorTransactionVO = fetchMainAccountPaymentMethodAccountTrans( accountId, pageable );
        }

        log.info( " Checking payment method id is only present in request" );
        if( paymentMethodOptional.isPresent() && !startDate.isPresent() && !endDate.isPresent() && !type.isPresent() )
        {
            payorTransactionVO = fetchPaymentMethodAccountTrans( accountId, pageable );
        }

        log.info( " Checking account id and transStartDate and transEndDate present in request" );
        if( accountOptional.isPresent() && Objects.nonNull( transStartDate ) && Objects.nonNull( transEndDate ) )
        {
            payorTransactionVO = fetchMainAccAndPayMethodAccTransWithDateRange( accountId, transStartDate, transEndDate, pageable );
        }

        log.info( " Checking payment method id and transStartDate and transEndDate present in request" );
        if( paymentMethodOptional.isPresent() && Objects.nonNull( transStartDate ) && Objects.nonNull( transEndDate ) )
        {
            payorTransactionVO = fetchPaymentMethodAccountTransWithDateRange( accountId, transStartDate, transEndDate, pageable );
        }

        log.info( " Checking account id and type present in request" );
        if( accountOptional.isPresent() && type.isPresent() )
        {
            payorTransactionVO = fetchMainAccountPaymentMethodAccountTransWithType( accountId, transType, pageable );
        }

        log.info( " Checking payment method id and type present in request" );
        if( paymentMethodOptional.isPresent() && type.isPresent() )
        {
            payorTransactionVO = fetchPaymentMethodAccountTransWithType( accountId, transType, pageable );
        }

        log.info( " Checking account id and type and transaction dates are present in request" );
        if( accountOptional.isPresent() && Objects.nonNull( transStartDate ) && Objects.nonNull( transEndDate ) && type.isPresent() )
        {
            payorTransactionVO = fetchMainAccAndPayMethodAccTransWithDateAndType( accountId, transStartDate, transEndDate, transType, pageable );
        }

        log.info( " Checking payment method id and type and transaction dates are present in request" );
        if( paymentMethodOptional.isPresent() && Objects.nonNull( transStartDate ) && Objects.nonNull( transEndDate ) && type.isPresent() )
        {
            payorTransactionVO = fetchPaymentMethodAccountTransWithDateAndType( accountId, transStartDate, transEndDate, transType, pageable );
        }

        List<PayorTransactionVO> payorTransactionVOList = new ArrayList<>();
        payorTransactionVOList.add( payorTransactionVO );
        return payorTransactionVOList;
    }

    private PayorTransactionVO fetchMainAccountPaymentMethodAccountTrans( UUID accountId, Pageable pageable )
    {
        List<Summary> mainAccountTransactionsList = accountSummaryRepository.findAllByAccountIdAndLocationIdIsNull( accountId, pageable );

        log.info( "Main account transactions list: {}", mainAccountTransactionsList );

        List<PaymentMethodAccount> paymentMethodAccountTransactionsList = paymentMethodAccountRepository.findPaymentMethodAccountByAccountId( accountId, pageable );

        log.info( "Payment method account transactions list: {}", paymentMethodAccountTransactionsList );

        return PayorTransactionVO.builder().mainAccountTransactions( ModelMapperUtils.mapAll( mainAccountTransactionsList, PayorMainAccountTransactionVO.class ) )
                                 .paymentMethodTransactions( ModelMapperUtils.mapAll( paymentMethodAccountTransactionsList, PaymentMethodTransactionVO.class ) ).build();
    }

    private PayorTransactionVO fetchPaymentMethodAccountTrans( UUID paymentMethodId, Pageable pageable )
    {
        List<PaymentMethodAccount> paymentMethodAccountTransactionsList = paymentMethodAccountRepository.findPaymentMethodAccountByPaymentMethodId( paymentMethodId, pageable );

        log.info( "paymentMethodAccountTransactionsList: {}", paymentMethodAccountTransactionsList );

        return PayorTransactionVO.builder().paymentMethodTransactions( ModelMapperUtils.mapAll( paymentMethodAccountTransactionsList, PaymentMethodTransactionVO.class ) ).build();
    }

    private PayorTransactionVO fetchMainAccAndPayMethodAccTransWithDateRange( UUID accountId, LocalDate transFromDate, LocalDate transToDate, Pageable pageable )
    {
        List<Summary> mainAccountTransactionsList =
            accountSummaryRepository
                .findByAccountIdAndLocationIdIsNullAndSummaryDateBetween( accountId, transFromDate.atStartOfDay(), transToDate.atTime( LocalTime.MAX ), pageable );

        log.info( "mainAccountTransactionsList with date range: {}", mainAccountTransactionsList );

        List<PaymentMethodAccount> paymentMethodAccountTransactionsList =
            paymentMethodAccountRepository.findByAccountIdAndSummaryDateBetween( accountId, transFromDate.atStartOfDay(), transToDate.atTime( LocalTime.MAX ), pageable );

        log.info( "paymentMethodAccountTransactionsList with date range: {}", paymentMethodAccountTransactionsList );

        return PayorTransactionVO.builder().mainAccountTransactions( ModelMapperUtils.mapAll( mainAccountTransactionsList, PayorMainAccountTransactionVO.class ) )
                                 .paymentMethodTransactions( ModelMapperUtils.mapAll( paymentMethodAccountTransactionsList, PaymentMethodTransactionVO.class ) ).build();
    }

    private PayorTransactionVO fetchPaymentMethodAccountTransWithDateRange( UUID paymentMethodId, LocalDate transFromDate, LocalDate transToDate, Pageable pageable )
    {
        List<PaymentMethodAccount> paymentMethodAccountTransactionsList =
            paymentMethodAccountRepository
                .findByPaymentMethodIdAndSummaryDateBetween( paymentMethodId, transFromDate.atStartOfDay(), transToDate.atTime( LocalTime.MAX ), pageable );

        log.info( "Payment method account transactions list with date range: {}", paymentMethodAccountTransactionsList );

        return PayorTransactionVO.builder().paymentMethodTransactions( ModelMapperUtils.mapAll( paymentMethodAccountTransactionsList, PaymentMethodTransactionVO.class ) ).build();
    }

    private PayorTransactionVO fetchMainAccountPaymentMethodAccountTransWithType( UUID accountId, TransactionType type, Pageable pageable )
    {
        List<Summary> mainAccountTransactionsWithTypeList = accountSummaryRepository.findByAccountIdAndTransactionTypeAndLocationIdIsNull( accountId, type, pageable );

        log.info( "mainAccountTransactionsWithTypeList with type: {}", mainAccountTransactionsWithTypeList );

        List<PaymentMethodAccount> paymentMethodAccountTransactionsWithTypeList = paymentMethodAccountRepository.findByAccountIdAndTransactionType( accountId, type, pageable );

        log.info( "paymentMethodAccountTransactionsWithTypeList with type: {}", paymentMethodAccountTransactionsWithTypeList );

        return PayorTransactionVO.builder().mainAccountTransactions( ModelMapperUtils.mapAll( mainAccountTransactionsWithTypeList, PayorMainAccountTransactionVO.class ) )
                                 .paymentMethodTransactions( ModelMapperUtils.mapAll( paymentMethodAccountTransactionsWithTypeList, PaymentMethodTransactionVO.class ) ).build();
    }

    private PayorTransactionVO fetchPaymentMethodAccountTransWithType( UUID paymentMethodId, TransactionType type, Pageable pageable )
    {
        List<PaymentMethodAccount> paymentMethodAccountTransactionsList = paymentMethodAccountRepository.findByPaymentMethodIdAndTransactionType( paymentMethodId, type, pageable );

        log.info( "Payment method account transactions list with type: {}", paymentMethodAccountTransactionsList );

        return PayorTransactionVO.builder().paymentMethodTransactions( ModelMapperUtils.mapAll( paymentMethodAccountTransactionsList, PaymentMethodTransactionVO.class ) ).build();
    }

    private PayorTransactionVO fetchMainAccAndPayMethodAccTransWithDateAndType( UUID accountId, LocalDate transFromDate, LocalDate transToDate, TransactionType type,
        Pageable pageable )
    {
        List<Summary> mainAccountTransactionsList =
            accountSummaryRepository
                .findByAccountIdAndTransactionTypeAndLocationIdIsNullAndSummaryDateBetween( accountId, type, transFromDate.atStartOfDay(), transToDate.atTime( LocalTime.MAX ),
                    pageable );

        log.info( "mainAccountTransactionsList with type and date: {}", mainAccountTransactionsList );

        List<PaymentMethodAccount> paymentMethodAccountTransactionsList =
            paymentMethodAccountRepository
                .findByAccountIdAndTransactionTypeAndSummaryDateBetween( accountId, type, transFromDate.atStartOfDay(), transToDate.atTime( LocalTime.MAX ), pageable );

        log.info( "paymentMethodAccountTransactionsList with type and date: {}", paymentMethodAccountTransactionsList );

        return PayorTransactionVO.builder().mainAccountTransactions( ModelMapperUtils.mapAll( mainAccountTransactionsList, PayorMainAccountTransactionVO.class ) )
                                 .paymentMethodTransactions( ModelMapperUtils.mapAll( paymentMethodAccountTransactionsList, PaymentMethodTransactionVO.class ) ).build();
    }

    private PayorTransactionVO fetchPaymentMethodAccountTransWithDateAndType( UUID paymentMethodId, LocalDate transFromDate, LocalDate transToDate, TransactionType type,
        Pageable pageable )
    {
        List<PaymentMethodAccount> paymentMethodAccountTransactionsList =
            paymentMethodAccountRepository
                .findByPaymentMethodIdAndTransactionTypeAndSummaryDateBetween( paymentMethodId, type, transFromDate.atStartOfDay(), transToDate.atTime( LocalTime.MAX ), pageable );

        log.info( "Payment method account transactions list with type and date: {}", paymentMethodAccountTransactionsList );

        return PayorTransactionVO.builder().paymentMethodTransactions( ModelMapperUtils.mapAll( paymentMethodAccountTransactionsList, PaymentMethodTransactionVO.class ) ).build();
    }

    @Transactional
    public PaymentMethodResponseVO createPaymentMethod( PaymentMethodRequestVO paymentMethodRequestVO )
    {
        log.info( "PaymentService createPaymentMethod PaymentMethodRequestVO{}", paymentMethodRequestVO );

        Account account = commonValidation.validatePayorAccountId( paymentMethodRequestVO.getAccountId() );

        commonValidation.validatePaymentMethodRequestVO( paymentMethodRequestVO );

        PaymentMethod paymentMethod = ModelMapperUtils.map( paymentMethodRequestVO, PaymentMethod.class );
        paymentMethod.setAccountId( account );
        paymentMethod.setLocationId( account.getLocation() );
        paymentMethod.setActive( Boolean.TRUE );
        paymentMethodRepository.save( paymentMethod );
        PaymentMethodResponseVO paymentMethodResponseVO = ModelMapperUtils.map( paymentMethod, PaymentMethodResponseVO.class );
        paymentMethodResponseVO.setAccountId( account.getAccountId() );

        return paymentMethodResponseVO;
    }

    @Transactional( readOnly = true )
    public List<PaymentMethodTransactionVO> getPaymentMethodAccountTransactions( UUID paymentMethodId, Pageable pageable )
    {
        Optional<PaymentMethod> paymentMethodOptional = paymentMethodRepository.findById( paymentMethodId );

        if( !paymentMethodOptional.isPresent() )
        {
            commonValidation.validatePaymentMethodId( paymentMethodId );
        }

        log.info( " Checking payment method id " );
        return fetchPaymentMethodAccountTransactions( paymentMethodId, pageable );
    }

    private List<PaymentMethodTransactionVO> fetchPaymentMethodAccountTransactions( UUID paymentMethodId, Pageable pageable )
    {
        List<PaymentMethodAccount> paymentMethodAccountTransactionsListSinceLastStatement = paymentMethodAccountRepository.findBySinceLastStatement( paymentMethodId, pageable );

        log.info( "paymentMethodAccountTransactionsListSinceLastStatement: {}", paymentMethodAccountTransactionsListSinceLastStatement );

        return ModelMapperUtils.mapAll( paymentMethodAccountTransactionsListSinceLastStatement, PaymentMethodTransactionVO.class );
    }

    public List<ClientAccountTransactionResponseVO> getClientAccountTransactions( UUID accountId, Optional<String> startDate, Optional<String> endDate, Optional<String> type,
        Pageable pageable )
    {
        commonValidation.validateClientAccountId( accountId );
        Optional<Account> accountOptional = accountRepository.getDetailsByAccountId( accountId );

        List<ClientAccountTransactionResponseVO> clientAccountTransactions = null;

        LocalDate transStartDate = null;
        LocalDate transEndDate = null;
        TransactionType transType = null;

        log.info( " Checking Transaction Type presense " );
        if( type.isPresent() )
        {
            transType = commonValidation.validateTransactionType( type.get() );
        }

        log.info( " Checking startDate and  endDate presense " );
        if( startDate.isPresent() && endDate.isPresent() )
        {
            transStartDate = CommonUtil.convertToDateTime( startDate.get() );
            transEndDate = CommonUtil.convertToDateTime( endDate.get() );

        }

        log.info( " Checking startDate and  endDate is not present " );
        if( startDate.isPresent() && !endDate.isPresent() )
        {
            transStartDate = CommonUtil.convertToDateTime( startDate.get() );
            transEndDate = LocalDate.now();
        }

        log.info( " Checking startDate is not present and  endDate is present " );
        if( accountOptional.isPresent() && !startDate.isPresent() && endDate.isPresent() )
        {
            transEndDate = CommonUtil.convertToDateTime( endDate.get() );

            transStartDate = accountOptional.get().getCreated().toLocalDate();

        }

        log.info( " Checking transStartDate and transEndDate " );
        if( Objects.nonNull( transStartDate ) && Objects.nonNull( transEndDate ) )
        {
            commonValidation.validateDateRange( transStartDate, transEndDate );
        }

        log.info( " Checking account id is only present in request" );
        if( accountOptional.isPresent() && !startDate.isPresent() && !endDate.isPresent() && !type.isPresent() )
        {
            clientAccountTransactions = fetchClientAccountTrans( accountId, pageable, accountOptional.get().getLocation() );
        }

        log.info( " Checking account id and transStartDate and transEndDate present " );
        if( accountOptional.isPresent() && Objects.nonNull( transStartDate ) && Objects.nonNull( transEndDate ) )
        {
            clientAccountTransactions = fetchClientAccountTransWithDateRange( accountId, transStartDate, transEndDate, pageable, accountOptional.get().getLocation() );
        }

        log.info( " Checking account id and type present in request" );
        if( accountOptional.isPresent() && type.isPresent() )
        {
            clientAccountTransactions = fetchClientAccountTransWithType( accountId, transType, pageable, accountOptional.get().getLocation() );
        }

        log.info( " Checking account id and type and transaction dates are present in request" );
        if( accountOptional.isPresent() && Objects.nonNull( transStartDate ) && Objects.nonNull( transEndDate ) && type.isPresent() )
        {
            clientAccountTransactions =
                fetchClientAccountTransWithTypeWithDateAndType( accountId, transStartDate, transEndDate, transType, pageable, accountOptional.get().getLocation() );
        }

        return clientAccountTransactions;
    }

    private List<ClientAccountTransactionResponseVO> fetchClientAccountTrans( UUID accountId, Pageable pageable, UUID locId )
    {
        List<Summary> clientAccountTransactionsList = accountSummaryRepository.findAllByAccountIdAndLocationIdIsNull( accountId, pageable );

        List<Summary> clientAccountTransactionsListUsingLocID = accountSummaryRepository.findAllByLocationId( locId, pageable );

        clientAccountTransactionsList.addAll( clientAccountTransactionsListUsingLocID );
        log.info( "Client account transactions list: {}", clientAccountTransactionsList );

        return ModelMapperUtils.mapAll( clientAccountTransactionsList, ClientAccountTransactionResponseVO.class );
    }

    private List<ClientAccountTransactionResponseVO> fetchClientAccountTransWithDateRange( UUID accountId, LocalDate transFromDate, LocalDate transToDate, Pageable pageable,
        UUID locId )
    {
        List<Summary> clientAccountTransactionsListByDateRange =
            accountSummaryRepository
                .findByAccountIdAndLocationIdIsNullAndSummaryDateBetween( accountId, transFromDate.atStartOfDay(), transToDate.atTime( LocalTime.MAX ), pageable );

        List<Summary> clientAccountTransactionsListByDateRangeUsingLocID =
            accountSummaryRepository.findByLocationIdAndSummaryDateBetween( locId, transFromDate.atStartOfDay(), transToDate.atTime( LocalTime.MAX ), pageable );

        clientAccountTransactionsListByDateRange.addAll( clientAccountTransactionsListByDateRangeUsingLocID );
        log.info( "Client account transactions list with date range: {}", clientAccountTransactionsListByDateRange );

        return ModelMapperUtils.mapAll( clientAccountTransactionsListByDateRange, ClientAccountTransactionResponseVO.class );
    }

    private List<ClientAccountTransactionResponseVO> fetchClientAccountTransWithType( UUID accountId, TransactionType type, Pageable pageable, UUID locId )
    {
        List<Summary> clientAccountTransactionsListByType = accountSummaryRepository.findByAccountIdAndTransactionTypeAndLocationIdIsNull( accountId, type, pageable );

        List<Summary> clientAccountTransactionsListByTypeUsingLocID = accountSummaryRepository.findByLocationIdAndTransactionType( locId, type, pageable );

        clientAccountTransactionsListByType.addAll( clientAccountTransactionsListByTypeUsingLocID );
        log.info( "Client account transactions list with type: {}", clientAccountTransactionsListByType );

        return ModelMapperUtils.mapAll( clientAccountTransactionsListByType, ClientAccountTransactionResponseVO.class );
    }

    private List<ClientAccountTransactionResponseVO> fetchClientAccountTransWithTypeWithDateAndType( UUID accountId, LocalDate transFromDate, LocalDate transToDate,
        TransactionType type,
        Pageable pageable, UUID locId )
    {
        List<Summary> clientAccountTransactionsListByDateAndType =
            accountSummaryRepository
                .findByAccountIdAndTransactionTypeAndLocationIdIsNullAndSummaryDateBetween( accountId, type, transFromDate.atStartOfDay(), transToDate.atTime( LocalTime.MAX ),
                    pageable );

        List<Summary> clientAccountTransactionsListByDateAndTypeUsingLocId =
            accountSummaryRepository.findByLocationIdAndTransactionTypeAndSummaryDateBetween( locId, type, transFromDate.atStartOfDay(), transToDate.atTime( LocalTime.MAX ),
                pageable );
        clientAccountTransactionsListByDateAndType.addAll( clientAccountTransactionsListByDateAndTypeUsingLocId );

        log.info( "Client account transactions list with type and date: {}", clientAccountTransactionsListByDateAndType );

        return ModelMapperUtils.mapAll( clientAccountTransactionsListByDateAndType, ClientAccountTransactionResponseVO.class );
    }

    @Transactional( readOnly = true )
    public PaymentMethodResponseVO getPaymentMethod( UUID paymentMethodId )
    {
        log.trace( "Inside the PaymentMethodResponseVO getPaymentMethodByPaymentMethodId( UUID paymentMethodId ) !!!." );

        Optional<PaymentMethod> paymentMethodOptional = paymentMethodRepository.findById( paymentMethodId );
        if( !paymentMethodOptional.isPresent() )
        {
            commonValidation.validatePaymentMethodId( paymentMethodId );
        }

        PaymentMethodResponseVO paymentMethodResponseVO = ModelMapperUtils.map( paymentMethodOptional.get(), PaymentMethodResponseVO.class );
        paymentMethodResponseVO.setAccountId( paymentMethodOptional.get().getAccountId().getAccountId() );
        return paymentMethodResponseVO;

    }

}


