package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import lombok.Data;

import java.util.UUID;

@Data

public class SubscriptionUpdateVO extends SubscriptionVO
{
    /**
     * original subscription id in freeze period
     */

    private UUID freezeSubId;
    /**
     * original subscription id in clone subscription
     */

    private UUID subRefferalId;
}
