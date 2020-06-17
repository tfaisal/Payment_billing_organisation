package com.abcfinancial.api.billing.generalledger.payment.repository;

import com.abcfinancial.api.billing.generalledger.payment.domain.DimeboxProcessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository

public interface ProcessorRepository extends JpaRepository<DimeboxProcessor, UUID>
{
    DimeboxProcessor findProcessorByLocationId( UUID uuid );
    
    @Query( value = "select * from dimebox_processor where loc_id = :loc_id", nativeQuery = true )
    DimeboxProcessor findProcessorUsingLocId( @Param( "loc_id" ) UUID locId );
}
