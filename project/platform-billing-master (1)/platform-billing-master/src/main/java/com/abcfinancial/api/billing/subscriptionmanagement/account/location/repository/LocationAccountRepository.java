package com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccountID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public interface LocationAccountRepository extends JpaRepository<LocationAccount, LocationAccountID>
{

    @Query( value = "select * from client_account WHERE accn_id  = :accountId", nativeQuery = true )
    Optional<LocationAccount> getDetailsByAccountId( @Param( "accountId" ) UUID accountId );

    @Query( value = "select * from client_account where loc_id = :loc_id", nativeQuery = true )
    Optional<LocationAccount> getDetailsByLocationId( @Param( "loc_id" ) UUID locationId );

    @Query( value = "select * from client_account where loc_id = :loc_id and loca_deactivated IS NULL", nativeQuery = true )
    List<LocationAccount> getAllByLocationIdAndDeActivate( @Param( "loc_id" ) UUID locationId );

    @Query( value = "select ca from LocationAccount ca where LOWER( ca.accountId.name ) LIKE LOWER( :name )" )
    List<LocationAccount> getAllByName( @Param( "name" ) String name, Pageable pageable );

    LocationAccount findByClientId( @Param( "clientId" ) UUID clientId );

    Optional<LocationAccount> findByLocaccIdLocation( UUID locationId );

    LocationAccount findByLocaccIdAccount( @Param( "accountId" ) UUID accountId );

}
