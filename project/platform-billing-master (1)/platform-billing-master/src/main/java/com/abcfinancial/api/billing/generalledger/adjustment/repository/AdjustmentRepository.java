package com.abcfinancial.api.billing.generalledger.adjustment.repository;

import com.abcfinancial.api.billing.generalledger.adjustment.domain.Adjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdjustmentRepository extends JpaRepository<Adjustment, UUID>
{
    @Query( value = "select * from account_adjustment where adj_id = :adj_id", nativeQuery = true )
    Adjustment findByAdjustmentId( @Param( "adj_id" ) UUID adjId );
}
