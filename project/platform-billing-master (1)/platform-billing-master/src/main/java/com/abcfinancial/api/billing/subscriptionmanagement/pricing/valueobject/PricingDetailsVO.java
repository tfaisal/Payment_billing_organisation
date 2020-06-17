package com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor

public class PricingDetailsVO
{
    /**
     * list of the items with price and tax amount.
     */

    @NotNull
    private List<ItemVO> items;
    /**
     * total calculated tax amount on items .
     */

    @NotNull
    private BigDecimal totalTax;
    /**
     * total selling price of items ie tax included.
     */

    @NotNull
    private BigDecimal totalAmount;
    /**
     * total net price of items ie tax excluded.
     */

    @NotNull
    private BigDecimal totalNetAmount;
}
