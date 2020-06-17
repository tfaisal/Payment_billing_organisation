package com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table( name = "client_account" )
@SQLDelete( sql = "UPDATE client_account set loca_deactivated = current_timestamp where loc_id  = ? and accn_id  = ?" )
@Where( clause = "loca_deactivated IS NULL" )
@EntityListeners( AuditingEntityListener.class )

public class LocationAccount
{
    @EmbeddedId
    private LocationAccountID locaccId;
    @Column( name = "mer_id" )
    private UUID merchantId;
    @JsonIgnore
    @Column( name = "loca_deactivated" )
    private LocalDateTime deactivated;

    @Column( name = "client_id" )
    private UUID clientId;
    @NotNull
    @CreatedDate
    @JsonIgnore
    @Column( name = "loca_created" )
    private LocalDateTime created;
    @ManyToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
    @JoinColumn( name = "accn_id", insertable = false, updatable = false )
    private Account accountId;
}
