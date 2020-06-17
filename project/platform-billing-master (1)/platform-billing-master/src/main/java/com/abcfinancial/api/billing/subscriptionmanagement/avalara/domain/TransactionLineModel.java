package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@JsonInclude( JsonInclude.Include.NON_NULL )

@Data

public class TransactionLineModel implements Serializable
{
    /**
     * The unique ID number of this transaction line item.
     */

    @JsonProperty( "id" )
    private long id;
    /**
     * The unique ID number of the transaction to which this line item belongs.
     */

    @JsonProperty( "transactionId" )
    private long transactionId;
    /**
     * The line number or code indicating the line on this invoice or receipt or document.
     */

    @JsonProperty( "lineNumber" )
    private String lineNumber;
    /**
     * The unique ID number of the boundary override applied to this line item.
     */

    @JsonProperty( "boundaryOverrideId" )
    private long boundaryOverrideId;
    /**
     * The entity use code for this line item. Usage type often affects taxability rules.
     */

    @JsonProperty( "entityUseCode" )
    private String entityUseCode;
    /**
     * A description of the item or service represented by this line.
     */

    @JsonProperty( "description" )
    private String description;
    /**
     * The unique ID number of the destination address where this line was
     * delivered or sold. In the case of a point-of-sale transaction,
     * the destination address and origin address will be the same.
     * In the case of a shipped transaction, they will be different.
     */

    @JsonProperty( "destinationAddressId" )
    private long destinationAddressId;
    /**
     * The unique ID number of the origin address where this line was delivered or sold.
     * In the case of a point-of-sale transaction, the origin address and destination address will be the same.
     * In the case of a shipped transaction, they will be different.
     */

    @JsonProperty( "originAddressId" )
    private long originAddressId;
    /**
     * The amount of discount that was applied to this line item.
     * This represents the difference between list price and sale price of the item.
     * In general, a discount represents money that did not change hands;
     * tax is calculated on only the amount of money that changed hands.
     */

    @JsonProperty( "discountAmount" )
    private long discountAmount;
    /**
     * The type of discount, if any, that was applied to this line item.
     */

    @JsonProperty( "discountTypeId" )
    private long discountTypeId;
    /**
     * The amount of this line item that was exempt.
     */

    @JsonProperty( "exemptAmount" )
    private long exemptAmount;
    /**
     * The unique ID number of the exemption certificate that applied to this line item.
     */

    @JsonProperty( "exemptCertId" )
    private long exemptCertId;
    /**
     * If this line item was exempt, this string contains the word Exempt.
     */

    @JsonProperty( "exemptNo" )
    private String exemptNo;
    /**
     * True if this item is taxable.
     */

    @JsonProperty( "isItemTaxable" )
    private Boolean isItemTaxable;
    /**
     * True if this item is a Streamlined Sales Tax line item.
     */

    @JsonProperty( "isSSTP" )
    private Boolean isSSTP;
    /**
     * The code string of the item represented by this line item.
     */

    @JsonProperty( "itemCode" )
    private String itemCode;
    /**
     * The total amount of the transaction, including both taxable and exempt.
     * This is the total price for all items. To determine the individual item price,
     * divide this by quantity.
     */

    @JsonProperty( "lineAmount" )
    private long lineAmount;
    /**
     * The quantity of products sold on this line item.
     */

    @JsonProperty( "quantity" )
    private long quantity;
    /**
     * A user-defined reference identifier for this transaction line item.
     */

    @JsonProperty( "ref1" )
    private String ref1;
    /**
     * Reporting date ( Read Only )
     * The date when this transaction should be reported. By default,
     * all transactions are reported on the date when the actual transaction took place. In some cases,
     * line items may be reported later due to delayed shipments or other business reasons.
     */

    @JsonProperty( "reportingDate" )
    private String reportingDate;
    /**
     * The revenue account number for this line item.
     */

    @JsonProperty( "revAccount" )
    private String revAccount;
    /**
     * Indicates whether this line item was taxed according to the origin or destination
     */

    @JsonProperty( "sourcing" )
    private String sourcing;
    /**
     * The tax for this line in this transaction.
     * If you used a taxOverride of type taxAmount for this line,
     * this value will represent the amount of your override.
     * AvaTax will still attempt to calculate the correct tax for this
     * line and will store that calculated value in the taxCalculated field.
     * You can compare the tax and taxCalculated fields to check for
     * any discrepancies between an external tax calculation provider and
     * the calculation performed by AvaTax.
     */

    @JsonProperty( "tax" )
    private Float tax;
    /**
     * The taxable amount of this line item.
     */

    @JsonProperty( "taxableAmount" )
    private long taxableAmount;
    /**
     * The amount of tax that AvaTax calculated for the transaction.
     * If you used a taxOverride of type taxAmount for this line,
     * there will be a difference between the tax field which represents your override,
     * and the taxCalculated field which represents the amount of tax that AvaTax calculated for this line.
     * You can compare the tax and taxCalculated fields to check for any discrepancies between
     * an external tax calculation provider and the calculation performed by AvaTax.
     */

    @JsonProperty( "taxCalculated" )
    private Float taxCalculated;
    /**
     * The code string for the tax code that was used to calculate this line item.
     */

    @JsonProperty( "taxCode" )
    private String taxCode;
    /**
     * The date that was used for calculating tax amounts for this line item.
     * By default, this date should be the same as the document date. In some cases,
     * for example when a consumer returns a product purchased previously, line items
     * may be calculated using a tax date in the past so that the consumer can receive
     * a refund for the correct tax amount that was charged when the item was originally purchased.
     */

    @JsonProperty( "taxDate" )
    private String taxDate;
    /**
     * The tax engine identifier that was used to calculate this line item.
     */

    @JsonProperty( "taxEngine" )
    private String taxEngine;
    /**
     * If a tax override was specified, this indicates the type of tax override.
     */

    @JsonProperty( "taxOverrideType" )
    private String taxOverrideType;
    /**
     * If a tax override was specified, this indicates the amount of tax that was requested.
     */

    @JsonProperty( "taxOverrideAmount" )
    private long taxOverrideAmount;
    /**
     * If a tax override was specified, represents the reason for the tax override.
     */

    @JsonProperty( "taxOverrideReason" )
    private String taxOverrideReason;
    /**
     * Indicates whether the amount for this line already includes tax.
     * If this value is true, the final price of this line including tax will equal the value in amount.
     * If this value is null or false, the final price will equal amount plus whatever taxes apply to this line.
     */

    @JsonProperty( "taxIncluded" )
    private Boolean taxIncluded;
    /**
     * Optional: A list of tax details for this line item.
     * Tax details represent taxes being charged by various tax authorities.
     * Taxes that appear in the details collection are intended to be displayed to the customer and charged as a
     * 'tax' on the invoice.To fetch this list, add the query string ?$include = Details to your URL.
     */

    @JsonProperty( "details" )
    private List<TransactionLineDetailModel> details;
    /**
     * Indicates the VAT number type for this line item.
     */

    @JsonProperty( "vatNumberTypeId" )
    private long vatNumberTypeId;
    /**
     *
     */

    @JsonProperty( "customerUsageType" )
    private String customerUsageType;
    /**
     * A user-defined reference identifier for this transaction line item.
     */

    @JsonProperty( "ref2" )
    private String ref2;
    /**
     * The unique ID number for the tax code that was used to calculate this line item.
     */

    @JsonProperty( "taxCodeId" )
    private long taxCodeId;
    /**
     * VAT business identification number used for this transaction.
     */

    @JsonProperty( "businessIdentificationNo" )
    private String businessIdentificationNo;
    /**
     * Optional: A list of non-passthrough tax details for this line item.
     * Tax details represent taxes being charged by various tax authorities.
     * Taxes that appear in the nonPassthroughDetails collection are taxes that must be
     * paid directly by the company and not shown to the customer.
     */

    @JsonProperty( "nonPassthroughDetails" )
    private List<TransactionLineDetailModel2> nonPassthroughDetails;
    /**
     * Optional: A list of location types for this line item.
     * To fetch this list, add the query string "?$include = LineLocationTypes" to your URL.
     */

    @JsonProperty( "lineLocationTypes" )
    private List<TransactionLineLocationTypeModel> lineLocationTypes;
    /**
     * Parameter Object
     */

    @JsonProperty( "parameters" )
    private Parameters parameters;
    /**
     * The cross-border harmonized system code ( HSCode ) used to calculate tariffs and duties for this line item.
     * For a full list of HS codes, see ListCrossBorderCodes( ).
     */

    @JsonProperty( "hsCode" )
    private String hsCode;
    /**
     * Indicates the VAT code for this line item.
     */

    @JsonProperty( "vatCode" )
    private String vatCode;
}
