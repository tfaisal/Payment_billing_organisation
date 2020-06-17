package com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor

public class MemberAccountID implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Column( name = "payor_id" )
    private UUID payorId;
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "accn_id", nullable = false )

    private Account accountId;
}
