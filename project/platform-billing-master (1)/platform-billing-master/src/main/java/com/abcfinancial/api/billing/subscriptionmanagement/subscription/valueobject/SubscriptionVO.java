package com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Duration;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.SubscriptionTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data

public class SubscriptionVO
{
    /**
     * Subscription Id
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID subId;
    /**
     * Subscription plan Id
     *
     * @deprecated Plan id is not require in subscription
     */

    @Deprecated
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID planId;
    /**
     * Member account Id
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID accountId;
    /**
     * Subscription location Id
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID locationId;
    /**
     * Subscription name
     *
     * @deprecated Name field is not require in subscription
     */

    @Deprecated
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private String name;
    /**
     * List of member Id
     * Either member Id or member list should be used but not both together
     */
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private List<UUID> memberIdList;
    /**
     * member Id
     * Either member Id or member list should be used but not both together
     */
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID memberId;
    /**
     * Subscription plan version
     *
     * @deprecated Plan version is not require in subscription
     */

    @Deprecated
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private long planVersion;
    /**
     * Subscription start date should be current date
     * or 90 Days back date from the current Date.
     */

    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private LocalDate start;
    /**
     * Frequency of subscription Mandatory
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private Frequency frequency;
    /**
     * Subscription duration is mandatory when only
     * subscription type is TERM
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private int duration;
    /**
     * expirationDate Mandatory only when
     * "openEnded":false.
     */

    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private LocalDate expirationDate;

    /**
     * Subscription duration unit
     *
     * @deprecated No longer in use in subscription
     */

    @Deprecated
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private Duration durationUnit;
    /**
     * Subscription items
     */

    @NotNull
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private List<SubscriptionItemVO> items;
    /**
     * Subscription sales employee id
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private UUID salesEmployeeId;
    /**
     * Total tax on subscription
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private BigDecimal totalTax;
    /**
     * Total amount
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private BigDecimal totalAmount;
    /**
     * Total net amount
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private BigDecimal totalNetPrice;
    /**
     * Invoice Date should be current date
     * or 60 Days back date from the current Date.
     */

    @JsonFormat( pattern = "MM-dd-yyyy" )
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private LocalDate invoiceDate;
    /**
     * If true subscription will be unlimited until
     * expire externally otherwise false for limited
     * time subscription.
     */

    private boolean openEnded;
    /**
     * Subscription duration SubscriptionTypeEnum[ NEW, RENEW, REWRITE]
     */

    private SubscriptionTypeEnum subscriptionTypeEnum;
    /**
     * isRenewable reflect if the auto renew of subscription is enable or not.
     */

    private boolean isRenewable;
    /**
     * Auto Renew is Mandatory for both "OPEN" and "TERM" Subscription
     */

    private boolean autoRenew;
    /**
     * original subscription id in freeze period
     */

    private UUID freezeSubId;
    /**
     * original subscription id in clone subscription
     */

    private UUID subRefferalId;
    /**
     * Renewal Options for renew the Subscription
     */

    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private RenewalOptionsVO renewalOptions;
    /**
     * status of subscription
     */

    private boolean isActive;
    /**
     * primary subscription for agreement only
     */

    private boolean primary;

    /**
     * Set to true when payment method id is used to create subscription
     */
    @JsonIgnore
    private boolean isPameIdAccount;

    /**
     * subscription cancel date
     */
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private LocalDateTime subCancellationDate;
}
