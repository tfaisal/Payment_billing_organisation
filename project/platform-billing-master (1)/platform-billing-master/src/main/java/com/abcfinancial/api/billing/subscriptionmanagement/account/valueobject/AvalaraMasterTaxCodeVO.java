package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class AvalaraMasterTaxCodeVO
{
    /**
     * Avalara Tax Id
     */

    @NotNull
    private UUID id;
    /**
     * Avalara Master Tax Code
     */

    @NotNull
    private String taxCode;
   /**
     * Avalara Master Tax Description
     */

   @NotNull
    private String description;
    // End
}
