package com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain;

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
@Table( name = "payor_account" )
@SQLDelete( sql = "UPDATE payor_account set meac_deactivated = current_timestamp where accn_id  = ?" )
@Where( clause = "meac_deactivated IS NULL" )
@EntityListeners( AuditingEntityListener.class )

public class MemberCreation
{
    @EmbeddedId
    private MemberAccountID memberAccountID;
    @Column( name = "m_id" )
    private UUID memberId;
    @NotNull
    @JsonIgnore
    @Column( name = "loc_id" )
    private UUID locId;
    @NotNull
    @JsonIgnore
    @Column( name = "meac_created" )
    @CreatedDate
    private LocalDateTime created;
    @JsonIgnore
    @Column( name = "meac_deactivated" )
    private LocalDateTime deactivated;
}
