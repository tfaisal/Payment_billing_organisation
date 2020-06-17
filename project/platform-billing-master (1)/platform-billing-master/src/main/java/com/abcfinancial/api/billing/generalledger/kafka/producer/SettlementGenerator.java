package com.abcfinancial.api.billing.generalledger.kafka.producer;

import com.abcfinancial.api.billing.generalledger.settlement.valueobject.SettlementEventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service

@RequiredArgsConstructor

public class SettlementGenerator
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    public void settlementEvent( SettlementEventVO settlementEventVO )
    {
        log.debug( "Settlement Vo Data {}", settlementEventVO );
        kafkaOperations.send( "settlement-event-created", settlementEventVO );
    }
}
