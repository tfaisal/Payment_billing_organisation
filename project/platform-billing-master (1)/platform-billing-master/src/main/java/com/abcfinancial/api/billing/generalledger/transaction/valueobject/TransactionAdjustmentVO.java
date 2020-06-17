package com.abcfinancial.api.billing.generalledger.transaction.valueobject;

import com.abcfinancial.api.billing.generalledger.adjustment.enums.AdjustmentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude( JsonInclude.Include.NON_NULL )
@Data
public class TransactionAdjustmentVO
{
    /**
     * Adjustment Id
     */
    private UUID adjustmentId;
    /**
     * Location id of Adjustment
     */
    private UUID locationId;
    /**
     * Amount to be Adjusted
     */
    private BigDecimal amount;
    /**
     * Adjustment Type -
     * LATE_FEE,
     * SERVICE_FEE,
     * CREDIT,
     * WAIVE_FEE,
     * ERROR_ADJUST
     */
    private AdjustmentType adjustmentType;
    /**
     * Adjustment Description - e.g ., deduction or credit etc .
     */
    private String adjustmentField;
}
