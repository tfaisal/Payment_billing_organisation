package com.abcfinancial.api.billing.subscriptionmanagement.pricing.service;

import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationTaxRate;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.AvalaraMasterTaxCodeRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.LocationTaxRateRepository;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject.ItemVO;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject.ItemsVO;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject.PricingDetailsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service

@Slf4j

public class PricingService
{
    @Autowired
    private LocationTaxRateRepository locationTaxRateRepository;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private AvalaraMasterTaxCodeRepository avalaraMasterTaxCodeRepository;

    @Transactional( readOnly = true )
    public PricingDetailsVO calculatePricing( ItemsVO itemsVO )
    {
        BigDecimal defaultValueZero = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = defaultValueZero;
        BigDecimal totalNetAmount;
        BigDecimal totalAmount;
        BigDecimal totalItemsAmount = defaultValueZero;
        BigDecimal transientTaxRate = null;
        List<ItemVO> items = new ArrayList<>();
        final int total = 100;
        //List<LocationTaxRate> locationTaxRates = locationTaxRateRepository.findByLocationId( itemsVO.getLocationId( ) );
        if( null == itemsVO.getLocationId() || itemsVO.getLocationId().toString().trim().length() <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), ItemsVO.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATION_ID_NULL ) ) );
        }
        //Changed the condition as per P3-1797
        if( !( itemsVO.getItems() == null || itemsVO.getItems().isEmpty() ) )
        {
            BigDecimal taxableAmount = defaultValueZero;
            for( int index = 0; index < itemsVO.getItems().size(); index++ )
            {
                taxableAmount = BigDecimal.ZERO;
                Optional<LocationTaxRate> optionalLocationTaxRate = null;
                if( Objects.nonNull( itemsVO.getItemCategoryId() ) && itemsVO.getItems().size() >= itemsVO.getItemCategoryId().size() )
                {
                    for( int i = 0; i < itemsVO.getItems().size(); i++ )
                    {
                        if( i + 1 > itemsVO.getItemCategoryId().size() )
                        {
                            itemsVO.getItemCategoryId().add( null );
                        }
                    }
                }
                if( itemsVO.getItemCategoryId() != null && itemsVO.getItemCategoryId().get( index ) != null )
                {
                    optionalLocationTaxRate =
                        locationTaxRateRepository.getLocationTaxRateOfMaxVersionForLocationAndCategoryId( itemsVO.getLocationId(), itemsVO.getItemCategoryId().get( index ) );
                }
                else
                {
                    optionalLocationTaxRate = locationTaxRateRepository.getLocationTaxRateForMaxVersion( itemsVO.getLocationId() );
                }
                if( optionalLocationTaxRate.isPresent() )
                {
                    LocationTaxRate locationTaxRate = optionalLocationTaxRate.get();
                    if( Objects.nonNull( locationTaxRate.getTaxRate() ) )
                    {
                        transientTaxRate = locationTaxRate.getTaxRate();
                    }
                    if( Objects.isNull( locationTaxRate.getDeactivated() ) )
                    {
                        if( Objects.nonNull( locationTaxRate.getIsOverriden() ) && locationTaxRate.getIsOverriden() == true )
                        {
                            locationTaxRate.setTaxRate( locationTaxRate.getSuggestedTaxRate() );
                        }
                        locationTaxRate.setTaxRate( locationTaxRate.getTaxRate() != null ? locationTaxRate.getTaxRate() : new BigDecimal( 0 ) );
                    }
                    else
                    {
                        locationTaxRate.setTaxRate( new BigDecimal( 0 ) );
                    }
                    if( locationTaxRate != null && locationTaxRate.getTaxRate().compareTo( defaultValueZero ) != 0 )
                    {
                        taxableAmount = itemsVO.getItems().get( index ).multiply( locationTaxRate.getTaxRate() ).divide( BigDecimal.valueOf( total ) );

                    }
                    locationTaxRate.setTaxRate( transientTaxRate );
                }
                if( Objects.nonNull( itemsVO.getItemCategoryId() ) )
                {
                    items.add( new ItemVO( itemsVO.getItems().get( index ), taxableAmount, itemsVO.getItemCategoryId().get( index ) ) );
                }
                else
                {
                    items.add( new ItemVO( itemsVO.getItems().get( index ), taxableAmount, null ) );
                }
                totalTaxAmount = totalTaxAmount.add( taxableAmount );
                totalItemsAmount = totalItemsAmount.add( itemsVO.getItems().get( index ) );
            }
            totalNetAmount = totalItemsAmount.setScale( 2, RoundingMode.HALF_UP );
            totalAmount = totalItemsAmount.add( totalTaxAmount ).setScale( 2, RoundingMode.HALF_UP );
            //P3-2587
            // totalTaxAmount = totalTaxAmount.setScale( 2, RoundingMode.FLOOR );
            totalTaxAmount = totalTaxAmount.setScale( 2, RoundingMode.HALF_UP );
        }
        else
        {
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), ItemsVO.class, applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ITEM_BLANK ) ) );
        }
        return new PricingDetailsVO( items, totalTaxAmount, totalAmount, totalNetAmount );
    }

    public PricingDetailsVO calculatePricingForOneItem( BigDecimal amount, UUID locationId )
    {
        ItemsVO itemsVO = new ItemsVO();
        List<BigDecimal> items = new ArrayList<>();
        items.add( amount );
        itemsVO.setItems( items );
        itemsVO.setLocationId( locationId );
        return calculatePricing( itemsVO );
    }
}
