package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodVO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class AccountDetailsVO
{
    /**
     * An unique id which behaves as the identification of a particular registered organization and its location.
     */

    @NotNull
    private UUID locationid;
    /**
     * Name of account holder
     */

    private String name;
    /**
     * Email of account holder
     */

    @Email( message = "Email should be valid" )
    private String email;
    /**
     * Phone number of account holder
     */

    private String phone;
    private PaymentMethodVO paymentMethodVO;
    private AccountVO account;
}
