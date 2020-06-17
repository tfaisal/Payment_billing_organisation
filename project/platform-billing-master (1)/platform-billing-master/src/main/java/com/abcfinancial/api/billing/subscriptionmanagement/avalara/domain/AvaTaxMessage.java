package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )
@Data

public class AvaTaxMessage implements Serializable
{
    private static final long serialVersionUID = 6178318759291669281L;
    /**
     * A brief summary of what this message tells us
     */

    @JsonProperty( "summary" )
    private String summary;
    /**
     * Detailed information that explains what the summary provided.
     */

    @JsonProperty( "details" )
    private String details;
    /**
     * Information about what object in your request this message refers to.
     */

    @JsonProperty( "refersTo" )
    private String refersTo;
    /**
     * A category that indicates how severely this message affects the results
     */

    @JsonProperty( "severity" )
    private String severity;
    /**
     * The name of the code or service that generated this message.
     */

    @JsonProperty( "source" )
    private String source;
}
