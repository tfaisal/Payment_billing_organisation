package com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Entity
@EntityListeners( AuditingEntityListener.class )
@Table( name = "subscription_member" )
@SQLDelete( sql = "UPDATE subscription_member set mesu_deactivated = current_timestamp where m_id =? and sub_id =?" )
@Where( clause = "mesu_deactivated IS NULL" )
@NoArgsConstructor
public class MemberSubscription
{
    /**
     * Member Subscription Id
     */

    @EmbeddedId
    private MemberSubscriptionId id;
    /**
     * Location Id
     */

    @Column( name = "loc_id" )
    private UUID locId;
    /**
     * Subscription object contains Subscription details
     */

    @ManyToOne
    @JoinColumn( name = "sub_id", insertable = false, updatable = false )
    @LazyCollection( LazyCollectionOption.FALSE )
    private Subscription subscription;
    /**
     * Date and time of Member subscription created
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "mesu_created", updatable = false, nullable = false )
    @CreationTimestamp
    private java.time.LocalDateTime created;
    /**
     * Date and time of Member subscription deactivated
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "mesu_deactivated" )
    private java.time.LocalDateTime deactivated;
}
