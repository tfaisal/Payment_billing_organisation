package com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data

public class LocationTaxRateResponse {
    /**
    An Unique Id Behaves as Location Tax Rate Id
     */

    @NotNull
    private UUID locTaxRateId;
    /**
    An Unique Id Behaves as Identity Of Employee.
     */

    @NotNull
    private UUID empId;
    /**
    Item Category Id
     */

    @JsonInclude( JsonInclude.Include.NON_NULL )
    private UUID itemCategoryId;
    /**
    Tax Code
     */

    @JsonInclude( JsonInclude.Include.NON_NULL )
    private String taxCode;
    /**
    Tax Rate for Locations
     */

    @JsonInclude( JsonInclude.Include.NON_NULL )
    private BigDecimal taxRate;
    /**
     Suggested Tax Rate for Locations
     */

    @JsonInclude( JsonInclude.Include.NON_NULL )
    private BigDecimal suggestedTaxRate;
    /**
     Location tax Rate version as Integer value.
     */

    @JsonInclude( JsonInclude.Include.NON_NULL )
    private Long version;
    /**
     * Location Tax Rate Overriden
     * Value can be true of false
     */

    private Boolean isOverriden;

}
