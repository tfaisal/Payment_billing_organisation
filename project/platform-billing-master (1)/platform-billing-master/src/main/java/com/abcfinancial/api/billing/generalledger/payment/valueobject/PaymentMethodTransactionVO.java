package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import com.abcfinancial.api.billing.generalledger.transaction.valueobject.TransactionInvoiceVO;
import com.abcfinancial.api.billing.generalledger.transaction.valueobject.TransactionPaymentVO;
import com.abcfinancial.api.billing.generalledger.transaction.valueobject.TransactionSettlementVO;
import com.abcfinancial.api.billing.generalledger.transaction.valueobject.TransactionStatementVo;
import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.generalledger.statements.domain.Type;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentMethodTransactionVO
{
    /**
     * Payor payment method account summary id
     */
    @NotNull
    private UUID pmaId;

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
     * Adjustment id
     */
    private UUID adjustment;

    /**
     * Payor payment method id
     */
    @NotNull
    private UUID paymentMethodId;

    /**
     * Payor main account id
     */
    @NotNull
    private UUID accountId;

    /**
     * Payor payment method account summary date
     */
    private LocalDateTime summaryDate;

    private Type type;

    private TransactionType transactionType;
}
