package com.abcfinancial.api.billing.scheduler.jobs;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.RenewType;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.SubscriptionService;
import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.abcfinancial.api.billing.generalledger.invoice.service.InvoiceService;
import com.abcfinancial.api.billing.generalledger.kafka.consumer.InvoiceListener;
import com.abcfinancial.api.billing.generalledger.kafka.producer.InvoiceGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NoHttpResponseException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@ToString
@DisallowConcurrentExecution
@PersistJobDataAfterExecution

public class GenerateInvoiceJob extends QuartzJobBean
{
    private UUID locationId;
    private UUID subscriptionId;
    private List<UUID> memberIdList;
    private boolean isPameIdAccount;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private InvoiceGenerator invoiceGenerator;
    @Autowired
    private InvoiceListener receiver;
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    protected void executeInternal( JobExecutionContext context )
    {
        try
        {
            log.debug( "Generating invoice for {} @ location {} on {}", getSubscriptionId(), getLocationId(), getMemberIdList() );
            Subscription subscription = subscriptionService.getSubscriptionByLocId( getLocationId(), getSubscriptionId() );
            subscription.setActive( true );
            subscriptionRepository.save( subscription );
            if( null == subscription.getRenewType() )

            {
                if( !subscription.isOpenEnded() )
                {
                    if( !subscription.getExpirationDate().isBefore( LocalDate.now( Clock.systemUTC() ) ) )
                    {
                        log.debug( "Generating invoice for term subscription {}", subscription.getSubId() );
                        generateInvoice( subscription );
                    }
                    else
                    {
                        log.info( "Not generating invoice for term subscription {} on the expiration date {}", subscription.getSubId(), subscription.getExpirationDate() );
                    }
                }
                else
                {
                    log.debug( "Generating invoice for open subscription {}", subscription.getSubId() );
                    generateInvoice( subscription );
                }
            }
            else
            {
                subscription.setInvoiceDate( subscription.getRenewInvoiceDate() );
                if( subscription.getRenewType() == RenewType.TERM )
                {
                    if( !subscription.getExpirationDate().isBefore( LocalDate.now( Clock.systemUTC() ) ) )
                    {
                        log.debug( "Generating invoice for renew term subscription {}", subscription.getSubId() );
                        generateInvoice( subscription );
                    }
                    else
                    {
                        log.info( "Not generating invoice for renew term subscription {} on the expiration date {}", subscription.getSubId(), subscription.getExpirationDate() );
                    }
                }
                else
                {
                    log.debug( "Generating invoice for renew open subscription {}", subscription.getSubId() );
                    generateInvoice( subscription );
                }
            }
        }
        catch( Exception exception )
        {
            log.warn( "Unable to generate invoice for subscription " + getSubscriptionId(), exception );
        }
    }

    public void generateInvoice( Subscription subscription ) throws NoHttpResponseException
    {
        Invoice invoice = invoiceService.createInvoice( subscription, isPameIdAccount );

        invoiceGenerator.send( invoice );
    }
}
