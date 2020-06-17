package com.abcfinancial.api.billing.generalledger.settlement.valueobject;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data

public class SettlementResponseVO
{
    /**
     * Settlement Id
     */
    @NotNull
    private UUID settlementId;
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
    /**
     * created date of settlement
     */
    @NotNull
    private LocalDateTime settlementDate;
}
