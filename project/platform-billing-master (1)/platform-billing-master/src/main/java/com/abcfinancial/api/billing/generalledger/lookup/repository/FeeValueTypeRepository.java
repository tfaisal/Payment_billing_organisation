package com.abcfinancial.api.billing.generalledger.lookup.repository;

import com.abcfinancial.api.billing.generalledger.lookup.domain.FeeValueType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeValueTypeRepository extends JpaRepository<FeeValueType, String>
{
}
