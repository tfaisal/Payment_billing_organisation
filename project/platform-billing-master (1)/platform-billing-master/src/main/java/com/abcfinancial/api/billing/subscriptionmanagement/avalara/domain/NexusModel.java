package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )

public class NexusModel implements Serializable
{
    @NotNull( message = "Country could not be null." )
    private String country;
    @NotNull( message = "Region could not be null." )
    private String region;
    @NotNull( message = "jurisTypeId could not be null." )
    private String jurisTypeId = "STA";
    @NotNull( message = "jurisdictionTypeId could not be null." )
    private String jurisdictionTypeId = "State";
    @NotNull( message = "jurisCode could not be null." )
    private String jurisCode = "06";
    @NotNull( message = "jurisName could not be null." )
    private String jurisName = "CALIFORNIA";
    @NotNull( message = "shortName could not be null." )
    private String shortName = "CA";
    @NotNull( message = "nexusTypeId could not be null." )
    private String nexusTypeId = "SalesOrSellersUseTax";

    public NexusModel()
    {
    }

    public NexusModel( String country, String region, String jurisTypeId, String jurisdictionTypeId, String jurisCode, String jurisName, String shortName, String nexusTypeId )
    {
        this.country = country;
        this.region = region;
        this.jurisTypeId = jurisTypeId;
        this.jurisdictionTypeId = jurisdictionTypeId;
        this.jurisCode = jurisCode;
        this.jurisName = jurisName;
        this.shortName = shortName;
        this.nexusTypeId = nexusTypeId;
    }

    public String getCountry()
    {
        return country;
    }

    public NexusModel setCountry( String country )
    {
        this.country = country;
        return this;
    }

    public String getRegion()
    {
        return region;
    }

    public NexusModel setRegion( String region )
    {
        this.region = region;
        return this;
    }

    public String getJurisTypeId()
    {
        return jurisTypeId;
    }

    public NexusModel setJurisTypeId( String jurisTypeId )
    {
        this.jurisTypeId = jurisTypeId;
        return this;
    }

    public String getJurisdictionTypeId()
    {
        return jurisdictionTypeId;
    }

    public NexusModel setJurisdictionTypeId( String jurisdictionTypeId )
    {
        this.jurisdictionTypeId = jurisdictionTypeId;
        return this;
    }

    public String getJurisCode()
    {
        return jurisCode;
    }

    public NexusModel setJurisCode( String jurisCode )
    {
        this.jurisCode = jurisCode;
        return this;
    }

    public String getJurisName()
    {
        return jurisName;
    }

    public NexusModel setJurisName( String jurisName )
    {
        this.jurisName = jurisName;
        return this;
    }

    public String getShortName()
    {
        return shortName;
    }

    public NexusModel setShortName( String shortName )
    {
        this.shortName = shortName;
        return this;
    }

    public String getNexusTypeId()
    {
        return nexusTypeId;
    }

    public NexusModel setNexusTypeId( String nexusTypeId )
    {
        this.nexusTypeId = nexusTypeId;
        return this;
    }

    @Override
    public String toString()
    {
        return "NexusModel{" +
               "country = '" + country + '\'' +
               ", region = '" + region + '\'' +
               ", jurisTypeId = '" + jurisTypeId + '\'' +
               ", jurisdictionTypeId = '" + jurisdictionTypeId + '\'' +
               ", jurisCode = '" + jurisCode + '\'' +
               ", jurisName = '" + jurisName + '\'' +
               ", shortName = '" + shortName + '\'' +
               ", nexusTypeId = '" + nexusTypeId + '\'' +
               '}';
    }
}
