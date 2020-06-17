package com.abcfinancial.api.billing.utility.response.constant;

public enum CodeMessageResponse
{
    RESPONSE1( 101, "Payment decline." );
    private final int code;
    private final String message;
    CodeMessageResponse( int code, String message )
    {
        this.code = code;
        this.message = message;
    }

    public int getCode( )
    {
        return code;
    }

    public String getMessage( )
    {
        return message;
    }
}
