package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemExpirationStart;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_DEFAULT )

public class SubscriptionItemVO
{
    /**
     * Subscription Item id
     */

    private UUID id;
    /**
     * Location id
     */

    private UUID locId;
    /**
     * Subscription item id
     */

    @NotNull
    private UUID itemId;
    /**
     * Subscription Item version
     */

    @NotNull
    private long version;
    /**
     * Item Name
     */

    @NotNull
    private String itemName;
    /**
     * Item price.
     */

    @NotNull
    private BigDecimal price;
    /**
     * Subscription item quantity
     */

    @NotNull
    private long quantity;
    /**
     * Subscription item type
     */

    @NotNull
    private ItemType type;
    /**
     * When unlimited is true, quantity will be one.
     */

    @NotNull
    private boolean unlimited;
    /**
     * Subscription item Expiration start.
     */

    private ItemExpirationStart expirationStart;
    /**
     * Category Id for Item
     */

    private UUID itemCategoryId;
}
