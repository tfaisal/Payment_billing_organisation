package com.abcfinancial.api.billing.subscriptionmanagement.account.member.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.MemberAccountID;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.MemberCreation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public interface MemberCreationRepository extends JpaRepository<MemberCreation, MemberAccountID>
{

    @Query( value = "select * FROM payor_account WHERE accn_id = :accn_id AND m_id = :mem_id AND loc_id = :loc_id", nativeQuery = true )
    MemberCreation findMemberCreation( @Param( "accn_id" ) UUID accnId, @Param( "mem_id" ) UUID memId, @Param( "loc_id" ) UUID locId );

    @Query( value = " select * from payor_account WHERE m_id = :mem_id ORDER BY m_id desc limit 1", nativeQuery = true )
    MemberCreation findMemberById( @Param( "mem_id" ) UUID memId );

    @Query( value = "select * from payor_account WHERE m_id = :mem_id AND loc_id = :loc_id ORDER BY m_id desc limit 1", nativeQuery = true )
    Optional<MemberCreation> findMemberByLocIdAndMemId( @Param( "loc_id" ) UUID locId, @Param( "mem_id" ) UUID memberId );

    @Query( value = "select * from payor_account WHERE m_id = :mem_id AND loc_id = :loc_id AND payor_id = :payor_id", nativeQuery = true )
    Optional<MemberCreation> findMemberByLocIdAndMemIdAndPayorId( @Param( "loc_id" ) UUID locId, @Param( "mem_id" ) UUID memberId, @Param( "payor_id" ) UUID payorId );

    @Query( value = "select * from payor_account WHERE accn_id  = :accountId", nativeQuery = true )
    Optional<MemberCreation> getDetailsByAccountId( @Param( "accountId" ) UUID accountId );
    // JIRA : P3-3015 Start

    @Query( value = "select * from payor_account WHERE m_id = :mem_id ", nativeQuery = true )
    MemberCreation findLocationByMemberId( @Param( "mem_id" ) UUID memberId );

    @Query( value = " select pa from MemberCreation pa where LOWER( pa.memberAccountID.accountId.name ) LIKE LOWER( :name )" )
    List<MemberCreation> getAllPayorAccountsByName( @Param( "name" ) String name, Pageable pageable );

    MemberCreation findByMemberAccountIDPayorId( @Param( "payorId" ) UUID payorId );

    Optional<MemberCreation> findByMemberAccountIDAccountId( Account account );

    MemberCreation findByMemberAccountIDAccountIdAccountId( @Param( "accountId" ) UUID accountId );
}
