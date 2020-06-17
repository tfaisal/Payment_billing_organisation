
package com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface AvaLocationRepository extends JpaRepository<AvaLocation, Long> {
    AvaLocation findByLocationId( Long addressId );
}
