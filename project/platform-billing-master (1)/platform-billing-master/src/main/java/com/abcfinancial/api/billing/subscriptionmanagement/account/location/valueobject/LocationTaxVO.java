package com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class LocationTaxVO {

    /**
     * Location Tax Rate  Employee Id
     */

    @NotNull( message = "Employee Id can not be null" )
    private UUID empId;
    /**
     * Location Tax Rate  Tax code
     */

    private String taxCode;
    /**
     * Location Tax Rate  locationTaxRate
     */

    private BigDecimal taxRate;
    /**
     * Location Tax Rate  is Overriden
     */

    private Boolean isOverriden;
    /**
     * Location Tax Rate  suggested Tax Rate
     */

    private BigDecimal suggestedTaxRate;
    /**
     * Location Tax Rate  Item Category Id
     */

    private UUID itemCategoryId;
}
