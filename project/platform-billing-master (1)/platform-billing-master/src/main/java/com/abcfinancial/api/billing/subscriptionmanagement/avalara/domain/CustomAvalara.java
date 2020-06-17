package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

public class CustomAvalara
{
    private ErrMessage error;

    public ErrMessage getError()
    {
        return error;
    }

    public void setError( ErrMessage error )
    {
        this.error = error;
    }

    @Override
    public String toString()
    {
        return "error = " + error +
               '}';
    }
}
