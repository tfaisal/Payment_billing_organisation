package com.abcfinancial.api.billing.generalledger.kafka.producer;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentDeclinedVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service

@RequiredArgsConstructor

public class PaymentProcessEvent
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    public void paymentApprovedSender( PaymentVO paymentVO )
    {
        log.debug( "sending payment approved  = '{}'", paymentVO.toString( ) );
        kafkaOperations.send( "payment-approved", paymentVO.getId( ).toString( ), paymentVO );
    }

    public void paymentDeclinedSender( PaymentDeclinedVO paymentDeclinedVO )
    {
        log.debug( "sending payment declined  = '{}'", paymentDeclinedVO.toString( ) );
        kafkaOperations.send( "payment-declined", paymentDeclinedVO.getMemberId( ).toString( ), paymentDeclinedVO );
    }
}
