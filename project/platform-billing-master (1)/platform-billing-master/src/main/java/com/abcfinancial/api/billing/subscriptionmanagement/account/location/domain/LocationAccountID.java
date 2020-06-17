package com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Data
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor

public class LocationAccountID implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Column( name = "loc_id" )
    private UUID location;
    @Column( name = "accn_id" )
    private UUID account;
}
