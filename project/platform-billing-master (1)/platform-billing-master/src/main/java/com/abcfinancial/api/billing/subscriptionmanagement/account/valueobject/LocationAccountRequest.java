package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class LocationAccountRequest
{
    /**
     * An unique id which behaves as the identification of a particular registered organization and its location.
     * It should be in java.util.UUID format.
     */

    @NotNull( message = "not allowed for locationId" )
    private UUID locationId;
    /**
     * An unique id which behaves as the identification of a particular client.
     * It should be in java.util.UUID format
     */

    private UUID clientId;
    /**
     * locationNumber
     */
    @Size( min = 1, max = 20 )
    private String locationNumber;
    /**
     * Details to create location account
     */

    @NotNull( message = "Account details cannot be null" )
    private AccountVO account;
    /**
     * Avalara On-boarding details.
     */

    private AvalaraAccountRequest avalaraAccount;
}
