package com.abcfinancial.api.billing.subscriptionmanagement.account.location;

import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.AvalaraMasterTaxCode;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationTaxRate;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.service.LocationService;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationAccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationTaxRateRequest;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationTaxRateResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationTaxVO;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static com.abcfinancial.api.billing.utility.common.AppConstants.*;

@Slf4j
@Validated
@RestController

public class LocationController
{
    @Autowired
    private LocationService locationService;
    private static Sort.Direction sequence = Sort.Direction.DESC;

    /**
     * @param locationId     Location Id must be UUID format
     * @param itemCategoryId is optional but must be UUID format
     */

    @PutMapping( value = { "/taxRate/{locationId}", "/tax-rate/{locationId}" } )
    @PreAuthorize( "#oauth2.hasScope( 'tax-rate:write' )" )
    public LocationTaxRate updateLocationTaxRate( @RequestHeader HttpHeaders headers, @RequestParam( value = "itemCategoryId", required = false ) UUID itemCategoryId,
        @Valid @PathVariable( "locationId" ) UUID locationId, @Valid @RequestBody LocationTaxVO locationTaxVO )
    {
        return locationService.updateLocationTaxRate( locationTaxVO, locationId, itemCategoryId );
    }

    @PostMapping( value = { "/createTaxRate", "/tax-rate" } )
    @PreAuthorize( "#oauth2.hasScope( 'tax-rate:write' )" )
    public ResponseEntity<LocationTaxRate> createLocationTaxRate( @RequestHeader HttpHeaders headers, @RequestBody LocationTaxRateRequest locationTaxRateRequest )
    {
        LocationTaxRate locationTaxRate = ModelMapperUtils.map( locationTaxRateRequest, LocationTaxRate.class );
        return ResponseEntity.status( HttpStatus.CREATED ).body( locationService.createLocationTaxRate( locationTaxRate ) );
    }

    /**
     * @param name Search name of client.
     * @param page Number of pages for respective result
     * @param size Number of records per page
     */

    @GetMapping( value = "/account/client" )
    @PreAuthorize( "#oauth2.hasScope( 'application:read' )" )
    public Page<LocationAccountVO> reviewClientAccounts( @RequestHeader HttpHeaders headers, @RequestParam( value = DEFAULT_NAME_VALUE, required = false ) String name,
        @RequestParam( value = DEFAULT_PAGE_VALUE, required = false, defaultValue = DEFAULT_PAGE ) int page,
        @RequestParam( value = DEFAULT_SIZE_VALUE, required = false, defaultValue = DEFAULT_SIZE ) int size )
    {
        Pageable pageable = PageRequest.of( ( page < 0 ) ? 20 : page, ( size <= 0 ) ? 20 : size, Sort.Direction.DESC, "created" );
        List<LocationAccountVO> locationAccountVO = locationService.getClients( name, pageable );
        return new PageImpl<>( locationAccountVO, pageable, locationAccountVO.size() );
    }

    /**
     * @param accountId client's accountId must be UUID format.
     */

    @GetMapping( "/account/client/{accountId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:read' )" )
    public LocationAccountVO reviewClientAccount( @PathVariable( "accountId" ) UUID accountId, @RequestHeader HttpHeaders headers )
    {
        return locationService.getClient( accountId );
    }

    @GetMapping( value = "/avalara/master/taxcode" )
    @PreAuthorize( "#oauth2.hasScope( 'application:read' )" )
    public Page<AvalaraMasterTaxCode> getAvalaraMasterTaxCode( @RequestHeader HttpHeaders headers,
        @RequestParam( value = DEFAULT_PAGE_VALUE, required = false, defaultValue = DEFAULT_PAGE ) int page,
        @RequestParam( value = DEFAULT_SIZE_VALUE, required = false, defaultValue = DEFAULT_SIZE ) int size )
    {
        Pageable pageable = null;
        int allRecordSize = locationService.getAvalaraMasterTaxCode( pageable ).size();
        pageable = PageRequest.of( ( page < 0 ) ? 20 : page, ( size <= 0 ) ? 20 : size );
        List<AvalaraMasterTaxCode> avalaraMasterTaxCodeVO = locationService.getAvalaraMasterTaxCode( pageable );
        return new PageImpl<>( avalaraMasterTaxCodeVO, pageable, allRecordSize );
    }

    /**
     * @param locationId Location Id must be UUID format
     */

    @GetMapping( value = { "/getTaxRates/{locationId}", "/tax-rate/{locationId}" } )
    public Page<LocationTaxRateResponse> getLocationTaxRates( @PathVariable( "locationId" ) UUID locationId,
        @RequestParam( value = "page", required = false, defaultValue = "0" ) int pageNumber,
        @RequestParam( value = "size", required = false, defaultValue = "20" ) int pageSize
    )
    {
        Pageable pageable = null;
        pageable = PageRequest.of( ( pageNumber < 0 ) ? 0 : pageNumber, ( pageSize <= 0 ) ? 20 : pageSize, sequence, "ltr_modified" );
        List<LocationTaxRateResponse> response = locationService.getLocationTaxRates( locationId, pageable );
        return new PageImpl<>( response, pageable, response.size() );
    }

    /**
     * @param locationId     Location Id must be UUID format
     * @param itemCategoryId is optional but must be UUID format
     */

    @DeleteMapping( value = { "/taxRate/{locationId}", "/tax-rate/{locationId}" } )
    @PreAuthorize( "#oauth2.hasScope( 'tax-rate:write' )" )
    public LocationTaxRate deleteLocationTaxRate( @RequestHeader HttpHeaders headers, @RequestParam( value = "itemCategoryId", required = false ) UUID itemCategoryId,
        @Valid @PathVariable( "locationId" ) UUID locationId )
    {
        return locationService.deleteLocationTaxRate( locationId, itemCategoryId );
    }
}
