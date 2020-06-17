package com.abcfinancial.api.billing.generalledger.settlement.service;

import com.abcfinancial.api.billing.generalledger.adjustment.domain.Adjustment;
import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.payment.service.PaymentService;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.ApplyPaymentRequestVO;
import com.abcfinancial.api.billing.generalledger.settlement.domain.Settlement;
import com.abcfinancial.api.billing.generalledger.settlement.repository.SettlementRepository;
import com.abcfinancial.api.billing.generalledger.settlement.valueobject.EvaluateSettlementResponseVO;
import com.abcfinancial.api.billing.generalledger.settlement.valueobject.SettlementEventVO;
import com.abcfinancial.api.billing.generalledger.settlement.valueobject.SettlementResponseVO;
import com.abcfinancial.api.billing.generalledger.statements.domain.Summary;
import com.abcfinancial.api.billing.generalledger.statements.domain.Type;
import com.abcfinancial.api.billing.generalledger.statements.repository.AccountSummaryRepository;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.PaymentRequestVO;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.PaymentResponseVO;
import com.abcfinancial.api.billing.scheduler.EventScheduler;
import com.abcfinancial.api.billing.scheduler.schedules.Schedule;
import com.abcfinancial.api.billing.scheduler.schedules.StatementEventDetails;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.LocationAccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Payment;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PayStatus;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.PaymentRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.restutil.HttpBillingService;
import com.abcfinancial.api.billing.utility.common.*;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import com.abcfinancial.api.common.domain.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.abcfinancial.api.billing.utility.common.AppConstants.*;

@Service
@Slf4j

public class SettlementService
{
    @Autowired
    PaymentService paymentService;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private AccountSummaryRepository accountSummaryRepository;
    @Autowired
    private LocationAccountRepository locationAccountRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private EventScheduler eventScheduler;
    @Autowired
    private HttpBillingService httpService;
    @Autowired
    private LocationAccountRepository clientAccountRepository;
    @Value( "${paymentGateway.uri.depositRequest}" )
    private String paymentRequestURL;
    @Autowired
    private AuthTokenUtil authTokenUtil;
    @Value( "${generalLedger.scheduleTime.settlement}" )
    private String clientSchedulerTime;

    @Transactional
    public EvaluateSettlementResponseVO evaluateSettlement( UUID accountId )
    {
        log.debug( "Start Evaluate Settlement." );
        if( accountId == null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Account.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NULL ) ) );
        }
        Optional<LocationAccount> locationAccount = locationAccountRepository.getDetailsByAccountId( accountId );
        if( !locationAccount.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Account.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + accountId ) );
        }
        Optional<Account> account = accountRepository.findById( accountId );
        if( !account.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Account.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + accountId ) );
        }
       log.debug( "Getting account summaries for account {} and client {}", account.get().getName(), locationAccount.get().getClientId() );
        List<Summary> summaries = accountSummaryRepository.findByLocationIdAndSettlementOrderBySummaryDateDesc( account.get().getLocation(), null );
        BigDecimal netAmountDue = BigDecimal.ZERO;
     log.debug( "Found {} summaries for account {} with client id {}", summaries.size(), account.get().getName(), locationAccount.get().getClientId() );
        for( Summary summary : summaries )
        {

            Payment payment = summary.getPayment();
            Adjustment adjustment = summary.getAdjustment();
            if( payment != null && adjustment == null )

            {
                netAmountDue = netAmountDue.add( payment.getPayAmount() );
                log.debug( "Added payment {} to net amount due {}", NumberFormat.getCurrencyInstance().format( netAmountDue ) );
            }
            if( adjustment != null )
            {
                netAmountDue = netAmountDue.add( adjustment.getAmount() );
                log.debug( "Added adjustment {} to net amount due {}", adjustment.getAdjustmentId(), NumberFormat.getCurrencyInstance().format( adjustment.getAmount() ) );
            }
        }
        Optional<Settlement> settlementOptional = settlementRepository.findSettlementByAccountIdAndDeactivated( account.get(), null );
        if( settlementOptional.isPresent() )
        {
            netAmountDue = netAmountDue.add( settlementOptional.get().getAmount() );
        }
        EvaluateSettlementResponseVO evaluateSettlementResponseVO = new EvaluateSettlementResponseVO();
        evaluateSettlementResponseVO.setAccountId( accountId );
        evaluateSettlementResponseVO.setLocationId( locationAccount.get().getLocaccId().getLocation() );
        evaluateSettlementResponseVO.setAmount( netAmountDue );
        log.debug( "End Evaluate Settlement " );
        return evaluateSettlementResponseVO;
    }

    @Transactional
    public void generateSettlementNext( SettlementEventVO settlementEventVO )
    {
        log.debug( "settlementEventStart settlement  start: {}", settlementEventVO.getPaymentMethodId() );
        PaymentMethod paymentMethod = paymentMethodRepository.findById( settlementEventVO.getPaymentMethodId() )
                                                             .orElseThrow( () -> new EntityNotFoundException( "requested paymentMethod detail doesn't exist" ) );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set( HEADER_AUTHORISATION, HEADER_AUTHORISATION_BEARER + authTokenUtil.getUserToken() );
        SettlementResponseVO settlementResponseVO = generateSettlement( headers, paymentMethod.getAccountId().getAccountId() );
        log.debug( "Generate Settlement Response {}", settlementResponseVO );
        scheduleNextSettlement( settlementEventVO, paymentMethod );
        log.debug( "settlementEventStart settlement  finish." );
    }

    @Transactional
    public SettlementResponseVO generateSettlement( HttpHeaders headers, UUID accountId )
    {
        log.debug( "Start generate Settlement." );
        if( accountId == null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Account.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NULL ) ) );
        }
        Optional<LocationAccount> locationAccount = locationAccountRepository.getDetailsByAccountId( accountId );
        if( !locationAccount.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Account.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + accountId ) );
        }
        Optional<Account> account = accountRepository.findById( accountId );
        if( !account.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Account.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + accountId ) );
        }
       log.debug( "Getting account summaries for account {} and client {}", account.get().getName(), locationAccount.get().getClientId() );
        List<Summary> summaries = accountSummaryRepository.findByLocationIdAndSettlementOrderBySummaryDateDesc( account.get().getLocation(), null );
        BigDecimal netAmountDue = BigDecimal.ZERO;
        log.debug( "Found {} summaries for account {} with client id {}", summaries.size(), account.get().getName(), locationAccount.get().getClientId() );
        for( Summary summary : summaries )
        {

            Payment payment = summary.getPayment();
            Adjustment adjustment = summary.getAdjustment();
            if( payment != null && adjustment == null )

            {
                netAmountDue = netAmountDue.add( payment.getPayAmount() );
                log.debug( "Added payment {} to net amount due {}", NumberFormat.getCurrencyInstance().format( netAmountDue ) );
            }
            if( adjustment != null )
            {
                netAmountDue = netAmountDue.add( adjustment.getAmount() );
                log.debug( "Added adjustment {} to net amount due {}", adjustment.getAdjustmentId(), NumberFormat.getCurrencyInstance().format( adjustment.getAmount() ) );
            }
        }
        Settlement settlementResponse = generateSettlement( netAmountDue, account.get() );
        summaries.parallelStream().forEach( summary ->
            summary.setSettlement( settlementResponse )
        );
        ApplyPaymentRequestVO applyPaymentRequestVO = new ApplyPaymentRequestVO();
        applyPaymentRequestVO.setPayAmount( settlementResponse.getAmount() );
        if( settlementResponse.getAmount().compareTo( BigDecimal.ZERO ) > 0 )
        {
            try
            {
                depositSettlementAmount( headers, settlementResponse, account.get() );
            }
            catch( Exception exception )
            {
                log.error( "deposit payment generate settlement:" + exception.getMessage() );
            }
        }
        SettlementResponseVO settlementResponseVO = ModelMapperUtils.map( settlementResponse, SettlementResponseVO.class );
        settlementResponseVO.setAccountId( settlementResponse.getAccountId().getAccountId() );
        return settlementResponseVO;
    }

    @Transactional
    public void scheduleNextSettlement( SettlementEventVO settlementEventVO, PaymentMethod paymentMethod )
    {
        log.debug( "Next Settlement generation start." );
        LocalDate nextBillingDate = calculateNextBillingDate( settlementEventVO.getFrequency(), settlementEventVO.getBillingDate() );
        log.debug( "Next generated subscriptionmanagement date: {}", nextBillingDate );
        paymentMethod.getAccountId().setBillingDate( nextBillingDate );
        paymentMethodRepository.save( paymentMethod );
        eventScheduler.deleteSettlementJob( settlementEventVO.getJobId() );
        eventScheduler
            .scheduleSettlementEvent( Schedule.<StatementEventDetails>builder().start( nextBillingDate.atTime( CommonUtil.convertTimeStringToLocalTime( clientSchedulerTime ) ) )
                                                                               .repeating( false )
                                                                               .properties( StatementEventDetails.builder()
                                                                                                                 .paymentMethodId(
                                                                                                                     settlementEventVO.getPaymentMethodId() ).build() )
                                                                               .build() );
        log.debug( "Next Settlement generation Finish." );
    }

    public Settlement generateSettlement( BigDecimal netAmountDue, Account account )
    {
        Optional<Settlement> settlementOptional = settlementRepository.findSettlementByAccountIdAndDeactivated( account, null );
        if( settlementOptional.isPresent() )
        {
            netAmountDue = netAmountDue.add( settlementOptional.get().getAmount() );
            settlementOptional.get().setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
            settlementRepository.save( settlementOptional.get() );
        }
        Settlement settlement = new Settlement();
        settlement.setLocationId( account.getLocation() );
        settlement.setCreated( LocalDateTime.now( Clock.systemUTC() ) );
        settlement.setSettlementDate( LocalDateTime.now( Clock.systemUTC() ) );
        settlement.setAmount( netAmountDue );
        settlement.setAccountId( account );
        settlement.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        Settlement settlementResponse = settlementRepository.save( settlement );
        log.debug( "Created new settlement on {} for {} for {}", settlement.getSettlementDate(), NumberFormat.getCurrencyInstance().format( netAmountDue ),
            account.getAccountId() );
        Summary summary = new Summary();
        summary.setSummaryDate( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        summary.setSettlement( settlement );
        summary.setType( Type.None );
        summary.setAccountId( account.getAccountId() );
        summary.setTransactionType( TransactionType.SETTLEMENT );
        accountSummaryRepository.save( summary );
        log.debug( "Created new account summary on {} for {}", summary.getSummaryDate(), account.getAccountId() );
        return settlementResponse;
    }

    private LocalDate calculateNextBillingDate( Frequency frequency, LocalDate billingDate )
    {
        switch( frequency )
        {
            case WEEKLY:
                return billingDate.plusWeeks( 1 );
            case MONTHLY:
                return billingDate.plusMonths( 1 );
            case ANNUALLY:
                return billingDate.plusYears( 1 );
            case EVERY_OTHER_WEEK:
                return billingDate.plusWeeks( 2 );
            case EVERY_OTHER_MONTH:
                return billingDate.plusMonths( 2 );
            case QUARTERLY:
                return billingDate.plusMonths( 3 );
            case SEMIANNUALLY:
                return billingDate.plusMonths( 6 );
            default:
                return billingDate.plusDays( 1 );
        }
    }

    private void depositSettlementAmount( HttpHeaders headers, Settlement settlement, Account account )
    {
        PaymentMethod paymentMethod = paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( account.getAccountId(), Boolean.TRUE );

        if( paymentMethod == null )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentMethod.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + account.getAccountId() ) );
        }

        log.debug( "Deposit payment start for accountId {}", paymentMethod.getAccountId().getAccountId() );
        PaymentRequestVO paymentRequestVO = new PaymentRequestVO();
        Optional<LocationAccount> clientAccount = clientAccountRepository.findByLocaccIdLocation( paymentMethod.getAccountId().getLocation() );
        paymentRequestVO.setMerchantId( clientAccount.isPresent() ? clientAccount.get().getMerchantId() : null );
        paymentRequestVO.setPaymentMethodId( paymentMethod.getTokenId() );
        paymentRequestVO.setAmount( settlement.getAmount() );
        paymentRequestVO.setSource( BILLING );
        paymentRequestVO.setReferenceId( String.valueOf( settlement.getSettlementId() ) );

        PaymentResponseVO paymentResponseVO = httpService.callApi( paymentRequestURL, paymentRequestVO, PaymentResponseVO.class, headers );
        log.debug( "Response from Server for URI {} is {}", paymentRequestURL, paymentResponseVO );

        Payment payment = new Payment();
        payment.setLocationId( account.getLocation() );
        payment.setPayAmount( settlement.getAmount() );
        payment.setPayStatus( PayStatus.PENDING );
        payment.setAccount( account );
        payment.setPameId( paymentMethod.getId() );
        payment.setSettlementId( settlement.getSettlementId() );
        payment.setCreated( LocalDateTime.now( Clock.systemUTC() ) );
        payment.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        paymentRepository.save( payment );

        log.info( "Successfully sent deposit to gateway for collection and saved payment as pending {} for {}",
            payment.getId(), NumberFormat.getCurrencyInstance().format( payment.getPayAmount() ) );

        settlement.setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
        settlementRepository.save( settlement );

        log.debug( " Setttlement Service :: depositSettlementAmount :: settlement successfully processed." );
    }
}
