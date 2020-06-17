package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Payment;
import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )
@NoArgsConstructor
@AllArgsConstructor

public class PaymentDeclinedVO
{
    private UUID memberId;
    private UUID locationId;
    private UUID subscriptionId;
    private UUID accountId;
    private Payment payment;
    private List<Invoice> invoiceList;
}
