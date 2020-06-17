package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

import java.util.Arrays;

public class ErrMessage
{
    private String code;
    private String message;
    private String target;
    private AvaErrorDetails[] details;

    public String getCode()
    {
        return code;
    }

    public void setCode( String code )
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget( String target )
    {
        this.target = target;
    }

    public AvaErrorDetails[] getDetails()
    {
        return details;
    }

    public void setDetails( AvaErrorDetails[] details )
    {
        this.details = details;
    }

    @Override
    public String toString()
    {
        return "ErrMessage{" +
               "code = '" + code + '\'' +
               ", message = '" + message + '\'' +
               ", target = '" + target + '\'' +
               ", details = " + Arrays.toString( details ) +
               '}';
    }
}
