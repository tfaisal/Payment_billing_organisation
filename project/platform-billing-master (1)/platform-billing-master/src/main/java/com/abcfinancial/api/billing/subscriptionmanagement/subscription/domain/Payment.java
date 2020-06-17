package com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.InvoiceTypeEnum;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PaySettlementStatus;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PayStatus;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.abcfinancial.api.billing.generalledger.payment.domain.Brand;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@EntityListeners( AuditingEntityListener.class )
@Table( name = "payment" )

public class Payment
{
    /**
     * Payment Id
     */

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Setter( AccessLevel.PROTECTED )
    @Column( name = "pay_id" )
    private UUID id;
    /**
     * Location id of Payment
     */

    @Column( name = "loc_id" )
    private UUID locationId;
    /**
     * Payment receive date
     */

    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME )
    @JsonFormat( pattern = "yyyy-MM-dd'T'HH:mm:ss" )
    @Column( name = "pay_received_date" )
    private LocalDateTime payReceivedDate;
    /**
     * Amount of payment
     */

    @Column( name = "pay_amount" )
    private BigDecimal payAmount;
    /**
     * Status of payment
     */

    @Enumerated( EnumType.STRING )
    @Column( name = "pay_status" )
    private PayStatus payStatus;
    /**
     * Settlement status of payment
     */

    @Enumerated( EnumType.STRING )
    @Column( name = "pay_settlement_status" )
    private PaySettlementStatus paySettlementStatus;
    /**
     * Payment method Id
     */

    @Column( name = "pame_id" )
    private UUID pameId;
    /**
     * Processor Id of Payment
     */

    @Column( name = "pay_processor_id" )
    private String payProcessorId;
    /**
     * Account object contains details of account for payment
     */

    @OneToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "accn_id", nullable = false )
    private Account account;
    /**
     * List of Invoice object contains details of Invoice
     */

    @ManyToMany( cascade = CascadeType.ALL )
    @LazyCollection( LazyCollectionOption.FALSE )
    @JoinTable(
        name = "invoice_payment",
        joinColumns = { @JoinColumn( name = "pay_id" ) },
        inverseJoinColumns = { @JoinColumn( name = "inv_id" ) }
    )
    private List<Invoice> invoices;
    /**
     * Created Date and Time of Payment
     */

    @JsonIgnore
    @Column( name = "pay_created", updatable = false, nullable = false )
    @CreationTimestamp
    private java.time.LocalDateTime created;
    /**
     * Modified Date and Time of Payment
     */

    @JsonIgnore
    @Column( name = "pay_modified" )
    @LastModifiedDate
    private java.time.LocalDateTime modified;
    /**
     * Deactivated Date and Time of Payment
     */

    @JsonIgnore
    @Column( name = "pay_deactivated" )
    private java.time.LocalDateTime deactivated;
    /**
     * Payment Refund Id
     */

    @Column( name = "pay_id_refund" )
    private UUID paymentIdRefund;
    /**
     * Payment Type
     */

    @Enumerated( EnumType.STRING )
    @Column( name = "pay_type" )
    private Brand paymentType;
    // JIRA-3015 start
    @Enumerated( EnumType.STRING )
    @Column( name = "pay_invoice_type" )
    private InvoiceTypeEnum invoiceType;
    @Column( name = "pay_invoice_no" )
    private String invoiceNumber;
    @Column( name = "pay_transfer_date" )
    private java.time.LocalDateTime transferDate;
    //JIRA REL1-4054
    @Column( name = "inv_id" )
    private UUID invoiceId;
    @Column( name = "st_id" )
    private UUID statementId;

    /**
     * Settlement Id
     */
    @Column( name = "stlm_id" )
    private UUID settlementId;
}
