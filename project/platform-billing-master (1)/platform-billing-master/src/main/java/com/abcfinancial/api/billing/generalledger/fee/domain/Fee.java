package com.abcfinancial.api.billing.generalledger.fee.domain;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table( name = "fee" )

public class Fee
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "fee_id" )
    private UUID feeId;

    @Column( name = "accn_id" )
    private UUID accountId;

    @Column( name = "fee_created" )
    @CreationTimestamp
    private LocalDateTime created;

    @Column( name = "fee_deactivated" )
    private LocalDateTime deactivated;

    @Column( name = "fee_modified" )
    @CreationTimestamp
    private LocalDateTime modified;

    @Column( name = "fm_key" )
    private String feeMode;

    @Column( name = "ft_key" )
    private String feeType;

    @Column( name = "ftt_key" )
    private String feeTransactionType;

    @Column( name = "fvt_key" )
    private String feeValueType;

    @Column( name = "fee_value" )
    private BigDecimal feeValue;

    @Transient
    private boolean active;

    public boolean isActive()
    {
        return deactivated == null || deactivated.isAfter( LocalDateTime.now() );
    }
}
