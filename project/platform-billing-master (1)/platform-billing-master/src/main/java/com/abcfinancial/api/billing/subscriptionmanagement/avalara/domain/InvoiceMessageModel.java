package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@JsonInclude( JsonInclude.Include.NON_NULL )
@Data

public class InvoiceMessageModel implements Serializable
{
    /**
     * The content of the invoice message.
     */

    @JsonProperty( "content" )
    private String content;
    /**
     * The applicable tax line numbers and codes.
     */

    @JsonProperty( "lineNumbers" )
    private List<String> lineNumbers;
}
