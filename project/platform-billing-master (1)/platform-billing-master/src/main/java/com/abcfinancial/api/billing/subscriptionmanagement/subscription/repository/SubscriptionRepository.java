package com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID>
{

    @Query( "SELECT s FROM Subscription s JOIN FETCH s.items WHERE s.subId = ( :subId )" )
    Optional<Subscription> findByIdFetchItemsEagerly( @Param( "subId" ) UUID id );

    @Query( value = "select * FROM subscription WHERE loc_id = :loc_id AND sub_id = :sub_id ", nativeQuery = true )
    Subscription findSubscription( @Param( "loc_id" ) UUID locId, @Param( "sub_id" ) UUID subId );
    // JIRA -P3-3015

    @Query( value = "SELECT nextval( 'seq_invoice_no' ) as seq_no", nativeQuery = true )
    String getInvoiceSequenceNo();

    @Query( value = "SELECT s FROM Subscription s JOIN FETCH s.items WHERE s.subId = ( :subId ) " )
    Optional<Subscription> findSubscriptionByIdAndRefferalIdNotNull( @Param( "subId" ) UUID subscriptionId );

    @Query( value = "SELECT s FROM Subscription s WHERE s.account.accountId = :accn_id AND s.isActive = false and s.subCancellationDate is not null " )
    Optional<List<Subscription>> findCanceledSubscriptionsByAccountId( @Param( "accn_id" ) UUID accountId );

    Optional<Subscription> findBySubPrevRefId( UUID subId );

    @Query( "SELECT s from Subscription s RIGHT JOIN s.items i WHERE s.subId = :subId" )
    List<Subscription> findPriceBySubId( @Param( "subId" ) UUID subId );

    Optional<Subscription> findBySubId( UUID subId );

}
