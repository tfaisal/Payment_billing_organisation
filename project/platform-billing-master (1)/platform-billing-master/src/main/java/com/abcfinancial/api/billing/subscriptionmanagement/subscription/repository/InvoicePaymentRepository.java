package com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.InvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository

public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, UUID>
{

    @Query( value = "select * from invoice_payment where inv_id = :inv_id", nativeQuery = true )
    InvoicePayment findByInvoiceId( @Param( "inv_id" ) UUID invId );
}
