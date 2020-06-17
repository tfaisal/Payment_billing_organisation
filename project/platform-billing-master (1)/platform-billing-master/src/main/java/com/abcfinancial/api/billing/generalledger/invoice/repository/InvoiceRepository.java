package com.abcfinancial.api.billing.generalledger.invoice.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>
{
    Invoice findFirstBySubscriptionOrderByInvoiceDateDesc( Subscription subscription );

    List<Invoice> findByMemberIdAndInvoiceDateBetweenOrderByInvoiceDateDesc( UUID memberId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable );
    // JIRA : P3-3015

    @Query( value = "select * from invoice WHERE m_id = :mem_id  FETCH FIRST ROW ONLY ", nativeQuery = true )
    Invoice findLocationByMemberId( @Param( "mem_id" ) UUID memberId );

    @Query( value = "select * from invoice WHERE m_id = :mem_id ", nativeQuery = true )
    List<Invoice> findLocationByMemberId2( @Param( "mem_id" ) UUID memberId );

    List<Invoice> findByAccountId( UUID id );

    Optional<Invoice> findByIdAndAccountId( UUID invoiceId, UUID accountId );

    @Query( value = "select * from invoice WHERE sub_id = :sub_id ", nativeQuery = true )
    List<Invoice> findBySubId( @Param( "sub_id" ) UUID subcriptionId );
}
