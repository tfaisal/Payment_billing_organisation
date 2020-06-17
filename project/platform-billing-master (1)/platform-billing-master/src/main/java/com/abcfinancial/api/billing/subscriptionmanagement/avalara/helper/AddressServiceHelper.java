package com.abcfinancial.api.billing.subscriptionmanagement.avalara.helper;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.Address;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAddress;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaLocation;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.common.domain.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component

public class AddressServiceHelper
{
    private static final Logger LOG = LoggerFactory.getLogger( AddressServiceHelper.class );
    private static ApplicationConfiguration applicationConfiguration;

    /**
     * @param avaAddress
     * @return return Address details
     */

    public static final Address prepareAddressFrom( final AvaAddress avaAddress )
    {
        Address address = new Address();
        if( avaAddress != null )
        {
            address.setLine( avaAddress.getLine() );
            address.setCity( avaAddress.getCity() );
            address.setCountry( avaAddress.getCountry() );
            address.setPostalCode( avaAddress.getPostalCode() );
            address.setRegion( avaAddress.getRegion() );
        }
        return address;
    }

    /**
     * @param address
     */

    public static final void validateMandatoryFieldForAddress( final Address address )
    {
        LOG.trace( "Validating Address Mandatory field Fields.." );
        Set<DataIntegrityViolationResponse> dataIntegrityViolationResponses = getAllDataIntegrityViolationResponse( address );
        if( !dataIntegrityViolationResponses.isEmpty() )
        {
            throw new ErrorResponse( dataIntegrityViolationResponses.toArray( new DataIntegrityViolationResponse[dataIntegrityViolationResponses.size()] ) );
        }
    }

    public static final Set<DataIntegrityViolationResponse> getAllDataIntegrityViolationResponse( Address address )
    {
        Set<DataIntegrityViolationResponse> dataIntegrityViolationResponses = new HashSet<>();
        if( StringUtils.isEmpty( address.getCity() ) )
        {
            dataIntegrityViolationResponses.add(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVACITY_INVALID ) ) );
        }
        else if( address.getCity().trim().length() > 100 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVACITY_LENGTH ) ) );
        }
        if( StringUtils.isEmpty( address.getCountry() ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVACOUNTRY_INVALID ) ) );
        }
        else if( address.getCountry().trim().length() < 2 || address.getCountry().trim().length() > 2 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVACOUNTRY_LENGTH ) ) );
        }
        if( StringUtils.isEmpty( address.getRegion() ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVAREGION_INVALID ) ) );
        }
        else if( address.getRegion().trim().length() < 2 || address.getRegion().trim().length() > 3 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVAREGION_LENGTH ) ) );
        }
        if( StringUtils.isEmpty( address.getPostalCode() ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVAPOSTALCODE_INVALID ) ) );
        }
        else if( !isNumeric( address.getPostalCode().trim() ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVAPOSTALCODE_NUMERIC ) ) );
        }
        else if( address.getPostalCode().trim().length() < 1 || address.getPostalCode().trim().length() > 10 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVAPOSTALCODE_LENGTH ) ) );
        }
        if( StringUtils.isEmpty( address.getLine() ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVALINE_INVALID ) ) );
        }
        else if( address.getLine().trim().length() > 100 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AddressServiceHelper.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AVALINE_LENGTH ) ) );
        }
        return dataIntegrityViolationResponses;
    }

    public static boolean isNumeric( String str )
    {
        return str.matches( "-?\\d+( \\.\\d+ )?" );
    }

    /**
     * This will prepare AvaAddress with AvaLocation and returns it
     *
     * @param locationId
     * @param address
     * @return return Avalara location details
     */

    public static AvaLocation prepareAvaLocation( final long locationId, Address address )
    {
        AvaLocation avaLocation = new AvaLocation();
        AvaAddress avaAddress = new AvaAddress();
        //now which is not present in the above vo object
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp( currentTimeMillis );
        if( Objects.nonNull( address ) )
        {
            //For Address Model
            avaAddress.setCity( address.getCity() );
            avaAddress.setCountry( address.getCountry() );
            avaAddress.setRegion( address.getRegion() );
            avaAddress.setPostalCode( address.getPostalCode() );
            avaAddress.setLine( address.getLine() );
        }
        avaAddress.setIsValidated( false );
        avaAddress.setCreatedDateTime( timestamp );
        avaAddress.setModifiedDateTime( timestamp );
        //For Location Model
        avaLocation.setCreatedDateTime( avaAddress.getCreatedDateTime() );
        avaLocation.setModifiedDateTime( avaAddress.getCreatedDateTime() );
        avaLocation.setLocationId( locationId );
        avaLocation.setAvaAddress( avaAddress );
        return avaLocation;
    }

    @Autowired
    public void setApplicationConfiguration( ApplicationConfiguration applicationConfiguration )
    {
        this.applicationConfiguration = applicationConfiguration;
    }
}
