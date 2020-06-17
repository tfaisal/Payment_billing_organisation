package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class SubscriptionDue {

    /**
     * Remaining invoice Count Due
     * It should be int format
     */
    @NotNull
    private int invoiceCountDue;
    /**
     * Remaining invoice Amount Due
     * Format BigDecimal
     */
    @NotNull
    private BigDecimal invoiceAmountDue;
}
