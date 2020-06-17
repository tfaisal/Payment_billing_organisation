package com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Data
@Entity
@EntityListeners( AuditingEntityListener.class )
@Table( name = "invoice_payment" )
@NoArgsConstructor

public class InvoicePayment
{
    @EmbeddedId
    private InvoicePaymentId id;
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "inpa_created" )
    @CreationTimestamp
    private java.time.LocalDateTime created;
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "inpa_deactivated" )
    private java.time.LocalDateTime deactivated;
}
