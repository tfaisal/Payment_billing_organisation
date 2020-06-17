package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data

public class CancelSubscriptionVO extends SubscriptionVO
{

    /**
     * subscription cancellation date
     */

    @NotNull( message = "Subscription cancellation Date must not be blank or null" )
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonProperty( "subCancellationDate" )
    private LocalDateTime subCancellationDate;

}
