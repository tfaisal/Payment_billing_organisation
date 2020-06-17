package com.abcfinancial.api.billing.subscriptionmanagement.agreement.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain.SubscriptionDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionDocumentRepository extends JpaRepository<SubscriptionDocuments, UUID>
{
    List<SubscriptionDocuments> findByIdSubId( UUID uuid );
}
