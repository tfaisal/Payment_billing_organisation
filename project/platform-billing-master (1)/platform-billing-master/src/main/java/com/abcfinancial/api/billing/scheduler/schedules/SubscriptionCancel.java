package com.abcfinancial.api.billing.scheduler.schedules;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@JsonInclude( JsonInclude.Include.NON_NULL )

public class SubscriptionCancel
{
    private List<UUID> memberIdList;
    private UUID subscriptionId;
    private UUID scheduleInvoicesId;
    private LocalDate subCancelDate;
}
