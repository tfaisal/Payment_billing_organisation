package com.abcfinancial.api.billing.scheduler.jobs;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.generalledger.kafka.producer.StatementGenerator;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.statements.enums.EventType;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.StatementEventVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@ToString
@DisallowConcurrentExecution
@PersistJobDataAfterExecution

public class AccountLedgerEventJob extends QuartzJobBean
{
    private UUID paymentMethodId;
    private BigDecimal netBalanceDue;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private StatementGenerator statementGenerator;

    @Override
    protected void executeInternal( JobExecutionContext context )
    {
        log.debug( "General Ledger Event scheduler start for payment method {}", getPaymentMethodId( ) );
        Optional<PaymentMethod> paymentMethodOptional = paymentMethodRepository.findById( paymentMethodId );
        paymentMethodOptional.ifPresent( paymentMethod -> {
            log.debug( "Payment method found: " + paymentMethod.getId( ) );
            Account account = paymentMethod.getAccountId( );
            StatementEventVO statementEventVO = new StatementEventVO( );
            statementEventVO.setEventType( EventType.STATEMENT );
            statementEventVO.setBillingDate( account.getBillingDate( ) );
            statementEventVO.setFrequency( Enum.valueOf( Frequency.class, account.getSevaluation( ) ) );
            statementEventVO.setPaymentMethodId( paymentMethod.getId( ) );
            statementEventVO.setPostDate( account.getBillingDate( ) );
            statementEventVO.setAmount( getNetBalanceDue( ) );
            statementEventVO.setJobId( UUID.fromString( context.getJobDetail( ).getKey( ).getName( ) ) );
            statementGenerator.statementEvent( statementEventVO );
        } );
        log.debug( "General Ledger Event scheduler stop for payment method {}", getPaymentMethodId( ) );
    }
}
