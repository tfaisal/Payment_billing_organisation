package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data

public class ResponseAddress
{
    /**
     * Address Line
     */

    @NotNull
    private String line;
    /**
     * Address City name
     */

    @NotNull
    private String city;
    /**
     * Name or ISO 3166 code identifying the country. as e.g US
     */

    @NotNull
    private String country;
    /**
     * Address Postal Code
     */

    @NotNull
    private String postalCode;
    /**
     * Name or ISO 3166 code identifying the region within the country.as eg.CA
     */

    @NotNull
    private String region;
    /**
     * Avalara addressId should be in java.util.UUID format
     */

    @NotNull
    private UUID addressId;
    /**
     * Avalara Address isValidated
     */

    @NotNull
    private Boolean isValidated;
    /**
     * Avalara Address latitude
     */

    @NotNull
    private Double latitude;
    /**
     * Avalara Address longitude
     */

    @NotNull
    private Double longitude;
}
