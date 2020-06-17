package com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer;

import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionExpired;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service

@RequiredArgsConstructor

public class SubscriptionExpireGenerator
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    public void send( SubscriptionExpired subscriptionExpired )
    {
        log.debug( "Sending data in Subscription expire event {}", subscriptionExpired );
        kafkaOperations.send( "subscription-expired", subscriptionExpired );
    }
}
