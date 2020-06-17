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
import java.time.LocalDateTime;
import java.util.UUID;

@Data

public class FreezeSubscriptionResponseVo extends SubscriptionVO
{
    /**
     * original subscription id in freeze period
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID freezeSubId;
    /**
     * original subscription id in clone subscription
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID subRefferalId;

    /**
     * start date time of freeze subscription
     */

    @JsonSerialize
    @JsonDeserialize
    @Future
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME )
    @JsonFormat( pattern = "MM-dd-yyyy HH:mm" )
    @NotNull( message = "freezeStartDate must not be null" )
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private LocalDateTime freezeStartDate;
    /**
     * end date time of freeze subscription
     */

    @JsonSerialize
    @JsonDeserialize
    @Future
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME )
    @JsonFormat( pattern = "MM-dd-yyyy HH:mm" )
    @NotNull( message = "freezeEndtDate must not be null" )
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private LocalDateTime freezeEndDate;
    /**
     * freeze denotes if subscription is freeze
     */

    private boolean freeze;
    /**
     * freezeAmount is the small amount that will be charge to customer while the subscription in freeze start
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private BigDecimal freezeAmount;
}
