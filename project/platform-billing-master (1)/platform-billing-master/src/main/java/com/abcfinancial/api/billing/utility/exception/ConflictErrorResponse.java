package com.abcfinancial.api.billing.utility.exception;

import com.abcfinancial.api.common.domain.ConflictResponseError;
import org.springframework.http.HttpStatus;

public class ConflictErrorResponse extends ConflictResponseError
{
    private int code;
    private String message;

    public ConflictErrorResponse( int code, Class entity, String message )
    {
        super( entity, null );
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus( )
    {
        return HttpStatus.CONFLICT;
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
}
