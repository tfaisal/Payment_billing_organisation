package com.abcfinancial.api.billing.subscriptionmanagement.agreement.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain.AgreementMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgreementMemberRepository extends JpaRepository<AgreementMember, UUID>
{
    List<AgreementMember> findByAgreementNumber( String agreementNumber );

    Optional<List<AgreementMember>> findByAgreementNumberAndIsActive( String agreementNumber, boolean isActive );

    Optional<AgreementMember> findByAgreementNumberAndPrimary( String agreementNumber, boolean primary );
}
