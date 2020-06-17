package com.abcfinancial.api.billing.generalledger.transaction.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class TransactionStatementVo
{
    /**
     * Statement Id
     */
    private UUID statementId;
    /**
     * Location Id for created statement
     */
    private UUID locationId;
    /**
     * Total amount of statement
     */
    private BigDecimal totalAmount;
    /**
     * created date of statement
     */
    private LocalDateTime statementDate;
    /**
     * Total pay amount
     */
    private BigDecimal payAmount;
}
