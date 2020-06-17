package com.abcfinancial.api.billing.utility.common.valueobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data

public class AuthorisationTokenVO
{
    @JsonProperty( value = "access_token" )
    private String accessToken;
}
