package com.abcfinancial.api.billing.generalledger.kafka.producer;

import com.abcfinancial.api.billing.generalledger.statements.produce.StatementProduce;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.StatementEventVO;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.StatementVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service

@RequiredArgsConstructor

public class StatementGenerator
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    public void send( StatementVo statementVo )
    {
        log.trace( "Statement Vo Data {}", statementVo );
        StatementProduce statementProduce = new StatementProduce( );
        statementProduce.setAccountId( statementVo.getAccountId( ).getAccountId( ) );
        statementProduce.setInvoiceId( statementVo.getInvoiceId( ).getId( ) );
        statementProduce.setLocationId( statementVo.getLocationId( ) );
        statementProduce.setStmdDate( statementVo.getStatementDate( ) );
        statementProduce.setTotalAmount( statementVo.getTotalAmount( ) );
        statementProduce.setStatementId( statementVo.getStatementId( ) );
        kafkaOperations.send( "statement-created", statementProduce );
    }

    public void statementEvent( StatementEventVO statementEventVO )
    {
        log.debug( "Statement Vo Data {}", statementEventVO );
        kafkaOperations.send( "statement-event-created", statementEventVO );
    }
}
