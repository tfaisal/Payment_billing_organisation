package com.abcfinancial.api.billing.generalledger.kafka.consumer;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.SubscriptionService;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentDeclinedVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.SubscriptionCancelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
 
@Slf4j
@Service

public class PaymentProcessEventListener {
     @Autowired
     private SubscriptionService subscriptionService;

     @KafkaListener( topics = "payment-declined" )
      public void paymentDeclinedScheduledListener( @Payload PaymentDeclinedVO paymentDeclinedVO ) {
        UUID subscriptionId = paymentDeclinedVO.getSubscriptionId( );
        log.debug( "Decline Payment Cancel Subscription with : " + subscriptionId + " id " );
         SubscriptionCancelVO cancellationVO = new SubscriptionCancelVO( );
         cancellationVO.setSubCancellationDate( LocalDate.now( Clock.systemUTC( ) ) );
        subscriptionService.cancelSubscription( subscriptionId, cancellationVO );
    }
}
