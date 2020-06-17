package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data

public class ResolveAddressResponse
{
    /**
     * Avalara location Id in java.lang.Long format
     */

    @JsonProperty( "avaLocationId" )
    @NotNull
    private Long locationId;
    /**
     * Avalara Location Address
     */

    @NotNull
    @JsonProperty( "avaAddress" )
    private ResponseAddress responseAddress;

}
