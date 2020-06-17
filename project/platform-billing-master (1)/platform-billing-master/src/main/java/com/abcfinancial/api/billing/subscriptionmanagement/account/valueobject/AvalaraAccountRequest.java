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

public class AvalaraAccountRequest
{
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
     *the website of company
     */

    @JsonProperty( "website" )
    private String website;
    /**
     *Last name of the primary contact person for this account
     */

    @NotNull( message = "Last Name can not be null" )
    @Size( min = 1, max = 50, message = "Last name size should be between 1 to 50" )
    @JsonProperty( "lastName" )
    private String lastName;
    /**
     *This option controls what type of a welcome email is sent when the account is created.
     * It may be Normal, Suppressed, Custom.
     */

    @JsonProperty( "welcomeEmail" )
    private String welcomeEmail;
    /**
     *Avalara Company code to be assigned to the company created for this account.
     */

    @NotNull
    @Size( min = 1, max = 50, message = "Avalara CompanyCode size should be between 1 to 50" )
    @JsonProperty( "avaCompanyCode" )
    private String avaCompanyCode;
    /**
     * Platform organization Id. It should be in java.util.UUID format.
     */

    @NotNull
    @JsonProperty( "organizationId" )
    private UUID organizationId;
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
    private Boolean acceptAvalaraTermsAndConditions;
    /**
     *Set this to true if and only if the owner of the newly created account
     * has fully read Avalara's terms and conditions for your account.
     * Reading and accepting Avalara's terms and conditions is necessary in
     * order for the account to receive a license key.
     */

    @NotNull
    @JsonProperty( "haveReadAvalaraTermsAndConditions" )
    private Boolean haveReadAvalaraTermsAndConditions;
}
