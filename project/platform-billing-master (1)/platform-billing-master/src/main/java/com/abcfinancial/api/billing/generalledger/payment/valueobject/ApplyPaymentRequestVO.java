package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class ApplyPaymentRequestVO
{
    /**
     * Invoice Id must be in java.util.UUID format.
     */

    private UUID invoiceId;
    /**
     * Pay Amount.
     */

    private BigDecimal payAmount;
    /**
     * statementId must be in java.util.UUID format.
     */

    private UUID statementId;
}
