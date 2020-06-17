package com.abcfinancial.api.billing.generalledger.kafka.consumer;

import com.abcfinancial.api.billing.generalledger.payment.service.PaymentService;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentQueuedVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service

public class PaymentScheduledListner
{
    @Autowired
    private PaymentService paymentService;

    @KafkaListener( topics = "payment-queued" )
    public void paymentScheduledListner( @Payload PaymentQueuedVO paymentQueuedVO ) throws IOException, InterruptedException
    {
        log.trace( "sending paymentScheduleVO  = '{}'" + paymentQueuedVO );
        paymentService.updatePayment( paymentQueuedVO );
    }
}
