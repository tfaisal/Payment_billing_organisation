
package com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAddress;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository

public interface AvaAddressRepository extends JpaRepository<AvaAddress, UUID> {
    AvaAddress findByAvaLocation( AvaLocation avaLocation );
}
