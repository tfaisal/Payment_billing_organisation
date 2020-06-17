package com.abcfinancial.api.billing.utility.exception;

import com.abcfinancial.api.common.domain.NotFoundResponseError;

public class EntityNotFoundResponseError extends NotFoundResponseError
{
    private int code;
    private String message;

    public EntityNotFoundResponseError( )
    {
    }

    public EntityNotFoundResponseError( int code, Class entity )
    {
        super( entity, null );
        this.code = code;
    }

    public EntityNotFoundResponseError( int code, Class entity, Object search )
    {
        super( entity, search );
        this.code = code;
    }

    public EntityNotFoundResponseError( int code, Class entity, String message )
    {
        super( entity, null );
        this.code = code;
        this.message = message;
    }

    public EntityNotFoundResponseError( int code, String message )
    {
        super( null );
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
}
