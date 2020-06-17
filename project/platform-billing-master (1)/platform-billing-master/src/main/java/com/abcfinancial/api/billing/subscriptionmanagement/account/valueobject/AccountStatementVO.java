package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class AccountStatementVO {
    /**
     * statementId
     */

    @NotNull
    private UUID statementId;
    /**
     * locationId
     */

    @NotNull
    private UUID locationId;
    /**
     * accountId
     */

    private UUID accountId;
    /**
     *  statementAmount
     */

    @NotNull
    private BigDecimal statementAmount;
    /**
     * statementCreated
     */

    @NotNull
    private LocalDateTime statementCreated;
    /**
     * statementModified
     */

    private LocalDateTime statementModified;
    /**
     * statementDeactivated
     */

    private LocalDateTime statementDeactivated;
    /**
     * statementDate
     */

    @NotNull
    private LocalDateTime statementDate;

}
