package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonPropertyOrder( {
                        "@recordsetCount",
                        "value"
                    } )

public class NexusResModel
{
    @JsonProperty( "@recordsetCount" )
    private int recordsetCount;
    @JsonProperty( "value" )
    private List<NexusResponseModel> value;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * No args constructor for use in serialization
     */

    public NexusResModel()
    {
    }

    /**
     * @param value
     * @param recordsetCount
     */

    public NexusResModel( int recordsetCount, List<NexusResponseModel> value )
    {
        super();
        this.recordsetCount = recordsetCount;
        this.value = value;
    }

    @JsonProperty( "@recordsetCount" )
    public int getRecordsetCount()
    {
        return recordsetCount;
    }

    @JsonProperty( "@recordsetCount" )
    public void setRecordsetCount( int recordsetCount )
    {
        this.recordsetCount = recordsetCount;
    }

    @JsonProperty( "value" )
    public List<NexusResponseModel> getValue()
    {
        return value;
    }

    @JsonProperty( "value" )
    public void setValue( List<NexusResponseModel> value )
    {
        this.value = value;
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
        return new ToStringBuilder( this ).append( "recordsetCount", recordsetCount ).append( "value", value ).append( "additionalProperties", additionalProperties ).toString();
    }
}
