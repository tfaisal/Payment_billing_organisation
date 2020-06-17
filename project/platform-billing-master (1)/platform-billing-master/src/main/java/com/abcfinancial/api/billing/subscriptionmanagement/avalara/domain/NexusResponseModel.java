package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )

public class NexusResponseModel implements Serializable
{
    /**
     * Nexus Id.
     * Read Only
     */

    @NotNull
    @JsonProperty( "id" )
    private Long nexusid;
    /**
     * The unique ID number of the company that declared nexus.
     */

    @NotNull
    @JsonProperty( "companyId" )
    private String companyId;
    /**
     * Name or ISO 3166 code identifying the country in which this company declared nexus.
     * eg. US
     */

    @NotNull
    @JsonProperty( "country" )
    private String country;
    /**
     * Name or ISO 3166 code identifying the region within the country.
     * eg. CA
     */

    @NotNull
    @JsonProperty( "region" )
    private String region;
    /**
     * he jurisdiction type of the jurisdiction in which this company declared nexus.
     * NOTE: Use jurisdictionTypeId instead. eg. STA
     */

    @NotNull
    @JsonProperty( "jurisTypeId" )
    private String jurisTypeId;
    /**
     * The code identifying the jurisdiction in which this company declared nexus.
     * This field is defined by Avalara. All Avalara-defined fields must match an
     * Avalara-defined nexus object found by calling ListNexus.eg 06.
     */

    @NotNull
    @JsonProperty( "jurisCode" )
    private String jurisCode;
    /**
     * The common name of the jurisdiction in which this company declared nexus.
     * This field is defined by Avalara. All Avalara-defined fields must match an
     * Avalara-defined nexus object found by calling ListNexus.eg CALIFORNIA
     */

    @NotNull
    @JsonProperty( "jurisName" )
    private String jurisName;
    /**
     * The short name of the jurisdiction.This field is defined by Avalara.
     * All Avalara-defined fields must match an Avalara-defined nexus object
     * found by calling ListNexus.
     */

    @NotNull
    @JsonProperty( "shortName" )
    private String shortName;
    /**
     * The type of nexus that this company is declaring.
     */

    @NotNull
    @JsonProperty( "nexusTypeId" )
    private String nexusTypeId;
    /**
     * User modified Id
     */

    @NotNull
    @JsonProperty( "modifiedUserId" )
    private String modifiedUserId;

    public Long getNexusid()
    {
        return nexusid;
    }

    public void setNexusid( Long nexusid )
    {
        this.nexusid = nexusid;
    }

    public String getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId( String companyId )
    {
        this.companyId = companyId;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry( String country )
    {
        this.country = country;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion( String region )
    {
        this.region = region;
    }

    public String getJurisTypeId()
    {
        return jurisTypeId;
    }

    public void setJurisTypeId( String jurisTypeId )
    {
        this.jurisTypeId = jurisTypeId;
    }

    public String getJurisCode()
    {
        return jurisCode;
    }

    public void setJurisCode( String jurisCode )
    {
        this.jurisCode = jurisCode;
    }

    public String getJurisName()
    {
        return jurisName;
    }

    public void setJurisName( String jurisName )
    {
        this.jurisName = jurisName;
    }

    public String getShortName()
    {
        return shortName;
    }

    public void setShortName( String shortName )
    {
        this.shortName = shortName;
    }

    public String getNexusTypeId()
    {
        return nexusTypeId;
    }

    public void setNexusTypeId( String nexusTypeId )
    {
        this.nexusTypeId = nexusTypeId;
    }

    public String getModifiedUserId()
    {
        return modifiedUserId;
    }

    public void setModifiedUserId( String modifiedUserId )
    {
        this.modifiedUserId = modifiedUserId;
    }
}

