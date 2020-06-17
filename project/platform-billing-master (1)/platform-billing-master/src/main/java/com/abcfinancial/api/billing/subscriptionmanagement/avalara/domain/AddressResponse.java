package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )

@Data

public class AddressResponse
{
    private Address address;
    private List<ValidatedAddress> validatedAddresses;
    private Coordinates coordinates;
    private String resolutionQuality;
    private List<TaxAuthority> taxAuthorities;
    private List<Messages> messages;
}
