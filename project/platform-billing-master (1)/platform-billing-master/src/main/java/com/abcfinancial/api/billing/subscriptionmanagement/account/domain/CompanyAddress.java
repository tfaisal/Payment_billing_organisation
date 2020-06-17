package com.abcfinancial.api.billing.subscriptionmanagement.account.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class CompanyAddress
{
    /**
     * Address Line
     */

    @NotNull( message = "Line can not be null" )
    private String line;
    /**
     * ISO 3166 code identifying the region within the country.
     * Fully spelled out names of the region in ISO supported languages
     * as eg.CA
     */

    @NotNull( message = "region can not be null" )
    private String region;
    /**
     * City name
     */

    @NotNull( message = "City can not be null" )
    @Size( min = 1, max = 50, message = "size of the field should be between 0 to 50" )
    private String city;
    /**
     * Name or ISO 3166 code identifying the country. as e.g US
     */

    @NotNull( message = "Country can not be null" )
    private String country;
    /**
     * Postal Code
     */

    @NotNull( message = "Postal code can not be null" )
    @Size( min = 1, max = 10, message = "Size should be between 0 to 10" )
    private String postalCode;
}
