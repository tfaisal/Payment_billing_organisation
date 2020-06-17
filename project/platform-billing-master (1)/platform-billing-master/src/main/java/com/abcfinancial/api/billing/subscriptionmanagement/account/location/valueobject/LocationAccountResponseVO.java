package com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.OnboardingAccountResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class LocationAccountResponseVO
{
    /**
     * An unique id which behaves as the identification of a particular registered organization and its location.
     * It should be in java.util.UUID format
     */

    @NotNull
    private UUID locationId;
    /**
     * An unique id which behaves as the identification of a particular client.
     * It should be in java.util.UUID format
     */

    private UUID clientId;
    /**
     * An unique id which behaves as the identification of a particular merchant.
     * It should be in java.util.UUID format
     */

    @NotNull
    private UUID merchantId;

    @Override
    public String toString( ) {
        return "LocationAccountResponseVO{" +
                "locationId = " + locationId +
                ", clientId = " + clientId +
                ", merchantId = " + merchantId +
                ", account = " + account +
                ", onboardingAccountResponse = " + onboardingAccountResponse +
                '}';
    }
    /**
     * Details to create location account
     */

    @NotNull( message = "Account details cannot be null" )
    private AccountVO account;
    /**
     * Avalara account created details.
     */

    private OnboardingAccountResponse onboardingAccountResponse;
}
