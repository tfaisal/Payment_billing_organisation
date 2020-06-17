package com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject;

import lombok.Data;

import java.util.UUID;

@Data

public class SubscriptionExistingPrimaryVO
{
    /**
     * subscription Id for existing Subscription
     */

    private UUID subId;
    /**
     * primary subscription for agreement only
     */

    private boolean primary;
}
