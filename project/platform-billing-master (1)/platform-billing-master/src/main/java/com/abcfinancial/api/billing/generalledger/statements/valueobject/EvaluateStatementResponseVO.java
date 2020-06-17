package com.abcfinancial.api.billing.generalledger.statements.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.statements.domain.Balance;
import com.abcfinancial.api.billing.generalledger.statements.domain.PaymentMethodAccount;
import com.abcfinancial.api.billing.generalledger.statements.domain.Statement;
import com.abcfinancial.api.billing.generalledger.statements.domain.Summary;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data

public class EvaluateStatementResponseVO
{
    /**
     * Evaluated statement amount.
     */

    private BigDecimal statementAmount;

    @JsonIgnore
    private Balance balance;
    @JsonIgnore
    private Statement statement;
    @JsonIgnore
    private PaymentMethod paymentMethod;
    @JsonIgnore
    private Account account;
    @JsonIgnore
    private BigDecimal netAmountDue;
    @JsonIgnore
    private List<PaymentMethodAccount> paymentMethodAccountSummaries;
    @JsonIgnore
    private List<Summary> summaries;
}
