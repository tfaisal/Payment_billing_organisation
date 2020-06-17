package com.abcfinancial.api.billing.scheduler.jobs;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.SubscriptionService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.UUID;

@Slf4j
@Getter
@Setter
@ToString
@DisallowConcurrentExecution
@PersistJobDataAfterExecution

public class ActiveSubscriptionJob extends QuartzJobBean
{
    private UUID locationId;
    private UUID subscriptionId;
    private UUID memberId;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    protected void executeInternal( JobExecutionContext context ) throws JobExecutionException
    {
        log.debug( "Set Active true  {} @ location {} on {}", getSubscriptionId( ), getLocationId( ), getMemberId( ) );
        try
        {
            Subscription subscription = subscriptionService.getSubscriptionByLocId( getLocationId( ), getSubscriptionId( ) );
            subscription.setActive( true );
            subscriptionRepository.save( subscription );
        }
        catch( Exception exception )
        {
            log.warn( "Set Active failed", exception );
        }
    }
}
