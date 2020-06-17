package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonPropertyOrder( {
                        "id",
                        "accountId",
                        "sstPid",
                        "companyCode",
                        "name",
                        "isDefault",
                        "isActive",
                        "taxpayerIdNumber",
                        "hasProfile",
                        "isReportingEntity",
                        "defaultCountry",
                        "baseCurrencyCode",
                        "roundingLevelId",
                        "warningsEnabled",
                        "isTest",
                        "taxDependencyLevelId",
                        "inProgress",
                        "createdDate",
                        "createdUserId",
                        "modifiedDate",
                        "modifiedUserId"
                    } )

public class Value implements Serializable
{
    @JsonProperty( "id" )
    private int id;
    @JsonProperty( "accountId" )
    private int accountId;
    @JsonProperty( "sstPid" )
    private String sstPid;
    @JsonProperty( "companyCode" )
    private String companyCode;
    @JsonProperty( "name" )
    private String name;
    @JsonProperty( "isDefault" )
    private boolean isDefault;
    @JsonProperty( "isActive" )
    private boolean isActive;
    @JsonProperty( "taxpayerIdNumber" )
    private String taxpayerIdNumber;
    @JsonProperty( "hasProfile" )
    private boolean hasProfile;
    @JsonProperty( "isReportingEntity" )
    private boolean isReportingEntity;
    @JsonProperty( "defaultCountry" )
    private String defaultCountry;
    @JsonProperty( "baseCurrencyCode" )
    private String baseCurrencyCode;
    @JsonProperty( "roundingLevelId" )
    private String roundingLevelId;
    @JsonProperty( "warningsEnabled" )
    private boolean warningsEnabled;
    @JsonProperty( "isTest" )
    private boolean isTest;
    @JsonProperty( "taxDependencyLevelId" )
    private String taxDependencyLevelId;
    @JsonProperty( "inProgress" )
    private boolean inProgress;
    @JsonProperty( "createdDate" )
    private String createdDate;
    @JsonProperty( "createdUserId" )
    private int createdUserId;
    @JsonProperty( "modifiedDate" )
    private String modifiedDate;
    @JsonProperty( "modifiedUserId" )
    private int modifiedUserId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * No args constructor for use in serialization
     */

    public Value()
    {
    }

    /**
     * @param warningsEnabled
     * @param sstPid
     * @param accountId
     * @param defaultCountry
     * @param roundingLevelId
     * @param isTest
     * @param modifiedUserId
     * @param inProgress
     * @param baseCurrencyCode
     * @param modifiedDate
     * @param companyCode
     * @param hasProfile
     * @param id
     * @param isActive
     * @param createdUserId
     * @param isDefault
     * @param taxpayerIdNumber
     * @param name
     * @param isReportingEntity
     * @param createdDate
     * @param taxDependencyLevelId
     */

    public Value( int id, int accountId, String sstPid, String companyCode, String name, boolean isDefault, boolean isActive, String taxpayerIdNumber, boolean hasProfile,
        boolean isReportingEntity, String defaultCountry, String baseCurrencyCode, String roundingLevelId, boolean warningsEnabled, boolean isTest, String taxDependencyLevelId,
        boolean inProgress, String createdDate, int createdUserId, String modifiedDate, int modifiedUserId )
    {
        super();
        this.id = id;
        this.accountId = accountId;
        this.sstPid = sstPid;
        this.companyCode = companyCode;
        this.name = name;
        this.isDefault = isDefault;
        this.isActive = isActive;
        this.taxpayerIdNumber = taxpayerIdNumber;
        this.hasProfile = hasProfile;
        this.isReportingEntity = isReportingEntity;
        this.defaultCountry = defaultCountry;
        this.baseCurrencyCode = baseCurrencyCode;
        this.roundingLevelId = roundingLevelId;
        this.warningsEnabled = warningsEnabled;
        this.isTest = isTest;
        this.taxDependencyLevelId = taxDependencyLevelId;
        this.inProgress = inProgress;
        this.createdDate = createdDate;
        this.createdUserId = createdUserId;
        this.modifiedDate = modifiedDate;
        this.modifiedUserId = modifiedUserId;
    }

    @JsonProperty( "id" )
    public int getId()
    {
        return id;
    }

    @JsonProperty( "id" )
    public void setId( int id )
    {
        this.id = id;
    }

    @JsonProperty( "accountId" )
    public int getAccountId()
    {
        return accountId;
    }

    @JsonProperty( "accountId" )
    public void setAccountId( int accountId )
    {
        this.accountId = accountId;
    }

    @JsonProperty( "sstPid" )
    public String getSstPid()
    {
        return sstPid;
    }

    @JsonProperty( "sstPid" )
    public void setSstPid( String sstPid )
    {
        this.sstPid = sstPid;
    }

    @JsonProperty( "companyCode" )
    public String getCompanyCode()
    {
        return companyCode;
    }

    @JsonProperty( "companyCode" )
    public void setCompanyCode( String companyCode )
    {
        this.companyCode = companyCode;
    }

    @JsonProperty( "name" )
    public String getName()
    {
        return name;
    }

    @JsonProperty( "name" )
    public void setName( String name )
    {
        this.name = name;
    }

    @JsonProperty( "isDefault" )
    public boolean isIsDefault()
    {
        return isDefault;
    }

    @JsonProperty( "isDefault" )
    public void setIsDefault( boolean isDefault )
    {
        this.isDefault = isDefault;
    }

    @JsonProperty( "isActive" )
    public boolean isIsActive()
    {
        return isActive;
    }

    @JsonProperty( "isActive" )
    public void setIsActive( boolean isActive )
    {
        this.isActive = isActive;
    }

    @JsonProperty( "taxpayerIdNumber" )
    public String getTaxpayerIdNumber()
    {
        return taxpayerIdNumber;
    }

    @JsonProperty( "taxpayerIdNumber" )
    public void setTaxpayerIdNumber( String taxpayerIdNumber )
    {
        this.taxpayerIdNumber = taxpayerIdNumber;
    }

    @JsonProperty( "hasProfile" )
    public boolean isHasProfile()
    {
        return hasProfile;
    }

    @JsonProperty( "hasProfile" )
    public void setHasProfile( boolean hasProfile )
    {
        this.hasProfile = hasProfile;
    }

    @JsonProperty( "isReportingEntity" )
    public boolean isIsReportingEntity()
    {
        return isReportingEntity;
    }

    @JsonProperty( "isReportingEntity" )
    public void setIsReportingEntity( boolean isReportingEntity )
    {
        this.isReportingEntity = isReportingEntity;
    }

    @JsonProperty( "defaultCountry" )
    public String getDefaultCountry()
    {
        return defaultCountry;
    }

    @JsonProperty( "defaultCountry" )
    public void setDefaultCountry( String defaultCountry )
    {
        this.defaultCountry = defaultCountry;
    }

    @JsonProperty( "baseCurrencyCode" )
    public String getBaseCurrencyCode()
    {
        return baseCurrencyCode;
    }

    @JsonProperty( "baseCurrencyCode" )
    public void setBaseCurrencyCode( String baseCurrencyCode )
    {
        this.baseCurrencyCode = baseCurrencyCode;
    }

    @JsonProperty( "roundingLevelId" )
    public String getRoundingLevelId()
    {
        return roundingLevelId;
    }

    @JsonProperty( "roundingLevelId" )
    public void setRoundingLevelId( String roundingLevelId )
    {
        this.roundingLevelId = roundingLevelId;
    }

    @JsonProperty( "warningsEnabled" )
    public boolean isWarningsEnabled()
    {
        return warningsEnabled;
    }

    @JsonProperty( "warningsEnabled" )
    public void setWarningsEnabled( boolean warningsEnabled )
    {
        this.warningsEnabled = warningsEnabled;
    }

    @JsonProperty( "isTest" )
    public boolean isIsTest()
    {
        return isTest;
    }

    @JsonProperty( "isTest" )
    public void setIsTest( boolean isTest )
    {
        this.isTest = isTest;
    }

    @JsonProperty( "taxDependencyLevelId" )
    public String getTaxDependencyLevelId()
    {
        return taxDependencyLevelId;
    }

    @JsonProperty( "taxDependencyLevelId" )
    public void setTaxDependencyLevelId( String taxDependencyLevelId )
    {
        this.taxDependencyLevelId = taxDependencyLevelId;
    }

    @JsonProperty( "inProgress" )
    public boolean isInProgress()
    {
        return inProgress;
    }

    @JsonProperty( "inProgress" )
    public void setInProgress( boolean inProgress )
    {
        this.inProgress = inProgress;
    }

    @JsonProperty( "createdDate" )
    public String getCreatedDate()
    {
        return createdDate;
    }

    @JsonProperty( "createdDate" )
    public void setCreatedDate( String createdDate )
    {
        this.createdDate = createdDate;
    }

    @JsonProperty( "createdUserId" )
    public int getCreatedUserId()
    {
        return createdUserId;
    }

    @JsonProperty( "createdUserId" )
    public void setCreatedUserId( int createdUserId )
    {
        this.createdUserId = createdUserId;
    }

    @JsonProperty( "modifiedDate" )
    public String getModifiedDate()
    {
        return modifiedDate;
    }

    @JsonProperty( "modifiedDate" )
    public void setModifiedDate( String modifiedDate )
    {
        this.modifiedDate = modifiedDate;
    }

    @JsonProperty( "modifiedUserId" )
    public int getModifiedUserId()
    {
        return modifiedUserId;
    }

    @JsonProperty( "modifiedUserId" )
    public void setModifiedUserId( int modifiedUserId )
    {
        this.modifiedUserId = modifiedUserId;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty( String name, Object value )
    {
        this.additionalProperties.put( name, value );
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder( this ).append( "id", id ).append( "accountId", accountId ).append( "sstPid", sstPid ).append( "companyCode", companyCode )
                                          .append( "name", name ).append( "isDefault", isDefault ).append( "isActive", isActive ).append( "taxpayerIdNumber", taxpayerIdNumber )
                                          .append( "hasProfile", hasProfile ).append( "isReportingEntity", isReportingEntity ).append( "defaultCountry", defaultCountry )
                                          .append( "baseCurrencyCode", baseCurrencyCode ).append( "roundingLevelId", roundingLevelId ).append( "warningsEnabled", warningsEnabled )
                                          .append( "isTest", isTest ).append( "taxDependencyLevelId", taxDependencyLevelId ).append( "inProgress", inProgress )
                                          .append( "createdDate", createdDate ).append( "createdUserId", createdUserId ).append( "modifiedDate", modifiedDate )
                                          .append( "modifiedUserId", modifiedUserId ).append( "additionalProperties", additionalProperties ).toString();
    }
}
