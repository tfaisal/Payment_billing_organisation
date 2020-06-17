package com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemVO
{
    /**
     * price of the item excluding tax.
     */

    @NotNull
    private BigDecimal price;
    /**
     * tax amount applicable on the item.
     */

    private BigDecimal taxAmount;
    /**
     * itemCategory applicable on the item.
     */

    private UUID itemCategoryId;
}
