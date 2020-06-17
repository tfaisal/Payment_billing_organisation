package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@JsonInclude( JsonInclude.Include.NON_NULL )

@Data
@JsonIgnoreProperties( ignoreUnknown = true )

public class TransactionModel implements Serializable
{
    /**
     * Transaction id( Read only )
     * Unique ID number of this transaction.
     */

    @JsonProperty( "id" )
    private long id;
    /**
     * Transaction code( Read only )
     * A unique customer-provided code identifying this transaction.
     */

    @JsonProperty( "code" )
    private String code;
    /**
     * The unique ID number of the company that recorded this transaction.
     */

    @JsonProperty( "companyId" )
    private long companyId;
    /**
     * The date on which this transaction occurred.
     */

    @JsonProperty( "date" )
    private Date date;
    /**
     * The status of the transaction.
     */

    @Enumerated( EnumType.STRING )
    @JsonProperty( "status" )
    private DocumentStatus status;
    /**
     * The type of the transaction as
     * SalesOrder, SalesInvoice, PurchaseOrder, PurchaseInvoice, ReturnOrder,
     * ReturnInvoice, InventoryTransferOrder, InventoryTransferInvoice,
     * ReverseChargeOrder, ReverseChargeInvoice, Any.
     */

    @Enumerated( EnumType.STRING )
    @JsonProperty( "type" )
    private DocumentType type;
    /**
     * This code indicates which batch.
     */

    @JsonProperty( "batchCode" )
    private String batchCode;
    /**
     * Three-character ISO 4217 currency code that was
     * used for payment for this transaction.
     */

    @JsonProperty( "currencyCode" )
    private String currencyCode;
    /**
     * The customer usage type for this transaction.
     */

    @JsonProperty( "customerUsageType" )
    private String customerUsageType;
    /**
     * The entity use code for this transaction.
     */

    @JsonProperty( "entityUseCode" )
    private String entityUseCode;
    /**
     * Customer Vendor Code
     */

    @JsonProperty( "customerVendorCode" )
    private String customerVendorCode;
    /**
     * Unique code identifying the customer that requested this transaction.
     */

    @JsonProperty( "customerCode" )
    private String customerCode;
    /**
     * If this transaction was exempt, this field will contain the word "Exempt".
     */

    @JsonProperty( "exemptNo" )
    private String exemptNo;
    /**
     * Set to true if transaction has been reconciled against the company's ledger.
     */

    @JsonProperty( "reconciled" )
    private boolean reconciled;
    /**
     * Location code
     */

    @JsonProperty( "locationCode" )
    private String locationCode;
    /**
     * For customers who use location-based tax reporting,
     * this field controls how this transaction will be
     * filed for multi-location tax filings.
     */

    @JsonProperty( "reportingLocationCode" )
    private String reportingLocationCode;
    /**
     * The customer-supplied purchase order number of this transaction.
     */

    @JsonProperty( "purchaseOrderNo" )
    private String purchaseOrderNo;
    /**
     * A user-defined reference code for this transaction.
     */

    @JsonProperty( "referenceCode" )
    private String referenceCode;
    /**
     * The salesperson who provided this transaction. Not required.
     */

    @JsonProperty( "salespersonCode" )
    private String salespersonCode;
    /**
     * If a tax override was applied to this transaction,
     * indicates what type of tax override was applied.
     */

    @JsonProperty( "taxOverrideType" )
    private String taxOverrideType;
    /**
     * If a tax override was applied to this transaction,
     * indicates the amount of tax that was requested by the customer.
     */

    @JsonProperty( "taxOverrideAmount" )
    private double taxOverrideAmount;
    /**
     * If a tax override was applied to this transaction,
     * indicates the reason for the tax override.
     */

    @JsonProperty( "taxOverrideReason" )
    private String taxOverrideReason;
    /**
     * The total amount of this transaction.
     */

    @JsonProperty( "totalAmount" )
    private double totalAmount;
    /**
     * The amount of this transaction that was exempt.
     */

    @JsonProperty( "totalExempt" )
    private double totalExempt;
    /**
     * The total amount of discounts applied to all
     * lines within this transaction.
     */

    @JsonProperty( "totalDiscount" )
    private double totalDiscount;
    /**
     * The total tax for all lines in this transaction.
     */

    @JsonProperty( "totalTax" )
    private double totalTax;
    /**
     * The portion of the total amount of this transaction that was taxable.
     */

    @JsonProperty( "totalTaxable" )
    private double totalTaxable;
    /**
     * The amount of tax that AvaTax calculated for the transaction.
     */

    @JsonProperty( "totalTaxCalculated" )
    private double totalTaxCalculated;
    /**
     * If this transaction was adjusted, indicates the unique
     * ID number of the reason why the transaction was adjusted.
     * i.e NotAdjusted, SourcingIssue, ReconciledWithGeneralLedger,
     * ExemptCertApplied, PriceAdjusted, ProductReturned,
     * ProductExchanged, BadDebt, OTHER, Offline
     */

    @Enumerated( EnumType.STRING )
    @JsonProperty( "adjustmentReason" )
    private AdjustmentReason adjustmentReason;
    /**
     * If this transaction was adjusted, indicates a description
     * of the reason why the transaction was adjusted.
     */

    @JsonProperty( "adjustmentDescription" )
    private String adjustmentDescription;
    /**
     * If this transaction has been reported to a tax authority,
     * this transaction is considered locked and may not be
     * adjusted after reporting.
     */

    @JsonProperty( "locked" )
    private boolean locked;
    /**
     * The two-or-three character ISO region code of the region
     * for this transaction
     */

    @JsonProperty( "region" )
    private String region;
    /**
     * The two-character ISO 3166 code of the country for this transaction.
     */

    @Size( min = 2, max = 2 )
    @JsonProperty( "country" )
    private String country;
    /**
     * If this transaction was adjusted, this indicates the version number
     * of this transaction. Incremented each time the transaction is adjusted.
     */

    @JsonProperty( "version" )
    private long version;
    /**
     * The software version used to calculate this transaction.
     */

    @JsonProperty( "softwareVersion" )
    private String softwareVersion;
    /**
     * The unique ID number of the origin address for this transaction.
     */

    @JsonProperty( "originAddressId" )
    private long originAddressId;
    /**
     * The unique ID number of the destination address for this transaction.
     */

    @JsonProperty( "destinationAddressId" )
    private long destinationAddressId;
    /**
     * If this transaction included foreign currency exchange,
     * this is the date as of which the exchange rate was calculated.
     */

    @JsonProperty( "exchangeRateEffectiveDate" )
    private Date exchangeRateEffectiveDate;
    /**
     * If this transaction included foreign currency exchange,
     * this is the exchange rate that was used.
     */

    @JsonProperty( "exchangeRate" )
    private double exchangeRate;
    /**
     * By default, the value is null, when the value is null,
     * the value can be set at nexus level and used. If the value is not null,
     * it will override the value at nexus level.If true, this seller was
     * considered the importer of record of a product shipped longernationally.
     * If this transaction is not an longernational transaction, this field may be left blank.
     */

    @JsonProperty( "isSellerImporterOfRecord" )
    private boolean isSellerImporterOfRecord;
    /**
     * Description of this transaction. Field permits unicode values.
     */

    @JsonProperty( "description" )
    private String description;
    /**
     * Email address associated with this transaction.
     */

    @JsonProperty( "email" )
    private String email;
    /**
     * VAT business identification number used for this transaction.
     */

    @JsonProperty( "businessIdentificationNo" )
    private String businessIdentificationNo;
    /**
     * Modified date( Read only )
     * The date/time when this record was last modified.
     */

    @JsonProperty( "modifiedDate" )
    private Date modifiedDate;
    /**
     * Modified user id( Read only )
     * The user ID of the user who last modified this record.
     */

    @JsonProperty( "modifiedUserId" )
    private long modifiedUserId;
    /**
     * Tax Date( Read only )
     * Tax date for this transaction
     */

    @JsonProperty( "taxDate" )
    private Date taxDate;
    /**
     * A list of line items in this transaction. To fetch this list,
     * add the query string ?$include = Lines or ?$include = Details to your URL.
     */

    @JsonProperty( "lines" )
    private List<TransactionLineModel> lines;
    /**
     * A list of line items in this transaction. To fetch this list, add the query string ?$include = Addresses to your URL.
     * For more information about transaction addresses, please see Using Address Types in the AvaTax Developer Guide.
     */

    @JsonProperty( "addresses" )
    private List<TransactionAddressModel> addresses;
    /**
     * A list of location types in this transaction. To fetch this list, add the query string ?$include = Addresses to your URL.
     */

    @JsonProperty( "locationTypes" )
    private List<TransactionLocationTypeModel> locationTypes;
    /**
     * Contains a summary of tax on this transaction.
     */

    @JsonProperty( "summary" )
    private List<TransactionSummary> summary;
    /**
     * List of informational and warning messages regarding this API call. These messages are only relevant to the current API call.
     */

    @JsonProperty( "messages" )
    private List<AvaTaxMessage> messages;
    /**
     * Invoice messages associated with this document. Currently, this stores legally-required VAT messages.
     */

    @JsonProperty( "invoiceMessages" )
    private List<InvoiceMessageModel> invoiceMessages;
    /**
     * Parameter Object
     */

    @JsonProperty( "parameters" )
    private Parameters parameters;
}
