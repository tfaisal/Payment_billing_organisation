package com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class AgreementMemberRequestVO
{
    /**
     * List of member Id
     */
    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID memberId;

    /**
     * Member primary
     */
    private boolean primary;

}
