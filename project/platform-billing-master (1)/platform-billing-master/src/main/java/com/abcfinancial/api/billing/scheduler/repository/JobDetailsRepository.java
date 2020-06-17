package com.abcfinancial.api.billing.scheduler.repository;

import com.abcfinancial.api.billing.scheduler.domain.JobDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobDetailsRepository extends JpaRepository<JobDetails, UUID>
{
    JobDetails findByDescriptionContaining( String value );
}
