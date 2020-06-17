package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.RecurringType;
import com.abcfinancial.api.billing.utility.common.CommonUtil;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@JsonInclude( JsonInclude.Include.NON_NULL )
@Data

public class DimeboxCardTransactionRequestVO
{
    /**
     * The ID of the account
     */

    @NotNull
    @JsonProperty( "account" )
    private String accountId;
    /**
     * Amount is charged without a decimal place e.g. $1.5 = 150. Currencies can
     * have different decimals/exponentials, see Currencies Section for more details.
     */

    @NotNull
    @JsonProperty( "amount" )
    private Integer dimeboxAmount;

    public void setDimeboxAmount( BigDecimal amount )
    {
        if( amount.compareTo( BigDecimal.ZERO ) <= 0 || amount.compareTo( BigDecimal.valueOf( 21474836 ) ) > 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), DimeboxCardTransactionRequestVO.class,
                "Dimebox request amount should be in range between 0-21474836 cent." ) );
        }
        this.dimeboxAmount = CommonUtil.convertDollartoCent( amount );
    }

    /**
     * The token representing the payment card
     */

    @NotNull
    private String card;
    /**
     * The IP address of the customer
     */

    @NotNull
    @JsonProperty( "customer_ip" )
    private String customerIp;
    /**
     * A short reference / descriptor that will show up on the customers bank
     * statement. Please refer to the card payment integration guide for the
     * format requirements, which are specific per card processor.
     */

    @NotNull
    @JsonProperty( "dynamic_descriptor" )
    private String dynamicDescriptor;
    /**
     * A reference specified by the merchant to identify the transaction
     */

    @NotNull
    @JsonProperty( "merchant_reference" )
    private String merchantReference;
    /**
     * The full user agent string of the device the customer used to
     * submit the transaction
     */

    @NotNull
    @JsonProperty( "user_agent" )
    private String userAgent;
    /**
     * Number of installments for payment. Submitting a value of 1
     * will result in a normal transaction.
     */

    @JsonProperty( "installments" )
    private int installments;
    /**
     * This field is to be used only when a transaction is part of a
     * series of recurring transactions. If it's the initial transaction,
     * set the value to 'FIRST', and if it's not, set it to 'REPEAT'.
     * This field must not be included in transactions that will not be
     * part of a series of repeated transactions. ( 'FIRST' and 'REPEAT'
     * are written in all lowercase letters ). Valid Value:"FIRST" or "REPEAT"
     */

    @Enumerated( EnumType.STRING )
    @JsonProperty( "recurring_type" )
    private RecurringType recurringType;
}
