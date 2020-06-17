package com.abcfinancial.api.billing.subscriptionmanagement.avalara.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class NexusModel implements Serializable
{
    @NotNull( message = "Country could not be null." )
    @JsonProperty( "country" )
    private String country;
    @NotNull( message = "Region could not be null." )
    @JsonProperty( "region" )
    private String region;
    @NotNull( message = "jurisTypeId could not be null." )
    @JsonProperty( "jurisTypeId" )
    private String jurisTypeId;
    @NotNull( message = "jurisdictionTypeId could not be null." )
    @JsonProperty( "jurisdictionTypeId" )
    private String jurisdictionTypeId;
    @NotNull( message = "jurisCode could not be null." )
    @JsonProperty( "jurisCode" )
    private String jurisCode;
    @NotNull( message = "jurisName could not be null." )
    @JsonProperty( "jurisName" )
    private String jurisName;
    @NotNull( message = "shortName could not be null." )
    @JsonProperty( "shortName" )
    private String shortName;
    @NotNull( message = "nexusTypeId could not be null." )
    @JsonProperty( "nexusTypeId" )
    private String nexusTypeId;
    @JsonProperty( "hasLocalNexus" )
    boolean hasLocalNexus;
    @JsonProperty( "hasPermanentEstablishment" )
    boolean hasPermanentEstablishment;
    @JsonProperty( "nexusTaxTypeGroup" )
    String nexusTaxTypeGroup;

}
