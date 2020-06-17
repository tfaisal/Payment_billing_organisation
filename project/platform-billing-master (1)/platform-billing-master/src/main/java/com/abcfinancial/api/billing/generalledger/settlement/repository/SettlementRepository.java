package com.abcfinancial.api.billing.generalledger.settlement.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.generalledger.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<Settlement, UUID>
{
    Optional<Settlement> findSettlementByAccountIdAndDeactivated( Account account, LocalDateTime deactivaDate );
}
