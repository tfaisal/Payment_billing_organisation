package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude( JsonInclude.Include.NON_NULL )

@Data

public class TaxAuthority
{
    private String avalaraId;
    private String jurisdictionName;
    private String jurisdictionType;
    private String signatureCode;
}
