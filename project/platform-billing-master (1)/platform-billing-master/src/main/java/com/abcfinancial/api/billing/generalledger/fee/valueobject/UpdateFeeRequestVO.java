package com.abcfinancial.api.billing.generalledger.fee.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data

@JsonInclude( JsonInclude.Include.NON_NULL )
public class UpdateFeeRequestVO
{
    /**
     * Must be one of [TRANSACTION]
     */

    private String feeMode;
    /**
     * Must be one of [ABC_TRANSACTION_FEE, PASS_THROUGH_FEE]
     */

    private String feeType;
    /**
     * Must be one of [EFT, MC, VISA, DISCOVER, AMEX]
     */

    private String feeTransactionType;
    /**
     * Must be one of [FLAT, PERCENTAGE]
     */

    private String feeValueType;
    /**
     * feeValue
     */

    private BigDecimal feeValue;

}
