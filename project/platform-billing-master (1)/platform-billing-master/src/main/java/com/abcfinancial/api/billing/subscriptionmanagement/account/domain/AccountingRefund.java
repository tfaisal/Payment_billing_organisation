package com.abcfinancial.api.billing.subscriptionmanagement.account.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@EntityListeners( AuditingEntityListener.class )
@Table( name = "accounting_refund" )

public class AccountingRefund
{
    /**
     * refund id
     */

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Setter( AccessLevel.PROTECTED )
    @Column( name = "acref_id" )
    private UUID id;
    /**
     * Location id
     */

    @Column( name = "loc_id" )
    private UUID locationId;
    /**
     * Refund net price
     */

    @NotNull
    @Column( name = "acref_net_price" )
    private BigDecimal refNetPrice;
    /**
     * Refund Tax
     */

    @Column( name = "acref_tax" )
    private BigDecimal refTax;
    /**
     * Member Id
     */

    @Column( name = "m_id" )
    private UUID memberId;
    /**
     * Invoice id
     */

    @Column( name = "inv_id" )
    private UUID invoiceId;
    /**
     * Invoice Item Id
     */

    @Column( name = "invi_id" )
    private UUID invoiceItemId;
    /**
     * Account Id
     */

    @Column( name = "accn_id" )
    private UUID accountId;
    /**
     * Accounting Transaction Id
     */

    @Column( name = "trde_id" )
    private UUID accountingTransactionId;
    @JsonIgnore
    @Column( name = "acref_created", updatable = false, nullable = false )
    @CreationTimestamp
    private java.time.LocalDateTime created;
    @JsonIgnore
    @Column( name = "acref_modified" )
    @LastModifiedDate
    private java.time.LocalDateTime modified;
    @JsonIgnore
    @Column( name = "acref_deactivated" )
    private java.time.LocalDateTime deactivated;

}
