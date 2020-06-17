package com.abcfinancial.api.billing.subscriptionmanagement.account.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.AccountingRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository

public interface AccountRefundRepository extends JpaRepository<AccountingRefund, UUID>
{
}
