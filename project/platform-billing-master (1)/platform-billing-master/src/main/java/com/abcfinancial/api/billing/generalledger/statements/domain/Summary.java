package com.abcfinancial.api.billing.generalledger.statements.domain;

import com.abcfinancial.api.billing.generalledger.adjustment.domain.Adjustment;
import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.abcfinancial.api.billing.generalledger.settlement.domain.Settlement;
import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Payment;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table( name = "account_summary" )

public class Summary
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "as_id" )
    private UUID id;
    @OneToOne( fetch = FetchType.EAGER )
    @JoinColumn( name = "pay_id" )
    private Payment payment;
    @OneToOne( fetch = FetchType.EAGER )
    @JoinColumn( name = "inv_id" )
    private Invoice invoice;
    @OneToOne( fetch = FetchType.EAGER )
    @JoinColumn( name = "st_id" )
    private Statement statement;
    @OneToOne( fetch = FetchType.EAGER )
    @JoinColumn( name = "stlm_id" )
    private Settlement settlement;
    @OneToOne( fetch = FetchType.EAGER )
    @JoinColumn( name = "adj_id" )
    private Adjustment adjustment;
    @Column( name = "accn_id" )
    private UUID accountId;
    @Column( name = "acsu_created" )
    @CreationTimestamp
    private LocalDateTime created;
    @Column( name = "acsu_deactivated" )
    private LocalDateTime deactivated;
    @Column( name = "acsu_modified" )
    @LastModifiedDate
    private LocalDateTime modified;
    @Column( name = "acsu_date" )
    private LocalDateTime summaryDate;
    @Enumerated( EnumType.STRING )
    @Column( name = "acsu_type" )
    private Type type;
    @Column( name = "loc_id" )
    private UUID locationId;
    @Enumerated( EnumType.STRING )
    @Column( name = "acsu_trans_type" )
    private TransactionType transactionType;
}
