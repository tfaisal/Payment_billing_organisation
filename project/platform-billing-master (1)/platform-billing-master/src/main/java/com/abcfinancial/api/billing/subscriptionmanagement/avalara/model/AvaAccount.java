package com.abcfinancial.api.billing.subscriptionmanagement.avalara.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Table( name = "ava_account" )
@EqualsAndHashCode
@ToString
@JsonPropertyOrder( { "org_id", "loc_id", "ava_id", "ava_created", "ava_modified", "ava_deactivated" } )

public class AvaAccount
{
    @JsonProperty( "org_id" )
    @Column( name = "org_id" )
    private UUID organizationId;
    @JsonProperty( "loc_id" )
    @Column( name = "loc_id" )
    private UUID locationId;
    @JsonProperty( "ava_id" )
    @Column( name = "ava_id" )
    @Id
    private Long avalaraAccId;
    @JsonProperty( "ava_created" )
    @Column( name = "ava_created" )
    private Timestamp createdDate;
    @JsonProperty( "ava_modified" )
    @Column( name = "ava_modified" )
    private Timestamp modifiedDate;
    @JsonProperty( "ava_deactivated" )
    @Column( name = "ava_deactivated" )
    private Timestamp deactivatedDate;
}
