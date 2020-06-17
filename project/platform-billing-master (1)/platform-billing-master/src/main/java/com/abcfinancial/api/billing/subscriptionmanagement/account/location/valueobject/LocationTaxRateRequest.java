package com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@JsonIgnoreProperties( ignoreUnknown = true )

public class LocationTaxRateRequest
{
    /**
     * Location Tax Rate  locationId
     * Format java.util.UUID
     */

    @NotNull
    private UUID locationId;
    /**
     * Location Tax Rate employee Id
     * Format java.util.UUID
     */

    @NotNull
    private UUID empId;
    /**
     * Location tax Rate itemCategoryId
     * Format java.util.UUID
     */

    private UUID itemCategoryId;
    /**
     * Location Tax taxCode
     * Maximum Size will be 30 characters
     */

    private String taxCode;
    /**
     * Location Tax Rate
     * Must be between 0 to 100 value only
     */

    private BigDecimal taxRate;
    /**
     * Location Tax Rate
     * Value can be true of false
     */

    private Boolean isOverriden;
    /**
     * Location Tax Rate suggestedTaxRate
     * Size would be between 0 to 100 integer value only
     */

    private BigDecimal suggestedTaxRate;
}
