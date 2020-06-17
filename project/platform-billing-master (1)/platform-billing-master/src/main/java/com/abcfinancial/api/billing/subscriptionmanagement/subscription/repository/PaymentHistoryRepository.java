package com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, UUID>
{
    
}
