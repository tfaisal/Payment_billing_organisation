package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Size;
import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )

public class TransactionLineDetailModel2 implements Serializable
{
    /**
     * Transaction details model id( Read only )
     * The unique ID number of this tax detail.
     */

    @JsonProperty( "id" )
    private long id;
    /**
     * Transaction Line id( Read only )
     * The unique ID number of the line within this transaction.
     */

    @JsonProperty( "transactionLineId" )
    private long transactionLineId;
    /**
     * Transaction id( Read only )
     * The unique ID number of this transaction.
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
     * ( DEPRECATED ) The type of the jurisdiction to which this tax detail applies.
     * NOTE: Use jurisdictionTypeId instead.
     */

    @JsonProperty( "jurisType" )
    private String jurisType;
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
    private double rate;
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
     * For Streamlined Sales Tax customers, the SST Electronic Return code under which this tax detail
     * should be applied.
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
    private double tax;
    /**
     * The taxable amount of this tax detail.
     */

    @JsonProperty( "taxableAmount" )
    private long taxableAmount;
    /**
     * The type of tax that was calculated. Depends on the company's nexus settings as well as
     * the jurisdiction's tax laws.
     */

    @JsonProperty( "taxType" )
    private String taxType;
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
     * this calculated tax value represents the amount of tax that would have
     * been charged if Avalara had calculated the tax for the rule.
     */

    @JsonProperty( "taxCalculated" )
    private double taxCalculated;
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
}
