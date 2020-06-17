package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAddress;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class AddressesModel implements Serializable
{
    private static final long serialVersionUID = -1770612439168776972L;
    /**
     * Single location
     */

    @JsonProperty( "singleLocation" )
    private AvaAddress singleLocation;
    /**
     * Ship from
     */

    @JsonProperty( "shipFrom" )
    private AvaAddress shipFrom;
    /**
     * Ship to
     */

    @JsonProperty( "shipTo" )
    private AvaAddress shipTo;
    /**
     * Point of order origin
     */

    @JsonProperty( "pointOfOrderOrigin" )
    private AvaAddress pointOfOrderOrigin;
    /**
     * Point of order Acceptance
     */

    @JsonProperty( "pointOfOrderAcceptance" )
    private AvaAddress pointOfOrderAcceptance;

}
