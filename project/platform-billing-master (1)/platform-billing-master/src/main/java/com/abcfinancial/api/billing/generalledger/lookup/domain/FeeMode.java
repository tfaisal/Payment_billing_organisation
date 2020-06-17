package com.abcfinancial.api.billing.generalledger.lookup.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table( name = "fee_mode" )

public class FeeMode
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "fm_key" )
    private String feeModeKey;

    @NotNull
    @Column( name = "fm_value" )
    private String feeModeValue;

}
