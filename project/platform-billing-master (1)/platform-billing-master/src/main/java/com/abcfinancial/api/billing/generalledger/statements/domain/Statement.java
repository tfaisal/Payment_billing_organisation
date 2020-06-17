package com.abcfinancial.api.billing.generalledger.statements.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Table( name = "statement" )
@EntityListeners( AuditingEntityListener.class )

public class Statement
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "st_id" )
    private UUID statementId;
    @NotNull
    @Column( name = "loc_id" )
    private UUID locationId;
    @Column( name = "st_created" )
    @CreationTimestamp
    private LocalDateTime created;
    @Column( name = "st_deactivated" )
    private LocalDateTime deactivated;

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "st_modified" )
    @LastModifiedDate
    private java.time.LocalDateTime modified;
    @Column( name = "st_date", nullable = false )
    @CreationTimestamp
    private LocalDateTime stmtDate;
    @NotNull
    @Column( name = "st_amount" )
    private BigDecimal totalAmount;
    @ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.MERGE )
    @JoinColumn( name = "accn_id" )
    private Account accountId;
    @OneToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "pame_id" )
    private PaymentMethod paymentMethod;
}
