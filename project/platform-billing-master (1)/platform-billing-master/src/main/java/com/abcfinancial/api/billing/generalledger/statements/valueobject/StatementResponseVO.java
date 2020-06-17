package com.abcfinancial.api.billing.generalledger.statements.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )
public class StatementResponseVO
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
     * Payor's account id for created statement
     */

    private UUID accountId;
    /**
     * Total amount of statement
     */

    private BigDecimal totalAmount;
    /**
     * created date of statement
     */

    private LocalDateTime statementDate;

    @JsonIgnore
    private PaymentRequestVO paymentRequestVO;
}
