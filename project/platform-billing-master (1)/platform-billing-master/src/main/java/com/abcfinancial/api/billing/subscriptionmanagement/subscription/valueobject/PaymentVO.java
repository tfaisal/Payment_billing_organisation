package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.abcfinancial.api.billing.generalledger.invoice.valueobject.InvoiceVO;
import com.abcfinancial.api.billing.generalledger.payment.domain.Brand;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PaySettlementStatus;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PayStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

@NoArgsConstructor
@AllArgsConstructor

public class PaymentVO
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

    @JsonFormat( pattern = "yyyy-MM-dd'T'HH:mm:ss" )
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME )
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
    /**
     * Payment account
     */

    private AccountVO account;
    /**
     * Invoice details
     */

    @JsonProperty( "invoices" )
    private List<InvoiceVO> invoices;
    private Brand paymentType;
    /**
     * Payment source name
     */

    private String paymentSource;
}
