package com.abcfinancial.api.billing.utility.exception;

import com.abcfinancial.api.common.domain.DataIntegrityViolationResponseError;
import org.springframework.http.HttpStatus;

public class DataIntegrityViolationResponse extends DataIntegrityViolationResponseError
{
    private int code;
    private String message;

    public DataIntegrityViolationResponse( int code, Class entity, String message )
    {
        super( entity );
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
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String toString( ) {
        return "DataIntegrityViolationResponse{" +
                "code = " + code +
                ", message = '" + message +
                '}';
    }
}
