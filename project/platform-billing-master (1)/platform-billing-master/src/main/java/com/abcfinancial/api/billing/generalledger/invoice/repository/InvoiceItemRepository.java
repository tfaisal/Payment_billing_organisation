package com.abcfinancial.api.billing.generalledger.invoice.repository;

import com.abcfinancial.api.billing.generalledger.invoice.domain.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID>
{
    
    @Query( value = "select * from invoice_item where inv_id = :inv_id", nativeQuery = true )
    List<InvoiceItem> findByInvoiceId( @Param( "inv_id" ) UUID invId );
}
