package com.abcfinancial.api.billing.generalledger.invoice.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class InvoiceWithoutSubscriptionVO
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
     * Account Id
     */

    private UUID accountId;
    /**
     * List of Items
     */

    @JsonProperty( "items" )
    private List<InvoiceItemVO> items;
    /**
     * Account object contains details of account
     */

    private Account account;
    /**
     * Date of invoice generation
     */

    private LocalDateTime invoiceDate;
}
