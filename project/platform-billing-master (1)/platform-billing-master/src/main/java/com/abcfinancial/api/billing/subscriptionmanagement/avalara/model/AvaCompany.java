package com.abcfinancial.api.billing.subscriptionmanagement.avalara.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Data
@Entity
@Table( name = "ava_company" )
@EqualsAndHashCode
@JsonPropertyOrder( { "avc_id", "avc_created", "avc_modified", "avc_deactivated", "avc_license_key", "ava_id" } )

public class AvaCompany
{
    @Id
    @JsonProperty( "avc_id" )
    @Column( name = "avc_id" )
    private Long companyId;
    @JsonProperty( "avc_created" )
    @Column( name = "avc_created" )
    private Timestamp createdDate;
    @JsonProperty( "avc_modified" )
    @Column( name = "avc_modified" )
    private Timestamp modifiedDate;
    @JsonProperty( "avc_deactivated" )
    @Column( name = "avc_deactivated" )
    private Timestamp deactivatedDate;
    @JsonProperty( "avc_license_key" )
    @Size( min = 1, max = 30, message = "License Key size should be between 0 to 30" )
    @Column( name = "avc_license_key" )
    private String licenseKey;
    @OneToOne( fetch = FetchType.EAGER, optional = false )
    @JoinColumn( name = "ava_id", nullable = false )
    private AvaAccount avaAccount;
}
