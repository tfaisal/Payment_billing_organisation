package com.abcfinancial.api.billing.generalledger.kafka.producer;

import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service

@RequiredArgsConstructor

public class InvoiceGenerator
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    public void send( Invoice invoice )
    {
        log.debug( "Created invoice event {}", invoice );
        kafkaOperations.send( "invoice-created", invoice );
    }
}
