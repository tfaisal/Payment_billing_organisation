package com.abcfinancial.api.billing.subscriptionmanagement.account.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.AccountingRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface AccountingRevenueRepository extends JpaRepository<AccountingRevenue, UUID>
{
    List<AccountingRevenue> findAccountingRevenueByinvoiceId( UUID invoiceId );
}
