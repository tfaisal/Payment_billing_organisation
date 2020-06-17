package com.abcfinancial.api.billing.generalledger.statements.service;

import com.abcfinancial.api.billing.generalledger.adjustment.domain.Adjustment;
import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.abcfinancial.api.billing.generalledger.invoice.repository.InvoiceRepository;
import com.abcfinancial.api.billing.generalledger.kafka.producer.PaymentStatusGenerator;
import com.abcfinancial.api.billing.generalledger.kafka.producer.StatementGenerator;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.statements.domain.*;
import com.abcfinancial.api.billing.generalledger.statements.repository.*;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.*;
import com.abcfinancial.api.billing.scheduler.EventScheduler;
import com.abcfinancial.api.billing.scheduler.schedules.Schedule;
import com.abcfinancial.api.billing.scheduler.schedules.StatementEventDetails;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.LocationAccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.MemberCreation;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.repository.MemberCreationRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Payment;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.InvoiceTypeEnum;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PayStatus;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.PaymentRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.restutil.HttpBillingService;
import com.abcfinancial.api.billing.utility.common.*;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.common.domain.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.abcfinancial.api.billing.utility.common.AppConstants.HEADER_AUTHORISATION;
import static com.abcfinancial.api.billing.utility.common.AppConstants.HEADER_AUTHORISATION_BEARER;

@Service

@Slf4j

public class StatementService
{
    @Autowired
    private StatementRepository statementRepository;
    @Autowired
    private StatementInvoiceRepository statementInvoiceRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private StatementGenerator statementGenerator;
    @Autowired
    private AccountSummaryRepository accountSummaryRepository;
    @Autowired
    private PaymentMethodAccountRepository paymentMethodAccountRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private MemberCreationRepository memberCreationRepository;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private PaymentStatusGenerator paymentStatusGenerator;
    @Value( "${paymentGateway.uri.paymentRequest}" )
    private String paymentRequestURL;
    @Autowired
    private HttpBillingService httpService;
    @Autowired
    private LocationAccountRepository clientAccountRepository;
    @Autowired
    private EventScheduler eventScheduler;
    @Autowired
    private AuthTokenUtil authTokenUtil;
    @Value( "${generalLedger.scheduleTime.statement}" )
    private String payorSchedulerTime;

    @Transactional( propagation = Propagation.REQUIRED )
    public StatementVo createStatement( StatementVo statementVo )
    {
        log.debug( "Statement Data {}", statementVo );
        Statement statement = ModelMapperUtils.map( statementVo, Statement.class );
        log.debug( "Statement Created Data {}", statement );
        statement.setAccountId( statementVo.getAccountId() );
        Statement dbStatement = statementRepository.save( statement );
        StatementInvoice statementInvoice = ModelMapperUtils.map( statementVo, StatementInvoice.class );
        StatementInvoiceId statementInvoiceId = new StatementInvoiceId();
        statementInvoiceId.setStatementId( dbStatement );
        statementInvoiceId.setInvoiceId( statementVo.getInvoiceId() );
        statementInvoice.setLocationId( statementVo.getLocationId() );
        statementInvoice.setStatementInvoiceId( statementInvoiceId );
        log.debug( "Statement invoice Data {}", statementInvoice );
        statementInvoiceRepository.save( statementInvoice );
        statementVo.setStatementId( dbStatement.getStatementId() );
        statementVo.setStatementDate( LocalDateTime.now() );
        log.info( "Generated statement {} for account {} with balance of {}",
            dbStatement.getStatementId(), dbStatement.getAccountId(), NumberFormat.getCurrencyInstance().format( dbStatement.getTotalAmount() ) );
        statementGenerator.send( statementVo );
        return statementVo;
    }

    @Transactional
    public List<GetStatementVo> getStatementByAccountId( UUID accountId, Pageable pageable )
    {
        log.debug( "Finding Statements: {}", accountId );
        List<GetStatementVo> statementVos = new ArrayList<>();
        Optional<Account> account = accountRepository.findById( accountId );
        if( account.isPresent() )
        {
            List<Statement> statements = statementRepository.findStatementsByAccountId( account.get(), pageable );
            GetStatementVo getStatementVo = null;
            for( Statement statement : statements )
            {
                getStatementVo = new GetStatementVo();
                getStatementVo.setAccountId( account.get().getAccountId() );
                getStatementVo.setStatementId( statement.getStatementId() );
                getStatementVo.setLocationId( statement.getLocationId() );
                getStatementVo.setTotalAmount( statement.getTotalAmount() );
                getStatementVo.setStatementDate( statement.getStmtDate() );
                List<StatementInvoice> statementInvoices = statementInvoiceRepository.findStatementInvoiceByStatementId( statement.getStatementId() );
                for( StatementInvoice statementInvoice : statementInvoices )
                {
                    List<Invoice> invoices = new ArrayList<>();
                    Invoice invoice = new Invoice();
                    invoice.setTotalNetPrice( statementInvoice.getStatementInvoiceId().getInvoiceId().getTotalNetPrice() );
                    invoice.setId( statementInvoice.getStatementInvoiceId().getInvoiceId().getId() );
                    invoice.setTotalTax( statementInvoice.getStatementInvoiceId().getInvoiceId().getTotalTax() );
                    invoice.setItems( statementInvoice.getStatementInvoiceId().getInvoiceId().getItems() );
                    invoice.setTotalAmount( statementInvoice.getStatementInvoiceId().getInvoiceId().getTotalAmount() );
                    invoice.setSalesEmployeeId( statementInvoice.getStatementInvoiceId().getInvoiceId().getSalesEmployeeId() );
                    invoice.setMemberId( statementInvoice.getStatementInvoiceId().getInvoiceId().getMemberId() );
                    invoice.setTotalDiscountAmount( statementInvoice.getStatementInvoiceId().getInvoiceId().getTotalDiscountAmount() );
                    invoices.add( invoice );
                    getStatementVo.setInvoices( invoices );
                }
                statementVos.add( getStatementVo );
            }
        }
        else
        {
            log.debug( "Account ID is not exist {}", accountId );
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Statement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NOT_EXIST ) + accountId ) );
        }
        log.debug( "list of statement {} ", statementVos.toString() );
        return statementVos;
    }

    @Transactional
    public void statementEventStart( StatementEventVO statementEventVO )
    {
        log.debug( "statementEventStart Statement Vo Data{}", statementEventVO );
        PaymentMethod paymentMethod = paymentMethodRepository.findById( statementEventVO.getPaymentMethodId() )
                                                             .orElseThrow( () -> new EntityNotFoundException( "requested paymentMethod detail doesn't exist" ) );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set( HEADER_AUTHORISATION, HEADER_AUTHORISATION_BEARER + authTokenUtil.getUserToken() );
        StatementResponseVO statementResponseVO = generateStatement( paymentMethod.getId(), headers );
        scheduleNextStatement( statementEventVO, paymentMethod );
        log.debug( "statementResponseVO {}", statementResponseVO );
        log.debug( "statementEventStart Statement event finish." );
    }

    public StatementResponseVO generateStatement( UUID accountId, HttpHeaders httpHeaders )
    {
        return statementCommit( accountId, httpHeaders );
    }

    private void scheduleNextStatement( StatementEventVO statementEventVO, PaymentMethod paymentMethod )
    {
        log.debug( "Next Statement generation start." );
        LocalDate nextBillingDate = calculateNextBillingDate( statementEventVO.getFrequency(), statementEventVO.getBillingDate() );
        log.debug( "Next generated subscriptionmanagement date: {}", nextBillingDate );
        eventScheduler.deleteAccountLedgerJob( statementEventVO.getJobId() );
        paymentMethod.getAccountId().setBillingDate( nextBillingDate );
        paymentMethodRepository.save( paymentMethod );
        eventScheduler
            .scheduleAccountLedgerEvent( Schedule.<StatementEventDetails>builder().start( nextBillingDate.atTime( CommonUtil.convertTimeStringToLocalTime( payorSchedulerTime ) ) )
                                                                                  .repeating( false )
                                                                                  .properties( StatementEventDetails.builder()
                                                                                                                    .paymentMethodId(
                                                                                                                        statementEventVO.getPaymentMethodId() ).build() )
                                                                                  .build() );
        log.debug( "Next Statement generation Finish." );
    }

    public StatementResponseVO statementCommit( UUID accountId, HttpHeaders httpHeaders )
    {
        log.debug( "Evaluate and generate statement." );
        EvaluateStatementResponseVO evaluateStatementResponseVO = evaluateStatement( accountId );
        Statement statementResponse = generateStatement( evaluateStatementResponseVO );
        if( evaluateStatementResponseVO.getPaymentMethodAccountSummaries() != null )
        {
            evaluateStatementResponseVO.getPaymentMethodAccountSummaries().parallelStream().forEach( paymentMethodSummary ->
                paymentMethodSummary.setStatement( statementResponse )
            );
            paymentMethodAccountRepository.saveAll( evaluateStatementResponseVO.getPaymentMethodAccountSummaries() );
        }
        else
        {
            evaluateStatementResponseVO.getSummaries().parallelStream().forEach( summary ->
                summary.setStatement( statementResponse )
            );
            accountSummaryRepository.saveAll( evaluateStatementResponseVO.getSummaries() );
        }
        StatementResponseVO statementResponseVO = ModelMapperUtils.map( statementResponse, StatementResponseVO.class );
        statementResponseVO
            .setAccountId( statementResponse.getPaymentMethod() != null ? statementResponse.getPaymentMethod().getId() : statementResponse.getAccountId().getAccountId() );
        if( evaluateStatementResponseVO.getPaymentMethod() != null )
        {
            try
            {
                collectPayment( httpHeaders, statementResponseVO, evaluateStatementResponseVO.getPaymentMethod() );
                statementResponse.setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
                statementRepository.save( statementResponse );
            }
            catch( Exception exception )
            {
                log.error( "collectPayment generate statement." + exception.getMessage() );
            }
        }
        return statementResponseVO;
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

    @Transactional( readOnly = true )
    public EvaluateStatementResponseVO evaluateStatement( UUID accountId )
    {
        if( accountId == null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Account.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NULL ) ) );
        }
        EvaluateStatementResponseVO evaluateStatementResponseVO;
        Optional<Account> account = accountRepository.findById( accountId );
        if( account.isPresent() )
        {
            evaluateStatementResponseVO = evaluateStatementByAccountId( accountId );
            evaluateStatementResponseVO.setAccount( account.get() );
            return evaluateStatementResponseVO;
        }
        evaluateStatementResponseVO = evaluateStatementByPaymentMethodId( accountId );
        return evaluateStatementResponseVO;
    }

    private Statement generateStatement( EvaluateStatementResponseVO evaluateStatementResponseVO )
    {
        Statement statement = evaluateStatementResponseVO.getStatement();
        if( statement != null )
        {
            statement.setDeactivated( LocalDateTime.now( Clock.systemUTC() ) );
            statementRepository.save( statement );
        }
        BigDecimal balanceAmount = evaluateStatementResponseVO.getBalance().getAmount();
        //As per our story, balance never be negative
        BigDecimal balanceLeft = BigDecimal.ZERO;
        boolean isNetAmtNegative = evaluateStatementResponseVO.getNetAmountDue().compareTo( BigDecimal.ZERO ) < 0;
        if( isNetAmtNegative )
        {
            balanceLeft = balanceAmount.add( evaluateStatementResponseVO.getNetAmountDue().abs() );
        }
        if( evaluateStatementResponseVO.getBalance().getAmount().compareTo( BigDecimal.ZERO ) > 0 )
        {
            if( balanceAmount.compareTo( evaluateStatementResponseVO.getNetAmountDue() ) >= 0 && !isNetAmtNegative )
            {
                balanceLeft = balanceAmount.subtract( evaluateStatementResponseVO.getNetAmountDue() );
            }
            if( balanceAmount.compareTo( evaluateStatementResponseVO.getNetAmountDue() ) < 0 && !isNetAmtNegative )
            {
                balanceLeft = BigDecimal.ZERO;
            }
        }
        Statement statementResponse;
        if( evaluateStatementResponseVO.getPaymentMethod() == null )
        {
            balanceService
                .updateBalance( evaluateStatementResponseVO.getAccount().getAccountId(), null, balanceLeft );
            log.debug( "Updated account balance to {} for {}", NumberFormat.getCurrencyInstance().format( balanceLeft ), evaluateStatementResponseVO.getAccount().getAccountId() );
            statement = new Statement();
            statement.setLocationId( evaluateStatementResponseVO.getAccount().getLocation() );
            statement.setStmtDate( LocalDateTime.now( Clock.systemUTC() ) );
            statement.setTotalAmount( evaluateStatementResponseVO.getStatementAmount() );
            statement.setAccountId( evaluateStatementResponseVO.getAccount() );
            statement.setCreated( LocalDateTime.now() );
            statement.setModified( LocalDateTime.now() );
            statementResponse = statementRepository.save( statement );
            log.debug( "Created new statement on {} for {} for {}", statement.getStmtDate(),
                NumberFormat.getCurrencyInstance().format( evaluateStatementResponseVO.getStatementAmount() ), evaluateStatementResponseVO.getAccount().getAccountId() );
            Summary summary = new Summary();
            summary.setSummaryDate( LocalDateTime.now( Clock.systemUTC() ) );
            summary.setCreated( LocalDateTime.now() );
            summary.setModified( LocalDateTime.now() );
            summary.setStatement( statement );
            summary.setType( Type.None );
            summary.setTransactionType( TransactionType.STATEMENT );
            summary.setAccountId( evaluateStatementResponseVO.getAccount().getAccountId() );
            log.debug( "Created new main account summary on {} for {}", summary.getSummaryDate(), summary.getAccountId() );
            accountSummaryRepository.save( summary );
        }
        else
        {
            balanceService
                .updateBalance( evaluateStatementResponseVO.getPaymentMethod().getAccountId().getAccountId(), evaluateStatementResponseVO.getPaymentMethod().getId(), balanceLeft );
            log.debug( "Updated account balance to {} for {}", NumberFormat.getCurrencyInstance().format( balanceLeft ), evaluateStatementResponseVO.getPaymentMethod().getId() );
            statement = new Statement();
            statement.setLocationId( evaluateStatementResponseVO.getPaymentMethod().getLocationId() );
            statement.setStmtDate( LocalDateTime.now( Clock.systemUTC() ) );
            statement.setTotalAmount( evaluateStatementResponseVO.getStatementAmount() );
            statement.setAccountId( evaluateStatementResponseVO.getPaymentMethod().getAccountId() );
            statement.setPaymentMethod( evaluateStatementResponseVO.getPaymentMethod() );
            statement.setCreated( LocalDateTime.now() );
            statement.setModified( LocalDateTime.now() );
            statementResponse = statementRepository.save( statement );
            log.debug( "Created new statement on {} for {} for {}", statement.getStmtDate(),
                NumberFormat.getCurrencyInstance().format( evaluateStatementResponseVO.getStatementAmount() ), evaluateStatementResponseVO.getPaymentMethod().getId() );
            PaymentMethodAccount paymentMethodAccount = new PaymentMethodAccount();
            paymentMethodAccount.setSummaryDate( LocalDateTime.now( Clock.systemUTC() ) );
            paymentMethodAccount.setModified( LocalDateTime.now( Clock.systemUTC() ) );
            paymentMethodAccount.setStatement( statement );
            paymentMethodAccount.setType( Type.None );
            paymentMethodAccount.setPaymentMethodId( evaluateStatementResponseVO.getPaymentMethod().getId() );
            paymentMethodAccount.setAccountId( evaluateStatementResponseVO.getPaymentMethod().getAccountId().getAccountId() );
            paymentMethodAccount.setCreated( LocalDateTime.now() );
            paymentMethodAccount.setModified( LocalDateTime.now() );
            paymentMethodAccount.setTransactionType( TransactionType.STATEMENT );
            log.debug( "Created new payment method account summary on {} for {}", paymentMethodAccount.getSummaryDate(), paymentMethodAccount.getPaymentMethodId() );
            paymentMethodAccountRepository.save( paymentMethodAccount );
        }
        log.debug( "Evaluate and generate statement End." );
        return statementResponse;
    }

    private void collectPayment( HttpHeaders headers, StatementResponseVO statement, PaymentMethod paymentMethod )
    {
        if( statement.getTotalAmount().compareTo( BigDecimal.ZERO ) > 0 )
        {
            log.debug( "Collect payment start for accountId {}", paymentMethod.getAccountId().getAccountId() );

            PaymentRequestVO paymentRequestVO = new PaymentRequestVO();
            Optional<LocationAccount> clientAccount = clientAccountRepository.findByLocaccIdLocation( paymentMethod.getAccountId().getLocation() );
            paymentRequestVO.setMerchantId( clientAccount.isPresent() ? clientAccount.get().getMerchantId() : null );
            paymentRequestVO.setPaymentMethodId( paymentMethod.getTokenId() );
            paymentRequestVO.setAmount( statement.getTotalAmount() );
            paymentRequestVO.setSource( "BILLING" );
            paymentRequestVO.setReferenceId( String.valueOf( statement.getStatementId() ) );
            PaymentResponseVO paymentResponseVO = httpService.callApi( paymentRequestURL, paymentRequestVO, PaymentResponseVO.class, headers );
            log.debug( "Response from Server for URI {} is {}", paymentRequestURL, paymentResponseVO );
            Payment payment = new Payment();
            payment.setLocationId( paymentMethod.getAccountId().getLocation() );
            payment.setPayAmount( statement.getTotalAmount() );
            payment.setPayStatus( PayStatus.PENDING );
            payment.setAccount( paymentMethod.getAccountId() );
            payment.setPameId( paymentMethod.getId() );
            payment.setInvoiceType( InvoiceTypeEnum.O );
            payment.setStatementId( statement.getStatementId() );
            paymentRepository.save( payment );

            log.info( "Successfully sent payment to gateway for collection and saved payment as pending {} for {}",
                payment.getId(), NumberFormat.getCurrencyInstance().format( payment.getPayAmount() ) );
            statement.setPaymentRequestVO( paymentRequestVO );
            log.debug( "Generate statement finish for accountId {}", paymentMethod.getAccountId().getAccountId() );
        }
        else
        {
            log.info( "Generated statement has a zero balance, no payment to collect on {} for {}", statement.getStatementDate(), statement.getStatementId() );
        }
    }

    private EvaluateStatementResponseVO evaluateStatementByAccountId( UUID accountId )
    {
        Optional<MemberCreation> memberCreation = memberCreationRepository.getDetailsByAccountId( accountId );
        if( !memberCreation.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Account.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + accountId ) );
        }
        log.debug( "Getting main account summaries for account {} and member {}", accountId, memberCreation.get().getMemberId() );
        List<Summary> summaries = accountSummaryRepository.findByAccountIdAndStatementAndSettlementOrderBySummaryDateDesc( accountId, null, null );
        BigDecimal netAmountDue = BigDecimal.ZERO;
        log.debug( "Found {} main account summaries for account {} with member id {}", summaries.size(), accountId, memberCreation.get().getMemberId() );
        for( Summary summary : summaries )
        {
            Invoice invoice = summary.getInvoice();
            Adjustment adjustment = summary.getAdjustment();
            if( invoice != null )
            {
                Optional<Payment> payment = paymentRepository.findByInvoiceId( invoice.getId() );
                if( payment.isPresent() )
                {
                    log.debug( "Payment already exists for invoice #{} and amount {}",
                        invoice.getInvoiceNumber(), NumberFormat.getCurrencyInstance().format( invoice.getTotalAmount() ) );
                    continue;
                }
                netAmountDue = netAmountDue.add( invoice.getTotalAmount() );
                log.debug( "Added invoice {} to net amount due {}", invoice.getInvoiceNumber(), NumberFormat.getCurrencyInstance().format( netAmountDue ) );
            }
            if( adjustment != null )
            {
                netAmountDue = netAmountDue.add( adjustment.getAmount() );
                log.debug( "Added adjustment {} to net amount due {}", adjustment.getAdjustmentId(), NumberFormat.getCurrencyInstance().format( adjustment.getAmount() ) );
            }
        }
        Statement statement = statementRepository.findStatementByAccountIdAccountIdAndDeactivatedAndPaymentMethodNull( accountId, null );
        if( statement != null && statement.getTotalAmount().compareTo( BigDecimal.ZERO ) >= 0 )
        {
            netAmountDue = netAmountDue.add( statement.getTotalAmount() );
        }
        BigDecimal newStatementAmount = netAmountDue;
        log.debug( "net Amount due {},newStatementAmount {} ", netAmountDue, newStatementAmount );
        Balance balance = balanceRepository.findByAccountIdAndDeactivatedAndPaymentMethodIdNull( accountId, null );
        if( balance == null )
        {
            balance = balanceService.createBalance( accountId, BigDecimal.ZERO );
        }
        newStatementAmount = netAmountDue.subtract( balance.getAmount() );

        EvaluateStatementResponseVO evaluateStatementResponseVO = new EvaluateStatementResponseVO();
        evaluateStatementResponseVO.setStatementAmount( newStatementAmount );
        evaluateStatementResponseVO.setBalance( balance );
        evaluateStatementResponseVO.setStatement( statement );
        evaluateStatementResponseVO.setPaymentMethod( null );
        evaluateStatementResponseVO.setNetAmountDue( netAmountDue );
        evaluateStatementResponseVO.setSummaries( summaries );
        return evaluateStatementResponseVO;
    }

    private EvaluateStatementResponseVO evaluateStatementByPaymentMethodId( UUID paymentMethodId )
    {
        Optional<PaymentMethod> paymentMethodOptional = paymentMethodRepository.findPaymentMethodByIdAndActive( paymentMethodId, Boolean.TRUE );
        if( !paymentMethodOptional.isPresent() )
        {
            throw new ErrorResponse( new ValidationError( HttpStatus.BAD_REQUEST.toString(), "", MessageUtils.ERROR_MESSAGE_PAYMENT_METHOD_NOT_FOUND ) );
        }
        UUID mainAccountId = paymentMethodOptional.get().getAccountId().getAccountId();
        Optional<MemberCreation> memberCreation = memberCreationRepository.getDetailsByAccountId( mainAccountId );
        if( !memberCreation.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Account.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + mainAccountId ) );
        }
        log.debug( "Getting payment method account summaries for account {} and member {}", paymentMethodId, memberCreation.get().getMemberId() );
        List<PaymentMethodAccount> paymentMethodAccountSummaries =
            paymentMethodAccountRepository.findByPaymentMethodIdAndStatementAndSettlementOrderBySummaryDateDesc( paymentMethodId, null, null );
        BigDecimal netAmountDue = BigDecimal.ZERO;
        log.debug( "Found {} main account summaries for account {} with member id {}", paymentMethodAccountSummaries.size(), paymentMethodId, memberCreation.get().getMemberId() );
        for( PaymentMethodAccount summary : paymentMethodAccountSummaries )
        {
            Invoice invoice = summary.getInvoice();
            Adjustment adjustment = summary.getAdjustment();
            if( invoice != null )
            {
                Optional<Payment> payment = paymentRepository.findByInvoiceId( invoice.getId() );
                if( payment.isPresent() )
                {
                    log.debug( "Payment already exists for invoice #{} and amount {}",
                        invoice.getInvoiceNumber(), NumberFormat.getCurrencyInstance().format( invoice.getTotalAmount() ) );
                    continue;
                }
                netAmountDue = netAmountDue.add( invoice.getTotalAmount() );
                log.debug( "Added invoice {} to net amount due {}", invoice.getInvoiceNumber(), NumberFormat.getCurrencyInstance().format( netAmountDue ) );
            }
            if( adjustment != null )
            {
                netAmountDue = netAmountDue.add( adjustment.getAmount() );
                log.debug( "Added adjustment {} to net amount due {}", adjustment.getAdjustmentId(), NumberFormat.getCurrencyInstance().format( adjustment.getAmount() ) );
            }
        }
        Statement statement = statementRepository.findStatementByPaymentMethodIdAndDeactivated( paymentMethodId, null );
        if( statement != null && statement.getTotalAmount().compareTo( BigDecimal.ZERO ) >= 0 )
        {
            netAmountDue = netAmountDue.add( statement.getTotalAmount() );
        }
        BigDecimal newstatementAmount = netAmountDue;

        log.debug( "net Amount due {},newStatementAmount {} ", netAmountDue, newstatementAmount );
        Balance balance = balanceRepository.findByPaymentMethodIdAndDeactivated( paymentMethodId, null );
        if( balance == null )
        {
            balance = balanceService.createPaymentMethodBalance( mainAccountId, paymentMethodId, BigDecimal.ZERO );
        }
        newstatementAmount = netAmountDue.subtract( balance.getAmount() );

        EvaluateStatementResponseVO evaluateStatementResponseVO = new EvaluateStatementResponseVO();
        evaluateStatementResponseVO.setStatementAmount( newstatementAmount );
        evaluateStatementResponseVO.setBalance( balance );
        evaluateStatementResponseVO.setStatement( statement );
        evaluateStatementResponseVO.setPaymentMethod( paymentMethodOptional.get() );
        evaluateStatementResponseVO.setNetAmountDue( netAmountDue );
        evaluateStatementResponseVO.setPaymentMethodAccountSummaries( paymentMethodAccountSummaries );
        return evaluateStatementResponseVO;
    }
}
