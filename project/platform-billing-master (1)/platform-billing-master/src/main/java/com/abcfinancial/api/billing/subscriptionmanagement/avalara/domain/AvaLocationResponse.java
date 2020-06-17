package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
@Data
@NoArgsConstructor

public class AvaLocationResponse
{
    private String locationId;
    private String message;
}
