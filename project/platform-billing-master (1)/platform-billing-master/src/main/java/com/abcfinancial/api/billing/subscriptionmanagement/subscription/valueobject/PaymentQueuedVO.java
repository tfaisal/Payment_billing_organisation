package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

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

public class PaymentQueuedVO
{
    private UUID id;
    private UUID locationId;
    private UUID paymentId;
    private UUID accountId;
    private BigDecimal totalAmount;
    private UUID invoiceId;
}
