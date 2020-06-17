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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Data

@JsonInclude( JsonInclude.Include.NON_NULL )

public class PaymentMethodRequestVO
{
    /**
     * Payor's accountId
     */
    @NotNull( message = "not allowed for accountId" )
    private UUID accountId;
    /**
     * Type of Payment Method.
     * <p>
     * For Payor account, only BANK_ACCOUNT or CREDIT_CARD is allowed.
     */
    @NotNull( message = "not allowed for type" )
    @Enumerated( EnumType.STRING )
    private Type type;
    /**
     * Mandatory for BANK_ACCOUNT.
     */
    @Enumerated( EnumType.STRING )
    private BankAccountType bankAccountType;
    /**
     * Last four Character of card/Account Number.
     */
    @Size( min = 4, max = 4 )
    private String display;
    /**
     * Routing accountNumber required for processing US bank accounts through the ACH network
     */
    @Pattern( regexp = "^(0|1|2|3)[0-9]{8}$", message = "routing number should be 9 digit numeric and starts with 0, 1, 2, 3 only" )
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
    private int expiryYear;
    /**
     * Expiry month of card.
     * <p>
     * Not applicable with payment method
     * type as BANK_ACCOUNT.
     */
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
    @Size( min = 1, max = 50 )
    private String alias;
    /**
     * accountHolderName
     */
    @Size( min = 1, max = 100 )
    private String accountHolderName;
    /**
     * tokenId Tokenized/Encrypted
     */
    private UUID tokenId;
    /**
     * Billing Date
     */
    @NotNull( message = "not allowed for billingDate" )
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    private LocalDate billingDate;

    @NotNull( message = "not allowed for sevaluation" )
    @Enumerated( EnumType.STRING )
    private Frequency sevaluation;

}
