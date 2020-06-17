package com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@EqualsAndHashCode( callSuper = false )
@EntityListeners( AuditingEntityListener.class )
@SQLDelete( sql = "UPDATE agreement set agre_deactivated = current_timestamp where agrm_id  = ?" )
@Table( name = "agreement" )
@NoArgsConstructor

public class Agreement
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Setter( AccessLevel.PROTECTED )
    @Column( name = "agrm_id" )
    private UUID agreementId;

    /**
     * Location Id for Agreement
     */
    @Column( name = "loc_id" )
    private UUID locationId;

    /**
     * AgreementNumber
     */
    @Column( name = "agreement_number" )
    private String agreementNumber;
    @Transient
    @JsonProperty( "agreementSubscription" )
    private AgreementSubscription agreementSubscription;
    @Transient
    @JsonProperty( "subscriptionDocuments" )
    private SubscriptionDocuments subscriptionDocuments;

    /**
     * Deactivated Date and Time of Agreement
     */
    @JsonIgnore
    @Column( name = "agre_deactivated" )
    private java.time.LocalDateTime deactivated;

    /**
     * Modified Date and Time of Agreement
     */
    @JsonIgnore
    @Column( name = "agre_modified" )
    @LastModifiedDate
    private java.time.LocalDateTime modified;

    /**
     * Created Date and Time of Agreement
     */
    @JsonIgnore
    @Column( name = "agre_created", updatable = false, nullable = false )
    @CreationTimestamp
    private java.time.LocalDateTime created;

    /**
     * Campaign of this Agreement
     */
    @JsonIgnore
    @Column( name = "agrm_campgn" )
    private String campaign;
}
