package com.abcfinancial.api.billing.subscriptionmanagement.account.location.helper;

import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationTaxRate;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationTaxVO;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component

@Slf4j

public class LocationServiceHelper
{
    private static ApplicationConfiguration applicationConfiguration;
    private static boolean flag;
    private static boolean validFlag = true;

    /**
     * Validates the mandatory fields If found invalid throws the exception with message
     *
     * @param locationTaxRate
     */

    public static final void validateMandatoryFieldForLocationTaxRate( LocationTaxRate locationTaxRate )
    {

        log.trace( "Validating location tax rate all fields value." );
        Set<DataIntegrityViolationResponse> dataIntegrityViolationResponses = new HashSet<>();
        if( locationTaxRate.getLocationId() == null || locationTaxRate.getEmpId() == null )
        {
            if( locationTaxRate.getLocationId() == null && locationTaxRate.getEmpId() == null )
            {
                dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATION_ID_NULL ) ) );
                dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SELLS_EMPID_INVALID ) ) );
            }
            else if( locationTaxRate.getEmpId() == null )
            {
                dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SELLS_EMPID_INVALID ) ) );
            }
            else
            {
                dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATION_ID_NULL ) ) );
            }
        }
        if( !Objects.isNull( locationTaxRate.getTaxCode() ) && locationTaxRate.getTaxCode().trim().length() > 30 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TAXCODE_SIZE ) ) );
        }
        if( Objects.nonNull( locationTaxRate.getTaxRate() ) && ( locationTaxRate.getTaxRate().floatValue() < 0 || locationTaxRate.getTaxRate().floatValue() > 100 ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TAX_RATE_SIZE ) ) );
        }

        if( ( Objects.isNull( locationTaxRate.getIsOverriden() ) || locationTaxRate.getIsOverriden() == flag ) && !Objects.isNull( locationTaxRate.getSuggestedTaxRate() ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_OVERRIDEN_TAX_FLAG ) ) );
        }
        if( !Objects.isNull( locationTaxRate.getIsOverriden() ) && locationTaxRate.getIsOverriden() == validFlag && Objects.isNull( locationTaxRate.getSuggestedTaxRate() ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUGGESTED_TAX ) ) );
        }
        if( !Objects.isNull( locationTaxRate.getSuggestedTaxRate() ) && locationTaxRate.getSuggestedTaxRate().floatValue() < 0 ||
            !Objects.isNull( locationTaxRate.getSuggestedTaxRate() ) && locationTaxRate.getSuggestedTaxRate().floatValue() > 100 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUGGESTED_TAX_SIZE ) ) );
        }
        if( !dataIntegrityViolationResponses.isEmpty() )
        {
            throw new ErrorResponse( dataIntegrityViolationResponses.toArray( new DataIntegrityViolationResponse[dataIntegrityViolationResponses.size()] ) );
        }
    }

    public static final void validateMandatoryFieldForLocationTaxRate( LocationTaxVO locationTaxVO )
    {
        log.trace( "Validating location tax rate all fields value." );
        Set<DataIntegrityViolationResponse> dataIntegrityViolationResponses = new HashSet<>();
        if( !Objects.isNull( locationTaxVO.getTaxCode() ) && locationTaxVO.getTaxCode().trim().length() > 30 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TAXCODE_SIZE ) ) );
        }
        if( Objects.nonNull( locationTaxVO.getTaxRate() ) &&
            ( locationTaxVO.getTaxRate().compareTo( BigDecimal.valueOf( 0 ) ) < 0 || locationTaxVO.getTaxRate().compareTo( BigDecimal.valueOf( 100 ) ) > 0 ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TAX_RATE_SIZE ) ) );
        }

        if( ( Objects.isNull( locationTaxVO.getIsOverriden() ) || locationTaxVO.getIsOverriden() == flag ) && !Objects.isNull( locationTaxVO.getSuggestedTaxRate() ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_OVERRIDEN_TAX_FLAG ) ) );
        }
        if( !Objects.isNull( locationTaxVO.getIsOverriden() ) && locationTaxVO.getIsOverriden() == validFlag && Objects.isNull( locationTaxVO.getSuggestedTaxRate() ) )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUGGESTED_TAX ) ) );
        }
        if( !Objects.isNull( locationTaxVO.getSuggestedTaxRate() ) && locationTaxVO.getSuggestedTaxRate().floatValue() < 0 ||
            !Objects.isNull( locationTaxVO.getSuggestedTaxRate() ) && locationTaxVO.getSuggestedTaxRate().floatValue() > 100 )
        {
            dataIntegrityViolationResponses.add( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUGGESTED_TAX_SIZE ) ) );
        }
        if( !dataIntegrityViolationResponses.isEmpty() )
        {
            throw new ErrorResponse( dataIntegrityViolationResponses.toArray( new DataIntegrityViolationResponse[dataIntegrityViolationResponses.size()] ) );
        }
    }

    @Autowired
    public void setApplicationConfiguration( ApplicationConfiguration applicationConfiguration )
    {
        this.applicationConfiguration = applicationConfiguration;
    }
}
