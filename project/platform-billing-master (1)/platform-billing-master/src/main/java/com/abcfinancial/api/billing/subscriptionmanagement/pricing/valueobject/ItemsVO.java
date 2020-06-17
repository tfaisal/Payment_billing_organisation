package com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data

public class ItemsVO
{
    /**
     * An unique id which behaves as the identification of a particular registered organization and its location.
     */

    @NotNull( message = "LocationID cannot be null" )
    UUID locationId;
    /**
     * list of the items price excluding taxes.
     */

    @NotNull
    private List<BigDecimal> items;
    private List<UUID> itemCategoryId;
}
