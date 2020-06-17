package com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer;

import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionCancel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service

@RequiredArgsConstructor

public class SubscriptionCancelGenerator
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    public void send( SubscriptionCancel subscriptionCancel )
    {
        log.debug( "Sending data in Subscription cancel event {}", subscriptionCancel );
        //Added condition as per P3-2259
        kafkaOperations.send( "subscription-cancelled", subscriptionCancel );
    }
}
