package com.abcfinancial.api.billing.scheduler.schedules;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@JsonInclude( JsonInclude.Include.NON_NULL )

public class SubscriptionExpired
{
    private UUID locationId;
    private UUID accountId;
    private UUID subscriptionId;
    private LocalDate subExpDate;
    private boolean freezeSubscriptionRequest;
}
