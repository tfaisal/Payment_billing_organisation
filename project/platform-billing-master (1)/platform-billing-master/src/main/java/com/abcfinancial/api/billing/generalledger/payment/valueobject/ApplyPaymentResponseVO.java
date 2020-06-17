package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PayStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class ApplyPaymentResponseVO
{
    /**
     * Payment Status.
     */

    private PayStatus payStatus;
    /**
     *  Message
     */

    private String message;
}
