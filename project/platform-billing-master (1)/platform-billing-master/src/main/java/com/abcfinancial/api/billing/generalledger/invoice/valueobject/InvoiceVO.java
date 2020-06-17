package com.abcfinancial.api.billing.generalledger.invoice.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.InvoiceTypeEnum;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.SubscriptionVO;
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

public class InvoiceVO
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
     * Details of subscription
     */

    @NotNull
    private SubscriptionVO subscription;
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
     * Account details
     */

    private Account account;
    /**
     * Date of invoice generation
     */

    private LocalDateTime invoiceDate;
    // JIRA 3015 start
    private String invoiceNumber;
    private InvoiceTypeEnum invoiceType;
    private java.time.LocalDateTime transferDate;
    // End
}
