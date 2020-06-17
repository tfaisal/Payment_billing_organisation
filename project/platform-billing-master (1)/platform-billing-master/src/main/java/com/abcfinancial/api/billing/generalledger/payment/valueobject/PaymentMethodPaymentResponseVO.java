package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import com.abcfinancial.api.billing.generalledger.payment.domain.BankAccountType;
import com.abcfinancial.api.billing.generalledger.payment.domain.Brand;
import com.abcfinancial.api.billing.generalledger.payment.domain.Processor;
import com.abcfinancial.api.billing.generalledger.payment.domain.Type;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;

import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class PaymentMethodPaymentResponseVO
{
    /**
     * Payment Method id
     */

    private UUID id;
    /**
     * Type of Payment Method.
     *<p>
     * For location account, only BANK_ACCOUNT is allowed.
     */

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
     */

    @JsonIgnore
    private UUID locationId;
    /**
     * Alias for Bank Account.
     * Not applicable with payment method type as CREDIT_CARD.
     */

    private String alias;

    @JsonIgnore
    public boolean isEmpty()
    {
        if( ( type == Type.BANK_ACCOUNT && Strings.isEmpty( accountNumber ) && Strings.isEmpty( routingNumber ) && Strings.isEmpty( alias ) && bankAccountType == null ) ||
            ( type == Type.CREDIT_CARD && Strings.isEmpty( token ) && processor == null ) )
        {
            return true;
        }
        return false;
    }
}
