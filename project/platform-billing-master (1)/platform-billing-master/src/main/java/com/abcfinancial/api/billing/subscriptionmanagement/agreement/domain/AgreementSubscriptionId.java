package com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain;

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

public class AgreementSubscriptionId implements Serializable
{
    /**
     * Agreement Id
     */

    @Column( name = "agrm_id" )
    private UUID agreementId;
    /**
     * Subscription Id
     */

    @Column( name = "sub_id" )
    private UUID subId;
}
