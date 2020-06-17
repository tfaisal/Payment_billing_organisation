package com.abcfinancial.api.billing.generalledger.statements.repository;

import com.abcfinancial.api.billing.generalledger.statements.domain.Balance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BalanceRepository extends JpaRepository<Balance, UUID>
{
    Balance findByAccountIdAndDeactivatedAndPaymentMethodIdNull( UUID accountId, LocalDateTime dateTime );

    Balance findByPaymentMethodIdAndDeactivated( UUID accountId, LocalDateTime dateTime );
}
