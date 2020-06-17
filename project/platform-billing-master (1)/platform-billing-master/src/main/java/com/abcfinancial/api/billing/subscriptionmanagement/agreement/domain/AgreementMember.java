package com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@EntityListeners( AuditingEntityListener.class )
@Table( name = "agreement_member" )
@SQLDelete( sql = "UPDATE agreement_member set agme_deactivated = current_timestamp where m_id =? and agrm_id =?" )
@Where( clause = "agme_deactivated IS NULL" )
@NoArgsConstructor
public class AgreementMember
{
    /**
     * Agreement Subscription Id
     */
    @EmbeddedId
    private AgreementMemberId agrmMemId;
    /**
     * AgreementNumber
     */
    @Column( name = "agreement_number" )
    private String agreementNumber;

    @NotNull
    @Column( name = "agme_is_active" )
    private boolean isActive;

    /**
     * Is Primary
     */

    @Column( name = "agme_is_primary" )
    private boolean primary;

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "agme_created", updatable = false, nullable = false )
    @CreationTimestamp
    private java.time.LocalDateTime created;

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "agme_deactivated" )
    private java.time.LocalDateTime deactivated;
}
