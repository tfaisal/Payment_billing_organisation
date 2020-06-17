package com.abcfinancial.api.billing.generalledger.statements.valueobject;

import com.abcfinancial.api.billing.generalledger.statements.enums.EventType;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data

public class StatementEventVO
{
    private EventType eventType;
    private LocalDate billingDate;
    private Frequency frequency;
    private UUID paymentMethodId;
    private LocalDate postDate;
    private BigDecimal amount;
    private UUID jobId;
}
