package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_DEFAULT )

public class FreezeSubscriptionVO
{
    /**
     * Subscription Id
     */

    private UUID id;
    /**
     * Renew Subscription Id
     */

    private UUID renewSubId;
    /**
     * start date time of freeze subscription
     */

    @JsonSerialize
    @JsonDeserialize
    @Future
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @NotNull( message = "freezeStartDate must not be null" )
    private LocalDate freezeStartDate;
    /**
     * end date time of freeze subscription.
     */

    @JsonSerialize
    @JsonDeserialize
    @Future
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @NotNull( message = "freezeEndDate must not be null" )
    private LocalDate freezeEndDate;
    /**
     * freezeAmount is the small amount that will be charge to customer while the subscription in freeze start
     */

    private BigDecimal freezeAmount;
    /**
     * end date time of freeze subscription.
     */

    @JsonSerialize
    @JsonDeserialize
    @Future
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @NotNull( message = "subExpirationDate must not be null" )
    private LocalDate subExpirationDate;
    /**
     * start date time for renew the Subscription
     */

    @JsonSerialize
    @JsonDeserialize
    @Future
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    private LocalDate renewStartDate;
    /**
     * end date time for renew the Subscription
     */

    @JsonSerialize
    @JsonDeserialize
    @Future
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    private LocalDate renewExpirationDate;
    /**
     * invoice date time  for renew the Subscription
     */

    @JsonSerialize
    @JsonDeserialize
    @Future
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    private LocalDate renewInvoiceDate;

}
