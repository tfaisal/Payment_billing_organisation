package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@JsonIgnoreProperties( ignoreUnknown = true )

public class LocationAccount
{

    /**
     * A code that identifies this location. Must be unique within your company.
     */

    @NotNull
    @JsonProperty( "locationCode" )
    @Size( max = 50, message = "location Code must be in range of 0 to 50." )
    private String locationCode;
    /**
     * Indicates whether this location is a physical place of business or a temporary salesperson location.
     */

    @NotNull
    @JsonProperty( "addressTypeId" )
    private AddressTypeId addressTypeId;
    /**
     * Indicates the type of place of business represented by this location.
     */

    @NotNull
    @JsonProperty( "addressCategoryId" )
    private AddressCategoryId addressCategoryId;
    /**
     * The FIRST line of the physical address of this location.
     */

    @NotNull
    @JsonProperty( "line1" )
    @Size( max = 50, message = "The FIRST line of the physical address of this location must be in range of 0 to 50." )
    private String line1;
    /**
     * The postal code or zip code of the physical address of this location.
     */

    @NotNull
    @JsonProperty( "postalCode" )
    @Size( max = 10, message = "The postal code or zip code of the physical address of this location must be in range of 0 to 10." )
    private String postalCode;
    /**
     * Name identifying the country of the physical address of this location.
     */

    @NotNull
    @JsonProperty( "country" )
    private String country;
    /**
     * Name or ISO 3166 code identifying the city. as e.g IRVINE
     */

    @NotNull
    @JsonProperty( "city" )
    private String city;
    /**
     * Name or ISO 3166 code identifying the region within the country.as eg.CA
     */

    @NotNull
    @JsonProperty( "region" )
    private String region;

}
