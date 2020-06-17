package com.abcfinancial.api.billing.scheduler.schedules;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder

public class SubscriptionDetails
{
    private UUID locationId;
    private UUID subscriptionId;
    private List<UUID> memberIdList;
    private BigDecimal freezeAmount;
    private boolean isPameIdAccount;
}
