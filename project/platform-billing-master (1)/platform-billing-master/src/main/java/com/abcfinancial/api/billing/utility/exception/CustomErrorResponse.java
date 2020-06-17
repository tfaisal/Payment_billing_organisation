package com.abcfinancial.api.billing.utility.exception;

import com.abcfinancial.api.common.domain.DataIntegrityViolationResponseError;
import org.springframework.http.HttpStatus;

public class CustomErrorResponse extends DataIntegrityViolationResponseError
{
    private int code;
    private String message;
    private HttpStatus httpStatus;

    public CustomErrorResponse( HttpStatus httpStatus, int code, Class entity, String message )
    {
        super( entity );
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode( )
    {
        return String.valueOf( code );
    }

    @Override
    public String getMessageKey( )
    {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus( )
    {
        return httpStatus;
    }
}
