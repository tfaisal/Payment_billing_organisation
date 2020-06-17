package com.abcfinancial.api.billing.generalledger.adjustment.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.generalledger.adjustment.enums.AdjustmentType;
import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table( name = "account_adjustment" )
@EntityListeners( AuditingEntityListener.class )
public class Adjustment
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "adj_id" )
    private UUID adjustmentId;
    @Column( name = "loc_id" )
    private UUID locationId;
    @Column( name = "adj_created" )
    @CreationTimestamp
    private LocalDateTime created;
    @Column( name = "adj_deactivated" )
    private LocalDateTime deactivated;
    @Column( name = "adj_modified" )
    @LastModifiedDate
    private LocalDateTime modified;
    @Column( name = "adj_amount", nullable = false )
    private BigDecimal amount;
    @OneToOne( fetch = FetchType.EAGER )
    @JoinColumn( name = "accn_id", nullable = false )
    private Account accountId;
    @OneToOne( fetch = FetchType.EAGER )
    @JoinColumn( name = "inv_id" )
    private Invoice invoice;
    @Column( name = "m_id" )
    private UUID memberId;
    @Column( name = "emp_id" )
    private UUID empId;
    @Column( name = "as_id" )
    private UUID accountSummaryId;
    @Enumerated( EnumType.STRING )
    @Column( name = "adjustment_type" )
    private AdjustmentType adjustmentType;
    @Column( name = "adjustment_field" )
    private String adjustmentField;
    @Column( name = "fm_key" )
    private String feeMode;
}
