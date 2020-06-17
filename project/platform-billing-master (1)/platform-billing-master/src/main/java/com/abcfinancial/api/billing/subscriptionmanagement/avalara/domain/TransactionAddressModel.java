package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )

@Data

public class TransactionAddressModel implements Serializable
{
    /**
     * Transaction address id ( Read only )
     * The unique ID number of this address.
     */

    @JsonProperty( "id" )
    private Long id;
    /**
     * Transaction Id( Read only )
     * The unique ID number of the document to which this address belongs.
     */

    @JsonProperty( "transactionId" )
    private Long transactionId;
    /**
     * Boundary Level( Read only )
     * The boundary level at which this address was validated.
     */

    @JsonProperty( "boundaryLevel" )
    private String boundaryLevel;
    /**
     * The FIRST line of the address.
     */

    @JsonProperty( "line1" )
    private String line1;
    /**
     * The city for the address.
     */

    @JsonProperty( "city" )
    private String city;
    /**
     * The ISO 3166 region code. E.g., the second part of ISO 3166-2.
     */

    @JsonProperty( "region" )
    private String region;
    /**
     * The postal code or zip code for the address.
     */

    @JsonProperty( "postalCode" )
    private String postalCode;
    /**
     * The ISO 3166 country code
     */

    @JsonProperty( "country" )
    private String country;
    /**
     * The unique ID number of the tax region for this address.
     */

    @JsonProperty( "taxRegionId" )
    private Long taxRegionId;
    /**
     * Latitude for this address
     */

    @JsonProperty( "latitude" )
    private double latitude;
    /**
     * Longitude for this address
     */

    @JsonProperty( "longitude" )
    private double longitude;

}
