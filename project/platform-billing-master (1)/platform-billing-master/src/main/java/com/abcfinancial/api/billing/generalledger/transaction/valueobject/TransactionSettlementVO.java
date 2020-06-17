package com.abcfinancial.api.billing.generalledger.transaction.valueobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data

public class TransactionSettlementVO
{
    /**
     * Settlement Id
     */
    private UUID settlementId;
    /**
     * Location Id for created settlement
     */
    private UUID locationId;
    /**
     * amount of settlement
     */
    private BigDecimal amount;
    /**
     * created date of settlement
     */
    private LocalDateTime settlementDate;
}
