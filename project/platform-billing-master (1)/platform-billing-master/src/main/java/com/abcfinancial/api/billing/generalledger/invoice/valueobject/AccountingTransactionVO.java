package com.abcfinancial.api.billing.generalledger.invoice.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Status;
import com.abcfinancial.api.billing.generalledger.payment.domain.Brand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor

public class AccountingTransactionVO
{

    @NotNull
    private UUID id;
    @NotNull
    private Date transactionDate;
    @NotNull
    private UUID locationId;
    private Brand paymentType;
    @NotNull
    private BigDecimal totalPrice;
    @NotNull
    private long quantity;
    private LocalDateTime expirationStartDate;
    private LocalDateTime expirationEndDate;
    private String promotionCode;
    @NotNull
    private BigDecimal totalDiscountAmount;
    @NotNull
    private BigDecimal totalNetPrice;
    @NotNull
    private BigDecimal totalTax;
    @NotNull
    private UUID invoiceId;
    @NotNull
    private UUID invoiceItemId;
    @NotNull
    private UUID itemId;
    @NotNull
    private long version;
    @NotNull
    private UUID memberId;
    @NotNull
    private UUID accountId;
    @NotNull
    private UUID employeeId;
    @NotNull
    private Status status;
    private String paymentDestination;
    private UUID paymentId;
}
