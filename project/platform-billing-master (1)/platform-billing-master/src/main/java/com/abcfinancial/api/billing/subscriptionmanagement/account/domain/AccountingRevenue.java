package com.abcfinancial.api.billing.subscriptionmanagement.account.domain;

import com.abcfinancial.api.billing.utility.exception.CustomErrorResponse;
import com.abcfinancial.api.common.domain.ErrorResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.http.HttpStatus;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j

@Data
@ToString
@Entity
@SQLDelete( sql = "UPDATE accounting_revenue set acre_deactivated = current_timestamp where rev_id  = ?" )
@Where( clause = "acre_deactivated IS NULL" )
@NoArgsConstructor
@EntityListeners( AuditingEntityListener.class )
@Table( name = "accounting_revenue" )

public class AccountingRevenue implements Cloneable
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "acre_id" )
    private UUID id;
    @Column( name = "loc_id" )
    private UUID locationId;
    @Basic( optional = true )
    @Column( name = "acre_created" )
    @CreatedDate
    private LocalDateTime created;
    @Basic( optional = true )
    @LastModifiedDate
    @Column( name = "acre_modified" )
    private LocalDateTime modified;
    @Basic( optional = true )
    @Column( name = "acre_deactivated" )
    private LocalDateTime deactivated;
    @Column( name = "acre_net_price" )
    private BigDecimal netPrice;
    @Column( name = "acre_item_type" )
    private String itemType;
    @Column( name = "acre_item_id" )
    private UUID itemId;
    @Column( name = "acre_item_version" )
    private long itemVersion;
    @Column( name = "acre_quantity_sold" )
    private long quantitySold;
    @Column( name = "acre_start_date" )
    private LocalDateTime revStartDate;
    @Basic( optional = true )
    @Column( name = "acre_revenue_period" )
    private int revPeriod;
    @Basic( optional = true )
    @Column( name = "acre_expiration_date" )
    private LocalDateTime revExpirationDate;
    @Column( name = "m_id" )
    private UUID mId;
    @Column( name = "inv_id" )
    private UUID invoiceId;
    @Column( name = "accn_id" )
    private UUID accId;
    @Column( name = "invi_id" )
    private UUID inviId;

    @Override
    public AccountingRevenue clone()
    {
        try
        {
            return (AccountingRevenue) super.clone();
        }
        catch( CloneNotSupportedException exception )
        {
            log.debug( "Why are we cloning this way???", exception );
            throw new ErrorResponse(
                new CustomErrorResponse( HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), AccountingRevenue.class, exception.getMessage() ) );
        }
    }
}
