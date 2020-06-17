package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Size;
import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )

public class AddressLocationInfo implements Serializable
{
    private static final long serialVersionUID = -9090822204189111091L;
    /**
     * Location Code
     */

    @JsonProperty( "locationCode" )
    private String locationCode;
    /**
     * First line of the street address
     */

    @Size( min = 0, max = 50 )
    @JsonProperty( "line1" )
    private String line1;
    /**
     * Second line of the street address
     */

    @Size( min = 0, max = 100 )
    @JsonProperty( "line2" )
    private String line2;
    /**
     * Third line of the street address
     */

    @Size( min = 0, max = 100 )
    @JsonProperty( "line3" )
    private String line3;
    /**
     * City
     */

    @Size( min = 0, max = 50 )
    @JsonProperty( "city" )
    private String city;
    /**
     * Name of region
     */

    @JsonProperty( "region" )
    private String region;
    /**
     * Name of Country
     */

    @JsonProperty( "country" )
    private String country;
    /**
     * Postal Code/Zip Code
     */

    @Size( min = 0, max = 11 )
    @JsonProperty( "postalCode" )
    private String postalCode;

    public String getLocationCode()
    {
        return locationCode;
    }

    public void setLocationCode( String locationCode )
    {
        this.locationCode = locationCode;
    }

    public String getLine1()
    {
        return line1;
    }

    public void setLine1( String line1 )
    {
        this.line1 = line1;
    }

    public String getLine2()
    {
        return line2;
    }

    public void setLine2( String line2 )
    {
        this.line2 = line2;
    }

    public String getLine3()
    {
        return line3;
    }

    public void setLine3( String line3 )
    {
        this.line3 = line3;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity( String city )
    {
        this.city = city;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion( String region )
    {
        this.region = region;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry( String country )
    {
        this.country = country;
    }

    public String getPostalCode()
    {
        return postalCode;
    }

    public void setPostalCode( String postalCode )
    {
        this.postalCode = postalCode;
    }

    @Override
    public String toString()
    {
        return "AddressLocationInfo{" +
               "locationCode = '" + locationCode + '\'' +
               ", line1 = '" + line1 + '\'' +
               ", line2 = '" + line2 + '\'' +
               ", line3 = '" + line3 + '\'' +
               ", city = '" + city + '\'' +
               ", region = '" + region + '\'' +
               ", country = '" + country + '\'' +
               ", postalCode = '" + postalCode + '\'' +
               '}';
    }
}
