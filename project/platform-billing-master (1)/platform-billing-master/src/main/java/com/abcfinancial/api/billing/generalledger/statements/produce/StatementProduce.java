package com.abcfinancial.api.billing.generalledger.statements.produce;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data

public class StatementProduce
{
    private UUID statementId;
    private UUID locationId;
    private UUID accountId;
    private BigDecimal totalAmount;
    @JsonIgnore
    private LocalDateTime stmdDate;
    private UUID invoiceId;
    private BigDecimal payAmount;
}
