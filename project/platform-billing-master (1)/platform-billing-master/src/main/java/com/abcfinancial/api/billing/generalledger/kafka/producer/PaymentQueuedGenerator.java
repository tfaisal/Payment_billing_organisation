package com.abcfinancial.api.billing.generalledger.kafka.producer;

import com.abcfinancial.api.billing.generalledger.statements.produce.StatementProduce;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentQueuedVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service

@RequiredArgsConstructor

public class PaymentQueuedGenerator
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    public void paymentQueuedSend( PaymentVO paymentVO, StatementProduce statementProduce )
    {
        PaymentQueuedVO paymentQueuedVOProduce = new PaymentQueuedVO( );
        paymentQueuedVOProduce.setLocationId( paymentVO.getLocationId( ) );
        paymentQueuedVOProduce.setPaymentId( paymentVO.getId( ) );
        paymentQueuedVOProduce.setAccountId( paymentVO.getAccount( ).getAccountId( ) );
        paymentQueuedVOProduce.setTotalAmount( paymentVO.getPayAmount( ) );
        paymentQueuedVOProduce.setInvoiceId( statementProduce.getInvoiceId( ) );
        kafkaOperations.send( "payment-queued", paymentQueuedVOProduce );
    }
}
