package com.abcfinancial.api.billing.scheduler.schedules;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.scheduler.EventScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component

public class SubscriptionDeleteJob
{
    @Autowired
    private EventScheduler eventScheduler;
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public void deleteJob( Optional<Subscription> subscription, boolean flag )
    {
        boolean cancelScheduledInvoicesStatus = false;
        boolean cancelScheduleRenewalStatus = false;
        boolean cancelScheduleExpireStatus = false;
        UUID scheduleRenewalId = null;
        UUID scheduleExpireId = null;
        UUID scheduleInvoiceId = null;
        if( subscription.isPresent() )
        {
            if( null != subscription.get().getScheduleInvoicesId() )
            {
                cancelScheduledInvoicesStatus = eventScheduler.cancelScheduledInvoices( subscription.get().getScheduleInvoicesId() );
                scheduleInvoiceId = subscription.get().getScheduleInvoicesId();
                subscription.get().setScheduleInvoicesId( null );
            }
            if( null != subscription.get().getSubExpiredEventId() )
            {
                cancelScheduleExpireStatus = eventScheduler.cancelScheduledExpire( subscription.get().getSubExpiredEventId() );
                scheduleExpireId = subscription.get().getSubExpiredEventId();
                subscription.get().setScheduleInvoicesId( null );
            }
            if( null != subscription.get().getSubScheduleRenewalId() && flag )
            {
                cancelScheduleRenewalStatus = eventScheduler.cancelRenewSubShcedule( subscription.get().getSubScheduleRenewalId() );
                //Optional<Subscription> renewSubscription = subscriptionRepository.findByRenewRefId( subscription.get().getSubId() );
                Optional<Subscription> renewSubscription = subscriptionRepository.findBySubPrevRefId( subscription.get().getSubId() );
                if( renewSubscription.isPresent() )
                {
                    renewSubscription.get().setSubCancellationDate( subscription.get().getSubCancellationDate() );
                    scheduleRenewalId = subscription.get().getSubScheduleRenewalId();
                    subscriptionRepository.save( renewSubscription.get() );
                }
                subscription.get().setSubScheduleRenewalId( null );
            }
            subscription.get().setActive( false );
            subscriptionRepository.save( subscription.get() );
        }
        log.info( "****canceled the agreement scheduled quartz job id {} for remaining invoices - Status {}", scheduleInvoiceId, cancelScheduledInvoicesStatus );
        log.info( "****canceled the agreement scheduled quartz job id {} for expire subscription - Status {}", scheduleExpireId, cancelScheduleExpireStatus );
        log.info( "****canceled the agreement scheduled quartz job id {} for expire renew subscription - Status {}", scheduleRenewalId, cancelScheduleRenewalStatus );
    }
}
