package com.abcfinancial.api.billing.generalledger.invoice.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemExpirationStart;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemExpirationUnit;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@EntityListeners( AuditingEntityListener.class )
@Table( name = "invoice_item" )

public class InvoiceItem
{
    /**
     * Invoice Item Id
     */

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Setter( AccessLevel.PROTECTED )
    @Column( name = "invi_id" )
    private UUID id;
    /**
     * location id
     */

    @Column( name = "loc_id" )
    private UUID locId;
    /**
     * Name of Item
     */

    @Column( name = "invi_name" )
    private String itemName;
    /**
     * Price of Item
     */

    @Column( name = "invi_price" )
    private BigDecimal price;
    /**
     * Version of Item
     */

    @Column( name = "it_version" )
    private Long version;
    /**
     * Tax Amount
     */

    @Column( name = "invi_tax_amount" )
    private BigDecimal taxAmount;
    /**
     * Discount Code
     */

    @Column( name = "invi_discount_code" )
    private String discountCode;
    /**
     * Discount amount
     */

    @Column( name = "invi_discount_amount" )
    private BigDecimal discountAmount;
    /**
     * Remaining amount
     */

    @Column( name = "invi_amount_remaining" )
    private BigDecimal amountRemaining;
    /**
     * Type of Item
     */

    @Enumerated( EnumType.STRING )
    @Column( name = "invi_type" )
    private ItemType type;
    /**
     * Quantity of Item
     */

    @Column( name = "invi_quantity" )
    private long quantity;
    /**
     * True if Item unlimited quantity otherwise false
     */

    @NotNull
    @Column( name = "invi_is_unlimited" )
    private boolean unlimited;
    /**
     * Expiration start
     */

    @Enumerated( EnumType.STRING )
    @Column( name = "invi_expiration_start" )
    private ItemExpirationStart expirationStart;
    /**
     * Time of expiration
     */

    @Column( name = "invi_expiration_period" )
    private int expirationTime;
    /**
     * Expiration Unit of Item
     */

    @Enumerated( EnumType.STRING )
    @Column( name = "invi_expiration_unit" )
    private ItemExpirationUnit expirationUnit;
    /**
     * Item Id
     */

    @Column( name = "it_id" )
    private UUID itemId;
    /**
     * Created date and time of Item
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "invi_created", updatable = false, nullable = false )
    @CreationTimestamp
    private java.time.LocalDateTime created;
    /**
     * Invoice Item modified date and time
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "invi_modified" )
    @LastModifiedDate
    private java.time.LocalDateTime modified;
    /**
     * Deactivated date and time of Item
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "invi_deactivated" )
    private java.time.LocalDateTime deactivated;
    /**
     * Category Id for Item
     */

    @Column( name = "invi_category_id" )
    private UUID itemCategoryId;
    @Column( name = "amtc_tax_code" )
    private String taxCode;
}
