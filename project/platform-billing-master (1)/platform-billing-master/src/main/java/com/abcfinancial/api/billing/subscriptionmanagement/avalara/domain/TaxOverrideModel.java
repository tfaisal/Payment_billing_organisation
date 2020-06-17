package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@JsonInclude( JsonInclude.Include.NON_NULL )

@Data

public class TaxOverrideModel implements Serializable
{
    /**
     * Total override of the calculated tax on the document
     */

    @JsonProperty( "taxAmount" )
    private double taxAmount;
    /**
     * The override tax date to use
     */

    @JsonProperty( "taxDate" )
    private String taxDate;
    /**
     * Reason for a tax override for audit purposes
     */

    @JsonProperty( "reason" )
    private String reason;
}
