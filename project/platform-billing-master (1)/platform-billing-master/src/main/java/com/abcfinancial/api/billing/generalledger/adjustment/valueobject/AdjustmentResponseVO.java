package com.abcfinancial.api.billing.generalledger.adjustment.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.generalledger.adjustment.enums.AdjustmentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude( JsonInclude.Include.NON_NULL )
@Data
public class AdjustmentResponseVO
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
     * Account Id  of Payor Account / Client Account
     */
    private Account accountId;

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
