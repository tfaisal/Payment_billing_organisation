package com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SubscriptionListDue
{

    /**
     * Subscription Id involved in an agreement should be UUID.
     */
    @NotNull
    private UUID subscriptionId;

    /**
     * Remaining invoice Count Due
     * It should be int format
     */
    @JsonInclude( JsonInclude.Include.NON_NULL )
    private int invoiceCountDue;
    /**
     * Remaining invoice Amount Due
     * Format BigDecimal
     */
    @JsonInclude( JsonInclude.Include.NON_NULL )
    private BigDecimal invoiceAmountDue;

    /**
     * Message for inappropriate request
     */
    @JsonInclude( JsonInclude.Include.NON_NULL )
    private String message;
}
