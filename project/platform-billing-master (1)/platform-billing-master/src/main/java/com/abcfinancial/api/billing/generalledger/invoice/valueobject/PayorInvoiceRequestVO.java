package com.abcfinancial.api.billing.generalledger.invoice.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class PayorInvoiceRequestVO
{
    /**
     * Location Id
     */

    @NotNull
    private UUID locationId;
    /**
     * Member Id
     */

    private UUID memberId;
    /**
     * Account details
     */

    @NotNull
    private UUID accountId;
    /**
     * Sales employee Id
     */

    private UUID salesEmployeeId;
    /**
     * Total tax
     */

    @NotNull
    private BigDecimal totalTax;
    /**
     * Total amount
     */

    @NotNull
    private BigDecimal totalAmount;
    /**
     * Total discount amount
     */

    @NotNull
    private BigDecimal totalDiscountAmount;
    /**
     * Total net price
     */

    @NotNull
    private BigDecimal totalNetPrice;
    /**
     * An unique id which behaves as the identification for subscription .
     */

    private UUID subscriptionId;
    /**
     * List of Items
     */

    @JsonProperty( "items" )
    private List<InvoiceItemRequestVO> items;
    /**
     * Transaction Id for invoice
     */

    private String avaTransactionId;
    /**
     * Invoice Date of Invoice
     */

    private LocalDateTime invoiceDate;
}
