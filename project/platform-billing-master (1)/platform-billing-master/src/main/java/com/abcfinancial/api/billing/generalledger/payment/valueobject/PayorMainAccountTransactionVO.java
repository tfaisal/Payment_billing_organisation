package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.generalledger.statements.domain.Type;
import com.abcfinancial.api.billing.generalledger.transaction.valueobject.*;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PayorMainAccountTransactionVO
{
    /**
     * Payor account summary id
     */
    @NotNull
    private UUID id;

    /**
     * Adjustment id
     */
    private TransactionAdjustmentVO adjustment;

    /**
     * If transactionType is PAYMENT, it should have data.
     */
    private TransactionPaymentVO payment;

    /**
     * If transactionType is INVOICE, it should have data.
     */
    private TransactionInvoiceVO invoice;

    /**
     * If transactionType is STATEMENT, it should have data.
     */
    private TransactionStatementVo statement;

    /**
     * If transactionType is SETTLEMENT, it should have data.
     */
    private TransactionSettlementVO settlement;

    /**
     * Payor main account id
     */
    @NotNull
    private UUID accountId;

    /**
     * Payor main account summary date
     */
    private LocalDateTime summaryDate;

    private Type type;

    /**
     * Client Location id
     */
    private UUID locationId;

    private TransactionType transactionType;
}
