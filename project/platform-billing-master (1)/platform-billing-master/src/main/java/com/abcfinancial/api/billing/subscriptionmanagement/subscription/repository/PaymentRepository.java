package com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Payment;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public interface PaymentRepository extends JpaRepository<Payment, UUID>
{
    List<Payment> findByAccountAccountId( UUID accountId, Pageable pageRequest );
    // JIRA : P3-3015

    @Query( value = "select * from payment where accn_id = :accnId FETCH FIRST ROW ONLY ", nativeQuery = true )
    Payment findByAccountIdByLocation( @Param( "accnId" ) UUID accountId );

    @Query( value = "select * from payment where accn_id = :accnId ", nativeQuery = true )
    List<Payment> findByAccountIdByLocation2( @Param( "accnId" ) UUID accountId );

    Optional<Payment> findByInvoiceIdAndAccount( UUID invoiceId, Account account );

    Optional<Payment> findByStatementIdAndAccountAccountId( UUID statementId, UUID accountId );

    Optional<Payment> findBySettlementIdAndAccountAccountId( UUID settlementId, UUID accountId );

    Optional<Payment> findByInvoiceId( UUID invoiceId );

    Optional<Payment> findByStatementIdAndPameId( UUID statementId, UUID pamentMethodId );

    Optional<Payment> findTopByStatementId( UUID statementId );
}
