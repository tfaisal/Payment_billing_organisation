package com.abcfinancial.api.billing.generalledger.statements.valueobject;

import lombok.Data;

import java.util.List;

@Data

public class StagingPaymentArrayVO
{
    List<PaymentRequestVO> stagingPaymentVOS;
}
