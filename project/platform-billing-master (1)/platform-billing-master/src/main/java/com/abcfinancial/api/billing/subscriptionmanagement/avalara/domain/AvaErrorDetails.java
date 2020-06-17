package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

public class AvaErrorDetails
{
    private String code;
    private String number;
    private String message;
    private String description;
    private String faultCode;
    private String helpLink;
    private String severity;

    public String getFaultCode()
    {
        return faultCode;
    }

    public void setFaultCode( String faultCode )
    {
        this.faultCode = faultCode;
    }

    public String getHelpLink()
    {
        return helpLink;
    }

    public void setHelpLink( String helpLink )
    {
        this.helpLink = helpLink;
    }

    public String getSeverity()
    {
        return severity;
    }

    public void setSeverity( String severity )
    {
        this.severity = severity;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode( String code )
    {
        this.code = code;
    }

    public String getNumber()
    {
        return number;
    }

    public void setNumber( String number )
    {
        this.number = number;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return "AvaErrorDetails{" +
               "code = '" + code + '\'' +
               ", number = '" + number + '\'' +
               ", message = '" + message + '\'' +
               ", description = '" + description + '\'' +
               ", faultCode = '" + faultCode + '\'' +
               ", helpLink = '" + helpLink + '\'' +
               ", severity = '" + severity + '\'' +
               '}';
    }
}
