package com.abcfinancial.api.billing.subscriptionmanagement.agreement.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgreementRepository extends JpaRepository<Agreement, UUID>
{
    Optional<Agreement> findByAgreementNumber( String agreementNumber );

    Optional<Agreement> findByAgreementNumber( Long agreementNumber );
}
