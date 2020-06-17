package com.abcfinancial.api.billing.subscriptionmanagement.account.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository

public interface AccountRepository extends JpaRepository<Account, UUID>
{
    
    @Query( "SELECT s FROM Account s JOIN PaymentMethod pm on  s.accountId = pm.accountId and s.location = pm.locationId join LocationAccount la on la.accountId = s.accountId and la.locaccId.location = s.location WHERE s.location = ( :id ) and pm.active = ( :active )" )
    Account getAccountDetailsByLocId( @Param( "id" ) UUID id, @Param( "active" ) boolean active );
    
    @Query( value = "select * from account where accn_id = :accn_id", nativeQuery = true )
    Optional<Account> getDetailsByAccountId( @Param( "accn_id" ) UUID accountId );
    
    @Query( value = "select * from account where accn_id = :accn_id", nativeQuery = true )
    Account getDetailsAccountId( @Param( "accn_id" ) UUID accountId );

    @Query( value = "select * from account where accn_id = ( select accn_id from payment_method where pame_id = :accn_id)", nativeQuery = true )
    Account getDetailsByPaymentMethodId( @Param( "accn_id" ) UUID accountId );
}
