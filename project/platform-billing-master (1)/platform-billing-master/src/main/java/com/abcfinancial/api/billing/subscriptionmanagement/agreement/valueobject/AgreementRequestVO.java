package com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.SubscriptionVO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data

public class AgreementRequestVO
{

    /**
     * Agreement Id
     */

    @NotNull
    private UUID agreementId;
    /**
     * Agreement number up to 15 alphanumeric
     */
    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private String agreementNumber;

    /**
     * List of member Id's for existing subscription
     */
    @NotNull
    private List<AgreementMemberRequestVO> memberIdList;
    /**
     * List of subscription Id's for existing Subscription
     * Either existing subscription or new subscription should be be available
     */
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private List<SubscriptionExistingPrimaryVO> subscriptionIdList;

    /**
     * Location Id
     */
    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID locationId;
    /**
     * Document List
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private List<UUID> documentIdList;

    /**
     * SubscriptionList of new subscriptions
     * Either existing subscription or new subscription should be available
     */
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private List<SubscriptionVO> subscriptionList;

    /**
     * Campaign of this Agreement
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private String campaign;

    /**
     * status of this Agreement Must be one of [ACTIVE, CANCELLED, PENDING_CANCELLATION , EXPIRED].
     * This status will be the part of response.
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private String status;

}
