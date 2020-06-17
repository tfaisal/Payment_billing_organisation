package com.abcfinancial.api.billing.utility.common;

import lombok.extern.slf4j.Slf4j;

import static com.abcfinancial.api.billing.utility.common.MessageUtils.ACCOUNT_ROUTING_NUMBER_REGEX;

@Slf4j
public final class ABARoutingNumber
{
    private ABARoutingNumber()
    {

    }

    public static boolean isValidRoutingNumber( String routingNumber )
    {
        boolean flag = false;
        if( routingNumber.matches( ACCOUNT_ROUTING_NUMBER_REGEX ) )
        {
            return flag;
        }
        int sum = 0;
        try
        {
            sum = ( 3 * ( Integer.parseInt( "" + routingNumber.charAt( 0 ) ) + Integer.parseInt( "" + routingNumber.charAt( 3 ) ) +
                          Integer.parseInt( "" + routingNumber.charAt( 6 ) ) ) )
                  + ( 7 * ( Integer.parseInt( "" + routingNumber.charAt( 1 ) ) + Integer.parseInt( "" + routingNumber.charAt( 4 ) ) +
                            Integer.parseInt( "" + routingNumber.charAt( 7 ) ) ) )
                  + ( 1 * ( Integer.parseInt( "" + routingNumber.charAt( 2 ) ) + Integer.parseInt( "" + routingNumber.charAt( 5 ) ) ) ) +
                  Integer.parseInt( "" + routingNumber.charAt( 8 ) );
            if( sum % 10 == 0 )
            {
                flag = true;
                return flag;
            }
            else
            {
                return flag;
            }
        }
        catch( Exception exception )
        {
            log.debug( "Why are we eating exceptions???", exception ); //todo MarkV why???
        }
        return flag;
    }
}
