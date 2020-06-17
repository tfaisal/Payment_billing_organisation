package com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EntityListeners;
import java.io.Serializable;
import java.util.UUID;

@Data
@EntityListeners( AuditingEntityListener.class )
@Embeddable

public class InvoicePaymentId implements Serializable
{
    @Column( name = "inv_id" )
    private UUID invoiceId;
    @Column( name = "pay_id" )
    private UUID paymentId;
}
