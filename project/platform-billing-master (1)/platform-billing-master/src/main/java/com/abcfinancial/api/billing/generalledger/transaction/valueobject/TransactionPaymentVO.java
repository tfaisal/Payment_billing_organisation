package com.abcfinancial.api.billing.generalledger.transaction.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PaySettlementStatus;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PayStatus;
import com.abcfinancial.api.billing.generalledger.payment.domain.Brand;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )
public class TransactionPaymentVO
{
    /**
     * Payment Id
     */
    private UUID id;
    /**
     * Payment Location id
     */
    private UUID locationId;
    /**
     * Payment received Date.format should be MM-dd-yyyy
     */
    private LocalDateTime payReceivedDate;
    /**
     * Payment amount
     */
    private BigDecimal payAmount;
    /**
     * Payment status
     */
    private PayStatus payStatus;
    /**
     * Payment settlement status
     */
    private PaySettlementStatus paySettlementStatus;
    /**
     * Payment method id
     */
    private UUID pameId;
    /**
     * Payment processor id
     */
    private String payProcessorId;

    private Brand paymentType;
    /**
     * Payment source name
     */
    private String paymentSource;
}
