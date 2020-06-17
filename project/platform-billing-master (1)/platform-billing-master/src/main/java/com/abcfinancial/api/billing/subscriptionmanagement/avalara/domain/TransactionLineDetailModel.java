package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )

@Data

public class TransactionLineDetailModel implements Serializable
{
    private static final long serialVersionUID = 1142716517695899924L;
    /**
     * The unique ID number of this tax detail( Read only ).
     */

    @JsonProperty( "id" )
    private long id;
    /**
     * The unique ID number of the line within this transaction( Read only ).
     */

    @JsonProperty( "transactionLineId" )
    private long transactionLineId;
    /**
     * The unique ID number of this transaction( Read only ).
     */

    @JsonProperty( "transactionId" )
    private long transactionId;
    /**
     * The unique ID number of the address used for this tax detail.
     */

    @JsonProperty( "addressId" )
    private long addressId;
    /**
     * The two character ISO 3166 country code of the country where this tax detail is assigned.
     */

    @Size( min = 2, max = 2 )
    @JsonProperty( "country" )
    private String country;
    /**
     * The two-or-three character ISO region code for the region where this tax detail is assigned.
     */

    @JsonProperty( "region" )
    private String region;
    /**
     * For U.S. transactions, the Federal Information Processing Standard ( FIPS ) code for the state
     * where this tax detail is assigned.
     */

    @JsonProperty( "stateFIPS" )
    private String stateFIPS;
    /**
     * The amount of this line that was considered exempt in this tax detail.
     */

    @JsonProperty( "exemptAmount" )
    private long exemptAmount;
    /**
     * The unique ID number of the exemption reason for this tax detail.
     */

    @JsonProperty( "exemptReasonId" )
    private long exemptReasonId;
    /**
     * True if this detail element represented an in-state transaction.
     */

    @JsonProperty( "inState" )
    private boolean inState;
    /**
     * The code of the jurisdiction to which this tax detail applies.
     */

    @JsonProperty( "jurisCode" )
    private String jurisCode;
    /**
     * The name of the jurisdiction to which this tax detail applies.
     */

    @JsonProperty( "jurisName" )
    private String jurisName;
    /**
     * The unique ID number of the jurisdiction to which this tax detail applies.
     */

    @JsonProperty( "jurisdictionId" )
    private long jurisdictionId;
    /**
     * The Avalara-specified signature code of the jurisdiction to which this tax detail applies.
     */

    @JsonProperty( "signatureCode" )
    private String signatureCode;
    /**
     * The state assigned number of the jurisdiction to which this tax detail applies.
     */

    @JsonProperty( "stateAssignedNo" )
    private String stateAssignedNo;
    /**
     * The type of the jurisdiction in which this tax detail applies.
     */

    @JsonProperty( "jurisType" )
    private String jurisType;
    /**
     * ( DEPRECATED ) The type of the jurisdiction to which this tax detail applies.
     * NOTE: Use jurisdictionTypeId instead.
     */

    @JsonProperty( "jurisdictionType" )
    private String jurisdictionType;
    /**
     * The amount of this line item that was considered nontaxable in this tax detail.
     */

    @JsonProperty( "nonTaxableAmount" )
    private long nonTaxableAmount;
    /**
     * The rule according to which portion of this detail was considered nontaxable.
     */

    @JsonProperty( "nonTaxableRuleId" )
    private long nonTaxableRuleId;
    /**
     * The type of nontaxability that was applied to this tax detail.
     */

    @JsonProperty( "nonTaxableType" )
    private String nonTaxableType;
    /**
     * The rate at which this tax detail was calculated.
     */

    @JsonProperty( "rate" )
    private long rate;
    /**
     * The unique ID number of the rule according to which this tax detail was calculated.
     */

    @JsonProperty( "rateRuleId" )
    private long rateRuleId;
    /**
     * The unique ID number of the source of the rate according to which this tax detail was calculated.
     */

    @JsonProperty( "rateSourceId" )
    private long rateSourceId;
    /**
     * For Streamlined Sales Tax customers, the SST Electronic Return code
     * under which this tax detail should be applied.
     */

    @JsonProperty( "serCode" )
    private String serCode;
    /**
     * Indicates whether this tax detail applies to the origin or destination of the transaction.
     */

    @JsonProperty( "sourcing" )
    private String sourcing;
    /**
     * The amount of tax for this tax detail.
     */

    @JsonProperty( "tax" )
    private long tax;
    /**
     * The taxable amount of this tax detail.
     */

    @JsonProperty( "taxableAmount" )
    private long taxableAmount;
    /**
     * The type of tax that was calculated. Depends on the company's nexus settings as well as the jurisdiction's tax laws.
     */

    @JsonProperty( "taxType" )
    private String taxType;
    /**
     * The id of the tax subtype.
     */

    @JsonProperty( "taxSubTypeId" )
    private String taxSubTypeId;
    /**
     * The id of the tax type group.
     */

    @JsonProperty( "taxTypeGroupId" )
    private String taxTypeGroupId;
    /**
     * The name of the tax against which this tax amount was calculated.
     */

    @JsonProperty( "taxName" )
    private String taxName;
    /**
     * The type of the tax authority to which this tax will be remitted.
     */

    @JsonProperty( "taxAuthorityTypeId" )
    private long taxAuthorityTypeId;
    /**
     * The unique ID number of the tax region.
     */

    @JsonProperty( "taxRegionId" )
    private long taxRegionId;
    /**
     * The amount of tax that was calculated. This amount may be different
     * if a tax override was used. If the customer specified a tax override,
     * this calculated tax value represents the amount of tax that would
     * have been charged if Avalara had calculated the tax for the rule.
     */

    @JsonProperty( "taxCalculated" )
    private long taxCalculated;
    /**
     * The amount of tax override that was specified for this tax line.
     */

    @JsonProperty( "taxOverride" )
    private long taxOverride;
    /**
     * ( DEPRECATED ) The rate type for this tax detail. Please use rateTypeCode instead.
     */

    @JsonProperty( "rateType" )
    private String rateType;
    /**
     * Indicates the code of the rate type that was used to calculate this tax detail.
     * Use /api/v2/definitions/ratetypes for a full list of rate type codes.
     */

    @JsonProperty( "rateTypeCode" )
    private String rateTypeCode;
    /**
     * Number of units in this line item that were calculated to be taxable according to this rate detail.
     */

    @JsonProperty( "taxableUnits" )
    private long taxableUnits;
    /**
     * Number of units in this line item that were calculated to be nontaxable according to this rate detail.
     */

    @JsonProperty( "nonTaxableUnits" )
    private long nonTaxableUnits;
    /**
     * Number of units in this line item that were calculated to be exempt according to this rate detail.
     */

    @JsonProperty( "exemptUnits" )
    private long exemptUnits;
    /**
     * When calculating units, what basis of measurement did we use for calculating the units?
     */

    @JsonProperty( "unitOfBasis" )
    private String unitOfBasis;
    /**
     * True if this value is a non-passthrough tax. A non-passthrough tax is a tax that may
     * not be charged to a customer; it must be paid directly by the company.
     */

    @JsonProperty( "isNonPassThru" )
    private boolean isNonPassThru;

}
