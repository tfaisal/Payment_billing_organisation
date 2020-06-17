package com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public final class SubscriptionCreatedEvent
{
    private UUID subId;
    private UUID planId;
    private List<UUID> memberIds;
    private UUID accountId;
    private UUID locationId;
    private String name;
    private long planVersion;
    @JsonFormat( pattern = "MM-dd-yyyy" )
    private LocalDate start;
    @JsonFormat( pattern = "MM-dd-yyyy" )
    private LocalDate expirationDate;
    private Frequency frequency;
    private Integer duration;
    private List<SubscriptionItem> items;
    private UUID salesEmployeeId;
    private BigDecimal totalTax;
    private BigDecimal totalAmount;
    private BigDecimal totalNetPrice;
}
