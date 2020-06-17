package com.abcfinancial.api.billing.generalledger.transaction.helper;

import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodTransactionVO;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.PayorMainAccountTransactionVO;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
public class TransactionPage
{
    /**
     * This show main account transactions.
     */
    private Page<PayorMainAccountTransactionVO> mainAccountTransactions;
    /**
     * This show payment method account transactions.
     */
    private Page<PaymentMethodTransactionVO> paymentMethodTransactions;
}
