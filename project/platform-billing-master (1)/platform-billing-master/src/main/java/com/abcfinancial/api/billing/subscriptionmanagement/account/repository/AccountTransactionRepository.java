package com.abcfinancial.api.billing.subscriptionmanagement.account.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.AccountingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface AccountTransactionRepository extends JpaRepository<AccountingTransaction, UUID>
{
    List<AccountingTransaction> findAccountingTransactionByinvoiceId( UUID invoiceId );
}
