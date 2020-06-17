package com.abcfinancial.api.billing.subscriptionmanagement.avalara.service;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.*;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.helper.AddressServiceHelper;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAddress;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaLocation;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AddressRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaAddressRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaLocationRepository;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import com.abcfinancial.api.common.domain.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

@Service

public class AvaLocationService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( AvaLocationService.class );
    Map<String, String> errorMap = new HashMap<>();
    @Autowired
    private AvaAddressRepository avaAddressRepository;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private AvaLocationRepository locationRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Value( "${avalara.uri.createLocationAccount}" )
    private String requestLocationAccountUrl;

    public ResolveAddressResponse makeResponse( AvaLocation avaLocation )
    {
        ResolveAddressResponse resolveAddressResponse = new ResolveAddressResponse();
        AvaAddress avaAddress = avaLocation.getAvaAddress();
        ResponseAddress responseAddress = new ResponseAddress();
        resolveAddressResponse.setLocationId( avaLocation.getLocationId() );
        responseAddress.setAddressId( avaAddress.getAddressId() );
        responseAddress.setLine( avaAddress.getLine() );
        responseAddress.setCity( avaAddress.getCity() );
        responseAddress.setCountry( avaAddress.getCountry() );
        responseAddress.setRegion( avaAddress.getRegion() );
        responseAddress.setPostalCode( avaAddress.getPostalCode() );
        responseAddress.setLatitude( avaAddress.getLatitude() );
        responseAddress.setLongitude( avaAddress.getLongitude() );
        responseAddress.setIsValidated( avaAddress.getIsValidated() );
        resolveAddressResponse.setResponseAddress( responseAddress );
        return resolveAddressResponse;
    }

    public ResponseEntity<List<LocationAccountResponse>> createLocationAccount( HttpHeaders headers,
        List<LocationAccount> locationAccount,
        String companyId, boolean isTest )
    {
        ResponseEntity<List<LocationAccountResponse>> requestedLocation = null;
        AvaLocation avaLocation = null;
        List<LocationAccountResponse> responseLocationAccount = new ArrayList<>();
        if( !isTest )
        {
            try
            {
                Map<String, String> params = new HashMap<>();
                params.put( "companyId", companyId );
                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString( requestLocationAccountUrl );
                URI createLocationUrl = builder.buildAndExpand( params ).toUri();
                requestedLocation = addressRepository.createLocation( locationAccount, headers, createLocationUrl );
                List<LocationAccountResponse> locationAccounts = requestedLocation.getBody();
                for( int account = 0; account < locationAccounts.size(); account++ )
                {
                    Address address = new Address();
                    address.setLine( locationAccounts.get( account ).getLine1() );
                    address.setCity( locationAccount.get( account ).getCity() );
                    address.setCountry( locationAccounts.get( account ).getCountry() );
                    address.setPostalCode( locationAccounts.get( account ).getPostalCode() );
                    address.setRegion( locationAccount.get( account ).getRegion() );
                    createLocation( locationAccounts.get( account ).getId(), address );
                    avaLocation = resolveaddressAvalara( locationAccounts, errorMap, account, address, headers );
                    if( Objects.nonNull( avaLocation ) && locationAccounts.get( account ).getAddressValidated() )
                    {
                        locationAccounts.get( account ).setAddressValidated( avaLocation.getAvaAddress().getIsValidated() );

                    }
                    locationAccounts.get( account ).setCity( locationAccount.get( account ).getCity() );
                    locationAccounts.get( account ).setRegion( locationAccount.get( account ).getRegion() );
                    responseLocationAccount.add( locationAccounts.get( account ) );
                }

            }
            catch( HttpServerErrorException | HttpClientErrorException exception )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvaLocationService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVALARA_LOCATION_ACCOUNT_FAIL ) ) );
            }
        }
        else
        {
            LocationAccountResponse locationAccountResponse = new LocationAccountResponse();
            locationAccountResponse.setId( Long.valueOf( 375534 ) );
            locationAccountResponse.setAddressTypeId( AddressTypeId.Location );
            locationAccountResponse.setAddressCategoryId( AddressCategoryId.MainOffice );
            locationAccountResponse.setLine1( "2000 Main Street" );
            locationAccountResponse.setPostalCode( "92614" );
            locationAccountResponse.setCountry( "US" );
            locationAccountResponse.setCity( "IRVINE" );
            locationAccountResponse.setRegion( "CA" );
            locationAccountResponse.setAddressValidated( true );
            responseLocationAccount.add( locationAccountResponse );

        }
        return new ResponseEntity<>( responseLocationAccount, HttpStatus.OK );
    }

    /**
     * This will save Avalara Location to the Billing Schema
     */
    public AvaLocation createLocation( final long locationId, Address address )
    {
        AvaLocation avaLocation = null;
        AvaLocation existingAvaLocation = locationRepository.findByLocationId( locationId );
        if( Objects.nonNull( existingAvaLocation ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvaLocationService.class,
                applicationConfiguration.getValue( ( MessageUtils.ERROR_MESSAGE_AVALOCATION_EXIST + "\t" + locationId ) ) ) );
        }
        AddressServiceHelper.validateMandatoryFieldForAddress( address );
        avaLocation = AddressServiceHelper.prepareAvaLocation( locationId, address );
        return locationRepository.save( avaLocation );
    }

    private AvaLocation resolveaddressAvalara( List<LocationAccountResponse> locationAccounts, Map<String, String> errorMap, int account, Address address, HttpHeaders headers )
    {
        AvaLocation avaLocation = null;
        try
        {
            avaLocation = resolveAddress( locationAccounts.get( account ).getId(), address, headers );
            locationAccounts.get( account ).setAddressValidated( true );
        }
        catch( HttpClientErrorException | HttpServerErrorException exception )
        {
            errorMap.put( "Resolve Address API", "Failed to resolve addresse  for above location" );
            locationAccounts.get( account ).setMessage( errorMap );
            locationAccounts.get( account ).setAddressValidated( false );
        }
        return avaLocation;
    }

    /**
     * This method checks that of Location is available then checks that
     * Address is valid or not if not valid then try to resolve with resolve Address API
     * After this If resolved then updates the ava_address table with making isValidFlag true
     * If not Resolved then Try to Resolve with New Address which is as Method Argument, If Resolved then Updates
     * the the same
     */

    public AvaLocation resolveAddress( final Long locationId, final Address address, HttpHeaders headers )
    {
        AvaAddress avaAddressEntity = null;
        AvaLocation avaLocation = locationRepository.findByLocationId( locationId );
        //Check If Validates Address is existing just return it
        if( avaLocation != null )
        {
            avaAddressEntity = avaLocation.getAvaAddress();
            if( avaAddressEntity != null )
            {
                if( avaAddressEntity.getIsValidated() )
                {
                    LOGGER.info( "Address Already Validated for locationId = {}  AddressId = {}", locationId, avaAddressEntity.getAddressId() );
                    return avaLocation;
                }
                if( address != null )
                {
                    AddressServiceHelper.validateMandatoryFieldForAddress( address );
                    address.setLine1( address.getLine() );
                    avaLocation = processAddressResolution( address, avaLocation, headers ).getAvaLocation();
                }
                else
                {
                    //This is for locationId passed only
                    Address preparedAddress = AddressServiceHelper.prepareAddressFrom( avaAddressEntity );
                    avaLocation = processAddressResolution( preparedAddress, avaLocation, headers ).getAvaLocation();
                }
            }
            else
            {
                throw new ErrorResponse(
                    new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), AvaLocationService.class, "Avalara Address Not Found for locationId = " + locationId ) );
            }
        }
        else
        {
            throw new ErrorResponse(
                new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), AvaLocationService.class, "Avalara Location Not Found for locationId = " + locationId ) );
        }
        return avaLocation;
    }

    private AvaAddress processAddressResolution( final Address address, AvaLocation avaLocation, HttpHeaders httpHeaders )
    {
        AddressResponse addressResponse = null;
        AvaAddress avaAddressEntity = null;
        avaAddressEntity = avaLocation.getAvaAddress();

        LOGGER.info( "Requesting Avalara Address Resolution Service." );
        addressResponse = addressRepository.resolveAddress( address, httpHeaders );

        LOGGER.info( "Avalara Address Response {} ", addressResponse );
        if( addressResponse != null && !( addressResponse.getValidatedAddresses().get( 0 ).getAddressType().equalsIgnoreCase( "UnknownAddressType" ) ) &&
            addressResponse.getCoordinates() != null && addressResponse.getMessages() == null )
        {

            avaAddressEntity = updateAvalaraAddress( addressResponse, avaAddressEntity );
            updateAvalaraLocation( avaLocation, avaAddressEntity.getModifiedDateTime() );

        }
        else if( Objects.nonNull( addressResponse ) && addressResponse.getValidatedAddresses().get( 0 ).getAddressType().equalsIgnoreCase( "UnknownAddressType" ) )
        {
            throw new HttpClientErrorException( HttpStatus.BAD_REQUEST );
        }
        else
        {
            List<Messages> messages = Objects.nonNull( addressResponse ) ? addressResponse.getMessages() : null;
            if( messages != null && !messages.isEmpty() )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvaLocationService.class, messages.get( 0 ).getSummary() ) );
            }
        }
        return avaAddressEntity;
    }

    @Transactional
    public AvaAddress updateAvalaraAddress( AddressResponse addressResponse, AvaAddress avaAddress )
    {
        LOGGER.info( "Updating Avalara Address to the DB" );
        AvaAddress updatedAddress = null;
        Coordinates coordinates = null;
        if( null != addressResponse && avaAddress != null )
        {
            long modifiedDateTimeMillis = System.currentTimeMillis();
            Timestamp modifiedDateTime = new Timestamp( modifiedDateTimeMillis );
            Address address = addressResponse.getAddress();
            coordinates = addressResponse.getCoordinates();
            avaAddress.setCity( address.getCity() );
            avaAddress.setCountry( address.getCountry() );
            avaAddress.setRegion( address.getRegion() );
            avaAddress.setLine( address.getLine1() );
            avaAddress.setPostalCode( address.getPostalCode() );
            avaAddress.setLatitude( coordinates.getLatitude() );
            avaAddress.setLongitude( coordinates.getLongitude() );
            avaAddress.setIsValidated( true );
            avaAddress.setModifiedDateTime( modifiedDateTime );
            updatedAddress = avaAddressRepository.save( avaAddress );
            LOGGER.info( "Address Updated Successfully." );
        }
        return updatedAddress;
    }

    /**
     * @param avaLocation
     * @param modifiedDateTime
     */

    private void updateAvalaraLocation( AvaLocation avaLocation, Timestamp modifiedDateTime )
    {
        LOGGER.info( "Updating avalara location modified datetime to the DB" );
        //now
        avaLocation.setModifiedDateTime( modifiedDateTime );
        locationRepository.save( avaLocation );
    }

    public AvaLocation resolveAddressTest( Long avaLocationId, Address address )
    {
        AvaLocation avaLocation = new AvaLocation();
        AvaAddress avaAddress = new AvaAddress();
        avaAddress.setLine( address.getLine() );
        avaAddress.setCity( address.getCity() );
        avaAddress.setRegion( address.getRegion() );
        avaAddress.setCountry( address.getCountry() );
        avaAddress.setPostalCode( address.getPostalCode() );
        avaAddress.setAddressId( UUID.randomUUID() );
        avaAddress.setIsValidated( true );
        avaAddress.setLatitude( 33.684689 );
        avaAddress.setLongitude( -117.851495 );
        avaLocation.setAvaAddress( avaAddress );
        avaLocation.setLocationId( avaLocationId );
        return avaLocation;
    }
}
