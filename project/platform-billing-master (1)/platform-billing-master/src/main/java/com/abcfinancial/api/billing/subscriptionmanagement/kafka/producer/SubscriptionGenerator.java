package com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.SubscriptionCreatedEvent;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.SubscriptionVO;
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

public class SubscriptionGenerator
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    public void send( Subscription subscription, SubscriptionVO subscriptionVO )
    {
        SubscriptionCreatedEvent subscriptionCreatedEvent = new SubscriptionCreatedEvent();
        List<UUID> memberIds = new ArrayList<UUID>();
        if( null != subscriptionVO.getMemberIdList() )
        {
            for( UUID memberId : subscriptionVO.getMemberIdList() )
            {
                memberIds.add( memberId );
            }
        }
        subscriptionCreatedEvent.setSubId( subscription.getSubId() );
        subscriptionCreatedEvent.setPlanVersion( subscription.getPlanVersion() );
        subscriptionCreatedEvent.setAccountId( subscription.getAccount().getAccountId() );
        subscriptionCreatedEvent.setDuration( subscription.getDuration() );
        subscriptionCreatedEvent.setFrequency( subscription.getFrequency() );
        subscriptionCreatedEvent.setItems( subscription.getItems() );
        subscriptionCreatedEvent.setExpirationDate( subscription.getExpirationDate() );
        subscriptionCreatedEvent.setMemberIds( memberIds );
        subscriptionCreatedEvent.setName( subscription.getName() );
        subscriptionCreatedEvent.setLocationId( subscription.getLocationId() );
        subscriptionCreatedEvent.setSalesEmployeeId( subscription.getSalesEmployeeId() );
        subscriptionCreatedEvent.setTotalTax( subscription.getTotalTax() );
        subscriptionCreatedEvent.setTotalNetPrice( subscription.getTotalNetPrice() );
        subscriptionCreatedEvent.setStart( subscription.getStart() );
        subscriptionCreatedEvent.setPlanId( subscription.getPlanId() );
        subscriptionCreatedEvent.setLocationId( subscription.getLocationId() );
        subscriptionCreatedEvent.setTotalAmount( subscription.getTotalAmount() );
        log.debug( "Sending subscription data on Kafka {} ", subscriptionCreatedEvent );
        kafkaOperations.send( "subscription-created", subscriptionCreatedEvent );
    }
}
