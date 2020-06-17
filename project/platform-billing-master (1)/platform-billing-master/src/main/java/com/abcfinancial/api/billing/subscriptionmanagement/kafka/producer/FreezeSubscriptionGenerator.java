package com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.MemberSubscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.SubscriptionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service

@RequiredArgsConstructor

public class FreezeSubscriptionGenerator
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    public void send( Subscription subscription )
    {
        SubscriptionCreatedEvent subscriptionCreatedEvent = new SubscriptionCreatedEvent();
        List<UUID> memberIds = new ArrayList<UUID>();

        for( MemberSubscription memberSubscription : subscription.getMemberSubscriptionList() )
        {
            memberIds.add( memberSubscription.getId().getMemId() );
        }
        subscriptionCreatedEvent.setSubId( subscription.getSubId() );
        subscriptionCreatedEvent.setAccountId( subscription.getAccount().getAccountId() );
        subscriptionCreatedEvent.setDuration( subscription.getDuration() );
        subscriptionCreatedEvent.setExpirationDate( subscription.getExpirationDate() );
        subscriptionCreatedEvent.setFrequency( subscription.getFrequency() );
        subscriptionCreatedEvent.setItems( subscription.getItems() );
        subscriptionCreatedEvent.setMemberIds( memberIds );
        subscriptionCreatedEvent.setName( subscription.getName() );
        subscriptionCreatedEvent.setLocationId( subscription.getLocationId() );
        subscriptionCreatedEvent.setPlanVersion( subscription.getPlanVersion() );
        subscriptionCreatedEvent.setSalesEmployeeId( subscription.getSalesEmployeeId() );
        subscriptionCreatedEvent.setTotalTax( subscription.getTotalTax() );
        subscriptionCreatedEvent.setTotalNetPrice( subscription.getTotalNetPrice() );
        subscriptionCreatedEvent.setStart( subscription.getStart() );
        subscriptionCreatedEvent.setPlanId( subscription.getPlanId() );
        subscriptionCreatedEvent.setLocationId( subscription.getLocationId() );
        subscriptionCreatedEvent.setTotalAmount( subscription.getTotalAmount() );
        log.debug( "Sending freeze subscription data on Kafka {} ", subscriptionCreatedEvent );
        kafkaOperations.send( "freeze-subscription-created", subscriptionCreatedEvent );
    }
}
