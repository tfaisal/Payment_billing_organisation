package com.abcfinancial.api.billing.generalledger.settlement.valueobject;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data

public class EvaluateSettlementResponseVO
{
    /**
     * Location Id for created settlement
     */
    @NotNull
    private UUID locationId;
    /**
     * Client's account id for created settlement
     */
    @NotNull
    private UUID accountId;
    /**
     * amount of settlement
     */
    @NotNull
    private BigDecimal amount;
}
