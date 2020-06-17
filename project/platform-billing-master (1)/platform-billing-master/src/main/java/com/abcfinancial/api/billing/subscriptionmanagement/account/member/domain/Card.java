package com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;

@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonPropertyOrder( {
                        "card_number",
                        "cvv",
                        "expiry_month",
                        "expiry_year",
                        "organisation"
                    } )

public class Card
{
    /**
     * The full card number, also known as PAN.
     */

    @NotNull
    @JsonProperty( "card_number" )
    private String cardNumber;
    /**
     * The Card Verification Value.
     */

    @JsonProperty( "cvv" )
    private String cvv;
    /**
     * Card expiry month. A string representing the month, valid values are 01 to 12.
     */

    @NotNull
    @JsonProperty( "expiry_month" )
    private String expiryMonth;
    /**
     * Card expiry year. A string representing the last two digits of the year, e.g. 19 for 2019.
     */

    @NotNull
    @JsonProperty( "expiry_year" )
    private String expiryYear;
    /**
     * The id of the organisation to which the card belongs.
     */

    @NotNull
    @JsonProperty( "organisation" )
    private String organisation;

    public String getCardNumber()
    {
        return cardNumber;
    }

    public void setCardNumber( String cardNumber )
    {
        this.cardNumber = cardNumber;
    }

    public String getExpiryMonth()
    {
        return expiryMonth;
    }

    public void setExpiryMonth( String expiryMonth )
    {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear()
    {
        return expiryYear;
    }

    public void setExpiryYear( String expiryYear )
    {
        this.expiryYear = expiryYear;
    }

    public String getCvv()
    {
        return cvv;
    }

    public void setCvv( String cvv )
    {
        this.cvv = cvv;
    }

    public String getOrganisation()
    {
        return organisation;
    }

    public void setOrganisation( String organisation )
    {
        this.organisation = organisation;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder( this )
            .append( "card_number", cardNumber )
            .append( "cvv", cvv )
            .append( "expiry_month", expiryMonth )
            .append( "expiry_year", expiryYear )
            .append( "organisation", organisation ).toString();
    }
}
