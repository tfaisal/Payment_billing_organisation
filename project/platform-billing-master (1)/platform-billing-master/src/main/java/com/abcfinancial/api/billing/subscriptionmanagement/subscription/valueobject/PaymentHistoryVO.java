package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class PaymentHistoryVO
{
    private UUID id;
    private LocalDateTime paymhCreated;
    private LocalDateTime paymhModified;
    private LocalDateTime paymhDeactivated;
    private UUID payId;
    private String payStatus;
    private String paySettlementStatus;
    private BigDecimal payAmount;
    private PaymentVO paymentVO;
}
