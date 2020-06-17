package com.abcfinancial.api.billing.subscriptionmanagement.pricing;

import com.abcfinancial.api.billing.subscriptionmanagement.pricing.service.PricingService;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject.ItemsVO;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject.PricingDetailsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class PricingController
{
    @Autowired
    private PricingService pricingService;

    /**
     * @param items Item details to calculate pricing.
     */

    @PostMapping( value = "/pricing" )
    @PreAuthorize( "#oauth2.hasScope( 'pricing:write' )" )
    public ResponseEntity<PricingDetailsVO> calculatePricing( @RequestHeader HttpHeaders headers, @RequestBody ItemsVO items )
    {
        return ResponseEntity.status( HttpStatus.CREATED ).body( pricingService.calculatePricing( items ) );
    }
}
