package com.abcfinancial.api.billing.subscriptionmanagement.avalara.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Table( name = "ava_address" )
@EqualsAndHashCode
@ToString
@JsonPropertyOrder( { "line", "city", "region", "country", "postalCode" } )
@JsonIgnoreProperties( ignoreUnknown = true )
@JsonInclude( JsonInclude.Include.NON_NULL )
public class AvaAddress implements Serializable
{
    /**
     * An unique id which behaves as the identification of a particular location's Address. It should be in java.util.UUID format.
     */
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( name = "avad_id", unique = true, nullable = false )
    private UUID addressId;

    /**
     * Avalara Address isValidated
     */
    @Column( name = "avad_validated" )
    @NotNull
    private Boolean isValidated;

    /**
     * Avalara Address line
     */
    @Column( name = "avad_line" )
    @JsonProperty( "line" )
    @NotNull
    private String line;

    /**
     * Avalara Address line1
     */
    @Transient
    @JsonProperty( "line1" )
    private String line1;

    /**
     * City name.
     * Size must be between 1 and 50 inclusive.
     */
    @Column( name = "avad_city" )
    @NotNull
    private String city;

    /**
     * Name or ISO 3166 code identifying the country. as e.g US.
     */
    @Column( name = "avad_country" )
    @NotNull
    private String country;

    /**
     * Postal Code.
     * Size must be between 1 and 10 inclusive.
     */
    @Column( name = "avad_postal_code" )
    @NotNull
    private String postalCode;

    /**
     * ISO 3166 code identifying the region within the country. Fully spelled out names of the region in ISO supported languages as eg.CA.
     */
    @Column( name = "avad_region" )
    @JsonProperty( "region" )
    @NotNull
    private String region;

    /**
     * Avalara Address Latitude
     */
    @Column( name = "avad_latitude" )
    private Double latitude;

    /**
     * Avalara Address Longitude
     */
    @Column( name = "avad_longitude" )
    private Double longitude;

    /**
     * Avalara Address createdDateTime
     */
    @Column( name = "avad_created" )
    @JsonIgnore
    @NotNull
    private Timestamp createdDateTime;

    /**
     * Avalara Address modifiedDateTime
     */
    @Column( name = "avad_modified" )
    @JsonIgnore
    private Timestamp modifiedDateTime;

    /**
     * Avalara Address deactivatedDateTime
     */
    @Column( name = "avad_deactivated" )
    @JsonIgnore
    private Timestamp deactivatedDateTime;

    /**
     * Avalara Address avaLocation
     */
    @OneToOne( mappedBy = "avaAddress", cascade = CascadeType.ALL )
    @JsonIgnore
    private AvaLocation avaLocation;

}
