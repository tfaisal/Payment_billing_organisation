package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.abcfinancial.api.billing.utility.common.CommonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data

public class TransactionInfo
{
    /**
     * object able to programmatically obtain the position of the location
     */

    @JsonProperty( "geo_location" )
    private String[] geoLocation;
    /**
     * An unique identification of the transaction
     */

    @JsonProperty( "_id" )
    private String id;
    /**
     * Account identification for under which a particular transaction is being performing
     */

    @JsonProperty( "account" )
    private String account;
    /**
     * Amount for which we are performing the transaction
     */

    @JsonProperty( "amount" )
    private int amount;

    public BigDecimal getAmount()
    {
        return CommonUtil.convertCenttoDollar( amount );
    }

    /**
     * code provided by issuing bank for validating a credit card
     */

    @JsonProperty( "authorization_code" )
    private String authorizationCode;
    /**
     *
     */

    @JsonProperty( "blocked" )
    private boolean bocked;
    /**
     * A token representing the payment card
     */

    @JsonProperty( "card" )
    private String card;
    /**
     * A city name for the address, 100 characters or less
     */

    @JsonProperty( "city" )
    private String city;
    /**
     * A 2-letter ISO3166 alpha-2. country code for the address
     */

    @JsonProperty( "countrty_code" )
    private String countryCode;
    /**
     * The date and time when the Transaction FIRST created
     */

    @JsonProperty( "created_at" )
    private Instant createdAt;
}
