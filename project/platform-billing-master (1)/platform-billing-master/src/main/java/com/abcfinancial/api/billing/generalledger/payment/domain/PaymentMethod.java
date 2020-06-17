package com.abcfinancial.api.billing.generalledger.payment.domain;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j

@Data
@Entity
@Table( name = "payment_method" )
@SQLDelete( sql = "UPDATE payment_method set pame_deactivated = current_timestamp where pame_id  = ?" )
@Where( clause = "pame_deactivated IS NULL" )
@EntityListeners( { AuditingEntityListener.class } )

public class PaymentMethod
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Setter( AccessLevel.PROTECTED )
    @Column( name = "pame_id" )
    private UUID id;
    @JsonIgnore
    @Column( name = "loc_id" )
    private UUID locationId;
    @NotNull
    @CreatedDate
    @JsonIgnore
    @Column( name = "pame_created" )
    private LocalDateTime created;
    @LastModifiedDate
    @JsonIgnore
    @Column( name = "pame_modified" )
    private LocalDateTime modified;
    @JsonIgnore
    @Column( name = "pame_deactivated" )
    private LocalDateTime deactivated;
    @NotNull
    @Column( name = "pame_is_active" )
    private Boolean active;
    @NotNull( message = "type cannot be null" )
    @Enumerated( EnumType.STRING )
    @Column( name = "pame_type" )
    private Type type;
    @Enumerated( EnumType.STRING )
    @Column( name = "pame_bank_accn_type" )
    private BankAccountType bankAccountType;
    @Column( name = "pame_display" )
    private String display;
    @Enumerated( EnumType.STRING )
    @Column( name = "pame_processor" )
    private Processor processor;
    @Enumerated( EnumType.STRING )
    @Column( name = "pame_brand" )
    private Brand brand;
    @Column( name = "pame_token" )
    private String token;
    @Column( name = "pame_routing" )
    private String routingNumber;
    @Column( name = "pame_account" )
    private String accountNumber;
    @Column( name = "pame_expiration_month" )
    @JsonIgnore
    private int expiryMonth;
    @Column( name = "pame_expiration_year" )
    @JsonIgnore
    private int expiryYear;
    @ManyToOne( fetch = FetchType.EAGER )
    @JoinColumn( name = "accn_id" )
    private Account accountId;
    @Column( name = "pame_alias" )
    private String alias;
    @Column( name = "pame_token_id" )
    private UUID tokenId;
    @Column( name = "pame_billing_date" )
    private LocalDate billingDate;
    @Enumerated( EnumType.STRING )
    @Column( name = "pame_s_evaluation" )
    private Frequency sevaluation;
    @Column( name = "pame_account_holder" )
    private String accountHolderName;
}
