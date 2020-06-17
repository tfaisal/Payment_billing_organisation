package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.CompanyAddress;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.UUID;

@Data
@JsonIgnoreProperties( ignoreUnknown = true )

public class AvalaraAccountVO
{
    /**
     * The offer code provided to you by your Avalara business development contact.
     *This code controls what services and rates the customer will be provisioned with upon creation.
     */

    @NotNull( message = "Offer Code can not be null" )
    @JsonProperty( "offer" )
    private String offer;
    /**
     *The name of the account to create. It should be unique.
     */

    @NotNull( message = "Account name can not be null" )
    @JsonProperty( "accountName" )
    @Size( min = 1, max = 50, message = "Account name size should be between 1 to 50" )
    private String accountName;
    /**
     *The date on which the account should expire
     */

    @JsonProperty( "endDate" )
    private Date endDate;
    /**
     *The date on which the account should expire
     */

    @JsonProperty( "website" )
    private String website;
    /**
     *First name of the primary contact person for this account
     */

    @NotNull( message = "First Name can not be null" )
    @Size( min = 1, max = 50, message = "First name size should be between 1 to 50" )
    @JsonProperty( "firstName" )
    private String firstName;
    /**
     *Last name of the primary contact person for this account
     */

    @NotNull( message = "Last Name can not be null" )
    @Size( min = 1, max = 50, message = "Last name size should be between 1 to 50" )
    @JsonProperty( "lastName" )
    private String lastName;
    /**
     *Email of the primary contact person for this account
     */

    @NotNull( message = "Email Id can not be null" )
    @Size( min = 1, max = 50, message = "Email size should be between 1 to 50" )
    @JsonProperty( "email" )
    private String email;
    /**
     *This option controls what type of a welcome email is sent when the account is created.
     * It may be Normal, Suppressed, Custom.
     */

    @JsonProperty( "welcomeEmail" )
    private String welcomeEmail;
    /**
     *Company code to be assigned to the company created for this account.
     * If no company code is provided, this will be defaulted to "DEFAULT" company code.
     */

    @Size( min = 1, max = 50, message = "companyCode size should be between 1 to 50" )
    @JsonProperty( "companyCode" )
    private String companyCode;
    /**
     *Address information of the account being created.
     */

    @NotNull( message = "Company Address can not be null." )
    @JsonProperty( "companyAddress" )
    private CompanyAddress companyAddress;
    /**
     *Set this to true if and only if the owner of the newly created account
     * accepts Avalara's terms and conditions for your account.
     * Reading and accepting Avalara's terms and conditions is necessary
     * in order for the account to receive a license key.
     */

    @NotNull
    @JsonProperty( "acceptAvalaraTermsAndConditions" )
    private boolean acceptAvalaraTermsAndConditions;
    /**
     *Set this to true if and only if the owner of the newly created account
     * has fully read Avalara's terms and conditions for your account.
     * Reading and accepting Avalara's terms and conditions is necessary in
     * order for the account to receive a license key.
     */

    @NotNull
    @JsonProperty( "haveReadAvalaraTermsAndConditions" )
    private boolean haveReadAvalaraTermsAndConditions;
    /**
     * Organization Id, It should be in UUID format.
     */

    @NotNull( message = "Organization Id should not be null." )
    private UUID organizationId;
    /**
     * Location Id. It should be in UUID format.
     */

    @NotNull( message = "Location Id should not be null." )
    private UUID locationId;
}
