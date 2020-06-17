package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class TransactionSummary implements Serializable
{
    /**
     * Two character ISO-3166 country code.
     */

    @JsonProperty( "country" )
    private String country;
    /**
     * Two or three character ISO region, state or province code, if applicable.
     */

    @JsonProperty( "region" )
    private String region;
    /**
     * The type of jurisdiction that collects this tax.
     */

    @JsonProperty( "jurisType" )
    private String jurisType;
    /**
     * Jurisdiction Code for the taxing jurisdiction
     */

    @JsonProperty( "jurisCode" )
    private String jurisCode;
    /**
     * The name of the jurisdiction that collects this tax.
     */

    @JsonProperty( "jurisName" )
    private String jurisName;
    /**
     * The unique ID of the Tax Authority Type that collects this tax.
     */

    @JsonProperty( "taxAuthorityType" )
    private long taxAuthorityType;
    /**
     * The state assigned number of the jurisdiction that collects this tax.
     */

    @JsonProperty( "stateAssignedNo" )
    private String stateAssignedNo;
    /**
     * The tax type of this tax.
     */

    @JsonProperty( "taxType" )
    private String taxType;
    /**
     * The name of the tax.
     */

    @JsonProperty( "taxName" )
    private String taxName;
    /**
     * ( DEPRECATED ) Indicates the tax rate type. Please use rateTypeCode instead.
     */

    @JsonProperty( "rateType" )
    private String rateType;
    /**
     * Tax Base - The adjusted taxable amount.
     */

    @JsonProperty( "taxable" )
    private long taxable;
    /**
     * Tax Rate - The rate of taxation, as a fraction of the amount.
     */

    @JsonProperty( "rate" )
    private BigDecimal rate;
    /**
     * Tax amount - The calculated tax ( Base * Rate ).
     */

    @JsonProperty( "tax" )
    private long tax;
    /**
     * Tax Calculated by Avalara AvaTax. This may be overriden by a TaxOverride.TaxAmount.
     */

    @JsonProperty( "taxCalculated" )
    private long taxCalculated;
    /**
     * The amount of the transaction that was non-taxable.
     */

    @JsonProperty( "nonTaxable" )
    private long nonTaxable;
    /**
     * The amount of the transaction that was exempt.
     */

    @JsonProperty( "exemption" )
    private long exemption;

}
