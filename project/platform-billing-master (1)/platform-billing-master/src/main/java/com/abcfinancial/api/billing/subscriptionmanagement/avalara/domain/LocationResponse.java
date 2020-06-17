package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder( {
                        "@recordsetCount",
                        "value"
                    } )

public class LocationResponse
{
    @JsonProperty( "@recordsetCount" )
    private int recordsetCount;
    @JsonProperty( "value" )
    private List<LocationModel> value;

    /**
     * No args constructor for use in serialization
     */

    public LocationResponse()
    {
    }

    public LocationResponse( int recordsetCount, List<LocationModel> value )
    {
        this.recordsetCount = recordsetCount;
        this.value = value;
    }

    public int getRecordsetCount()
    {
        return recordsetCount;
    }

    public void setRecordsetCount( int recordsetCount )
    {
        this.recordsetCount = recordsetCount;
    }

    public List<LocationModel> getValue()
    {
        return value;
    }

    public void setValue( List<LocationModel> value )
    {
        this.value = value;
    }
}
