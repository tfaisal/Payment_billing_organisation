package com.abcfinancial.api.billing.generalledger.fee.repository;

import com.abcfinancial.api.billing.generalledger.fee.domain.Fee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeeRepository extends JpaRepository<Fee, UUID>
{

    Optional<Fee> findFeeByAccountIdAndFeeTypeAndFeeTransactionTypeAndFeeValueTypeAndDeactivated( UUID uuid, String feeType, String feeTransactionType, String feeValueType,
        LocalDateTime localDateTime );

    List<Fee> findFeeByAccountIdAndDeactivatedIsNull( UUID uuid );

    List<Fee> findFeeByAccountIdAndFeeTransactionType( UUID uuid, String feeTransactionType, Pageable pageable );

    List<Fee> findFeeByAccountId( UUID uuid, Pageable pageable );

    Optional<Fee> getDetailsByFeeId( @Param( "fee_id" ) UUID feeId );
}
