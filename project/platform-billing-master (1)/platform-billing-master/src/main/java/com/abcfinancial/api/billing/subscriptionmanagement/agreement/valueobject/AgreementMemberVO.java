package com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_DEFAULT )

public class AgreementMemberVO
{
    /**
     * List of member Id
     */
    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private List<UUID> memberIdList;
}
