package com.abcfinancial.api.billing.scheduler;

import com.abcfinancial.api.billing.scheduler.jobs.*;
import com.abcfinancial.api.billing.scheduler.schedules.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.core.jmx.JobDataMapSupport.newJobDataMap;

@Slf4j
@Service

@RequiredArgsConstructor

public class EventScheduler
{
    private final SchedulerFactoryBean schedulerFactoryBean;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public Optional<UUID> scheduleInvoices( Schedule<SubscriptionDetails> schedule )
    {
        return scheduleJob( Job.GENERATE_INVOICES, schedule );
    }

    private Optional<UUID> scheduleJob( Job job, Schedule schedule )
    {
        Optional<UUID> result = Optional.empty();
        try
        {
            JobDetail jobDetail = createJobDetail( job, schedule );
            log.debug( "Created job {} for {}", job.getGroup(), schedule );
            Trigger trigger = createTrigger( job, schedule );
            if( trigger != null )
            {
                log.debug( "Trigger details = {}", trigger );
                Date firstTrigger = scheduler().scheduleJob( jobDetail, trigger );
                log.debug( "Scheduled trigger and FIRST run will be {}", firstTrigger );
                TriggerKey triggerKey = trigger.getKey();
                JobKey jobKey = jobDetail.getKey();
                log.debug( "Trigger key = {} and job key = {}", triggerKey, jobKey );
                result = Optional.of( jobKey.getName() ).map( UUID::fromString );
            }
        }
        catch( Exception exception )
        {
            log.warn( "Unable to schedule a job", exception );

        }
        return result;
    }

    private JobDetail createJobDetail( Job job, Schedule schedule )
    {
        return newJob()
            .withIdentity( UUID.randomUUID().toString(), job.getGroup() )
            .ofType( job.getClazz() )
            .withDescription( String.format( "%s for %s", job.getGroup(), schedule.getProperties() ) )
            .build();
    }

    private Trigger createTrigger( Job job, Schedule schedule )
    {
        Trigger trigger = null;
        if( job.name().equalsIgnoreCase( "GENERATE_INVOICES" ) )
        {
            trigger = newTrigger()
                .withIdentity( UUID.randomUUID().toString(), job.getGroup() )
                .startAt( Date.from( schedule.getStart().toInstant( ZoneOffset.UTC ) ) )
                .withSchedule( scheduleBuilder( schedule ) )
                .withPriority( 10 )
                .usingJobData( newJobDataMap( objectMapper.convertValue( schedule.getProperties(), Map.class ) ) )
                .build();
        }
        if( job.name().equalsIgnoreCase( "ACCOUNT_LEDGER_EVENT" ) )
        {
            trigger = newTrigger()
                .withIdentity( UUID.randomUUID().toString(), job.getGroup() )
                .startAt( Date.from( schedule.getStart().toInstant( ZoneOffset.UTC ) ) )
                .withSchedule( scheduleBuilder( schedule ) )
                .withPriority( 2 )
                .usingJobData( newJobDataMap( objectMapper.convertValue( schedule.getProperties(), Map.class ) ) )
                .build();
        }
        else if( job.name().equalsIgnoreCase( "SUBSCRIPTION_CANCEL" ) )
        {
            trigger = newTrigger()
                .withIdentity( UUID.randomUUID().toString(), job.getGroup() )
                .startAt( Date.from( schedule.getCancel().atStartOfDay().toInstant( ZoneOffset.UTC ) ) )
                .withSchedule( scheduleBuilder( schedule ) )
                .withPriority( 9 )
                .usingJobData( newJobDataMap( objectMapper.convertValue( schedule.getProperties(), Map.class ) ) )
                .build();
        }
        else if( job.name().equalsIgnoreCase( "SUBSCRIPTION_EXPIRE" ) )
        {
            trigger = newTrigger()
                .withIdentity( UUID.randomUUID().toString(), job.getGroup() )
                .startAt( Date.from( schedule.getExpire().atStartOfDay().toInstant( ZoneOffset.UTC ) ) )
                .withSchedule( scheduleBuilder( schedule ) )
                .withPriority( 10 )
                .usingJobData( newJobDataMap( objectMapper.convertValue( schedule.getProperties(), Map.class ) ) )
                .build();
        }
        else if( job.name().equalsIgnoreCase( "SUBSCRIPTION_AUTO_RENEW" ) )
        {
            trigger = newTrigger()
                .withIdentity( UUID.randomUUID().toString(), job.getGroup() )
                .startAt( Date.from( schedule.getStart().toInstant( ZoneOffset.UTC ) ) )
                .withSchedule( scheduleBuilder( schedule ) )
                .withPriority( 7 )
                .usingJobData( newJobDataMap( objectMapper.convertValue( schedule.getProperties(), Map.class ) ) )
                .build();
        }
        else if( job.name().equalsIgnoreCase( "CREATE_SUBSCRIPTION" ) )
        {
            trigger = newTrigger()
                .withIdentity( UUID.randomUUID().toString(), job.getGroup() )
                .startAt( Date.from( schedule.getStart().toInstant( ZoneOffset.UTC ) ) )
                .withSchedule( scheduleBuilder( schedule ) )
                .withPriority( 8 )
                .usingJobData( newJobDataMap( objectMapper.convertValue( schedule.getProperties(), Map.class ) ) )
                .build();
        }
        else if( job.name().equalsIgnoreCase( "SETTLEMENT_EVENT" ) )
        {
            trigger = newTrigger()
                .withIdentity( UUID.randomUUID().toString(), job.getGroup() )
                .startAt( Date.from( schedule.getStart().toInstant( ZoneOffset.UTC ) ) )
                .withSchedule( scheduleBuilder( schedule ) )
                .withPriority( 1 )
                .usingJobData( newJobDataMap( objectMapper.convertValue( schedule.getProperties(), Map.class ) ) )
                .build();
        }
        else if( job.name().equalsIgnoreCase( "ACTIVE_SUBSCRIPTION" ) )
        {
            trigger = newTrigger()
                .withIdentity( UUID.randomUUID().toString(), job.getGroup() )
                .startAt( Date.from( schedule.getStart().toInstant( ZoneOffset.UTC ) ) )
                .withSchedule( scheduleBuilder( schedule ) )
                .withPriority( 7 )
                .usingJobData( newJobDataMap( objectMapper.convertValue( schedule.getProperties(), Map.class ) ) )
                .build();
        }
        return trigger;
    }

    private Scheduler scheduler()
    {
        return schedulerFactoryBean.getScheduler();
    }

    private ScheduleBuilder<? extends Trigger> scheduleBuilder( Schedule schedule )
    {
        ScheduleBuilder<? extends Trigger> scheduleBuilder;
        if( schedule.isRepeating() )
        {
            log.trace( "Creating schedule builder, frequency = {} and duration = {}", schedule.getFrequency(), schedule.getDuration() );
            scheduleBuilder = PeriodScheduleBuilder.scheduleBuilder( clock )
                                                   .frequency( schedule.getFrequency() )
                                                   .duration( schedule.getDuration() );
        }
        else
        {
            log.trace( "Schedule is not repeating, no schedule builder required" );
            scheduleBuilder = null;
        }
        return scheduleBuilder;
    }

    public Optional<UUID> scheduleAccountLedgerEvent( Schedule<StatementEventDetails> schedule )
    {
        return scheduleJob( Job.ACCOUNT_LEDGER_EVENT, schedule );
    }

    public Optional<UUID> scheduleSettlementEvent( Schedule<StatementEventDetails> schedule )
    {
        return scheduleJob( Job.SETTLEMENT_EVENT, schedule );
    }

    public Optional<UUID> scheduleSubscriptionExpire( Schedule<SubscriptionExpired> schedule )
    {
        return scheduleJob( Job.SUBSCRIPTION_EXPIRE, schedule );
    }

    public Optional<UUID> scheduleSubscriptionCancelation( Schedule<SubscriptionCancel> schedule )
    {
        return scheduleJob( Job.SUBSCRIPTION_CANCEL, schedule );
    }

    public Optional<UUID> subscriptionAutoRenew( Schedule<SubscriptionDetails> schedule )
    {
        return scheduleJob( Job.SUBSCRIPTION_AUTO_RENEW, schedule );
    }

    public boolean cancelScheduledInvoices( UUID id )
    {
        return deleteJob( Job.GENERATE_INVOICES, id );
    }

    private boolean deleteJob( Job job, UUID id )
    {
        boolean cancelled;
        try
        {
            JobDetail jobDetail = scheduler().getJobDetail( JobKey.jobKey( id.toString(), job.getGroup() ) );
            cancelled = scheduler().deleteJob( jobDetail.getKey() );
        }
        catch( Exception exception )
        {
            log.warn( "Unable to cancel job {}", id, exception );
            cancelled = false;
        }
        return cancelled;
    }

    public boolean deleteSubscriptionExpireJob( UUID id )
    {
        return deleteJob( Job.SUBSCRIPTION_EXPIRE, id );
    }

    public boolean deleteAccountLedgerJob( UUID id )
    {
        return deleteJob( Job.ACCOUNT_LEDGER_EVENT, id );
    }

    public boolean deleteSettlementJob( UUID id )
    {
        return deleteJob( Job.SETTLEMENT_EVENT, id );
    }

    public Optional<UUID> scheduleActive( Schedule<SubscriptionDetails> schedule )
    {
        return scheduleJob( Job.ACTIVE_SUBSCRIPTION, schedule );
    }

    public boolean cancelScheduledExpire( UUID subExpiredEventId )
    {
        return deleteJob( Job.SUBSCRIPTION_EXPIRE, subExpiredEventId );
    }

    public boolean cancelSub( UUID subExpiredEventId )
    {
        return deleteJob( Job.SUBSCRIPTION_CANCEL, subExpiredEventId );
    }

    public boolean cancelRenewSubShcedule( UUID subExpiredEventId )
    {
        return deleteJob( Job.SUBSCRIPTION_AUTO_RENEW, subExpiredEventId );
    }

    @Getter

    @RequiredArgsConstructor
    private enum Job
    {
        GENERATE_INVOICES( "generate-invoices", GenerateInvoiceJob.class ),
        ACCOUNT_LEDGER_EVENT( "account-ledger-event", AccountLedgerEventJob.class ),
        SETTLEMENT_EVENT( "settlement-event", SettlementEventJob.class ),
        SUBSCRIPTION_EXPIRE( "subscription-expire", SubscriptionExpireJob.class ),
        SUBSCRIPTION_CANCEL( "subscription-cancel", SubscriptionCancelJob.class ),
        SUBSCRIPTION_AUTO_RENEW( "subscription-auto-renew", SubscriptionAutoRenewJob.class ),
        CREATE_SUBSCRIPTION( "create-subscription", CreateSubscriptionJob.class ),
        ACTIVE_SUBSCRIPTION( "active-subscription", ActiveSubscriptionJob.class );
        private final String group;
        private final Class<? extends QuartzJobBean> clazz;
    }
}
