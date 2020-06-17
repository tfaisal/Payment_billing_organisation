package com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data

public class AgreementCancelResponseVO
{
    /**
     * Agreement Id
     */

    @NotNull
    private UUID agreementId;

    /**
     * Agreement cancellation date is mandatory when cancel the agreement.
     * While removing cancellation date then it's optional
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    private LocalDate agrmCancellationDate;

    /**
     * List of subscription which are associated with agreement
     */
    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private List<UUID> subscriptionIdList;

    /**
     * Agreement number up to 15 alphanumeric
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private String agreementNumber;

    /**
     * Agreement location Id
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID locationId;

}
