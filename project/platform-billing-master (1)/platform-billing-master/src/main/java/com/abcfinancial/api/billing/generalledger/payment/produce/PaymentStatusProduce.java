package com.abcfinancial.api.billing.generalledger.payment.produce;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data

public class PaymentStatusProduce
{
    private UUID merchantId;
    private UUID paymentMethodId;
    private BigDecimal amount;
    private String source;
    private String referenceId;
    private String paymentStatus;
}
