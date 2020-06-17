package com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@EntityListeners( AuditingEntityListener.class )
@Table( name = "ava_master_tax_code" )
@SQLDelete( sql = "UPDATE subscription set amtc_tax_deactivated = current_timestamp where amtc_tax_id  = ?" )
@Where( clause = "amtc_tax_deactivated IS NULL" )
@ToString
@Setter
@Getter

public class AvalaraMasterTaxCode
{
    /**
     * Avalara Tax Id
     */

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @NotNull
    @Column( name = "amtc_tax_id" )
    private UUID id;
    /**
     * Created Date and Time of Avalara Master Tax Code
     */

    @JsonIgnore
    @Column( name = "amtc_tax_created", updatable = false, nullable = false )
    @CreationTimestamp
    private LocalDateTime created;
    /**
     * Modified Date and Time of Avalara Master Tax Code
     */

    @JsonIgnore
    @Column( name = "amtc_tax_modified" )
    @LastModifiedDate
    private LocalDateTime modified;
    /**
     * Deactivated Date and Time for Avalara Master Tax Code
     */

    @JsonIgnore
    @Column( name = "amtc_tax_deactivated" )
    private LocalDateTime deactivated;
    /**
     * Avalara Master Tax Code
     */

    @Column( name = "amtc_tax_code" )
    @NotNull
    private String taxCode;
    /**
     * Avalara Master Tax Description
     */

    @Column( name = "amtc_tax_description" )
    @NotNull
    private String description;
    // End
}
