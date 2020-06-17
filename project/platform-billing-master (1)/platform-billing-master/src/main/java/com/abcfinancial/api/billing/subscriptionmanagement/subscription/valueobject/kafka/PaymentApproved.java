package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.kafka;

import com.abcfinancial.api.billing.generalledger.invoice.valueobject.InvoiceVO;
import com.abcfinancial.api.billing.generalledger.payment.domain.Brand;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data


public class PaymentApproved
{
    private UUID locationId;
    private UUID paymentId;
    private List<InvoiceVO> invoices;
    private Brand paymentType;
}
