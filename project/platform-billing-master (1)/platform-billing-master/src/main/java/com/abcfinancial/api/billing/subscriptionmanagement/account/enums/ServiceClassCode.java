package com.abcfinancial.api.billing.subscriptionmanagement.account.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum ServiceClassCode
{
    DEBITS_ONLY( 225, "Debits Only" ),
    CREDITS_ONLY( 220, "Credits Only" ),
    MIXED( 200, "MIXED Debits and Credits" );
    @Getter
    private final Integer code;
    @Getter
    private final String description;
    @JsonCreator
    public static ServiceClassCode get( String code )
    {
        return Arrays.stream( ServiceClassCode.values( ) )
            .filter( x -> x.toString( ).equalsIgnoreCase( code ) )
            .findFirst( )
            .orElse( null );
    }

    @Override
    @JsonValue
    public String toString( )
    {
        return code + "";
    }
}
