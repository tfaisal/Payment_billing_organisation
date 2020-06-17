package com.abcfinancial.api.billing.subscriptionmanagement.kafka.consumer;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.SubscriptionService;
import com.google.i18n.phonenumbers.NumberParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service

public class SubscriptionAutoRenewListner
{
    @Autowired
    private SubscriptionService subscriptionService;

    @KafkaListener( topics = "subscription-auto-renew" )
    public void receiveSubscriptionDetails( @Payload Subscription subscription ) throws IOException, NumberParseException, CloneNotSupportedException
    {
        log.trace( "subscription-auto-renew - receiving subscription details  = '{}'" + subscription );
        subscriptionService.createRenewSubscription( subscription );
    }
}
