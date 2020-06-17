
package com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaCompany;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaNexus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface AvaNexusRepository extends JpaRepository<AvaNexus, Long> {
    List<AvaNexus> findByAvaCompany( AvaCompany avaCompany );
}
