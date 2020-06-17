package com.abcfinancial.api.billing.subscriptionmanagement.avalara.controller;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.LocationAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.LocationAccountResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.service.AvaLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class LocationAccountController
{
    @Autowired
    private AvaLocationService avaLocationService;

    /**
     * @param companyId Avalara Company Id.
     */

    @PostMapping( "/create-location/{companyId}" )
    public ResponseEntity<List<LocationAccountResponse>> createLocation( @RequestHeader HttpHeaders headers, @RequestBody List<LocationAccount> locationAccount,
        @PathVariable String companyId )
    {

        boolean isTest;
        if( locationAccount.get( 0 ).getLocationCode().equalsIgnoreCase( "LocationTest" ) )
        {
            isTest = true;
        }
        else
        {
            isTest = false;
        }
        return avaLocationService.createLocationAccount( headers, locationAccount, companyId, isTest );
    }
}
