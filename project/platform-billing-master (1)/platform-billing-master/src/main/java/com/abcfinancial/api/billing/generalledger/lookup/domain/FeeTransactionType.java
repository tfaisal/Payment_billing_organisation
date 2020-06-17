package com.abcfinancial.api.billing.generalledger.lookup.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table( name = "fee_transaction_type" )

public class FeeTransactionType
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "ftt_key" )
    private String feeTransactionTypeKey;

    @NotNull
    @Column( name = "ftt_value" )
    private String feeTransactionTypeValue;
}
