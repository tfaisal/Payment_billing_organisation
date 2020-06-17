package com.abcfinancial.api.billing.subscriptionmanagement.subscription.restutil;

import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.common.domain.InternalServerError;
import com.abcfinancial.api.common.domain.ServiceUnavailableError;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.exception.CustomErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodDimeboxVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Slf4j

@Repository
public class HttpBillingRepository
{
    @Autowired
    private RestTemplate restTemplate;
    private String thirdPartyException = "Third Party API Http Exception: ";
    private String thirdPartyServerException = "Third Party API Http Server/Client Error ";
    private String thirdPartyMissingField = "Third Party API some fields are missing.";

    public <REQ, RES> ResponseEntity<RES> callApiRepo( String requestUrl, HttpEntity<REQ> httpEntity, Class<RES> resClass )
    {
        try
        {
            log.trace( requestUrl );
            return restTemplate.exchange( requestUrl, HttpMethod.POST, httpEntity, resClass );
        }
        catch( HttpClientErrorException exception )
        {
            log.debug( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, exception );
            if( exception.getStatusCode() == HttpStatus.BAD_REQUEST )
            {
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, thirdPartyMissingField ) );
            }
            if( exception.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpBillingRepository.class,
                    MessageUtils.ERROR_MESSAGE_THIRD_PARTY ) );
            }
            try
            {
                RES res = new ObjectMapper().readValue( exception.getResponseBodyAsString(), resClass );
                return new ResponseEntity<RES>( res, exception.getStatusCode() );
            }
            catch( IOException innerException )
            {
                log.debug( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, innerException );
                throw new ErrorResponse(
                    new CustomErrorResponse( exception.getStatusCode(), HttpStatus.CONFLICT.value(), HttpBillingRepository.class, innerException.getMessage() ) );
            }
        }
        catch( HttpServerErrorException exception )
        {
            log.debug( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, exception );
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, thirdPartyServerException ) );
        }
        catch( Exception exception )
        {
            log.warn( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, exception );
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, thirdPartyException + exception.getMessage() ) );
        }
    }

    public <REQ, RES> ResponseEntity<RES> callApiRepoCustom( String requestUrl, HttpEntity<REQ> httpEntity, Class<RES> resClass )
    {
        try
        {
            log.trace( requestUrl );
            return restTemplate.exchange( requestUrl, HttpMethod.POST, httpEntity, resClass );
        }
        catch( HttpClientErrorException exception )
        {
            log.debug( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, exception );
            if( exception.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpBillingRepository.class,
                    MessageUtils.ERROR_MESSAGE_THIRD_PARTY ) );
            }
            try
            {
                RES res = new ObjectMapper().readValue( exception.getResponseBodyAsString(), resClass );
                return new ResponseEntity<RES>( res, exception.getStatusCode() );
            }
            catch( IOException errorInTheError )
            {
                log.debug( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, errorInTheError );
                throw new ErrorResponse(
                    new CustomErrorResponse( exception.getStatusCode(), HttpStatus.CONFLICT.value(), HttpBillingRepository.class, errorInTheError.getMessage() ) );
            }
        }
        catch( HttpServerErrorException hex )
        {
            log.debug( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, hex );
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, thirdPartyServerException ) );
        }
        catch( Exception exception )
        {
            log.warn( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, exception );
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, thirdPartyException + exception.getMessage() ) );
        }
    }

    public <REQ, RES> ResponseEntity<RES> callGetAPI( String requestUrl, HttpEntity<REQ> httpEntity, Class<RES> resClass )
    {
        try
        {
            log.trace( requestUrl );
            return restTemplate.exchange( requestUrl, HttpMethod.GET, httpEntity, resClass );
        }
        catch( HttpClientErrorException exception )
        {
            log.debug( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, exception );
            if( exception.getStatusCode() == HttpStatus.BAD_REQUEST )
            {
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, thirdPartyMissingField ) );
            }
            if( exception.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpBillingRepository.class,
                    MessageUtils.ERROR_MESSAGE_THIRD_PARTY ) );
            }
            try
            {
                PaymentMethodDimeboxVO res = new ObjectMapper().readValue( exception.getResponseBodyAsString(), PaymentMethodDimeboxVO.class );
                throw new ErrorResponse( new CustomErrorResponse( exception.getStatusCode(), res.getCode(), HttpBillingRepository.class, res.getMessage() ) );
            }
            catch( IOException errorInTheError )
            {
                log.debug( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, errorInTheError );
                throw new ErrorResponse(
                    new CustomErrorResponse( exception.getStatusCode(), HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, errorInTheError.getMessage() ) );
            }
        }
        catch( HttpServerErrorException hex )
        {
            log.debug( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, hex );
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, thirdPartyServerException ) );
        }
        catch( Exception exception )
        {
            log.warn( MessageUtils.ERROR_MESSAGE_THIRD_PARTY, exception );
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, thirdPartyException + exception.getMessage() ) );
        }
    }

    public <REQ, RES> ResponseEntity<List<RES>> callApiRepoWithResponseArray( String requestUrl, HttpEntity<REQ> httpEntity, Class<RES> resClass )
    {
        try
        {
            log.trace( requestUrl );
            return restTemplate.exchange( requestUrl, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<List<RES>>()
            {
            } );
        }
        catch( HttpClientErrorException exception )
        {
            log.debug( "Error calling {} ", requestUrl, exception );
            if( exception.getStatusCode() == HttpStatus.BAD_REQUEST )
            {
                throw new ErrorResponse( new InternalServerError( exception ) );
            }
            else
            {
                throw new ErrorResponse( new ServiceUnavailableError( "Unable to access required resource" ) );
            }
        }
        catch( HttpServerErrorException exception )
        {
            log.debug( "Server error calling {}", requestUrl, exception );
            throw new ErrorResponse( new ServiceUnavailableError( "Unable to access required resource" ) );
        }
    }

    public <REQ, RES> ResponseEntity<RES> callApiPut( String requestUrl, HttpEntity<REQ> httpEntity, Class<RES> resClass )
    {
        try
        {
            log.trace( requestUrl );
            return restTemplate.exchange( requestUrl, HttpMethod.PUT, httpEntity, resClass );
        }
        catch( HttpClientErrorException exception )
        {
            log.debug( "Third Party API Error detail: ", exception );
            if( exception.getStatusCode() == HttpStatus.BAD_REQUEST )
            {
                throw new ErrorResponse(
                    new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, thirdPartyMissingField ) );
            }
            if( exception.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpBillingRepository.class,
                    "Third Party API Http Server Error " ) );
            }
            try
            {
                RES res = new ObjectMapper().readValue( exception.getResponseBodyAsString(), resClass );
                return new ResponseEntity<RES>( res, exception.getStatusCode() );
            }
            catch( IOException errorInTheError )
            {
                log.debug( "Error parsing the error in the error", errorInTheError );
                throw new ErrorResponse(
                    new CustomErrorResponse( exception.getStatusCode(), HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, errorInTheError.getMessage() ) );
            }
        }
        catch( HttpServerErrorException exception )
        {
            log.debug( "Third Party API Error detail: ", exception );
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, thirdPartyServerException ) );
        }
        catch( Exception exception )
        {
            log.warn( "Third Party API Exception detail: ", exception );
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), HttpBillingRepository.class, thirdPartyException + exception.getMessage() ) );
        }
    }
}
