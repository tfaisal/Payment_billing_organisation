package com.abcfinancial.api.billing.generalledger.statements.domain;

import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString

public class StatementInvoiceId implements Serializable
{
    private static final long serialVersionUID = -3542326762906154961L;
    @ManyToOne( fetch = FetchType.EAGER, cascade = CascadeType.MERGE )
    @JoinColumn( name = "st_id", nullable = false )
    private Statement statementId;
    @ManyToOne( fetch = FetchType.EAGER, cascade = CascadeType.MERGE )
    @JoinColumn( name = "inv_id" )
    private Invoice invoiceId;
}
