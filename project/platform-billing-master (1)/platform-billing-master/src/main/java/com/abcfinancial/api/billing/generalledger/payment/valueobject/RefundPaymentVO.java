package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import lombok.Data;

import java.math.BigDecimal;

@Data

public class RefundPaymentVO
{
    /**
     * The amount to be refund at Dimebox.
     */

    private BigDecimal amount;
}
