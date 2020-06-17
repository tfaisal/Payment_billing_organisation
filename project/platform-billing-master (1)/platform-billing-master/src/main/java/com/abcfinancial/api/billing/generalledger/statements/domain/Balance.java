package com.abcfinancial.api.billing.generalledger.statements.domain;

import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.billing.utility.exception.CustomErrorResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.http.HttpStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Data
@Entity
@Table( name = "balance" )
@SQLDelete( sql = "UPDATE balance set bal_deactivated = current_timestamp where bal_id  = ?" )
@Where( clause = "bal_deactivated IS NULL" )

public class Balance implements Cloneable
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "bal_id" )
    private UUID id;
    @Column( name = "accn_id" )
    private UUID accountId;
    @Column( name = "pame_id" )
    private UUID paymentMethodId;
    @NotNull
    @Column( name = "bal_amount" )
    private BigDecimal amount;
    @Column( name = "bal_date" )
    private LocalDateTime balanceDate;
    @Column( name = "bal_created" )
    @CreationTimestamp
    private LocalDateTime created;
    @Column( name = "bal_deactivated" )
    private LocalDateTime deactivated;
    @Column( name = "bal_modified" )
    @LastModifiedDate
    private LocalDateTime modified;

    @Override
    public Balance clone( )
    {
        try
        {
            return (Balance) super.clone( );
        }
        catch( CloneNotSupportedException exception )
        {
            log.debug( "Why are we cloning this way in the FIRST place", exception );
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value( ), Balance.class, exception.getMessage( ) ) );
        }
    }
}
