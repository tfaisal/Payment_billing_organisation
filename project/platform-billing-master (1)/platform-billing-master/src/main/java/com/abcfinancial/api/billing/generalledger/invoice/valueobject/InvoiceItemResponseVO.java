package com.abcfinancial.api.billing.generalledger.invoice.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemExpirationStart;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude( JsonInclude.Include.NON_NULL )
@Data

public class InvoiceItemResponseVO
{
    /**
     * Invoice Item Id
     */

    private UUID id;
    /**
     * Location id of Invoice
     */

    private UUID locId;
    /**
     * Invoice Item Name
     */

    private String itemName;
    /**
     * Item Price
     */

    private BigDecimal price;
    /**
     * Invoice Item Verion
     */

    private Long version;
    /**
     * Tax amount
     */

    private BigDecimal taxAmount;
    /**
     * Discount code on Item
     */

    private String discountCode;
    /**
     * Discount amount on Item
     */

    private BigDecimal discountAmount;
    /**
     * Remaining amount for item
     */

    private BigDecimal amountRemaining;
    /**
     * Item Type
     */

    private ItemType type;
    /**
     * Quantity of Item
     */

    private long quantity;
    /**
     * Item Expiration Start
     */

    private ItemExpirationStart expirationStart;
    /**
     * Item Id
     */

    private UUID itemId;
    /**
     * Item category Id for Item
     */

    private UUID itemCategoryId;
}
