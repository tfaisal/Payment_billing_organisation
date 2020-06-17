package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )

@Data

public class ValidatedAddress
{
    private String addressType;
    private String line1;
    private String city;
    private String region;
    private String country;
    private String postalCode;
    private String latitude;
    private String longitude;
}
