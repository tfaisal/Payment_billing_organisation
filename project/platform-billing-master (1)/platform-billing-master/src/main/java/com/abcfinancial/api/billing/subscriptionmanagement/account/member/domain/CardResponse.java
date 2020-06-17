package com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;

@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonPropertyOrder( {
                        "_id", 
                        "bin", 
                        "brand", 
                        "card_holder_country", 
                        "card_holder_name", 
                        "cvv_verified", 
                        "expiry_month", 
                        "expiry_year", 
                        "issuer_country", 
                        "issuer_name", 
                        "last_four", 
                        "last_update_check", 
                        "organisation", 
                        "currency", 
                        "prepaid", 
                        "type", 
                        "variant", 
                        "created_at", 
                        "updated_at"
                    } )

public class CardResponse
{
    /**
     * The Id of the card
     */

    @JsonProperty( "_id" )
    private String id;
    /**
    *The Bank Identification Number.
     */

    @JsonProperty( "bin" )
    private String bin;
    /**
     * The card brand e.g. MasterCard, Visa or Maestro.
     */

    @JsonProperty( "brand" )
    private String brand;
    /**
     * Two-letter ISO country code identifying the country of the card holder.
     */

    @JsonProperty( "issuer_country" )
    private String issuerCountry;
    /**
     * The name of the card issuer
     */

    @JsonProperty( "issuer_name" )
    private String issuerName;
    /**
     * The last 4 digits of the card number.
     */

    @JsonProperty( "last_four" )
    private String lastFour;
    /**
     * The date this card was last checked for updates by an AU module
     */

    @JsonProperty( "last_update_check" )
    private String lastUpdateCheck;
    /**
     * The id of the organisation to which the card belongs
     */

    @NotNull
    @JsonProperty( "organisation" )
    private String organisation;
    /**
     * The currency of the card
     */

    @JsonProperty( "currency" )
    private String currency;
    /**
     * Specifies whether or not the card is prepaid
     */

    @JsonProperty( "prepaid" )
    private boolean prepaid;
    /**
     *The card type.
     */

    @JsonProperty( "type" )
    private String type;
    /**
     * The card variant.
     */

    @JsonProperty( "variant" )
    private String variant;
    /**
     * Date-Time when card is created.
     */

    @JsonProperty( "created_at" )
    private String createdAt;
    /**
     * Date-Time when card is updated.
     */

    @JsonProperty( "updated_at" )
    private String updatedAt;

    public String getId( )
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getLastFour( )
    {
        return lastFour;
    }

    public void setLastFour( String lastFour )
    {
        this.lastFour = lastFour;
    }

    public String getLastUpdateCheck( )
    {
        return lastUpdateCheck;
    }

    public void setLastUpdateCheck( String lastUpdateCheck )
    {
        this.lastUpdateCheck = lastUpdateCheck;
    }

    public String getUpdatedAt( )
    {
        return updatedAt;
    }

    public void setUpdatedAt( String updatedAt )
    {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt( )
    {
        return createdAt;
    }

    public void setCreatedAt( String createdAt )
    {
        this.createdAt = createdAt;
    }

    public String getBin( )
    {
        return bin;
    }

    public void setBin( String bin )
    {
        this.bin = bin;
    }

    public String getBrand( )
    {
        return brand;
    }

    public void setBrand( String brand )
    {
        this.brand = brand;
    }

    public String getIssuerCountry( )
    {
        return issuerCountry;
    }

    public void setIssuerCountry( String issuerCountry )
    {
        this.issuerCountry = issuerCountry;
    }

    public String getIssuerName( )
    {
        return issuerName;
    }

    public void setIssuerName( String issuerName )
    {
        this.issuerName = issuerName;
    }

    public String getOrganisation( )
    {
        return organisation;
    }

    public void setOrganisation( String organisation )
    {
        this.organisation = organisation;
    }

    public String getCurrency( )
    {
        return currency;
    }

    public void setCurrency( String currency )
    {
        this.currency = currency;
    }

    public boolean isPrepaid( )
    {
        return prepaid;
    }

    public void setPrepaid( boolean prepaid )
    {
        this.prepaid = prepaid;
    }

    public String getType( )
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getVariant( )
    {
        return variant;
    }

    public void setVariant( String variant )
    {
        this.variant = variant;
    }

    @Override
    public String toString( )
    {
        return new ToStringBuilder( this )
            .append( "_id", id )
            .append( "bin", bin )
            .append( "brand", brand )
            .append( "issuer_country", issuerCountry )
            .append( "issuer_name", issuerName )
            .append( "last_four", lastFour )
            .append( "last_update_check", lastUpdateCheck )
            .append( "organisation", organisation )
            .append( "currency", currency )
            .append( "prepaid", prepaid )
            .append( "type", type )
            .append( "variant", variant )
            .append( "created_at", createdAt )
            .append( "updated_at", updatedAt ).toString( );
    }
}
