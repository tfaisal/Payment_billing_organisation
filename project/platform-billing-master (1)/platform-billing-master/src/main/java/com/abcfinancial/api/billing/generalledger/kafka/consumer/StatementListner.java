package com.abcfinancial.api.billing.generalledger.kafka.consumer;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PayStatus;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentHistoryVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentVO;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.payment.service.PaymentService;
import com.abcfinancial.api.billing.generalledger.statements.produce.StatementProduce;
import com.abcfinancial.api.billing.generalledger.statements.service.StatementService;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.StatementEventVO;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Slf4j
@Service

public class StatementListner
{
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private StatementService statementService;

    @KafkaListener( topics = "statement-created" )
    public void statementReceive( @Payload StatementProduce statement )
    {
        log.trace( "receiving statement  = '{}'" + statement );
        PaymentVO paymentVO = new PaymentVO();
        Account account = accountRepository.findById( statement.getAccountId() )
                                           .orElseThrow( () -> new EntityNotFoundException( "requested account detail doesn't exist" ) );
        PaymentMethod paymentMethod = paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( account.getAccountId(), Boolean.TRUE );
        AccountVO accountVO = ModelMapperUtils.map( account, AccountVO.class );
        paymentVO.setAccount( accountVO );
        paymentVO.setLocationId( statement.getLocationId( ) );
        paymentVO.setPayStatus( PayStatus.SCHEDULED );
        paymentVO.setPayAmount( statement.getTotalAmount( ) );
        paymentVO.setPameId( paymentMethod.getId( ) );
        log.debug( "Found account {} and payment method {} from statement {}, generating payment", accountVO.getAccountId( ), paymentMethod.getId( ), statement.getStatementId( ) );
        paymentVO = paymentService.savePayment( paymentVO, statement );
        ModelMapperUtils.map( paymentVO, PaymentHistoryVO.class );
    }

    @KafkaListener( topics = "statement-event-created" )
    public void consumeStatementEvent( StatementEventVO statementEventVO )
    {
        statementService.statementEventStart( statementEventVO );
    }
}
