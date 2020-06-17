package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonPropertyOrder( { "latitude", "longitude" } )
@Data

public class Coordinates implements Serializable
{
    @JsonProperty( "latitude" )
    double latitude;
    @JsonProperty( "longitude" )
    double longitude;
}
