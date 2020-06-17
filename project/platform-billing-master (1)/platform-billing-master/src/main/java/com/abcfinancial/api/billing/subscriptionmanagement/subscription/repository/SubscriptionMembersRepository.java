package com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.MemberSubscription;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface SubscriptionMembersRepository extends PagingAndSortingRepository<MemberSubscription, UUID>
{
    @Query( value = "SELECT * FROM subscription_member where m_id = :memId ORDER BY mesu_created", nativeQuery = true )
    List<MemberSubscription> findAll( @Param( "memId" ) UUID memId );

    @Query( value = "SELECT * FROM subscription_member where sub_id=:sub_id", nativeQuery = true )
    List<MemberSubscription> findListBySubId( @Param( "sub_id" ) UUID subId );

    @Query( value = "SELECT * FROM subscription_member where sub_id=:subId and m_id=:mId", nativeQuery = true )
    MemberSubscription findMemberSubscriptionBySubIdAndMemId( @Param( "subId" ) UUID subId, @Param( "mId" ) UUID mId );

    List<MemberSubscription> findByIdSubIdAndDeactivated( UUID subId, java.time.LocalDateTime deactivated );
}
