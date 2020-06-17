package com.abcfinancial.api.billing.scheduler.service;

import com.abcfinancial.api.billing.scheduler.domain.JobDetails;
import com.abcfinancial.api.billing.scheduler.repository.JobDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class JobDetailsService
{
    @Autowired
    private JobDetailsRepository jobDetailsRepository;

    public UUID getJobActiveId( UUID accountId )
    {
        UUID jobName = null;
        log.info( "GETTING ACTIVE SCHEDULER RECORD {}", accountId );
        JobDetails jobDetails = jobDetailsRepository.findByDescriptionContaining( String.valueOf( accountId ) );
        if( !Objects.isNull( jobDetails ) )
            jobName = UUID.fromString( jobDetails.getJobDetailsID().getJobName() );
        return jobName;
    }
}
