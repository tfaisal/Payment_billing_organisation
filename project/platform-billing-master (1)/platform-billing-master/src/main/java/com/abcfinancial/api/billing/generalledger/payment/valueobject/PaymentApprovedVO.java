package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )
@NoArgsConstructor
@AllArgsConstructor

public class PaymentApprovedVO
{
    private UUID merchantId;
    private UUID paymentMethodId;
    private BigDecimal amount;
    private String source;
    private String referenceId;
    private String paymentStatus;
}
