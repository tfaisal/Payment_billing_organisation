package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class SubscriptionExpireVO
{
    /**
     * expired status will return true if subscription expired
     */

    Boolean expired;
    /**
     * Subscription Id
     */

    private UUID subId;
    /**
     * Location id
     */

    private UUID locationId;
    /**
     * Member Id
     */

    private UUID memberId;
    /**
     * expirationDate
     */

    @NotNull( message = "expirationDate must not be null" )
    @FutureOrPresent
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    private LocalDate expirationDate;
}
