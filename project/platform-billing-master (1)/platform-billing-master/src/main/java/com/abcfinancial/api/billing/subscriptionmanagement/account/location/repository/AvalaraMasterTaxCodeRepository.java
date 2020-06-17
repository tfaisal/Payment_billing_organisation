package com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.AvalaraMasterTaxCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public interface AvalaraMasterTaxCodeRepository extends JpaRepository<AvalaraMasterTaxCode, UUID>
{
    
    @Query( value = "select * from ava_master_tax_code", nativeQuery = true )
    List<AvalaraMasterTaxCode> getAll( Pageable pageable );

    Optional<AvalaraMasterTaxCode> findByTaxCode( String taxCode );
}
