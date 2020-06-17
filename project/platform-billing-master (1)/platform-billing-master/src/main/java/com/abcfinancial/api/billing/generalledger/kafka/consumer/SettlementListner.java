package com.abcfinancial.api.billing.generalledger.kafka.consumer;

import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.settlement.service.SettlementService;
import com.abcfinancial.api.billing.generalledger.settlement.valueobject.SettlementEventVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service

public class SettlementListner
{
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private SettlementService settlementService;

    @KafkaListener( topics = "settlement-event-created" )
    public void consumeSettlementEvent( SettlementEventVO settlementEventVO )
    {
        log.debug( "settlement-event-created settlement Vo Data{}", settlementEventVO );
        settlementService.generateSettlementNext( settlementEventVO );
    }
}
