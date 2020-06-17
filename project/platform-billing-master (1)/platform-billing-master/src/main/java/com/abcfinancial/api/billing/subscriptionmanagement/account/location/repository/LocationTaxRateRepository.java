package com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationTaxRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository

public interface LocationTaxRateRepository extends JpaRepository<LocationTaxRate, UUID>
{
    
    @Query( value = "select * from location_tax_rate where loc_id = :locationId and itca_id = :itemCategoryId AND ltr_version = ( SELECT MAX( ltr_version ) FROM location_tax_rate WHERE loc_id = :locationId AND itca_id = :itemCategoryId )", nativeQuery = true )
    Optional<LocationTaxRate> getLocationTaxRateOfMaxVersionForLocationAndCategoryId( @Param( "locationId" )UUID locationId, @Param( "itemCategoryId" )UUID itemCategoryId );

    Optional<LocationTaxRate> findByLocationIdAndItemCategoryId( UUID locationId, UUID itemCategoryId );
    
    @Query( value = "select * from location_tax_rate where loc_id = :locId and itca_id is null AND ltr_version = ( select MAX( ltr_version ) from location_tax_rate where loc_id = :locId and itca_id is null  )", nativeQuery = true )
    Optional<LocationTaxRate> getLocationTaxRateForMaxVersion( @Param ( "locId" ) UUID locId );

    @Query( value = "select * from location_tax_rate where loc_id = :locId and ltr_deactivated IS NULL", nativeQuery = true )
    Page<LocationTaxRate> getByLocationId( @Param( "locId" ) UUID locationId, Pageable pageable );

}
