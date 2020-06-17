package com.abcfinancial.api.billing.generalledger.lookup.repository;

import com.abcfinancial.api.billing.generalledger.lookup.domain.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeTypeRepository extends JpaRepository<FeeType, String>
{
}
