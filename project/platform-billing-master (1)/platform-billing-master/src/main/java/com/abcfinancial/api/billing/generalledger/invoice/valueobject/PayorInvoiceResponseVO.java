package com.abcfinancial.api.billing.generalledger.invoice.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.InvoiceTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class PayorInvoiceResponseVO
{
    /**
     * Invoice Id
     */

    private UUID id;
    /**
     * Location Id for created Invoice
     */

    private UUID locationId;
    /**
     * Total net price of invoice
     */

    private BigDecimal totalNetPrice;
    /**
     * Total discount amount
     */

    private BigDecimal totalDiscountAmount;
    /**
     * Total tax of invoice
     */

    private BigDecimal totalTax;
    /**
     * Total amount of invoice
     */

    private BigDecimal totalAmount;
    /**
     * Sales employee Id
     */

    private UUID salesEmployeeId;
    /**
     * Subscription Id
     */

    private UUID subscriptionId;
    /**
     * List of Invoice Items
     */

    private List<InvoiceItemResponseVO> items;
    /**
     * Invoice Date
     */

    private java.time.LocalDateTime invoiceDate;
 
    private InvoiceTypeEnum invoiceType;
    /**
     * Invoice Number
     */

    private String invoiceNumber;
    /**
     * Invoice tranferDate
     */

    private java.time.LocalDateTime transferDate;

}
