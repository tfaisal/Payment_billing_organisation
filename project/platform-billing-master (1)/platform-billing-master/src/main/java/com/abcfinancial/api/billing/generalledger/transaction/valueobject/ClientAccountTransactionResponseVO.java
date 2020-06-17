package com.abcfinancial.api.billing.generalledger.transaction.valueobject;

import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.generalledger.statements.domain.Type;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data

public class ClientAccountTransactionResponseVO
{
    /**
     * Client account summary id
     */
    private UUID id;

    /**
     * Adjustment id
     */
    private TransactionAdjustmentVO adjustment;

    /**
     * Payment id
     */
    private TransactionPaymentVO payment;

    /**
     * Invoice id
     */
    private TransactionInvoiceVO invoice;

    /**
     * Statement id
     */
    private TransactionStatementVo statement;

    /**
     * Settlement id
     */
    private TransactionSettlementVO settlement;

    /**
     * Client account id
     */
    private UUID accountId;

    /**
     * Client account summary date
     */
    private LocalDateTime summaryDate;

    private Type type;

    /**
     * Client Location id
     */
    private UUID locationId;

    private TransactionType transactionType;
}
