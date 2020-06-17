package com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.*;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.helper.AddressServiceHelper;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.common.domain.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j

@Repository
public class AddressRepository
{
    public static final String AUTHORIZATION = "Authorization";
    @Autowired
    RestTemplate restTemplate;
    @Value( "${avalara.uri.resolveAddress}" )
    String avalaraResolveAdressUrl;

    public AddressResponse resolveAddress( Address address, HttpHeaders httpHeaders )
    {
        HttpEntity<Address> httpEntity = new HttpEntity<Address>( address, prepareBasicAuthHeader( httpHeaders ) );
        AddressResponse addressResponse = restTemplate.postForObject( avalaraResolveAdressUrl, httpEntity, AddressResponse.class );
        return addressResponse;
    }

    /**
     * This will generate the basic Authentication Header
     *
     * @param httpHeaders
     */
    private static HttpHeaders prepareBasicAuthHeader( HttpHeaders httpHeaders )
    {
        Map<String, String> headerMap = httpHeaders.toSingleValueMap();
        if( !headerMap.containsKey( "username" ) || !headerMap.containsKey( "password" ) )
        {
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class, "Invalid Header, must contain username and password." ) );
        }
        String username = headerMap.get( "username" );
        String password = headerMap.get( "password" );
        String authHeader = null;
        String auth = username + ":" + password;
        try
        {
            authHeader = "Basic " + Base64.getEncoder().encodeToString( auth.getBytes( "utf-8" ) );
        }
        catch( UnsupportedEncodingException exception )
        {
            log.debug( "Base 64 fail, why is that even a thing", exception ); //todo MarkV is this an error we should be eating???
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add( AUTHORIZATION, authHeader );
        return headers;
    }

    public ResponseEntity<List<LocationAccountResponse>> createLocation( List<LocationAccount> locationAccounts, HttpHeaders httpHeaders, URI requestLocationAccountUrl )
    {
        HttpEntity<List<LocationAccount>> httpEntity = new HttpEntity<>( locationAccounts, prepareBasicAuthHeader( httpHeaders ) );
        ResponseEntity<List<LocationAccountResponse>> requestedLocation = restTemplate.exchange( requestLocationAccountUrl,
            HttpMethod.POST, httpEntity, new ParameterizedTypeReference<List<LocationAccountResponse>>()
            {
            } );
        return requestedLocation;
    }

    public ResponseEntity<List<NexusResponseModel>> createNexus( List<NexusResponseModel> nexusList, HttpHeaders httpHeaders, URI createNexusURI )
    {
        HttpEntity<List<NexusResponseModel>> httpEntity = new HttpEntity<>( nexusList, prepareBasicAuthHeader( httpHeaders ) );
        ResponseEntity<List<NexusResponseModel>> requestedNexusList = restTemplate.exchange( createNexusURI,
            HttpMethod.POST, httpEntity, new ParameterizedTypeReference<List<NexusResponseModel>>()
            {
            } );
        return requestedNexusList;
    }
}
