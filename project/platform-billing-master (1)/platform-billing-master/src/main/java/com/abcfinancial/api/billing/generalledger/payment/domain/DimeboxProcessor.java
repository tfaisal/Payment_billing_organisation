package com.abcfinancial.api.billing.generalledger.payment.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity

@Table( name = "dimebox_processor" )
@SQLDelete( sql = "UPDATE dimebox_processor set dipr_deactivated = current_timestamp where dipr_id  = ?" )
@Where( clause = "dipr_deactivated IS NULL" )
@EntityListeners( AuditingEntityListener.class )

public class DimeboxProcessor
{
    /**
     * An unique id which behaves as the identification of a particular processor.
     */

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Setter( AccessLevel.PROTECTED )
    @Column( name = "dipr_id" )
    private UUID id;
    @CreatedDate
    @JsonIgnore
    @Column( name = "dipr_created" )
    private LocalDateTime created;
    @LastModifiedDate
    @JsonIgnore
    @Column( name = "dipr_modified" )
    private LocalDateTime modified;
    @JsonIgnore
    @Column( name = "dipr_deactivated" )
    private LocalDateTime deactivated;
    /**
     * Dimebox organizationId
     */

    @NotNull( message = "organization Id cannot be null" )
    @NotBlank( message = "organization Id cannot be blank" )
    @Column( name = "dipr_orgn_id" )
    private String organizationId;
    /**
     * Dimebox accountId.
     */

    @NotNull( message = "Account Id cannot be null" )
    @NotBlank( message = "Account Id cannot be blank" )
    @Column( name = "dipr_acnt_id" )
    private String accountId;
    /**
     * An unique id which behaves as the identification of a particular registered organization and its location.
     */

    @NotNull( message = "location Id cannot be null" )
    @Column( name = "loc_id" )
    private UUID locationId;
}
