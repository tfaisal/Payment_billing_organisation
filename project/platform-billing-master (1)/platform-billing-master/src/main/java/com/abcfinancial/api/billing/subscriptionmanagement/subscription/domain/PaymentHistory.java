package com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PaySettlementStatus;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PayStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table( name = "payment_history" )
@EntityListeners( AuditingEntityListener.class )

public class PaymentHistory
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Setter( AccessLevel.PROTECTED )
    @Column( name = "pamh_id" )
    private UUID id;
    @Column( name = "pamh_created" )
    @CreationTimestamp
    @NonNull
    private LocalDateTime paymhCreated;
    @LastModifiedDate
    @Column( name = "pamh_modified" )
    private LocalDateTime paymhModified;
    @Column( name = "pamh_deactivated" )
    private LocalDateTime paymhDeactivated;
    @NotNull
    @Enumerated( EnumType.STRING )
    @Column( name = "pamh_status" )
    private PayStatus paymhStatus;

    @Column( name = "pamh_settlement_status" )
    @Enumerated( EnumType.STRING )
    private PaySettlementStatus paymhSettlementStatus;
    @NotNull
    @Column( name = "pamh_amount" )
    private BigDecimal paymhAmount;
    @ManyToOne( cascade = CascadeType.ALL )
    @JoinColumn( name = "pay_id" )
    @JsonProperty( "payment" )
    private Payment payment;

}
