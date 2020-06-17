package com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface AvaCompanyRepository extends JpaRepository<AvaCompany, Long>
{
    AvaCompany findByAvaAccount( AvaAccount avaAccount );

    AvaCompany findByCompanyId( Long companyId );
}
