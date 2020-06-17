package com.abcfinancial.api.billing.scheduler.jobs;

import com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer.SubscriptionExpireGenerator;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.SubscriptionService;
import com.abcfinancial.api.billing.scheduler.EventScheduler;
import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionDeleteJob;
import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionExpired;
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
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@ToString
@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution

public class SubscriptionExpireJob extends QuartzJobBean
{
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private EventScheduler eventScheduler;
    private UUID locationId;
    private UUID accountId;
    private UUID memberId;
    private UUID subscriptionId;
    private String subExpDate;
    private boolean freezeSubscriptionRequest;
    @Autowired
    private SubscriptionExpireGenerator subscriptionExpireGenerator;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private SubscriptionDeleteJob subscriptionDeleteJob;

    @Override
    protected void executeInternal( JobExecutionContext context ) throws JobExecutionException
    {
        log.info( "Generating subscription expire event data LocationId {} MemberId {} SubExpDate {} SubscriptionId {} ", getLocationId(), getMemberId(), getSubExpDate(),
            getSubscriptionId() );
        LocalDate subExpirationDate = LocalDate.parse( subExpDate );
        SubscriptionExpired subscriptionExpired = new SubscriptionExpired( getLocationId(), getAccountId(), getSubscriptionId(), subExpirationDate, isFreezeSubscriptionRequest() );
        log.trace( "Subscription Expire event data FOR NEW JOB {}", subscriptionExpired );
        Optional<Subscription> subscription = subscriptionRepository.findById( subscriptionExpired.getSubscriptionId() );
        subscriptionDeleteJob.deleteJob( subscription, false );
        subscriptionExpireGenerator.send( subscriptionExpired );
    }
}

