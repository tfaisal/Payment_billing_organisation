package com.abcfinancial.api.billing.scheduler.jobs;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.generalledger.kafka.producer.SettlementGenerator;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.settlement.valueobject.SettlementEventVO;
import com.abcfinancial.api.billing.generalledger.statements.enums.EventType;
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
    
public class SettlementEventJob extends QuartzJobBean
    {
        private UUID paymentMethodId;
        private BigDecimal netBalanceDue;
        @Autowired
        private PaymentMethodRepository paymentMethodRepository;
        @Autowired
        private SettlementGenerator settlementGenerator;

        @Override
        protected void executeInternal( JobExecutionContext context )
        {
            log.debug( "General Ledger Event scheduler start for payment method {}", getPaymentMethodId( ) );
            Optional<PaymentMethod> paymentMethodOptional = paymentMethodRepository.findById( paymentMethodId );
            paymentMethodOptional.ifPresent( paymentMethod -> {
                log.debug( "Payment method found: " + paymentMethod.getId( ) );
                Account account = paymentMethod.getAccountId( );
                SettlementEventVO accountLedgerEventVO = new SettlementEventVO( );
                accountLedgerEventVO.setEventType( EventType.SETTLEMENT );
                accountLedgerEventVO.setBillingDate( account.getBillingDate( ) );
                accountLedgerEventVO.setFrequency( Enum.valueOf( Frequency.class, account.getSevaluation( ) ) );
                accountLedgerEventVO.setPaymentMethodId( paymentMethod.getId( ) );
                accountLedgerEventVO.setPostDate( account.getBillingDate( ) );
                accountLedgerEventVO.setNetBalanceDue( getNetBalanceDue( ) );
                accountLedgerEventVO.setJobId( UUID.fromString( context.getJobDetail( ).getKey( ).getName( ) ) );
                settlementGenerator.settlementEvent( accountLedgerEventVO );
            } );
            log.debug( "General Ledger Event scheduler stop for payment method {}", getPaymentMethodId( ) );
        }
    }

