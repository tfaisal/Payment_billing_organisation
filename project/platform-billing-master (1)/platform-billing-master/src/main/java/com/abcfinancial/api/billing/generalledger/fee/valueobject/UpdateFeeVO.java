package com.abcfinancial.api.billing.generalledger.fee.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data

@JsonInclude( JsonInclude.Include.NON_NULL )
public class UpdateFeeVO
{

    /**
     * Client's accountId
     */
    @NotNull
    private UUID accountId;
    /**
     * Must be one of [TRANSACTION]
     */
    @NotNull
    private String feeMode;
    /**
     * Must be one of [ABC_TRANSACTION_FEE, PASS_THROUGH_FEE]
     */
    @NotNull
    private String feeType;
    /**
     * Must be one of [EFT, MC, VISA, DISCOVER, AMEX]
     */
    @NotNull
    private String feeTransactionType;
    /**
     * Must be one of [FLAT, PERCENTAGE]
     */
    @NotNull
    private String feeValueType;
    /**
     * feeValue
     */
    @NotNull
    private BigDecimal feeValue;

    /**
     * Fee is active or deactive
     */
    @NotNull
    private boolean active;

}
