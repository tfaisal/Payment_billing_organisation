package com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemExpirationStart;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemExpirationUnit;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.ItemType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@EntityListeners( AuditingEntityListener.class )
@JsonInclude( JsonInclude.Include.NON_NULL )
@Table( name = "subscription_item" )
@SQLDelete( sql = "UPDATE subscription_item set suit_deactivated = current_timestamp where suit_id  = ?" )
@Where( clause = "suit_deactivated IS NULL" )
@NoArgsConstructor

public class SubscriptionItem
{
    /**
     * Subscription Item Id
     */

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "suit_id" )
    private UUID id;
    /**
     * Location Id of Subscription Item
     */

    @Column( name = "loc_id" )
    private UUID locId;
    /**
     * Item Id
     */

    @Column( name = "it_id" )
    private UUID itemId;
    /**
     * Version of Item
     */

    @Column( name = "it_version" )
    private long version;
    /**
     * Name of Item
     */

    @Column( name = "suit_name" )
    private String itemName;
    /**
     * Subscription item price
     */

    @Column( name = "suit_price" )
    private BigDecimal price;
    /**
     * Quantity of subscription Item
     */

    @Column( name = "suit_quantity" )
    private long quantity;
    /**
     * Item Type
     */

    @Enumerated( EnumType.STRING )
    @Column( name = "suit_type" )
    private ItemType type;
    /**
     * True if Subscription Item is Unlimited otherwise False
     */

    @NonNull
    @Column( name = "suit_is_unlimited" )
    private boolean unlimited;
    /**
     * Type of Item for Expiration start
     */

    @Enumerated( EnumType.STRING )
    @Column( name = "suit_expiration_start" )
    private ItemExpirationStart expirationStart;
    /**
     * Unit of Item expiration
     */

    @Enumerated( EnumType.STRING )
    @Column( name = "suit_expiration_unit" )
    private ItemExpirationUnit expirationUnit;
    /**
     * Expiration Time of Subscription Item
     */

    @Column( name = "suit_expiration_period" )
    private int expirationTime;
    /**
     * Created date and time of Subscription Item
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "suit_created", updatable = false, nullable = false )
    @CreationTimestamp
    private java.time.LocalDateTime created;
    /**
     * Modified date and Time of Subscription Item
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "suit_modified" )
    @LastModifiedDate
    private java.time.LocalDateTime modified;
    /**
     * Deactivated Date and Time of Subscription Item
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "suit_deactivated" )
    private java.time.LocalDateTime deactivated;
    /**
     * Category Id for Item
     */

    @Column( name = "suit_category_id" )
    private UUID itemCategoryId;
}
