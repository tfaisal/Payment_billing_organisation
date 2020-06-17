package com.abcfinancial.api.billing.generalledger.statements.valueobject;

import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude( JsonInclude.Include.NON_NULL )

public class GetStatementVo {
    /**
     * It is statement Id.
     */

    private UUID statementId;
    /**
     * location Id for the Statement.
     */

    private UUID locationId;
    /**
     * Account Id for the statement.
     */

    private UUID accountId;
    /**
     * invoice Id for the statement.
     */

    private Invoice invoiceId;
    /**
     * Total amount of the statement.
     */

    private BigDecimal totalAmount;
    /**
     * Date and Time for the statement.
     */

    private LocalDateTime statementDate;
    /**
     * Pay Amount of the statement.
     */

    private BigDecimal payAmount;
    /**
     * List of invoices for the Statement.
     */

    private List<Invoice> invoices;
}
