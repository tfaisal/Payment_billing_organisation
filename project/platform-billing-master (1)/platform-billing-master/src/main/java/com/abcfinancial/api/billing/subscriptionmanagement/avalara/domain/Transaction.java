package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.abcfinancial.api.billing.generalledger.invoice.domain.LineItemModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@JsonInclude( JsonInclude.Include.NON_NULL )

@Data

public class Transaction implements Serializable
{
    /**
     * Transaction Code
     */

    @Size( min = 0, max = 50 )
    @JsonProperty( "code" )
    private String code;
    /**
     * List of Line Items
     */

    @NotNull
    @JsonProperty( "lines" )
    private List<LineItemModel> lines;
    /**
     * Document type
     */

    @JsonProperty( "type" )
    private String type;
    /**
     * Company Code
     * Default value is DEFAULT
     */

    @JsonProperty( "companyCode" )
    private String companyCode;
    /**
     * Transaction Date
     */

    @NotNull
    @JsonProperty( "date" )
    private String date;
    /**
     * Customer Code
     */

    @Size( min = 0, max = 50 )
    @JsonProperty( "customerCode" )
    private String customerCode;
    /**
     * SALESPERSON Code
     */

    @Size( min = 0, max = 50 )
    @JsonProperty( "salespersonCode" )
    private String salespersonCode;
    /**
     * Customer Usage Type
     */

    @Size( min = 0, max = 25 )
    @JsonProperty( "customerUsageType" )
    private String customerUsageType;
    /**
     * Entity Use Code
     */

    @Size( min = 0, max = 25 )
    @JsonProperty( "entityUseCode" )
    private String entityUseCode;
    /**
     * The discount amount to apply to the document
     */

    @JsonProperty( "discount" )
    private Double discount;
    /**
     * Purchase Order Number for this document
     */

    @Size( min = 0, max = 50 )
    @JsonProperty( "purchaseOrderNo" )
    private String purchaseOrderNo;
    /**
     * Exemption Number for this document
     */

    @Size( min = 0, max = 25 )
    @JsonProperty( "exemptionNo" )
    private String exemptionNo;
    /**
     * Address details
     */

    @JsonProperty( "addresses" )
    private AddressesModel addresses;
    /**
     * Customer-provided Reference Code
     */

    @Size( min = 0, max = 1024 )
    @JsonProperty( "referenceCode" )
    private String referenceCode;
    /**
     * sale location code ( Outlet ID )
     * for reporting this document to the tax authority
     */

    @Size( min = 0, max = 50 )
    @JsonProperty( "reportingLocationCode" )
    private String reportingLocationCode;
    /**
     * Document to be committed if true
     * Applicable for invoice document types, not orders
     */

    @JsonProperty( "commit" )
    private boolean commit;
    /**
     * BatchCode for batch operations
     */

    @Size( min = 0, max = 25 )
    @JsonProperty( "batchCode" )
    private String batchCode;
    /**
     * The three-character ISO 4217 currency code for this transaction
     */

    @Size( min = 0, max = 3 )
    @JsonProperty( "currencyCode" )
    private String currencyCode;
    /**
     * Currency exchange rate
     */

    @JsonProperty( "exchangeRate" )
    private Double exchangeRate;
    /**
     * Effective date of the exchange rate
     */

    @JsonProperty( "exchangeRateEffectiveDate" )
    private String exchangeRateEffectiveDate;
    /**
     * Point of Sale Lane Code sent by the User for this document
     */

    @Size( min = 0, max = 50 )
    @JsonProperty( "posLaneCode" )
    private String posLaneCode;
    /**
     * Business identification number for the customer for this transaction
     */

    @Size( min = 0, max = 25 )
    @JsonProperty( "businessIdentificationNo" )
    private String businessIdentificationNo;
    /**
     * Value-added and cross-border taxes calculated
     * with the seller as the importer of record
     */

    @JsonProperty( "isSellerImporterOfRecord" )
    private boolean isSellerImporterOfRecord;
    /**
     * User-supplied description for this transaction
     */

    @Size( min = 0, max = 2048 )
    @JsonProperty( "description" )
    private String description;
    /**
     * User-supplied email address relevant for this transaction
     */

    @Size( min = 0, max = 50 )
    @JsonProperty( "email" )
    private String email;
    /**
     * Specifies a tax override for the entire document
     */

    @JsonProperty( "taxOverride" )
    private TaxOverrideModel taxOverride;

    public static class TransactionBuilder
    {
        private String code;
        private List<LineItemModel> lines;
        private String type;
        private String companyCode;
        private String date;
        private String customerCode;
        private String salespersonCode;
        private String customerUsageType;
        private String entityUseCode;
        private Double discount;
        private String purchaseOrderNo;
        private String exemptionNo;
        private AddressesModel addresses;
        private String referenceCode;
        private String reportingLocationCode;
        private String batchCode;
        private String currencyCode;
        private Double exchangeRate;
        private String exchangeRateEffectiveDate;
        private String posLaneCode;
        private String businessIdentificationNo;
        private boolean isSellerImporterOfRecord;
        private String description;
        private String email;
        private TaxOverrideModel taxOverride;

        public TransactionBuilder( AddressesModel addressesModel, List<LineItemModel> lineItemModels, String date, String customerCode, String type, String companyCode )
        {
            this.addresses = addressesModel;
            this.lines = lineItemModels;
            this.date = date;
            this.customerCode = customerCode;
            this.type = type;
            this.companyCode = companyCode;
        }

        public Transaction build()
        {
            Transaction transaction = new Transaction();
            transaction.setAddresses( this.addresses );
            transaction.setLines( this.lines );
            transaction.setDate( this.date );
            transaction.setCustomerCode( this.customerCode );
            transaction.setType( this.type );
            transaction.setCompanyCode( this.companyCode );
            return transaction;
        }

        public TransactionBuilder setCode( String code )
        {
            this.code = code;
            return this;
        }

        public TransactionBuilder setCompanyCode( String companyCode )
        {
            this.companyCode = companyCode;
            return this;
        }

        public TransactionBuilder setSalespersonCode( String salespersonCode )
        {
            this.salespersonCode = salespersonCode;
            return this;
        }

        public TransactionBuilder setCustomerUsageType( String customerUsageType )
        {
            this.customerUsageType = customerUsageType;
            return this;
        }

        public TransactionBuilder setEntityUseCode( String entityUseCode )
        {
            this.entityUseCode = entityUseCode;
            return this;
        }

        public TransactionBuilder setDiscount( Double discount )
        {
            this.discount = discount;
            return this;
        }

        public TransactionBuilder setPurchaseOrderNo( String purchaseOrderNo )
        {
            this.purchaseOrderNo = purchaseOrderNo;
            return this;
        }

        public TransactionBuilder setExemptionNo( String exemptionNo )
        {
            this.exemptionNo = exemptionNo;
            return this;
        }

        public TransactionBuilder setReferenceCode( String referenceCode )
        {
            this.referenceCode = referenceCode;
            return this;
        }

        public TransactionBuilder setReportingLocationCode( String reportingLocationCode )
        {
            this.reportingLocationCode = reportingLocationCode;
            return this;
        }

        public TransactionBuilder setBatchCode( String batchCode )
        {
            this.batchCode = batchCode;
            return this;
        }

        public TransactionBuilder setCurrencyCode( String currencyCode )
        {
            this.currencyCode = currencyCode;
            return this;
        }

        public TransactionBuilder setExchangeRate( Double exchangeRate )
        {
            this.exchangeRate = exchangeRate;
            return this;
        }

        public TransactionBuilder setExchangeRateEffectiveDate( String exchangeRateEffectiveDate )
        {
            this.exchangeRateEffectiveDate = exchangeRateEffectiveDate;
            return this;
        }

        public TransactionBuilder setPosLaneCode( String posLaneCode )
        {
            this.posLaneCode = posLaneCode;
            return this;
        }

        public TransactionBuilder setBusinessIdentificationNo( String businessIdentificationNo )
        {
            this.businessIdentificationNo = businessIdentificationNo;
            return this;
        }

        public TransactionBuilder setSellerImporterOfRecord( boolean sellerImporterOfRecord )
        {
            isSellerImporterOfRecord = sellerImporterOfRecord;
            return this;
        }

        public TransactionBuilder setDescription( String description )
        {
            this.description = description;
            return this;
        }

        public TransactionBuilder setEmail( String email )
        {
            this.email = email;
            return this;
        }

        public TransactionBuilder setTaxOverride( TaxOverrideModel taxOverride )
        {
            this.taxOverride = taxOverride;
            return this;
        }
    }
}
