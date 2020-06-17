package com.abcfinancial.api.billing.scheduler.jobs;

import com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer.SubscriptionCancelGenerator;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.scheduler.EventScheduler;
import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionCancel;
import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionDeleteJob;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@ToString
@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution

public class SubscriptionCancelJob extends QuartzJobBean
{
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionCancelGenerator cancelGenerator;
    @Autowired
    private SubscriptionDeleteJob subscriptionDeleteJob;
    private List<UUID> memberIdList;
    private UUID subscriptionId;
    private UUID scheduleInvoicesId;
    private String subCancelDate;
    @Autowired
    private EventScheduler eventScheduler;

    @Override
    protected void executeInternal( JobExecutionContext context ) throws JobExecutionException
    {
        log.info( "Subscription cancelation is going on for scheduleInvoicesId {}", scheduleInvoicesId );
        Optional<Subscription> subscription = subscriptionRepository.findById( subscriptionId );
        subscriptionDeleteJob.deleteJob( subscription, true );
        SubscriptionCancel subscriptionCancel =
            new SubscriptionCancel( getMemberIdList(), getSubscriptionId(), getScheduleInvoicesId(), LocalDate.parse( getSubCancelDate() ) );
        cancelGenerator.send( subscriptionCancel );
    }
}

