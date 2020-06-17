package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )

public class TransactionLocationTypeModel implements Serializable
{
    /**
     * Location type ID( Read Only )
     */

    @JsonProperty( "documentLocationTypeId" )
    private long documentLocationTypeId;
    /**
     * Transaction ID( Read Only )
     */

    @JsonProperty( "documentId" )
    private long documentId;
    /**
     * Address ID for the transaction( Read Only )
     */

    @JsonProperty( "documentAddressId" )
    private long documentAddressId;
    /**
     * Location type code( Read Only )
     */

    @JsonProperty( "locationTypeCode" )
    private String locationTypeCode;

}
