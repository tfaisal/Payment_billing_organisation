package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.RenewType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data

public class RenewalOptionsVO
{
    /**
     * Renew Date will be the Invoice Date for Renew Subscription
     */

    @NotNull
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private LocalDate renewDate;
    /**
     * Renew expirationDate Mandatory only when
     * Renew Type is "TERM"
     */

    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private LocalDate renewExpireDate;
    /**
     * Renew Type[Open, Term]
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private RenewType renewType;
    /**
     * Subscription renewal duration is mandatory only when
     * subscription type is TERM
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private int renewDuration;
    /**
     * Frequency of renew subscription
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    @NotNull
    private Frequency renewFrequency;
    /**
     * Renew Subscription ID
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID renewSubId;
    /**
     * Renew Subscription Amount
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    @NotNull
    private BigDecimal renewAmount;
    /**
     * Renew Subscription invoice date
     */

    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    @NotNull
    private LocalDate renewInvoiceDate;
}
