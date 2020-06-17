package com.abcfinancial.api.billing.utility.common;

import java.util.Optional;

public class Validation
{
    private Validation()
    {
    }

    public static String digitsOnly( String value )
    {
        return digitsOnly( Optional.ofNullable( value ) ).orElse( null );
    }

    public static Optional<String> digitsOnly( Optional<String> value )
    {
        return trim( value ).map( v -> v.replaceAll( "[^\\d]", "" ) );
    }

    public static Optional<String> trim( Optional<String> value )
    {
        return value.map( String::trim ).map( v -> v.isEmpty() ? null : v );
    }

    public static String trim( String value )
    {
        return trim( Optional.ofNullable( value ) ).orElse( null );
    }

}
