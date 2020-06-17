package com.abcfinancial.api.billing.subscriptionmanagement.account.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Table( name = "account" )
@SQLDelete( sql = "UPDATE account set accn_deactivated = current_timestamp where accn_id  = ?" )
@Where( clause = "accn_deactivated IS NULL" )
@EntityListeners( AuditingEntityListener.class )

public class Account implements Serializable
{
    /**
     * Account id
     */

    @Id
    /* @Setter( AccessLevel.PROTECTED )*/

    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "accn_id" )
    private UUID accountId;
    /**
     * Location id
     */

    @NotNull
    @Column( name = "loc_id" )
    private UUID location;
    /**
     * Account created date and time
     */

    @CreatedDate
    @NotNull
    @JsonIgnore
    @Column( name = "accn_created" )
    private LocalDateTime created;
    /**
     * Modified date and time of Account
     */

    @Basic( optional = true )
    @LastModifiedDate
    @JsonIgnore
    @Column( name = "accn_modified" )
    private LocalDateTime modified;
    /**
     * Deactivated date and time of Account
     */

    @Basic( optional = true )
    @JsonIgnore
    @Column( name = "accn_deactivated" )
    private LocalDateTime deactivated;
    /**
     * Account Holder Name
     */

    @NotNull
    @Size( min = 1, max = 100, message = "name must be between 1 and 100 characters" )
    @Column( name = "accn_name" )
    private String name;
    /**
     * Email of Account Holder
     */

    @Email( message = "Email should be valid" )
    @Column( name = "accn_email" )
    private String email;
    /**
     * Phone number
     */

    @Basic( optional = true )
    @Column( name = "accn_phone" )
    private String phone;
    /**
     * sEvaluation - refers to the cycle ( can be settlement ( frequency / threshold ( to be used in future ) ) - in case of client account / statement ( frequency ) - in case
     * of payor account  ) for Account.
     * frequency can be -
     * 1. DAILY  -  daily
     * 2. WEEKLY -   weekly
     * 3. MONTHLY -   monthly
     * 4. ANNUALLY -  yearly
     * 5. QUARTERLY -  3 months
     * 6. SEMIANNUALLY -  6 months
     * 7. EVERY_OTHER_MONTH -  2 months
     * 8. EVERY_OTHER_WEEK -  2 weeks
     */

    @NotNull
    @Basic( optional = true )
    @Column( name = "accn_s_evaluation" )
    private String sevaluation;
    /**
     * Date refers to client Settlement Date and payor Billing Date
     */

    @Column( name = "accn_cycle_date" )
    private LocalDate billingDate;

}
