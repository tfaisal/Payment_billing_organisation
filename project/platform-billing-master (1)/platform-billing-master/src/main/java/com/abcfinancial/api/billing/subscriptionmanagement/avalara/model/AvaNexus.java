package com.abcfinancial.api.billing.subscriptionmanagement.avalara.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table( name = "ava_nexus" )
@EqualsAndHashCode
@ToString
@JsonPropertyOrder( { "avn_id", "avn_created", "avn_modified", "avn_deactivated", "avc_id", "avl_id" } )

public class AvaNexus
{
    @Id
    @JsonProperty( "avn_id" )
    @Column( name = "avn_id" )
    private Long nexusid;
    @JsonProperty( "avn_created" )
    @Column( name = "avn_created" )
    private Timestamp createdDate;
    @JsonProperty( "avn_modified" )
    @Column( name = "avn_modified" )
    private Timestamp modifiedDate;
    @JsonProperty( "avn_deactivated" )
    @Column( name = "avn_deactivated" )
    private Timestamp deactivatedDate;
    @JsonProperty( "avl_id" )
    @ManyToOne( fetch = FetchType.EAGER, optional = false )
    @JoinColumn( name = "avl_id", nullable = false )
    private AvaLocation avaLocation;
    @ManyToOne( fetch = FetchType.EAGER, optional = false )
    @JoinColumn( name = "avc_id", nullable = false )
    private AvaCompany avaCompany;
}
