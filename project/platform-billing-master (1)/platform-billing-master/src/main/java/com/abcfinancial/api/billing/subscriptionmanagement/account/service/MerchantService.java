package com.abcfinancial.api.billing.subscriptionmanagement.account.service;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.restutil.HttpBillingService;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.MerchantDetailsVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.MerchantRequestVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.MerchantResponseVO;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.billing.utility.common.AuthTokenUtil;
import com.abcfinancial.api.billing.utility.exception.ConflictErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.abcfinancial.api.billing.utility.common.AppConstants.HEADER_AUTHORISATION;
import static com.abcfinancial.api.billing.utility.common.AppConstants.HEADER_AUTHORISATION_BEARER;

@Slf4j
@Service

public class MerchantService
{
    @Autowired
    private HttpBillingService httpService;
    @Value( "${paymentGateway.uri.createMerchant}" )
    private String createMerchantURL;
    @Value( "${paymentGateway.uri.activateMerchant}" )
    private String activateMerchantURL;
    @Value( "${paymentGateway.uri.processorId}" )
    private String processorId;
    @Value( "${paymentGateway.uri.companyId}" )
    private String companyId;
    @Autowired
    private AuthTokenUtil authTokenUtil;

    public MerchantResponseVO createMerchant( HttpHeaders httpHeaders, String merchantName )
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set( HEADER_AUTHORISATION, HEADER_AUTHORISATION_BEARER + authTokenUtil.getUserToken() );
        List<String> processor = new ArrayList<>();
        processor.add( processorId );
        MerchantDetailsVO merchantDetailsVO = new MerchantDetailsVO();
        merchantDetailsVO.setCompanyName( "Testing Company" );
        merchantDetailsVO.setProcessors( processor );
        MerchantRequestVO merchantRequestVO = new MerchantRequestVO();
        merchantRequestVO.setCompanyId( companyId );
        merchantRequestVO.setName( merchantName );
        merchantRequestVO.setMerchantDetails( merchantDetailsVO );
        MerchantResponseVO merchantResponseVO = httpService.callApiCustom( createMerchantURL, headers, merchantRequestVO, MerchantResponseVO.class );
        if( merchantResponseVO.getErrors() != null )
        {
            if( merchantResponseVO.getStatusCode().equals( HttpStatus.CONFLICT.name() ) )
            {
                throw new ErrorResponse( new ConflictErrorResponse( HttpStatus.CONFLICT.value(), LocationAccount.class, merchantResponseVO.getErrors().get( 0 ).getMessage() ) );
            }
            if( merchantResponseVO.getStatusCode().equals( HttpStatus.BAD_REQUEST.name() ) )
            {
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), LocationAccount.class, merchantResponseVO.getErrors().get( 0 ).getMessage() ) );
            }
        }
        log.debug( "Response from Server for createMerchantURL {} is {}", createMerchantURL, merchantResponseVO );
        MerchantResponseVO activateMerchantResponseVO = httpService.callPutApi( activateMerchantURL + merchantResponseVO.getId(), headers, "", MerchantResponseVO.class );
        log.debug( "Response from Server for activateMerchantURL {} is {}", activateMerchantURL, activateMerchantResponseVO );
        //REL1-5206 end
        return activateMerchantResponseVO;
    }
}
