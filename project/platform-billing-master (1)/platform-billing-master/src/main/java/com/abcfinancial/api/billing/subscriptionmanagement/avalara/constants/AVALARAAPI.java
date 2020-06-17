package com.abcfinancial.api.billing.subscriptionmanagement.avalara.constants;

public enum AVALARAAPI
{
    CREAETE_ACCOUNT( "CREATENEWACCOUNT", 1 ),
    QUERY_COMPANY( "QUERYCOMPANY", 2 ),
    QUERY_LOCATION( "QUERYLOCATION", 3 ),
    RESOLVE_ADDRESS( "RESOLVEADDRESS", 4 ),
    GET_NEXUS( "GETNEXUS", 5 );

    AVALARAAPI( String name, int value )
    {
        this.apiName = name;
        this.apiValue = value;
    }

    private String apiName;
    private int apiValue;

    public String getApiName()
    {
        return apiName;
    }
}
