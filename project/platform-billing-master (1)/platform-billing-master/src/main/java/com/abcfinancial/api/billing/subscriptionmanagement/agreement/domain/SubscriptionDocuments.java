package com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Data
@Entity
@EntityListeners( AuditingEntityListener.class )
@Table( name = "subscription_document" )
@SQLDelete( sql = "UPDATE subscription_document set docsu_deactivated = current_timestamp where doc_id  = ? and sub_id  = ?" )
@Where( clause = "docsu_deactivated IS NULL" )
@NoArgsConstructor

public class SubscriptionDocuments
{
    /**
     * Subscription Document Id
     */

    @EmbeddedId
    private SubscriptionDocumentId id;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "sub_id", insertable = false, updatable = false )
    private Subscription subId;
    /**
     * Date and time of Member subscription created
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "docsu_created", updatable = false, nullable = false )
    @CreationTimestamp
    private java.time.LocalDateTime created;
    /**
     * Date and time of Member subscription deactivated
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "docsu_deactivated" )
    private java.time.LocalDateTime deactivated;
}
