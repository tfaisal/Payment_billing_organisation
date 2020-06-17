package com.abcfinancial.api.billing.subscriptionmanagement.account.domain;

import com.abcfinancial.api.billing.generalledger.payment.domain.Brand;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table( name = "accounting_transaction_detail" )
@Entity

@SQLDelete( sql = "UPDATE accounting_transaction_detail set trde_deactivated = current_timestamp where trde_id  = ?" )
@Where( clause = "trde_deactivated IS NULL" )
@EntityListeners( AuditingEntityListener.class )

public class AccountingTransaction
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "trde_id" )
    private UUID id;
    @Column( name = "trde_transaction_date" )
    private Date transactionDate;
    @Column( name = "loc_id" )
    private UUID locationId;
    @Column( name = "trde_created" )
    @CreatedDate
    private LocalDateTime created;
    @Basic( optional = true )
    @LastModifiedDate
    @Column( name = "trde_modified" )
    private LocalDateTime modified;
    @Basic( optional = true )
    @Column( name = "trde_deactivated" )
    private LocalDateTime deactivated;
    @Column( name = "trde_total_price" )
    private BigDecimal totalPrice;
    @Enumerated( EnumType.STRING )
    @Column( name = "trde_payment_type" )
    private Brand paymentType;
    @Column( name = "trde_quantity" )
    private long quantity;
    @Basic( optional = true )
    @Column( name = "trde_expiration_start_date" )
    private LocalDateTime expirationStartDate;
    @Basic( optional = true )
    @Column( name = "trde_expiration_end_date" )
    private LocalDateTime expirationEndDate;
    @Column( name = "trde_promotion_code" )
    private String promotionCode;
    @Column( name = "trde_discount_amount" )
    private BigDecimal totalDiscountAmount;
    @Column( name = "trde_net_price" )
    private BigDecimal totalNetPrice;
    @Column( name = "trde_tax" )
    private BigDecimal totalTax;
    @Column( name = "trde_invoice_id" )
    private UUID invoiceId;
    @Column( name = "trde_invoice_item_id" )
    private UUID invoiceItemId;
    @Column( name = "trde_item_id" )
    @NotNull
    private UUID itemId;
    @Column( name = "trde_item_version" )
    private long itemVersion;
    @Column( name = "m_id" )
    private UUID memberId;
    @Column( name = "accn_id" )
    private UUID accountId;
    @Column( name = "emp_id" )
    private UUID employeeId;
    @Enumerated( EnumType.STRING )
    @Column( name = "trde_status" )
    private Status status;
    @Column( name = "trde_payment_id" )
    private UUID paymentId;
    @Column( name = "trde_payment_destination" )
    private String paymentDestination;
}
