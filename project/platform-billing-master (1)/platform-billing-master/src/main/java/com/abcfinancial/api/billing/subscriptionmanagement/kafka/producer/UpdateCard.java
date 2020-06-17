package com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer;

import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.CardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service

@RequiredArgsConstructor

public class UpdateCard
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    public void updateCardSender( CardResponse cardResponse )
    {
        log.debug( "sending update Card  = '{}'", cardResponse.toString() );
        kafkaOperations.send( "card-update", cardResponse.getId(), cardResponse );
    }
}
