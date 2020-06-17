package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.NexusResponseModel;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaLocation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude( JsonInclude.Include.NON_NULL )

public class OnboardingAccountResponse implements Serializable
{
    /**
     * Avalara account Id
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    @NotNull( message = "Account Id should not be null." )
    private String accountId;
    /**
     * Created date
     */

    @JsonIgnore
    @NotNull( message = "Created Date Id should not be null." )
    private Timestamp createdDate;
    /**
     * License Key of avalara account
     */

    @NotNull( message = "License key should not be null." )
    private String licenseKey;
    /**
     * Company Id
     */

    @NotNull( message = "Company Id should not be null." )
    private String companyId;

    @Override
    public String toString( ) {
        return "OnboardingAccountResponse{" +
                "accountId = '" + accountId + '\'' +
                ", createdDate = " + createdDate +
                ", licenseKey = '" + licenseKey + '\'' +
                ", companyId = '" + companyId + '\'' +
                ", message = " + message +
                ", avaLocation = " + avaLocation +
                ", nexusResponseModels = " + nexusResponseModels +
                '}';
    }
    /**
     * Exception message comes during Avalara account On-boarding.
     */

    private Map<String, String> message;
    /**
     * Avalara Account Location details
     */

    @NotNull( message = "AvaLocation details should not be null." )
    private AvaLocation avaLocation;
    /**
     * List of the Created Nexuses.
     */

    @NotNull( message = "Nexus list for company should not be null." )
    private List<NexusResponseModel> nexusResponseModels;
}
