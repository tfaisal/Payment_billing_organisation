package com.abcfinancial.api.billing.generalledger.lookup.repository;

import com.abcfinancial.api.billing.generalledger.lookup.domain.FeeMode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeModeRepository extends JpaRepository<FeeMode, String>
{

}
