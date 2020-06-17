package com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@EntityListeners( AuditingEntityListener.class )
@Table( name = "agreement_subscription" )
@SQLDelete( sql = "UPDATE agreement_subscription set agrmsu_deactivated = current_timestamp where m_id  = ? and sub_id  = ?" )
@Where( clause = "agrmsu_deactivated IS NULL" )
@NoArgsConstructor

public class AgreementSubscription
{
    /**
     * Agreement Subscription Id
     */

    @EmbeddedId
    private AgreementSubscriptionId agrmSuId;
    /**
     * Is Primary
     */

    @Column( name = "agrm_sum_is_primary" )
    private boolean primary;
    /**
     * Subscription object contains Subscription details
     */

    @ManyToOne( cascade = CascadeType.ALL )
    @JoinColumn( name = "sub_id", insertable = false, updatable = false )
    @LazyCollection( LazyCollectionOption.FALSE )
    private Subscription subId;
    /**
     * Date and time of Member subscription created
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "agrmsu_created", updatable = false, nullable = false )
    @CreationTimestamp
    private java.time.LocalDateTime created;
    /**
     * Date and time of Member subscription deactivated
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "agrmsu_deactivated" )
    private java.time.LocalDateTime deactivated;
}
