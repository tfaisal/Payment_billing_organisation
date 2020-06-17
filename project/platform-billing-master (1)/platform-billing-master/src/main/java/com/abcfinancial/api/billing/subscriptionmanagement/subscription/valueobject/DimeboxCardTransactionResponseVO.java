package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.RecurringType;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ShoppingInformation;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.StatusTypes;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@EqualsAndHashCode( callSuper = true )
@JsonInclude( JsonInclude.Include.NON_NULL )

public class DimeboxCardTransactionResponseVO extends TransactionInfo
{
    /**
     * Response code.
     */

    private int code;
    /**
     * Response message.
     */

    private String message;
    /**
     * The time when the error occured
     */

    private long timestamp;
    /**
     * Optional, may contain information specific to the error. Used for debugging
     */

    private String details;
    /**
     * The reason of the refund
     */

    @JsonProperty( "reason" )
    private String reason;
    /**
     * The ID of the transaction
     */

    @JsonProperty( "transaction" )
    private String transaction;
    /**
     * The IP address of the customer
     */

    @JsonProperty( "customer_ip" )
    private String customerIp;
    /**
     * The ID of a customer
     */

    @JsonProperty( "customer" )
    private String customer;
    /**
     * True if the card was used with a cvv
     */

    @JsonProperty( "cvv_present" )
    private boolean cvvPresent;
    /**
     * A reference specified by the merchant to identify the transaction
     */

    @JsonProperty( "merchant_reference" )
    private String merchantReference;
    /**
     * A short reference / descriptor that will show up on the customers bank
     * statement. Please refer to the card payment integration guide for the
     * format requirements, which are specific per card processor.
     */

    @JsonProperty( "dynamic_descriptor" )
    private String dynamicDescriptor;
    /**
     * The payment product corresponding to this transaction
     */

    @JsonProperty( "payment_product" )
    private String paymentProduct;
    /**
     * The payment product type corresponding to this transaction
     * Valid Values:"bnpparibas" "dummy" "billpro" "cielo" "payreto"
     * "credorax" "getnet" "native" "ems" "payvision" "six" "vantiv" "payworks" "sabadell"
     */

    @JsonProperty( "payment_product_type" )
    private String paymentProductType;
    /**
     * A reason code assigned by the acquiring platform; '00' in case of success
     */

    @JsonProperty( "reason_code" )
    private String reasonCode;
    /**
     * The name of the processor used across this transaction
     * Valid Values:"bnpparibas" "dummy" "billpro" "cielo" "payreto" "credorax"
     * "getnet" "native" "ems" "payvision" "six" "vantiv" "payworks" "sabadell"
     */

    @Enumerated( EnumType.STRING )
    @JsonProperty( "processor" )
    private String processor;
    /**
     * This field is to be used only when a transaction is part of a series of
     * recurring transactions. If it's the initial transaction, set the value to
     * 'FIRST', and if it's not, set it to 'REPEAT'. This field must not be included
     * in transactions that will not be part of a series of repeated transactions.
     * ( 'FIRST' and 'REPEAT' are written in all lowercase letters ).
     * Valid Value:"FIRST" or "REPEAT"
     */

    @Enumerated( EnumType.STRING )
    @JsonProperty( "recurring_type" )
    private RecurringType recurringType;
    /**
     * The full user agent string of the device the customer used to submit
     * the transaction
     */

    @JsonProperty( "user_agent" )
    private String userAgent;
    /**
     * Valid Values:"DECLINED" "AUTHORIZED" "FAILED" "PENDING" "UNKNOWN"
     */

    @Enumerated( EnumType.STRING )
    @JsonProperty( "status" )
    private StatusTypes status;
    /**
     * A webhook url that is called when a transaction is updated
     */

    @JsonProperty( "webhook_transaction_update" )
    private String webhookTransactionUpdate;
    /**
     * Determines the point of sale of a customer. Possible values: POS,
     * MOTO, and ECOMMERCE. Valid Values : "ECOMMERCE" "POS" "MOTO" "UNKNOWN"
     */

    @Enumerated( EnumType.STRING )
    @JsonProperty( "shopper_interaction" )
    private ShoppingInformation shopperInteraction;
    @JsonProperty( "account" )
    private String accountId;
}
