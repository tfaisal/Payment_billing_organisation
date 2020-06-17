package com.abcfinancial.api.billing.generalledger.lookup.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table( name = "fee_type" )

public class FeeType
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "ft_key" )
    private String feeTypeKey;

    @NotNull
    @Column( name = "ft_value" )
    private String feeTypeValue;
}
