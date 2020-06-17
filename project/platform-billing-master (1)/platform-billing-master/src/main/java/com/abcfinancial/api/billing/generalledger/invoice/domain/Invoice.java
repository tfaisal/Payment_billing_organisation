package com.abcfinancial.api.billing.generalledger.invoice.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.InvoiceTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@EntityListeners( AuditingEntityListener.class )
@Table( name = "invoice" )
@ToString
@Setter
@Getter

public class Invoice
{
    /**
     * Invoice Id
     */

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "inv_id" )
    private UUID id;
    /**
     * Location Id for created Invoice
     */

    @Column( name = "loc_id" )
    private UUID locationId;
    /**
     * Total net price of invoice
     */

    @Column( name = "total_net_price" )
    private BigDecimal totalNetPrice;
    /**
     * Total discount amount
     */

    @Column( name = "total_discount_amount" )
    private BigDecimal totalDiscountAmount;
    /**
     * Total tax of invoice
     */

    @Column( name = "total_tax" )
    private BigDecimal totalTax;
    /**
     * Total amount of invoice
     */

    @Column( name = "total_price" )
    private BigDecimal totalAmount;
    /**
     * Sales employee Id
     */

    @Column( name = "emp_id" )
    private UUID salesEmployeeId;
    /**
     * Member Id
     */

    @Column( name = "m_id" )
    private UUID memberId;
    /**
     * Account Id of Invoice
     */

    @Column( name = "accn_id" )
    @NotNull
    private UUID accountId;
    /**
     * LIst of Invoice Items
     */

    @OneToMany( cascade = CascadeType.ALL )
    @JoinColumn( name = "inv_id", nullable = false )
    @LazyCollection( LazyCollectionOption.FALSE )
    private List<InvoiceItem> items;

    /**
     * Subscription Object contains details of Subscription
     */

    @ManyToOne( cascade = CascadeType.MERGE )
    @JoinColumn( name = "sub_id" )
    @JsonProperty( "subscription" )
    private Subscription subscription;
    /**
     * Created Date and Time of Invoice
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "inv_created", updatable = false, nullable = false )
    @CreationTimestamp
    private java.time.LocalDateTime created;
    /**
     * Date and Time of Invoice
     */

    //Changed as per P3-862
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "inv_date", updatable = false, nullable = false )
    private java.time.LocalDateTime invoiceDate;
    /**
     * Modified Date and Time of Invoice
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "inv_modified" )
    @LastModifiedDate
    private java.time.LocalDateTime modified;
    /**
     * Deactivated Date and Time for Invoice
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "inv_deactivated" )
    private java.time.LocalDateTime deactivated;
    // JIRA-3015 start
    @Enumerated( EnumType.STRING )
    @Column( name = "inv_invoice_type" )
    private InvoiceTypeEnum invoiceType;
    @Column( name = "inv_invoice_no" )
    private String invoiceNumber;
    @Column( name = "inv_transfer_date" )
    private java.time.LocalDateTime transferDate;
    @Column( name = "ava_txn_id" )
    private String transactionId;
    @Column( name = "pame_id" )
    private UUID paymentMethodId;
    // End
}
