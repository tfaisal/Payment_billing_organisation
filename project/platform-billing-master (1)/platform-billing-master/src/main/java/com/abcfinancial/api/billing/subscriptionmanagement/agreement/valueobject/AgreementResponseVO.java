package com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.SubscriptionVO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data

public class AgreementResponseVO
{
    /**
     * Agreement Id
     */

    @NotNull
    private UUID agreementId;
    /**
     * Agreement location Id
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID locationId;
    /**
     * Agreement number up to 15 alphanumeric
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private String agreementNumber;
    /**
     * Document List
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private List<UUID> documentIdList;
    /**
     * List of member Id for existing Subscription
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private List<AgreementMemberRequestVO> memberIdList;
    /**
     * SubscriptionList of new subscription creation
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private List<SubscriptionVO> subscriptionList;

    /**
     * Campaign of this Agreement
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private String campaign;

    /**
     * status of this Agreement Must be one of [ACTIVE, CANCELLED, PENDING_CANCELLATION , EXPIRED].
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private String status;
}
