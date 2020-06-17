package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude( JsonInclude.Include.NON_NULL )

@Data
@JsonIgnoreProperties( ignoreUnknown = true )

public class Messages
{
    @JsonProperty
    private String summary;
    @JsonProperty
    private String details;
    @JsonProperty
    private String refersTo;
    @JsonProperty
    private String severity;
    @JsonProperty
    private String source;
}
