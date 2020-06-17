package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;

@Data
@JsonIgnoreProperties( ignoreUnknown = true )

public class Address
{
    /**
     * Address Line
     */

    @NotNull
    private String line;
    /**
     * Address line1
     */

    private String line1;
    /**
     * City name
     */

    @NotNull
    private String city;
    /**
     * Name or ISO 3166 code identifying the country. as e.g US
     */

    @NotNull
    private String country;
    /**
     * Postal Code
     */

    @NotNull
    private String postalCode;
    /**
     * Name or ISO 3166 code identifying the region within the country.as eg.CA
     */

    @NotNull
    private String region;
}
