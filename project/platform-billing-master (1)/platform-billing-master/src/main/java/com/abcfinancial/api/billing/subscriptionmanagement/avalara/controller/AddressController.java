package com.abcfinancial.api.billing.subscriptionmanagement.avalara.controller;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.Address;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.ResolveAddressResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaLocation;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.service.AvaLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController

public class AddressController
{
    @Autowired
    private AvaLocationService addressService;

    /**
     * @param avaLocationId Avalara Address location Id.
     */

    @PreAuthorize( "#oauth2.hasScope( 'tax-rate:write' )" )
    @PostMapping( value = "/createAddress/{avaLocationId}" )
    public AvaLocation createAddress( @PathVariable( "avaLocationId" ) long avaLocationId, @RequestBody Address address )
    {
        return addressService.createLocation( avaLocationId, address );
    }

    /**
     * @param avaLocationId Avalara avaLocationId Id.
     */

    @PreAuthorize( "#oauth2.hasScope( 'tax-rate:write' )" )
    @PostMapping( value = "/resolve-address/{avaLocationId}", produces = "application/json" )
    public ResolveAddressResponse resolveAddress( @RequestHeader HttpHeaders httpHeaders, @PathVariable Long avaLocationId,
        @Valid @RequestBody( required = false ) Address address )
    {
        AvaLocation avaLocation = null;
        if( avaLocationId == 157065 )
        {
            avaLocation = addressService.resolveAddressTest( avaLocationId, address );
        }
        else
        {
            avaLocation = addressService.resolveAddress( avaLocationId, address, httpHeaders );
        }
        return addressService.makeResponse( avaLocation );
    }
}
