package com.abcfinancial.api.billing.generalledger.kafka.producer;

import com.abcfinancial.api.billing.generalledger.payment.produce.PaymentStatusProduce;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.PaymentRequestVO;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service

@RequiredArgsConstructor

@Component

public class PaymentStatusGenerator
{
    @Autowired
    private final KafkaOperations<String, Object> kafkaOperations;

    @Transactional
    public void send( PaymentRequestVO paymentRequestVO )
    {
        log.debug( "PaymentRequestVO Data{}", paymentRequestVO );
        PaymentStatusProduce paymentProduce = ModelMapperUtils.map( paymentRequestVO, PaymentStatusProduce.class );
        paymentProduce.setPaymentStatus( MessageUtils.SUCCESS );
        log.debug( "sending payment status  = '{}'", paymentProduce.toString( ) );
        kafkaOperations.send( "payment-status", paymentProduce );
    }
}
