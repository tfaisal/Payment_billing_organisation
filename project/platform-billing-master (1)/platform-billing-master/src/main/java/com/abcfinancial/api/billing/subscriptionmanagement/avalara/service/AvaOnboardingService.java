package com.abcfinancial.api.billing.subscriptionmanagement.avalara.service;

import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AvalaraAccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.OnboardingAccountResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.constants.AVALARAAPI;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.*;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.helper.OnboadingServiceHelper;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaCompany;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaLocation;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaNexus;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaCompanyRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaLocationRepository;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.CommonUtil;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.common.domain.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j

public class AvaOnboardingService<T>
{
    private static final int ONBOARDING_ESTIMATED_MINUTES = 5;
    Map<String, String> errorMap = new HashMap<>( 5 );
    HttpHeaders httpHeaders = new HttpHeaders();
    @Autowired
    private AvaAccountService avaAccountService;
    @Autowired
    private AvaCompanyService avaCompanyService;
    @Autowired
    private AvaLocationService avaLocationService;
    @Autowired
    private AvaNexusService avaNexusService;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private AvaCompanyRepository avaCompanyRepository;
    @Autowired
    private AvaLocationRepository avaLocationRepository;

    private final <T> T nextCall( LocalTime localTime, int estimatedMinutes, AVALARAAPI avalaraapi, OnboardingAccountResponse onboardingAccountResponse,
        LocationModel locationModel, String companyId, Class<T> responseType )
    {

        if( Objects.nonNull( onboardingAccountResponse ) && Objects.nonNull( onboardingAccountResponse.getAccountId() ) &&
            Objects.nonNull( onboardingAccountResponse.getLicenseKey() ) && Objects.nonNull( localTime ) && Objects.nonNull( avalaraapi ) && Objects.nonNull( responseType ) )
        {

            final int startMinute = localTime.getMinute();
            final LocalTime currentTime = LocalTime.now();
            final int endMinute = currentTime.getMinute();
            final int consumedMinutes = endMinute - startMinute;

            if( consumedMinutes == estimatedMinutes || consumedMinutes > estimatedMinutes )
            {
                errorMap.put( avalaraapi.getApiName(), " Exceeds the Estimated Minutes  " + estimatedMinutes );

            }

            while( true )
            {
                try
                {
                    switch( avalaraapi )
                    {
                        case CREAETE_ACCOUNT:
                            return null;
                        case QUERY_COMPANY:
                            return (T) avaCompanyService.queryCompanies( onboardingAccountResponse.getAccountId(), onboardingAccountResponse.getLicenseKey() );
                        case QUERY_LOCATION:
                            return (T) avaAccountService.queryLocation( onboardingAccountResponse.getAccountId(), onboardingAccountResponse.getLicenseKey() );
                        case RESOLVE_ADDRESS:
                        {
                            if( Objects.nonNull( locationModel ) )
                            {

                                httpHeaders.add( "username", onboardingAccountResponse.getAccountId() );
                                httpHeaders.add( "password", onboardingAccountResponse.getLicenseKey() );
                                Long locationId = Long.valueOf( locationModel.getId() );
                                return (T) avaLocationService.resolveAddress( locationId, OnboadingServiceHelper.prepareAddress( locationModel ), httpHeaders );
                            }

                        }
                        case GET_NEXUS:
                            return (T) avaNexusService.getNexus( onboardingAccountResponse.getAccountId(), onboardingAccountResponse.getLicenseKey(), companyId );
                    }
                }
                catch( Exception exception )
                {
                    //IF Avalara Side is exception then
                    if( consumedMinutes == estimatedMinutes || consumedMinutes > estimatedMinutes )
                    {
                        throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvaOnboardingService.class,
                            "  API Name " + avalaraapi.getApiName() + " Exceeds the Consumption Minute " + estimatedMinutes ) );

                    }

                    if( exception instanceof HttpClientErrorException && ( ( (HttpClientErrorException) exception ).getStatusCode() == HttpStatus.BAD_REQUEST ||
                                                                           ( (HttpClientErrorException) exception ).getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR ) )
                    {

                        throw new HttpClientErrorException( HttpStatus.BAD_REQUEST );
                    }

                    nextCall( localTime, estimatedMinutes, avalaraapi, onboardingAccountResponse, locationModel, companyId, responseType );
                }
            }

        }

        return null;
    }

    public OnboardingAccountResponse onboardAccount( AvalaraAccountVO avalaraAccountVO )
    {
        OnboardingAccountResponse onboardingAccountResponse = new OnboardingAccountResponse();
        OnboardingAccountResponse onboardingAccResponse = null;
        String username = null;
        String companyId = null;
        AvaAccount avaAccount = null;
        CompanyResponse companyResponse = null;
        AvaCompany avaCompany = null;

        boolean isReadTermsAndConditions = avalaraAccountVO.isHaveReadAvalaraTermsAndConditions();
        boolean isAcceptTermsConditions = avalaraAccountVO.isAcceptAvalaraTermsAndConditions();
        LocationResponse locationResponse = null;
        AvaLocation avalaraLocation = null;

        if( isReadTermsAndConditions && isAcceptTermsConditions )
        {
            try
            {
                onboardingAccountResponse = new OnboardingAccountResponse();
                onboardingAccResponse = avaAccountService.requestNewAccount( avalaraAccountVO );
                avaAccount = OnboadingServiceHelper.prepare( onboardingAccResponse, avalaraAccountVO );
                avaAccount = avaAccountService.save( avaAccount );
                onboardingAccResponse.setCreatedDate( avaAccount.getCreatedDate() );
                onboardingAccountResponse.setAccountId( onboardingAccResponse.getAccountId() );
                onboardingAccountResponse.setLicenseKey( onboardingAccResponse.getLicenseKey() );
            }
            catch( HttpServerErrorException | HttpClientErrorException exception )
            {
                String message = null;
                if( exception instanceof HttpServerErrorException )
                {
                    message = CommonUtil.buildExceptionMessage( ( (HttpServerErrorException) exception ).getResponseBodyAsString() );
                }
                else
                {
                    message = CommonUtil.buildExceptionMessage( ( (HttpClientErrorException) exception ).getResponseBodyAsString() );
                }
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvaOnboardingService.class,
                    message ) );
            }

            LocalTime currentTime = LocalTime.now();

            try
            {
                errorMap.clear();
                companyResponse = doQueryCompany( onboardingAccResponse, avaAccount, currentTime );
                if( companyResponse.getRecordsetCount() == 0 )
                {
                    errorMap.put( AVALARAAPI.QUERY_COMPANY.getApiName(),
                        CommonUtil.buildExceptionMessage( "Query Company Failing on Onbarding : " + AVALARAAPI.QUERY_COMPANY.getApiName() ) );
                    onboardingAccountResponse.setMessage( errorMap );
                    return onboardingAccountResponse;
                }
                if( Objects.nonNull( companyResponse ) )
                {
                    companyId = String.valueOf( companyResponse.getValue().get( 0 ).getId() );
                    onboardingAccResponse.setCompanyId( companyId );
                    onboardingAccountResponse.setCompanyId( companyId );

                }
            }
            catch( HttpServerErrorException | HttpClientErrorException exception )
            {
                errorMap.put( AVALARAAPI.QUERY_COMPANY.getApiName(),
                    CommonUtil.buildExceptionMessage( "Query Company Failing on Onbarding : " + AVALARAAPI.QUERY_COMPANY.getApiName() ) );
            }

            //STEP 3 Query Location API
            LocationModel locationModel = null;
            try
            {
                locationModel = doQueryLocation( onboardingAccResponse, currentTime );
            }
            catch( HttpServerErrorException | HttpClientErrorException exception )
            {
                log.error( "Exception occurred IN doQuery Location {}  ", exception.getResponseBodyAsString() );
                errorMap.put( "", ( "Bad Request For Avalara API : " + AVALARAAPI.QUERY_LOCATION.getApiName() ) );
            }

            //STEP 4 Resolve Address API
            try
            {
                avalaraLocation = doResolveAddress( currentTime, onboardingAccResponse, locationModel );
                onboardingAccResponse.setAvaLocation( avalaraLocation );
                onboardingAccountResponse.setAvaLocation( avalaraLocation );
            }
            catch( HttpServerErrorException | HttpClientErrorException exception )
            {
                log.error( "Exception occurred while Resolving Address {}", exception );
                errorMap.put( AVALARAAPI.RESOLVE_ADDRESS.getApiName(), ( "Error in resolving address For Avalara API : " + AVALARAAPI.RESOLVE_ADDRESS.getApiName() ) );
            }

            //GET 5 Nexus API
            try
            {
                if( !StringUtils.isEmpty( companyId ) && Objects.nonNull( locationModel ) )
                {

                    List<NexusResponseModel> nexusResponseModelList = doGetNexus( currentTime, onboardingAccResponse, companyId, locationModel );
                    onboardingAccountResponse.setNexusResponseModels( nexusResponseModelList );
                }
            }
            catch( HttpServerErrorException | HttpClientErrorException exception )
            {
                errorMap.put( AVALARAAPI.GET_NEXUS.getApiName(), ( "Error in getting nexus For Avalara API :  " + AVALARAAPI.GET_NEXUS.getApiName() ) );
            }
        }
        else
        {
            errorMap.put( "Error RequestNewAccount", "For avalara onboarding Avalara Terms and condition should be Accepted" );
        }

        if( errorMap.size() != 0 )
        {
            onboardingAccountResponse.setMessage( errorMap );
        }

        return onboardingAccountResponse;
    }

    private List<NexusResponseModel> doGetNexus( LocalTime localTime, OnboardingAccountResponse onboardingAccountResponse, String companyId, LocationModel locationModel )
    {
        List<NexusResponseModel> nexusResponseModelList = null;
        if( Objects.nonNull( onboardingAccountResponse ) && Objects.nonNull( localTime ) &&
            Objects.nonNull( onboardingAccountResponse.getAccountId() ) && Objects.nonNull( onboardingAccountResponse.getLicenseKey() ) )
        {
            nexusResponseModelList =
                (List) nextCall( localTime, ONBOARDING_ESTIMATED_MINUTES, AVALARAAPI.GET_NEXUS, onboardingAccountResponse, null,
                    companyId, NexusResponse.class );

            if( Objects.nonNull( nexusResponseModelList ) && !nexusResponseModelList.isEmpty() )
            {
                onboardingAccountResponse.setNexusResponseModels( nexusResponseModelList );

                AvaCompany avaCompany = avaCompanyRepository.findByCompanyId( Long.parseLong( companyId ) );
                AvaLocation avaLocation = avaLocationRepository.findByLocationId( Long.valueOf( locationModel.getId() ) );

                for( NexusResponseModel nexusResponseModel : nexusResponseModelList )
                {

                    if( Objects.nonNull( avaLocation ) && Objects.nonNull( avaCompany ) )
                    {
                        AvaNexus avaNexus = OnboadingServiceHelper.prepareAvaNexus( nexusResponseModel, avaLocation, avaCompany );
                        log.debug( "Saving AvaNexus to the Persistent Store." );
                        if( avaNexus != null )
                        {
                            avaNexusService.save( avaNexus );
                            break;
                        }
                    }
                }
            }
        }
        return nexusResponseModelList;
    }

    private AvaLocation doResolveAddress( LocalTime startTime, OnboardingAccountResponse onboardingAccountResponse, LocationModel locationModel )
    {
        AvaLocation avaLocation = null;
        try
        {
            avaLocation = nextCall( startTime, ONBOARDING_ESTIMATED_MINUTES, AVALARAAPI.RESOLVE_ADDRESS, onboardingAccountResponse, locationModel,
                null, AvaLocation.class );
            if( Objects.isNull( avaLocation ) )
            {
                avaLocation = nextCall( startTime, ONBOARDING_ESTIMATED_MINUTES, AVALARAAPI.RESOLVE_ADDRESS, onboardingAccountResponse, locationModel,
                    null, AvaLocation.class );
            }

            if( Objects.nonNull( avaLocation ) )
            {
                onboardingAccountResponse.setAvaLocation( avaLocation );
            }

        }
        catch( Exception exception )
        {
            throw new HttpClientErrorException( HttpStatus.BAD_REQUEST );
        }
        return avaLocation;

    }

    private LocationModel doQueryLocation( OnboardingAccountResponse onboardingAccountResponse, LocalTime startTime )
    {
        LocationResponse locationResponse = null;
        Long locationId = null;
        LocationModel locationModel = null;

        if( Objects.nonNull( onboardingAccountResponse ) && Objects.nonNull( startTime ) &&
            Objects.nonNull( onboardingAccountResponse.getAccountId() ) && Objects.nonNull( onboardingAccountResponse.getLicenseKey() ) )
        {
            locationResponse =
                nextCall( startTime, ONBOARDING_ESTIMATED_MINUTES, AVALARAAPI.QUERY_LOCATION, onboardingAccountResponse, null,
                    null, LocationResponse.class );

            List<LocationModel> locationModels = locationResponse.getValue();

            if( Objects.nonNull( locationModels ) && !locationModels.isEmpty() )
            {
                locationModel = locationModels.get( 0 );

                Address address = OnboadingServiceHelper.prepareAddress( locationModels.get( 0 ) );
                log.debug( "Saving Avalara Location to the DB." );
                avaLocationService.createLocation( locationModel.getId(), address );
            }
        }
        return locationModel;
    }

    public void processCompanyResponse( OnboardingAccountResponse onboardingAccountResponse, CompanyResponse companyResponse, AvaAccount avaAccount )
    {
        AvaCompany avaCompany = null;
        if( Objects.isNull( onboardingAccountResponse ) || Objects.isNull( companyResponse ) || Objects.isNull( avaAccount ) )
        {
            throw new IllegalArgumentException( "Invalid OnboardingAccountResponse or CompanyResponse" );
        }
        else
        {
            if( Objects.nonNull( companyResponse.getValue() ) && !companyResponse.getValue().isEmpty() )
            {
                avaCompany =
                    OnboadingServiceHelper.prepareAvaCompany( companyResponse.getValue().get( 0 ).getId() + "", avaAccount, onboardingAccountResponse.getLicenseKey() );
                log.debug( "Saving Avalara Company to the  DB." );
                avaCompanyService.save( avaCompany );
            }
        }
    }

    public CompanyResponse doQueryCompany( OnboardingAccountResponse onboardingAccResponse, AvaAccount avaAccount, LocalTime currentTime )
    {

        CompanyResponse companyResponse = null;
        if( Objects.nonNull( onboardingAccResponse ) && Objects.nonNull( avaAccount ) && Objects.nonNull( currentTime ) &&
            Objects.nonNull( onboardingAccResponse.getAccountId() ) && Objects.nonNull( onboardingAccResponse.getLicenseKey() ) )
        {
            log.debug( "Requesting for query company.." );
            companyResponse =
                nextCall( currentTime, ONBOARDING_ESTIMATED_MINUTES, AVALARAAPI.QUERY_COMPANY, onboardingAccResponse, null, null, CompanyResponse.class );

            processCompanyResponse( onboardingAccResponse, companyResponse, avaAccount );
        }
        return companyResponse;
    }
}
