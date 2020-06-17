package com.abcfinancial.api.billing.subscriptionmanagement.avalara.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table( name = "ava_location" )
@Data
@EqualsAndHashCode
@ToString
@JsonPropertyOrder( { "avaLocationId", "avaAddress" } )

public class AvaLocation implements Serializable
{
    /**
     * An unique id which behaves as the identification of a particular location. It should be in java.util.UUID format.
     */
    @Id
    @Column( name = "avl_id", unique = true, nullable = false )
    @JsonProperty( "avaLocationId" )
    @NotNull
    private Long locationId;

    /**
     * Avalara Address createdDateTime
     */
    @Column( name = "avl_created" )
    @JsonIgnore
    @NotNull
    private Timestamp createdDateTime;

    /**
     * Avalara Address modifiedDateTime
     */
    @Column( name = "avl_modified" )
    @JsonIgnore
    private Timestamp modifiedDateTime;

    /**
     * Avalara Address deactivatedDateTime
     */
    @Column( name = "avl_deactivated" )
    @JsonIgnore
    private Timestamp deactivatedDateTime;

    /**
     * Details Of Avalara Location Address
     */
    @OneToOne( cascade = CascadeType.ALL )
    @JoinColumn( name = "avad_id", nullable = false, unique = true )
    private AvaAddress avaAddress;

}

