package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )

@Data

public class TransactionLineLocationTypeModel implements Serializable
{
    /**
     * Document Line Location Type id( Read Only )
     * The unique ID number of this line location address model
     */

    @JsonProperty( "documentLineLocationTypeId" )
    private long documentLineLocationTypeId;
    /**
     * Document line Id( Read only )
     * The unique ID number of the document line associated with this line location address model
     */

    @JsonProperty( "documentLineId" )
    private long documentLineId;
    /**
     * Document Address Id( Read only )
     * The address ID corresponding to this model
     */

    @JsonProperty( "documentAddressId" )
    private long documentAddressId;
    /**
     * Location Type code( Read only )
     * The location type code corresponding to this model
     */

    @JsonProperty( "locationTypeCode" )
    private String locationTypeCode;
}
