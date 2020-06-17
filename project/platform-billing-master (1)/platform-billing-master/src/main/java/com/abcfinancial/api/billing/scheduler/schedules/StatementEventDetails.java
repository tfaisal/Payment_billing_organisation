package com.abcfinancial.api.billing.scheduler.schedules;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder

public class StatementEventDetails
{
    private UUID paymentMethodId;
    private BigDecimal netBalanceDue;
}
