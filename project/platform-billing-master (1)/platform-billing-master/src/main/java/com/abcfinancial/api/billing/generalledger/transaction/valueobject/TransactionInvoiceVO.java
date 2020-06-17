package com.abcfinancial.api.billing.generalledger.transaction.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.InvoiceTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class TransactionInvoiceVO
{
    /**
     * Invoice id
     */

    private UUID id;
    /**
     * Location Id
     */

    private UUID locationId;
    /**
     * Total net price
     */

    private BigDecimal totalNetPrice;
    /**
     * Total discount amount
     */

    private BigDecimal totalDiscountAmount;
    /**
     * Total tax
     */

    private BigDecimal totalTax;
    /**
     * Total amount
     */

    private BigDecimal totalAmount;
    /**
     * Sales employee Id
     */

    private UUID salesEmployeeId;
    /**
     * Member Id
     */

    private UUID memberId;

    /**
     * Date of invoice generation
     */

    private LocalDateTime invoiceDate;
    private String invoiceNumber;
    private InvoiceTypeEnum invoiceType;
    private LocalDateTime transferDate;
}
