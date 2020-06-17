package com.abcfinancial.api.billing.scheduler.jobs;

import com.abcfinancial.api.billing.subscriptionmanagement.account.member.repository.MemberCreationRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer.SubscriptionAutoRenewGenerator;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.scheduler.EventScheduler;
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

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@ToString
@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution

public class SubscriptionAutoRenewJob extends QuartzJobBean
{
    private UUID memberId;
    private UUID subscriptionId;
    private UUID locationId;
    private boolean isPameIdAccount;
    @Autowired
    private EventScheduler eventScheduler;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private MemberCreationRepository memberCreationRepository;
    @Autowired
    private SubscriptionAutoRenewGenerator subscriptionAutoRenewGenerator;

    @Override
    protected void executeInternal( JobExecutionContext context ) throws JobExecutionException
    {
        log.debug( "Subscription auto renew Job for subscription Id {} memberId {} ", subscriptionId, memberId );
        Optional<Subscription> subscription = subscriptionRepository.findById( subscriptionId );

        if( subscription.isPresent() )
        {
            subscription.get().setPameIdAccount( isPameIdAccount );
            subscriptionAutoRenewGenerator.send( subscription.get() );
        }
    }
}
