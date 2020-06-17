package com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AccountVO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class LocationAccountVO
{
    /**
     * An unique id which behaves as the identification of a particular registered organization and its location.
     */

    @NotNull
    private UUID locationId;
    /**
     * An unique id which behaves as the identification of a particular client.
     */

    private UUID clientId;
    /**
     * An unique id which behaves as the identification of a particular merchant.
     */

    private UUID merchantId;
    /**
     * Details to create location account
     */

    @NotNull( message = "Account details cannot be null" )
    private AccountVO account;
}
