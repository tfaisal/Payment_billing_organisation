package com.abcfinancial.api.billing.utility.email.valueobject;

import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.common.email.domain.EmailTemplateVO;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Data

public class PurchaseEmailVO extends EmailTemplateVO
{
    /**
     * email template
     */

    private String template;
    /**
     * Application configuration object
     */

    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    /**
     * Subscription Id
     */

    @NotNull
    private UUID subscriptionId;
    /**
     * Email of User
     */

    @Email
    private String userEmail;
    /**
     * Business Logo
     */

    @NotNull
    private String businessLogo;
    /**
     * First Name
     */

    @NotNull
    private String firstName;
    /**
     * Class date
     */

    @NotNull
    private String classDate;
    /**
     * Total price of Subscription
     */

    @NotNull
    private String subtotalPrice;
    /**
     * Tax value of Subscription
     */

    @NotNull
    private String taxesValue;
    /**
     * Total value of Subscription
     */

    @NotNull
    private String totalValue;
    //these are to be filled from db
    /**
     * Membership of subscription
     */

    private String membership;
    /**
     * Installment price of Subscription
     */

    private String installmentPrice;
    /**
     * frequency of subscription
     */

    private String frequency;
    /**
     * Billing cycle of subscription
     */

    private String billingCycles;
    /**
     * Name of Business
     */

    @NotNull
    private String businessName;
    /**
     * First Business Address
     */

    @NotNull
    private String businessAddress1;
    /**
     * Second Business Address
     */

    private String businessAddress2;
    /**
     * City of Business
     */

    @NotNull
    private String businessCity;
    /**
     * State of Business
     */

    @NotNull
    private String businessState;
    /**
     * Zip code of Business address
     */

    @NotNull
    private String businessZIP;
    /**
     * Phone Number of Business
     */

    @NotNull
    private String businessPhoneNumber;
    /**
     * Email of Business Administrative
     */

    @Email
    private String businessAdministrativeEmail;
    /**
     * Address of Business website
     */

    @NotNull
    private String businessWebsiteAddress;

    @Override
    public String buildParameters( )
    {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder( )
                .add( "businessLogo", businessLogo )
                .add( "firstName", firstName )
                .add( "classDate", classDate )
                .add( "subtotalPrice", subtotalPrice )
                .add( "taxesValue", taxesValue )
                .add( "totalValue", totalValue )
                .add( "membership", StringUtils.defaultIfBlank( membership, "N/A" ) )
                .add( "installmentPrice", StringUtils.defaultIfBlank( installmentPrice, "N/A" ) )
                .add( "frequency", StringUtils.defaultIfBlank( frequency, "N/A" ) )
                .add( "billingCycles", StringUtils.defaultIfBlank( billingCycles, "N/A" ) )
                .add( "businessName", businessName )
                .add( "businessAddress1", businessAddress1 )
                .add( "businessCity", businessCity )
                .add( "businessState", businessState )
                .add( "businessZIP", businessZIP )
                .add( "businessPhoneNumber", businessPhoneNumber )
                .add( "businessAdministrativeEmail", businessAdministrativeEmail )
                .add( "businessWebsiteAddress", businessWebsiteAddress );
        if( StringUtils.isNotBlank( businessAddress2 ) )
        {
            objectBuilder.add( "hasAddressLine2", true );
            objectBuilder.add( "businessAddress2", businessAddress2 );
        }
        else
        {
            objectBuilder.add( "hasAddressLine2", false );
        }
        return objectBuilder.build( ).toString( );
    }

    @Override
    public String templateName( )
    {
        return Optional.ofNullable( template )
                       .orElseThrow( ( ) -> new IllegalArgumentException( applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_EMAIL_TEMPLATE ) ) );
    }
}
