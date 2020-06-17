package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import com.abcfinancial.api.billing.generalledger.payment.domain.BankAccountType;
import com.abcfinancial.api.billing.generalledger.payment.domain.Brand;
import com.abcfinancial.api.billing.generalledger.payment.domain.Type;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data

@JsonInclude( JsonInclude.Include.NON_NULL )

public class PaymentMethodResponseVO
{
    /**
     * Payment Method id
     */
    @NotNull
    private UUID id;
    /**
     * Payor's accountId
     */
    @NotNull
    private UUID accountId;
    /**
     * Type of Payment Method.
     * <p>
     * For location account, only BANK_ACCOUNT is allowed.
     */
    @NotNull
    @Enumerated( EnumType.STRING )
    private Type type;
    /**
     * Bank account type. mandatory Only if type is BANK_ACCOUNT.
     */
    @Enumerated( EnumType.STRING )
    private BankAccountType bankAccountType;
    /**
     * Last four digits of card/Account Number.
     */

    private String display;

    /**
     * Routing Number for Account.
     * <p>
     * Mandatory field in case of payment method type as BANK_ACCOUNT.
     */

    private String routingNumber;
    /**
     * Credit card brand Type
     * Not applicable with payment method type as BANK_ACCOUNT or CASH.
     */
    @Enumerated( EnumType.STRING )
    private Brand brand;
    /**
     * Expiry year of card.
     * Not applicable with payment method type as BANK_ACCOUNT.
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private int expiryYear;
    /**
     * Expiry month of card.
     * <p>
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

    /**
     * accountHolderName
     */
    private String accountHolderName;
    /**
     * tokenId Tokenized/Encrypted
     */
    private UUID tokenId;
    /**
     * Billing Date
     */
    @NotNull
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    private LocalDate billingDate;
    @NotNull
    private Frequency sevaluation;
    /**
     * True for active account else false.
     */
    @NotNull
    private Boolean active;

}
