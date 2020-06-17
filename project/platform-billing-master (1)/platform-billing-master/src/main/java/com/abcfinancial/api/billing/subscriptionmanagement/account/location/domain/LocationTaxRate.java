package com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Table( name = "location_tax_rate" )
@SQLDelete( sql = "UPDATE location_tax_rate set ltr_deactivated = current_timestamp where ltr_id =?" )
@Where( clause = "ltr_deactivated IS NULL" )
@EntityListeners( AuditingEntityListener.class )
@JsonIgnoreProperties( ignoreUnknown = true )
@JsonPropertyOrder( { "id", "locationId", "empId", "itemCategoryId", "taxRate", "taxCode", "isOverriden", "suggestedTaxRate" } )
public class LocationTaxRate
{
    /**
     * Location tax rate Id.
     */
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Setter( AccessLevel.PUBLIC )
    @Column( name = "ltr_id" )
    @NotNull
    private UUID id;

    /**
     * Location Tax Rate  locationId
     * Format java.util.UUID
     */
    @Column( name = "loc_id" )
    @NotNull
    private UUID locationId;

    /**
     * Location Tax Rate employee Id
     * Format java.util.UUID
     */
    @Column( name = "emp_id" )
    @NotNull
    private UUID empId;

    /**
     * Location tax Rate itemCategoryId
     * Format java.util.UUID
     */
    @Column( name = "itca_id" )
    private UUID itemCategoryId;

    /**
     * Location Tax taxCode
     * Maximum Size will be 30 characters
     */
    @Column( name = "ltr_tax_code" )
    private String taxCode;

    /**
     * Location Tax Rate
     * Must be between 0 to 100 value only
     */
    @Column( name = "ltr_tax_rate" )
    private BigDecimal taxRate;

    /**
     * Location Tax Rate
     * Value can be true of false
     */
    @Column( name = "ltr_is_overriden" )
    private Boolean isOverriden;

    /**
     * Location Tax Rate suggestedTaxRate
     * Size would be between 0 to 100 integer value only
     */
    @Column( name = "ltr_suggested_tax_rate" )
    private BigDecimal suggestedTaxRate;

    /**
     * Location tax Rate create date time value
     */
    @CreatedDate
    @JsonIgnore
    @Column( name = "ltr_created" )
    private Timestamp created;

    /**
     * Location tax Rate modified data time value
     */
    @LastModifiedDate
    @JsonIgnore
    @Column( name = "ltr_modified" )
    private Timestamp modified;

    /**
     * Location tax Rate deactivated date time value
     */
    @JsonIgnore
    @Column( name = "ltr_deactivated" )
    private Timestamp deactivated;

    /**
     * Location tax Rate version as Integer value
     */
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    @Column( name = "ltr_version" )
    @NotNull
    private Long version;

}
