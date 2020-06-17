package com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service

@RequiredArgsConstructor

public class SubscriptionAutoRenewGenerator
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    public void send( Subscription subscription )
    {
        log.debug( "sending subscription details for Auto Renew  = '{}'", subscription );
        kafkaOperations.send( "subscription-auto-renew", subscription.getSubId().toString(), subscription );
    }
}
