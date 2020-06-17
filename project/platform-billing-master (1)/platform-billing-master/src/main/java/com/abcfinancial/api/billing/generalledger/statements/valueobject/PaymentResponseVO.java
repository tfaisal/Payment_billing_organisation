package com.abcfinancial.api.billing.generalledger.statements.valueobject;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@JsonPropertyOrder( { "id", "merchantId", "requested", "status", "paymentMethodId", "amount", "source", "referenceId", "metadata" } )

public class PaymentResponseVO
{
    private UUID id;
    private UUID merchantId;
    private String requested;
    private String status;
    private UUID paymentMethodId;
    private BigDecimal amount;
    private String source;
    private String referenceId;
}



