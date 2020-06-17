package com.abcfinancial.api.billing.generalledger.lookup.repository;

import com.abcfinancial.api.billing.generalledger.lookup.domain.FeeTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeTransactionTypeRepository extends JpaRepository<FeeTransactionType, String>
{

}
