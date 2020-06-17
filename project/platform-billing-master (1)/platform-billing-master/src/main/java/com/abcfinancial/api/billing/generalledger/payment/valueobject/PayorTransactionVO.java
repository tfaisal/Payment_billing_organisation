package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude( JsonInclude.Include.NON_NULL )
public class PayorTransactionVO
{
    private List<PayorMainAccountTransactionVO> mainAccountTransactions;
    private List<PaymentMethodTransactionVO> paymentMethodTransactions;
}
