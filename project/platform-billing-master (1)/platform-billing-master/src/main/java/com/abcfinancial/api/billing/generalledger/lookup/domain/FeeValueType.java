package com.abcfinancial.api.billing.generalledger.lookup.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table( name = "fee_value_type" )

public class FeeValueType
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "fvt_key" )
    private String feeValueTypeKey;

    @NotNull
    @Column( name = "fvt_value" )
    private String feeValueTypeValue;
}
