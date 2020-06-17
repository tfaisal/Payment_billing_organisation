package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class UpdateMemberSubscriptionVO
{

    /**
     * Member Id
     */
    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID memberId;
}
