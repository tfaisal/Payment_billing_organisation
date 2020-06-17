package com.abcfinancial.api.billing.generalledger.settlement.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table( name = "settlement" )

public class Settlement
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "stlm_id" )
    private UUID settlementId;
    @NotNull
    @Column( name = "loc_id" )
    private UUID locationId;
    @OneToOne( fetch = FetchType.EAGER )
    @JoinColumn( name = "accn_id", nullable = false )
    private Account accountId;
    @Column( name = "stlm_created" )
    @CreationTimestamp
    private LocalDateTime created;
    @Column( name = "stlm_deactivated" )
    private LocalDateTime deactivated;
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "stlm_modified" )
    @LastModifiedDate
    private java.time.LocalDateTime modified;
    @Column( name = "stlm_date" )
    private LocalDateTime settlementDate;
    @NotNull
    @Column( name = "stlm_amount" )
    private BigDecimal amount;
}
