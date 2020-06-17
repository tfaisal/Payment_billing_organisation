package com.abcfinancial.api.billing.generalledger.statements.valueobject;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@JsonDeserialize( builder = PaymentRequestVO.PaymentRequestVOBuilder.class )
@JsonPropertyOrder( { "merchantId", "paymentMethodId", "amount", "source", "referenceId", "metadata" } )

public class PaymentRequestVO
{
    private UUID merchantId;
    private UUID paymentMethodId;
    private BigDecimal amount;
    private String source;
    private String referenceId;
    private Map<String, String> metadata;

    @JsonPOJOBuilder( withPrefix = "" )
    public static class PaymentRequestVOBuilder
    {
        private UUID id = UUID.randomUUID( );
        private Map<String, String> metadata = new HashMap<>( );
    }
}



