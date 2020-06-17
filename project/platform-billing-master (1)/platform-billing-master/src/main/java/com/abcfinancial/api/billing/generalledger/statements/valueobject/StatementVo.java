package com.abcfinancial.api.billing.generalledger.statements.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
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

public class StatementVo
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
     * Account object contains details for created statement
     */

    private Account accountId;
    /**
     * Invoice object contains invoice details
     */

    private Invoice invoiceId;
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
    /**
     * List of invoices
     */

    private List<Invoice> invoices;
}
