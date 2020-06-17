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
@Table( name = "payment_method_account" )

public class PaymentMethodAccount
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "pma_id" )
    private UUID pmaId;
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
    @Column( name = "pame_id" )
    private UUID paymentMethodId;
    @Column( name = "accn_id" )
    private UUID accountId;
    @Column( name = "pma_created" )
    @CreationTimestamp
    private LocalDateTime created;
    @Column( name = "pma_deactivated" )
    private LocalDateTime deactivated;
    @Column( name = "pma_modified" )
    @LastModifiedDate
    private LocalDateTime modified;
    @Column( name = "pma_date" )
    private LocalDateTime summaryDate;
    @Enumerated( EnumType.STRING )
    @Column( name = "pma_type" )
    private Type type;
    @Enumerated( EnumType.STRING )
    @Column( name = "pma_trans_type" )
    private TransactionType transactionType;
}
