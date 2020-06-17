package com.abcfinancial.api.billing.generalledger.statements.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table( name = "statement_invoice" )
@EntityListeners( AuditingEntityListener.class )

public class StatementInvoice {

    @EmbeddedId
    private StatementInvoiceId statementInvoiceId;
    @Column( name = "stin_created", nullable = false )
    @CreationTimestamp
    private LocalDateTime created;
    @Column( name = "stin_deactivated" )
    private LocalDateTime deactivated;
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @Column( name = "stin_modified" )
    @LastModifiedDate
    private java.time.LocalDateTime modified;
    @NotNull
    @Column( name = "loc_id" )
    private UUID locationId;
}
