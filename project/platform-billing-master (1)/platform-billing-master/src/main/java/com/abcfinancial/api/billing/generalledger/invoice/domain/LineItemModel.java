package com.abcfinancial.api.billing.generalledger.invoice.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.TaxOverrideModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class LineItemModel implements Serializable
{
    /**
     * Line Number of line Within document
     */

    @JsonProperty( "number" )
    private String number;
    /**
     * Quantity of Items
     */

    @JsonProperty( "quantity" )
    private double quantity;
    /**
     * Total amount for this line
     */

    @NotNull
    @JsonProperty( "amount" )
    private double amount;
    /**
     * Tax Code
     */

    @Size( min = 0, max = 25 )
    @JsonProperty( "taxCode" )
    private String taxCode;
    /**
     * Customer usage type
     */

    @Size( min = 0, max = 25 )
    @JsonProperty( "customerUsageType" )
    private String customerUsageType;
    /**
     * Entity use code
     */

    @Size( min = 0, max = 25 )
    @JsonProperty( "entityUseCode" )
    private String entityUseCode;
    /**
     * Item code
     */

    @Size( min = 0, max = 50 )
    @JsonProperty( "itemCode" )
    private String itemCode;
    /**
     * Exemption certificate number for this line
     */

    @JsonProperty( "exemptionCode" )
    private String exemptionCode;
    /**
     * Address for this transaction
     */

    @JsonProperty( "discounted" )
    private boolean discounted;
    /**
     * True if the document discount
     */

    @JsonProperty( "taxIncluded" )
    private boolean taxIncluded;
    /**
     * Revenue Account ( Customer Defined Field )
     */

    @Size( min = 0, max = 50 )
    @JsonProperty( "revenueAccount" )
    private String revenueAccount;
    /**
     * Ref1 ( Customer Defined Field )
     */

    @Size( min = 0, max = 250 )
    @JsonProperty( "ref1" )
    private String ref1;
    /**
     * Ref2 ( Customer Defined Field )
     */

    @Size( min = 0, max = 250 )
    @JsonProperty( "ref2" )
    private String ref2;
    /**
     * Item description
     */

    @Size( min = 0, max = 2096 )
    @JsonProperty( "description" )
    private String description;
    /**
     * Specifies a tax override for the entire document
     */

    @JsonProperty( "taxOverride" )
    private TaxOverrideModel taxOverride;

}
