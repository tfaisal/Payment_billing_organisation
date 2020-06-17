package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.abcfinancial.api.billing.generalledger.payment.domain.BankAccountType;
import com.abcfinancial.api.billing.generalledger.payment.domain.Brand;
import com.abcfinancial.api.billing.generalledger.payment.domain.Processor;
import com.abcfinancial.api.billing.generalledger.payment.domain.Type;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class PaymentMethodVO
{
    /**
     * Payment Method id
     * It should be in java.util.UUID format
     */

    private UUID id;
    /**
     * Type of Payment Method.
     *<p>
     * For location account, only BANK_ACCOUNT is allowed.
     */

    @NotNull
    private Type type;
    /**
     * Bank account type. mandatory Only if type is BANK_ACCOUNT.
     */

    private BankAccountType bankAccountType;
    /**
     * Last four digits of card/Account Number.
     */

    private String display;
    private Processor processor;
    /**
     * True for active account else false.
     * ( Read Only )
     */

    @JsonIgnore
    private boolean isActive;
    /**
     * Routing Number for Account.
     *<p>
     * Mandatory field in case of payment method type as BANK_ACCOUNT.
     */

    private String routingNumber;
    /**
     * Credit card brand Type
     * Not applicable with payment method type as BANK_ACCOUNT or CASH.
     */

    private Brand brand;
    /**
     * Credit card token number.
     * Mandatory field in case of payment method type as CREDIT_CARD.
     * Not applicable with payment method type as BANK_ACCOUNT.
     */

    private String token;
    /**
     * Bank Account Number.
     * Mandatory field in case of Bank Account.
     * Not applicable with payment method type as CREDIT_CARD.
     */

    private String accountNumber;
    /**
     * Expiry year of card.
     * Not applicable with payment method type as BANK_ACCOUNT.
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private int expiryYear;
    /**
     * Expiry month of card.
     *<p>
     * Not applicable with payment method
     * type as BANK_ACCOUNT.
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private int expiryMonth;
    /**
     * An unique id which behaves as the identification of a
     * particular registered organization and its location.
     * It should be in java.util.UUID format
     */

    @JsonIgnore
    private UUID locationId;
    /**
     * Alias for Bank Account.
     * Not applicable with payment method type as CREDIT_CARD.
     */

    private String alias;
    /**
     * An unique id which behaves as the identification of a
     * payment method details.
     * It should be in java.util.UUID format
     */

    private UUID tokenId;

    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    private LocalDate billingDate;

    private Frequency sevaluation;

    @JsonIgnore
    public boolean isEmpty()
    {
        if( type == Type.BANK_ACCOUNT && Strings.isEmpty( accountNumber ) && Strings.isEmpty( routingNumber ) && Strings.isEmpty( alias ) && bankAccountType == null )
        {
            return true;
        }

        if( type == Type.CREDIT_CARD && Strings.isEmpty( token ) && processor == null )
        {
            return true;

        }

        return false;
    }
}
