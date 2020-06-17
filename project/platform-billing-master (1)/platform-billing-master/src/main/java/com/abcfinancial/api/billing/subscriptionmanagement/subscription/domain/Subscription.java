package com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Duration;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.RenewType;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.SubscriptionTypeEnum;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@EqualsAndHashCode( callSuper = false )
@EntityListeners( AuditingEntityListener.class )
@Table( name = "subscription" )
@SQLDelete( sql = "UPDATE subscription set sub_deactivated = current_timestamp where sub_id  = ?" )
@Where( clause = "sub_deactivated IS NULL" )
@NoArgsConstructor

public class Subscription implements Cloneable
{
    /**
     * Subscription Id
     */

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "sub_id" )
    private UUID subId;
    /**
     * Location Id for Subscription
     */

    @Column( name = "loc_id" )
    private UUID locationId;
    /**
     * Plan Id
     */

    @Deprecated
    @Column( name = "supl_id" )
    private UUID planId;
    /**
     * Employee Id
     */

    @Column( name = "emp_id" )
    private UUID salesEmployeeId;
    /**
     * Total tax amount
     */

    @Transient
    private BigDecimal totalTax;
    /**
     * Total Amount
     */

    @Transient
    private BigDecimal totalAmount;
    /**
     * Total net Price of subscription
     */

    @Transient
    private BigDecimal totalNetPrice;
    /**
     * Account object contains details of account
     */

    @OneToOne( fetch = FetchType.EAGER )
    @JoinColumn( name = "accn_id", nullable = false )
    private Account account;
    /**
     * Member Subscription object contains details of subscribed member
     */

    @Transient
    @JsonProperty( "memberSubscription" )
    private List<MemberSubscription> memberSubscriptionList;
    /**
     * Plan version
     */

    @Deprecated
    @Column( name = "supl_version" )
    private long planVersion;
    /**
     * Subscription start date
     */

    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @Column( name = "sub_start_date" )
    private LocalDate start;
    /**
     * Subscription expire date
     */

    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @Column( name = "sub_expiration_date" )
    private LocalDate expirationDate;
    /**
     * Frequency of subscription
     */

    @Enumerated( EnumType.STRING )
    @Column( name = "sub_frequency" )
    private Frequency frequency;
    /**
     * Duration of Subscription
     */

    @Column( name = "sub_duration" )
    private int duration;
    /**
     * Duration Unit of Subscription
     */

    @Deprecated
    @Enumerated( EnumType.STRING )
    @Column( name = "sub_duration_unit" )
    private Duration durationUnit;
    /**
     * Name of Subscription
     */

    @Deprecated
    @Column( name = "sub_name" )
    private String name;
    /**
     * List of Subscription Item
     */

    @OneToMany( fetch = FetchType.EAGER, cascade = CascadeType.ALL )
    @JoinColumn( name = "sub_id", nullable = false )
    private List<SubscriptionItem> items;
    /**
     * Created Date and Time of Subscription
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "sub_created", updatable = false, nullable = false )
    @CreationTimestamp
    private LocalDateTime created;
    /**
     * Modified Date and Time of Subscription
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "sub_modified" )
    @LastModifiedDate
    private LocalDateTime modified;
    /**
     * Deactivated Date and Time of Subscription
     */

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "sub_deactivated" )
    private LocalDateTime deactivated;
    /**
     * Billing date of Subscription
     */

    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @Column( name = "sub_invoice_date" )
    private LocalDate invoiceDate;
    /**
     * Status of Subscription. True if Subscription active otherwise False
     */

    @NotNull
    @Column( name = "sub_is_active" )
    private boolean isActive;
    /**
     * scheduled Invoice Id of Subscription
     */

    @Column( name = "sub_schedule_invoices_id" )
    private UUID scheduleInvoicesId;
    /**
     * Cancellation Date and time of Subscription
     */

    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @Column( name = "sub_cancellation_date" )
    private LocalDateTime subCancellationDate;
    /**
     * True if Subscription is Open that means not have expiration date
     * otherwise False
     */

    @Column( name = "sub_is_open" )
    private boolean openEnded;
    /**
     * Cancel Event Id of Subscription
     */

    @Column( name = "sub_cancel_event_id" )
    private UUID subCancelEventId;
    @Enumerated( EnumType.STRING )
    @Column( name = "sub_type" )
    private SubscriptionTypeEnum subscriptionTypeEnum;
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    // @Column( name = "sub_renew_date" )
    // private LocalDate renewDate;
    @Column( name = "sub_is_renew" )
    private boolean isRenewable;
    @Enumerated( EnumType.STRING )
    @Column( name = "sub_renew_type" )
    private RenewType renewType;
    @Column( name = "sub_is_auto_renew" )
    private boolean autoRenew;
    @Column( name = "sub_schedule_renewal_id" )
    private UUID subScheduleRenewalId;
    @JsonSerialize
    @JsonDeserialize
    @Column( name = "sub_freeze_start_date" )
    private LocalDateTime freezeStartDate;
    @JsonSerialize
    @JsonDeserialize
    @Column( name = "sub_freeze_end_date" )
    private LocalDateTime freezeEndDate;
    @Column( name = "sub_freeze_amount" )
    private BigDecimal freezeAmount;
    @Column( name = "sub_expired_event_id" )
    private UUID subExpiredEventId;
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @Column( name = "sub_renewal_invoice_date" )
    private LocalDate renewInvoiceDate;
    @Transient
    private boolean isPameIdAccount;
    @Column( name = "sub_ref_prev_id" )
    private UUID subPrevRefId;
    @Column( name = "sub_ref_next_id" )
    private UUID subNextRefId;

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
