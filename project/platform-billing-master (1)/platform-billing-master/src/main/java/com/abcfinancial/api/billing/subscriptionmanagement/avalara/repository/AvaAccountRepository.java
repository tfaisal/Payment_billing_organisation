
package com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface AvaAccountRepository extends JpaRepository<AvaAccount, Long> {
    List<AvaAccount> findByLocationId( UUID locationId );
}
