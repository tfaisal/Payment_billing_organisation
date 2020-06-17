package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@JsonInclude( JsonInclude.Include.NON_DEFAULT )

public class SubscriptionCancelVO
{
    /**
     * subscription cancellation date
     */

    @NotNull( message = "Subscription cancellation Date must not be blank or null" )
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @JsonProperty( "subCancellationDate" )
    private LocalDate subCancellationDate;
}
