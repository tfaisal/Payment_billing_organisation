package com.abcfinancial.api.billing.utility.advice;

import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.common.domain.ResponseError;
import com.abcfinancial.api.common.domain.ValidationError;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.abcfinancial.api.billing.utility.common.AppConstants.EXCEPTION_VALIDATING_INPUTS;

@Slf4j
@RestControllerAdvice
@Order( Ordered.HIGHEST_PRECEDENCE )

public class GlobalExceptionHandler {
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private HttpServletRequest httpServletRequest;

    @ExceptionHandler
    @ResponseStatus( HttpStatus.BAD_REQUEST )
    public ErrorResponse handleMethodArgumentTypeMismatchException( MethodArgumentTypeMismatchException exception ) {
        log.trace( EXCEPTION_VALIDATING_INPUTS, exception );
        ErrorResponse errorResponse = null;
        String erroMessage = null;
        if( exception instanceof MethodArgumentTypeMismatchException )
        {
            Map<String, Object> uriVariableMap = (Map<String, Object>) httpServletRequest.getAttribute( ( HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE ) );
            Set<Map.Entry<String, Object>> entrySet = uriVariableMap.entrySet();
            Map.Entry<String, Object> entry = entrySet.stream( ).findAny( ).orElse( null );
            if ( entry != null ) {
                String key = entry.getKey( );
                Object value = entry.getValue( );
                erroMessage = applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_UUID ) + " " + key + " = " + value;
                errorResponse = new ErrorResponse( localizedValidationResponse( HttpStatus.BAD_REQUEST.value( ) + "", erroMessage ) );
            } else
                                     {
                //Code to get Query String
                erroMessage = applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID ) + " " + httpServletRequest.getQueryString( );
                errorResponse = new ErrorResponse( localizedValidationResponse( HttpStatus.BAD_REQUEST.value( ) + "", erroMessage ) );
            }
        }
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus( HttpStatus.BAD_REQUEST )
    public ErrorResponse handleIllegalStateException( IllegalStateException exception ) {
        log.debug( EXCEPTION_VALIDATING_INPUTS, exception );
        ErrorResponse errorResponse = null;
        return errorResponse = new ErrorResponse( localizedValidationResponse( HttpStatus.BAD_REQUEST.value( ) + "", applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_REQUEST_URI ) ) );
    }

    @ExceptionHandler
    @ResponseStatus( HttpStatus.BAD_REQUEST )
    public ErrorResponse handleHttpRequestMethodNotSupportedException( HttpRequestMethodNotSupportedException exception ) {
        log.trace( EXCEPTION_VALIDATING_INPUTS, exception );
        ErrorResponse errorResponse = null;
        return errorResponse = new ErrorResponse( localizedValidationResponse( HttpStatus.BAD_REQUEST.value( ) + "", applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_REQUEST_URI ) ) );
    }

    @ExceptionHandler
    @ResponseStatus( HttpStatus.BAD_REQUEST )
    public ErrorResponse handleHttpMessageNotReadableException( HttpMessageNotReadableException exception ) {
        log.trace( EXCEPTION_VALIDATING_INPUTS, exception );
        ErrorResponse errorResponse = null;
        String erroMessage = null;
        HttpMessageNotReadableException httpMessageNotReadableException = exception;
        Throwable cause = httpMessageNotReadableException.getMostSpecificCause( );
        {
            if ( cause instanceof InvalidFormatException ) {
                InvalidFormatException formatException = (InvalidFormatException) cause;
                String field = formatException.getPath( ).stream( )
                        .map( JsonMappingException.Reference::getFieldName )
                        .collect( Collectors.joining( "." ) );
                String parent = formatException.getPath( ).get( 0 ).getFrom( ).getClass( ).getSimpleName( ).toLowerCase( Locale.US );
                String errorCode = parent + "." + field + ".invalid";
                erroMessage = "Unable to parse field " + parent + "." + field;
                if ( "subscriptionexpirevo.locationId.invalid".equals( errorCode ) || "subscriptionvo.locationId.invalid".equals( errorCode ) || "dimeboxprocessor.locationId.invalid".equals( errorCode ) || "membercreationvo.locationId.invalid".equals( errorCode ) || "locationaccountvo.locationId.invalid".equals( errorCode ) || "locationtaxratebuilder.location.invalid".equals( errorCode ) || "itemsvo.locationId.invalid".equals( errorCode ) ) {
                    erroMessage = applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_LOCATION_ID );
                }
                else if ( "locationaccountvo.account.paymentMethod.type.invalid".equals( errorCode ) || "membercreationvo.account.paymentMethod.type.invalid".equals( errorCode ) || "updateaccountdetailvo.account.paymentMethod.type.invalid".equals( errorCode ) ) {
                    erroMessage = applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYMENT_NOT_FOUND );
                }
                else if ( "subscriptionexpirevo.memberId.invalid".equals( errorCode ) || "membercreationvo.memberId.invalid".equals( errorCode ) || "subscriptionvo.memberId.invalid".equals( errorCode ) ) {
                    erroMessage = applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBERID_INVALID );
                }
                else if ( "subscriptionvo.planId.invalid".equals( errorCode ) ) {
                    erroMessage = applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PLAN_ID_INVALID );
                }
                else if ( "subscriptionvo.salesEmployeeId.invalid".equals( errorCode ) ) {
                    erroMessage = applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SELLS_EMPID_INVALID );
                }
                else if ( "subscriptionvo.accountId.invalid".equals( errorCode ) ) {
                    erroMessage = applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNTID_INVALID );
                }
                else if ( "subscriptionvo.duration.invalid".equals( errorCode ) ) {
                    erroMessage = applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DURATION_VALUE );
                } else if ( "subscriptionvo.renewType.invalid".equals( errorCode ) ) {
                    erroMessage = applicationConfiguration.getValue( MessageUtils.RENEW_TYPE_MUST_NOT_HAVE_SPACES );
                }
                else if( "subscriptionvo.items.null.itemId.invalid".equalsIgnoreCase( errorCode ) ) {
                    erroMessage = applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ITEMID_INVALID );
                } else
                                     {
                    if( "items.null.type".equalsIgnoreCase( field ) )
                    {
                        field = "item type";
                    }
                    if( "items.null.price".equalsIgnoreCase( field ) )
                    {
                        field = "item price";
                    }
                    erroMessage = applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PARSE_ANY_FIELD ) + field;
                }
            return errorResponse = new ErrorResponse( localizedValidationResponse( HttpStatus.BAD_REQUEST.value( ) + "", erroMessage ) );
            }
            if ( cause instanceof DateTimeParseException || cause instanceof DateTimeException ) {
                InvalidFormatException invalidFormatException = (InvalidFormatException) exception.getCause( );
                StringBuffer fieldName = new StringBuffer( );
                for( int i = 0; i < invalidFormatException.getPath( ).size( ); i++ )
                {
                    fieldName.append( invalidFormatException.getPath( ).get( i ).getFieldName( ) );
                    if( i + 1 < invalidFormatException.getPath( ).size( ) )
                    {
                        fieldName.append( " -> " );
                    }
                }

                errorResponse = new ErrorResponse( localizedValidationResponse( HttpStatus.BAD_REQUEST.value( ) + "", fieldName + " " + applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_REQUEST_METHOD_DATE_TIME ) ) );
                return errorResponse;
            } else 
                                     {
                errorResponse = new ErrorResponse( localizedValidationResponse( HttpStatus.BAD_REQUEST.value( ) + "", applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_REQUEST_METHOD ) ) );
            }
        }
        return errorResponse;
    }

    private ResponseError localizedValidationResponse( String code, String defaultMessage, Object... arguments ) {
        final String codeKey = removeBracketPairsWithContent( code );
        return new ValidationError( code, messageSource.getMessage( codeKey, arguments, defaultMessage, LocaleContextHolder.getLocale( ) ) );
    }

    private String removeBracketPairsWithContent( String str ) {
        return StringUtils.isEmpty( str ) ? str : str.replaceAll( "\\[( .*? )]", "" );
    }
}
