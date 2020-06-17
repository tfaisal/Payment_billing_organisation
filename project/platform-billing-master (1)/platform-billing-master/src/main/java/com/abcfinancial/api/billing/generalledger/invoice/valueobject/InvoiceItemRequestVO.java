package com.abcfinancial.api.billing.generalledger.invoice.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude( JsonInclude.Include.NON_NULL )
@Data
public class InvoiceItemRequestVO
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

    @NotNull
    private String itemName;
    /**
     * Item Price
     */

    @NotNull
    private BigDecimal price;
    /**
     * Invoice Item Verion
     */

    @NotNull
    private Long version;
    /**
     * Tax amount
     */

    @NotNull
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

    @NotNull
    private BigDecimal amountRemaining;
    /**
     * Item Type
     */

    @NotNull
    private ItemType type;
    /**
     * Quantity of Item
     */

    @NotNull
    private long quantity;
    /**
     * Item Id
     */

    @NotNull
    private UUID itemId;
    /**
     * Item category Id for Item
     */

    private UUID itemCategoryId;

    @JsonIgnore
    private String taxCode;
}
