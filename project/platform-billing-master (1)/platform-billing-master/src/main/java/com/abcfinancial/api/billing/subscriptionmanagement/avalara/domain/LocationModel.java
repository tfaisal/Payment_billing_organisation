package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonPropertyOrder( {
                        "id",
                        "companyId",
                        "locationCode",
                        "description",
                        "addressTypeId",
                        "addressCategoryId",
                        "line1",
                        "city",
                        "county",
                        "postalCode",
                        "country",
                        "isDefault",
                        "isRegistered",
                        "dbaName",
                        "outletName",
                        "createdDate",
                        "createdUserId",
                        "modifiedDate",
                        "modifiedUserId"
                    } )

public class LocationModel implements Serializable
{
    @JsonProperty( "id" )
    private int id;
    @JsonProperty( "companyId" )
    private int companyId;
    @JsonProperty( "locationCode" )
    private String locationCode;
    @JsonProperty( "description" )
    private String description;
    @JsonProperty( "addressTypeId" )
    private String addressTypeId;
    @JsonProperty( "addressCategoryId" )
    private String addressCategoryId;
    @JsonProperty( "line1" )
    private String line1;
    @JsonProperty( "city" )
    private String city;
    @JsonProperty( "county" )
    private String county;
    @JsonProperty( "region" )
    private String region;
    @JsonProperty( "postalCode" )
    private String postalCode;
    @JsonProperty( "country" )
    private String country;
    @JsonProperty( "isDefault" )
    private String isDefault;
    @JsonProperty( "isRegistered" )
    private String isRegistered;
    @JsonProperty( "dbaName" )
    private String dbaName;
    @JsonProperty( "outletName" )
    private String outletName;
    private String createdDate;
    private long createdUserId;
    private String modifiedDate;
    private long modifiedUserId;

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public int getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId( int companyId )
    {
        this.companyId = companyId;
    }

    public String getLocationCode()
    {
        return locationCode;
    }

    public void setLocationCode( String locationCode )
    {
        this.locationCode = locationCode;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getAddressTypeId()
    {
        return addressTypeId;
    }

    public void setAddressTypeId( String addressTypeId )
    {
        this.addressTypeId = addressTypeId;
    }

    public String getAddressCategoryId()
    {
        return addressCategoryId;
    }

    public void setAddressCategoryId( String addressCategoryId )
    {
        this.addressCategoryId = addressCategoryId;
    }

    public String getLine1()
    {
        return line1;
    }

    public void setLine1( String line1 )
    {
        this.line1 = line1;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity( String city )
    {
        this.city = city;
    }

    public String getCounty()
    {
        return county;
    }

    public void setCounty( String county )
    {
        this.county = county;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion( String region )
    {
        this.region = region;
    }

    public String getPostalCode()
    {
        return postalCode;
    }

    public void setPostalCode( String postalCode )
    {
        this.postalCode = postalCode;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry( String country )
    {
        this.country = country;
    }

    public String getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault( String isDefault )
    {
        this.isDefault = isDefault;
    }

    public String getIsRegistered()
    {
        return isRegistered;
    }

    public void setIsRegistered( String isRegistered )
    {
        this.isRegistered = isRegistered;
    }

    public String getDbaName()
    {
        return dbaName;
    }

    public void setDbaName( String dbaName )
    {
        this.dbaName = dbaName;
    }

    public String getOutletName()
    {
        return outletName;
    }

    public void setOutletName( String outletName )
    {
        this.outletName = outletName;
    }

    public String getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate( String createdDate )
    {
        this.createdDate = createdDate;
    }

    public long getCreatedUserId()
    {
        return createdUserId;
    }

    public void setCreatedUserId( long createdUserId )
    {
        this.createdUserId = createdUserId;
    }

    public String getModifiedDate()
    {
        return modifiedDate;
    }

    public void setModifiedDate( String modifiedDate )
    {
        this.modifiedDate = modifiedDate;
    }

    public long getModifiedUserId()
    {
        return modifiedUserId;
    }

    public void setModifiedUserId( long modifiedUserId )
    {
        this.modifiedUserId = modifiedUserId;
    }

    @Override
    public String toString()
    {
        return "LocationModel{" +
               "id = " + id +
               ", companyId = " + companyId +
               ", locationCode = '" + locationCode + '\'' +
               ", description = '" + description + '\'' +
               ", addressTypeId = '" + addressTypeId + '\'' +
               ", addressCategoryId = '" + addressCategoryId + '\'' +
               ", line1 = '" + line1 + '\'' +
               ", city = '" + city + '\'' +
               ", county = '" + county + '\'' +
               ", region = '" + region + '\'' +
               ", postalCode = '" + postalCode + '\'' +
               ", country = '" + country + '\'' +
               ", isDefault = '" + isDefault + '\'' +
               ", isRegistered = '" + isRegistered + '\'' +
               ", dbaName = '" + dbaName + '\'' +
               ", outletName = '" + outletName + '\'' +
               ", createdDate = '" + createdDate + '\'' +
               ", createdUserId = " + createdUserId +
               ", modifiedDate = '" + modifiedDate + '\'' +
               ", modifiedUserId = " + modifiedUserId +
               '}';
    }
}
