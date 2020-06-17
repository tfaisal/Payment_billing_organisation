package com.abcfinancial.api.billing.subscriptionmanagement.account.location.service;

import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.AvalaraMasterTaxCode;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationTaxRate;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.helper.LocationServiceHelper;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.AvalaraMasterTaxCodeRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.LocationAccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.LocationTaxRateRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationAccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationTaxRateResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationTaxVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AccountVO;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import com.abcfinancial.api.billing.utility.exception.CustomErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Service

public class LocationService
{
    private static String locationMessage = "Saving New LocationTaxRate to the Persistent store.";
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private LocationTaxRateRepository locationTaxRateRepository;
    @Autowired
    private LocationAccountRepository locationAccountRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private AvalaraMasterTaxCodeRepository avalaraMasterTaxCodeRepository;

    @Transactional( propagation = Propagation.REQUIRED )
    public LocationTaxRate updateLocationTaxRate( LocationTaxVO locationTaxVO, UUID locationId, UUID itemCategoryId )
    {
        LocationTaxRate locationTaxRate = null;
        LocationTaxRate transientLocationTaxRate = new LocationTaxRate();
        Long savedVersion = null;
        Optional<LocationTaxRate> optionalLocationTaxRate = null;
        Optional<LocationTaxRate> optionalLocationTaxRateWithCategory = null;
        LocationServiceHelper.validateMandatoryFieldForLocationTaxRate( locationTaxVO );
        if( locationId != null && itemCategoryId != null )
        {
            optionalLocationTaxRate = locationTaxRateRepository.getLocationTaxRateOfMaxVersionForLocationAndCategoryId( locationId, itemCategoryId );
        }
        else
        {
            optionalLocationTaxRate = locationTaxRateRepository.getLocationTaxRateForMaxVersion( locationId );
        }
        if( !optionalLocationTaxRate.isPresent() )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), LocationTaxRate.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOC_ID_DEACTIVATED ) ) );
        }
        if( Objects.nonNull( optionalLocationTaxRate.get().getDeactivated() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOC_ID_DEACTIVATED ) ) );
        }

        if( locationTaxVO.getItemCategoryId() != null && itemCategoryId == null )
        {
            optionalLocationTaxRateWithCategory = locationTaxRateRepository.getLocationTaxRateOfMaxVersionForLocationAndCategoryId( locationId, locationTaxVO.getItemCategoryId() );
            if( optionalLocationTaxRateWithCategory.isPresent() )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MISSING_ITEM_CAT_ID ) ) );
            }
        }
        if( locationTaxVO.getItemCategoryId() != null && itemCategoryId != null )
        {
            if( !( locationTaxVO.getItemCategoryId().equals( itemCategoryId ) ) )
            {
                optionalLocationTaxRateWithCategory =
                    locationTaxRateRepository.getLocationTaxRateOfMaxVersionForLocationAndCategoryId( locationId, locationTaxVO.getItemCategoryId() );
                if( optionalLocationTaxRateWithCategory.isPresent() )
                {
                    throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), LocationAccount.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DUPLICATE_LOCID_AND_ITEM_CAT_ID ) ) );
                }
            }
        }

        if( optionalLocationTaxRate.isPresent() )
        {
            locationTaxRate = optionalLocationTaxRate.get();
            transientLocationTaxRate.setLocationId( locationTaxRate.getLocationId() );
            transientLocationTaxRate.setEmpId( locationTaxRate.getEmpId() );
            if( Objects.nonNull( locationTaxRate.getTaxRate() ) )
            {
                transientLocationTaxRate.setTaxRate( locationTaxRate.getTaxRate() );
            }
            if( Objects.nonNull( locationTaxRate.getTaxCode() ) )
            {
                transientLocationTaxRate.setTaxCode( locationTaxRate.getTaxCode() );
            }
            if( Objects.nonNull( locationTaxRate.getItemCategoryId() ) )
            {
                transientLocationTaxRate.setItemCategoryId( locationTaxRate.getItemCategoryId() );
            }
            if( Objects.nonNull( locationTaxRate.getIsOverriden() ) )
            {
                transientLocationTaxRate.setIsOverriden( locationTaxRate.getIsOverriden() );
            }
            if( Objects.nonNull( locationTaxRate.getSuggestedTaxRate() ) )
            {
                transientLocationTaxRate.setSuggestedTaxRate( locationTaxRate.getSuggestedTaxRate() );
            }
            savedVersion = locationTaxRate.getVersion();
            if( locationTaxVO.getTaxRate() != null )
            {
                locationTaxVO.setTaxRate( locationTaxVO.getTaxRate().setScale( 3, RoundingMode.HALF_UP ) );
            }
            if( Objects.nonNull( locationTaxVO.getTaxRate() ) )
            {
                if( Objects.nonNull( locationTaxRate.getTaxRate() ) )
                {
                    if( locationTaxVO.getTaxRate().doubleValue() != locationTaxRate.getTaxRate().doubleValue() )
                    {
                        transientLocationTaxRate.setTaxRate( locationTaxVO.getTaxRate() );
                        savedVersion++;
                    }
                }
                else
                {
                    transientLocationTaxRate.setTaxRate( locationTaxVO.getTaxRate() );
                    savedVersion++;
                }
            }
            if( Objects.nonNull( locationTaxVO.getEmpId() ) )
            {
                if( Objects.nonNull( locationTaxRate.getEmpId() ) )
                {
                    if( !locationTaxVO.getEmpId().equals( locationTaxRate.getEmpId() ) )
                    {
                        transientLocationTaxRate.setEmpId( locationTaxVO.getEmpId() );
                        savedVersion++;
                    }
                }
                else
                {
                    transientLocationTaxRate.setEmpId( locationTaxVO.getEmpId() );
                    savedVersion++;
                }
            }
            if( Objects.nonNull( locationTaxVO.getTaxCode() ) )
            {
                if( Objects.nonNull( locationTaxRate.getTaxCode() ) )
                {
                    if( !locationTaxVO.getTaxCode().equalsIgnoreCase( locationTaxRate.getTaxCode() ) )
                    {
                        transientLocationTaxRate.setTaxCode( locationTaxVO.getTaxCode() );
                        savedVersion++;
                    }
                }
                else
                {
                    transientLocationTaxRate.setTaxCode( locationTaxVO.getTaxCode() );
                    savedVersion++;
                }
            }
            if( Objects.nonNull( locationTaxVO.getItemCategoryId() ) )
            {
                if( Objects.nonNull( locationTaxRate.getItemCategoryId() ) )
                {
                    if( !locationTaxVO.getItemCategoryId().equals( locationTaxRate.getItemCategoryId() ) )
                    {
                        transientLocationTaxRate.setItemCategoryId( locationTaxVO.getItemCategoryId() );
                        savedVersion++;
                    }
                }
                else
                {
                    transientLocationTaxRate.setItemCategoryId( locationTaxVO.getItemCategoryId() );
                    savedVersion++;
                }
            }
            if( Objects.nonNull( locationTaxVO.getIsOverriden() ) )
            {
                if( locationTaxVO.getIsOverriden() )
                {
                    if( Objects.nonNull( locationTaxVO.getSuggestedTaxRate() ) )
                    {
                        if( Objects.nonNull( locationTaxRate.getSuggestedTaxRate() ) )
                        {
                            if( locationTaxVO.getSuggestedTaxRate().doubleValue() != locationTaxRate.getSuggestedTaxRate().doubleValue() )
                            {
                                transientLocationTaxRate.setSuggestedTaxRate( locationTaxVO.getSuggestedTaxRate() );
                                savedVersion++;
                            }
                        }
                        else
                        {
                            transientLocationTaxRate.setSuggestedTaxRate( locationTaxVO.getSuggestedTaxRate() );
                            savedVersion++;
                        }
                    }
                    else
                    {
                        throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUGGESTED_TAX ) ) );
                    }
                }
            }
            if( Objects.nonNull( locationTaxVO.getIsOverriden() ) )
            {
                if( Objects.nonNull( locationTaxRate.getIsOverriden() ) )
                {
                    if( locationTaxVO.getIsOverriden() != locationTaxRate.getIsOverriden() )
                    {
                        transientLocationTaxRate.setIsOverriden( locationTaxVO.getIsOverriden() );
                        savedVersion++;
                    }
                }
                else
                {
                    transientLocationTaxRate.setIsOverriden( locationTaxVO.getIsOverriden() );
                    savedVersion++;
                }
            }
            if( null != savedVersion )
            {
                if( savedVersion != locationTaxRate.getVersion() )
                {
                    transientLocationTaxRate.setVersion( locationTaxRate.getVersion() + 1 );
                }
                else
                {
                    return locationTaxRate;
                }
            }
            transientLocationTaxRate.setLocationId( locationTaxRate.getLocationId() );
            //update last version before saving new version
            Date date = new Date();
            locationTaxRate.setDeactivated( new Timestamp( date.getTime() ) );
            locationTaxRateRepository.save( locationTaxRate );
            //saving new version
            locationTaxRate = locationTaxRateRepository.save( transientLocationTaxRate );
        }

        return locationTaxRate;
    }

    @Transactional( readOnly = true )
    public List<LocationAccountVO> getClients( String name, Pageable pageable )
    {
        if( name == null )
        {
            name = Strings.EMPTY;
        }
        else if( name.length() < 4 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_NAME_LENGTH ) ) );
        }
        List<LocationAccount> locationAccountList = locationAccountRepository.getAllByName( "%" + name + "%", pageable );
        List<LocationAccountVO> locationAccountVOList = ModelMapperUtils.mapAll( locationAccountList, LocationAccountVO.class );
        IntStream.range( 0, locationAccountVOList.size() ).forEach( i -> {
            LocationAccountVO locationAccountVO = locationAccountVOList.get( i );
            LocationAccount locationAccount = locationAccountList.get( i );
            locationAccountVO.setLocationId( locationAccount.getLocaccId().getLocation() );
            locationAccountVO.setClientId( locationAccount.getClientId() );
            locationAccountVO.setAccount( ModelMapperUtils.map( locationAccount.getAccountId(), AccountVO.class ) );
            PaymentMethod paymentMethod =
                paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( locationAccount.getAccountId().getAccountId(), Boolean.TRUE );
            PaymentMethodVO paymentMethodVO = ModelMapperUtils.map( paymentMethod, PaymentMethodVO.class );
            locationAccountVO.getAccount().setPaymentMethod( paymentMethodVO );
        } );
        if( locationAccountList.isEmpty() )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_RECORD_NOT_FOUND ) ) );
        }
        else
        {
            return locationAccountVOList;
        }
    }

    @Transactional( readOnly = true )
    public LocationAccountVO getClient( UUID accountId )
    {
        PaymentMethodVO paymentMethodVO = null;
        Optional<LocationAccount> locationAccount = Optional.ofNullable( locationAccountRepository.findByLocaccIdAccount( accountId ) );
        if( !locationAccount.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CLIENT_ACCOUNT_NOT_FOUND ) + accountId ) );
        }
        Account account = locationAccount.get().getAccountId();
        PaymentMethod paymentMethod =
            paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( locationAccount.get().getAccountId().getAccountId(), Boolean.TRUE );
        if( paymentMethod != null )
        {
            paymentMethodVO = ModelMapperUtils.map( paymentMethod, PaymentMethodVO.class );
        }
        else
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.NOT_FOUND.value(), LocationAccount.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_NOT_FOUND ) ) );
        }
        AccountVO accountVO = ModelMapperUtils.map( account, AccountVO.class );
        LocationAccountVO locationAccountVO = ModelMapperUtils.map( locationAccount, LocationAccountVO.class );
        locationAccountVO.setLocationId( account.getLocation() );
        locationAccountVO.setClientId( locationAccount.get().getClientId() );
        locationAccountVO.setMerchantId( UUID.randomUUID() );
        accountVO.setPaymentMethod( paymentMethodVO );
        locationAccountVO.setAccount( accountVO );
        return locationAccountVO;
    }

    @Transactional( readOnly = true )
    public List<AvalaraMasterTaxCode> getAvalaraMasterTaxCode( Pageable pageable )
    {
        return avalaraMasterTaxCodeRepository.getAll( pageable );
    }

    public LocationTaxRate createLocationTaxRate( LocationTaxRate locationTaxRate )
    {
        LocationServiceHelper.validateMandatoryFieldForLocationTaxRate( locationTaxRate );
        locationTaxRate.setVersion( 1L );
        LocationTaxRate transientLocationTaxRate = locationTaxRate;
        if( !Objects.isNull( locationTaxRate.getItemCategoryId() ) )
        {
            //If Not Present
            Optional<LocationTaxRate> optionalLocationTaxRate =
                locationTaxRateRepository.getLocationTaxRateOfMaxVersionForLocationAndCategoryId( locationTaxRate.getLocationId(), locationTaxRate.getItemCategoryId() );
            if( !optionalLocationTaxRate.isPresent() )
            {
                log.debug( locationMessage );
                locationTaxRate = saveLocationTaxRate( transientLocationTaxRate );
                log.info( "LocationTaxRate saved successfully to the db store." );
            }
            else if( Objects.nonNull( optionalLocationTaxRate.get().getDeactivated() ) )
            {
                transientLocationTaxRate.setVersion( optionalLocationTaxRate.get().getVersion() + 1 );
                log.debug( locationMessage );
                locationTaxRate = saveLocationTaxRate( transientLocationTaxRate );
                log.info( "LocationTaxRate saved successfully to the Persistent store." );
            }
            else
            {
                log.debug( "Already LocationTaxRate Existing in Persistent Store with locationId {} and itemCategoryId {} ", locationTaxRate.getLocationId(),
                    locationTaxRate.getItemCategoryId() );
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.NOT_FOUND.value(), LocationAccount.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DUPLICATE_LOCID_AND_ITEM_CAT_ID ) ) );
            }
        }
        else
        {
            if( !Objects.isNull( locationTaxRate ) )
            {
                Optional<LocationTaxRate> optionalLocationTaxRate = locationTaxRateRepository.getLocationTaxRateForMaxVersion( locationTaxRate.getLocationId() );
                if( !optionalLocationTaxRate.isPresent() )
                {
                    locationTaxRate = saveLocationTaxRate( transientLocationTaxRate );
                }
                else if( Objects.nonNull( optionalLocationTaxRate.get().getDeactivated() ) )
                {
                    transientLocationTaxRate.setVersion( optionalLocationTaxRate.get().getVersion() + 1 );
                    log.debug( locationMessage );
                    locationTaxRate = saveLocationTaxRate( transientLocationTaxRate );
                    log.info( "LocationTaxRate saved successfully to the Persistent store." );
                }
                else
                {
                    throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.NOT_FOUND.value(), LocationAccount.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DUPLICATE_LOCID ) ) );
                }
            }
        }
        return locationTaxRate;
    }

    private LocationTaxRate saveLocationTaxRate( LocationTaxRate locationTaxRate )
    {
        log.trace( "Persisting LocationTaxRate to the Persistent Store." );
        locationTaxRate = locationTaxRateRepository.save( locationTaxRate );
        log.debug( "LocationTaxRate persisted to the Persistent Store." );
        return locationTaxRate;
    }

    public List<LocationTaxRateResponse> getLocationTaxRates( UUID locationId, Pageable pageable )
    {
        boolean flag = false;
        Page<LocationTaxRate> locationTaxRates = locationTaxRateRepository.getByLocationId( locationId, pageable );
        List<LocationTaxRate> listOfTaxRates = locationTaxRates.getContent();
        List<LocationTaxRateResponse> responseList = new ArrayList<>();
        listOfTaxRates.forEach( objetctOfList -> {
            LocationTaxRateResponse response = new LocationTaxRateResponse();
            response.setLocTaxRateId( objetctOfList.getId() );
            response.setEmpId( objetctOfList.getEmpId() );
            if( objetctOfList.getItemCategoryId() != null )
            {
                response.setItemCategoryId( objetctOfList.getItemCategoryId() );
            }
            if( objetctOfList.getTaxCode() != null )
            {
                response.setTaxCode( objetctOfList.getTaxCode() );
            }
            if( objetctOfList.getIsOverriden() != null )
            {
                if( objetctOfList.getIsOverriden() == flag && objetctOfList.getTaxRate() != null )
                {
                    response.setTaxRate( objetctOfList.getTaxRate() );
                }
                else
                {
                    if( objetctOfList.getSuggestedTaxRate() != null )
                    {
                        response.setIsOverriden( true );
                        response.setSuggestedTaxRate( objetctOfList.getSuggestedTaxRate() );
                    }
                }
            }
            else
            {
                if( objetctOfList.getTaxRate() != null )
                {
                    response.setTaxRate( objetctOfList.getTaxRate() );
                }
            }
            response.setVersion( objetctOfList.getVersion() );
            responseList.add( response );
        } );
        return responseList;
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public LocationTaxRate deleteLocationTaxRate( UUID locationId, UUID itemCategoryId )
    {
        Optional<LocationTaxRate> locationTaxRateOptional = null;
        LocationTaxRate locationTaxRate = null;
        try
        {
            if( !Objects.isNull( locationId ) && Objects.isNull( itemCategoryId ) )
            {
                locationTaxRateOptional = locationTaxRateRepository.getLocationTaxRateForMaxVersion( locationId );
            }
            if( !Objects.isNull( locationId ) && !Objects.isNull( itemCategoryId ) )
            {
                locationTaxRateOptional = locationTaxRateRepository.getLocationTaxRateOfMaxVersionForLocationAndCategoryId( locationId, itemCategoryId );
            }
            if( !Objects.isNull( locationTaxRateOptional ) && locationTaxRateOptional.isPresent() )
            {
                locationTaxRate = locationTaxRateOptional.get();
                if( !Objects.isNull( locationTaxRate ) && Objects.isNull( locationTaxRate.getDeactivated() ) )
                {
                    Date date = new Date();
                    locationTaxRate.setDeactivated( new Timestamp( date.getTime() ) );
                    locationTaxRateRepository.save( locationTaxRate );
                }
                else
                {
                    throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), LocationTaxRate.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOC_ID_DEACTIVATED ) ) );
                }
            }
            else
            {
                if( !Objects.isNull( locationId ) && Objects.isNull( itemCategoryId ) )
                {
                    throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), LocationTaxRate.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOC_ID_DEACTIVATED ) ) );
                }
                if( !Objects.isNull( locationId ) && !Objects.isNull( itemCategoryId ) )
                {
                    throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), LocationTaxRate.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOC_ID_DEACTIVATED ) ) );
                }
            }
        }
        catch( DataIntegrityViolationException | JsonParseException exception )
        {
            log.error( "Unable to get location tax rate for {}", locationId, exception );
        }
        return locationTaxRate;
    }
}
