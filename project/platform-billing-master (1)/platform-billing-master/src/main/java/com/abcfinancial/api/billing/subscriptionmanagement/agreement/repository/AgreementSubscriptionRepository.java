package com.abcfinancial.api.billing.subscriptionmanagement.agreement.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain.AgreementSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgreementSubscriptionRepository extends JpaRepository<AgreementSubscription, UUID>
{
    List<AgreementSubscription> findByAgrmSuIdAgreementId( UUID agreementId );

    @Query( value = "SELECT s.agrmSuId.subId FROM AgreementSubscription s WHERE s.agrmSuId.agreementId = :agreementId" )
    List<UUID> findSubId( @Param( "agreementId" ) UUID agreementId );

    @Query( value = " SELECT * FROM agreement_subscription  where agrm_id=:agreementId and sub_id=:subscriptionId ", nativeQuery = true )
    AgreementSubscription findAgreementSubscriptionByAgrIdAndSubId( @Param( "agreementId" ) UUID agreementId, @Param( "subscriptionId" ) UUID subscriptionId );

    @Query( value = " SELECT * FROM agreement_subscription  where agrm_id=:agreementId limit 1", nativeQuery = true )
    AgreementSubscription findAgreementByAgrId( @Param( "agreementId" ) UUID agreementId );

    List<AgreementSubscription> findByAgrmSuIdAgreementIdOrderByPrimaryDesc( UUID agreementId );
}
