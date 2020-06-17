package com.abcfinancial.api.billing.scheduler.jobs;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.MemberSubscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.SubscriptionService;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.SubscriptionItemVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.SubscriptionVO;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@ToString
@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution

public class CreateSubscriptionJob extends QuartzJobBean
{
    @Autowired
    SubscriptionRepository subscriptionRepository;
    @Autowired
    SubscriptionService subscriptionService;
    private List<UUID> memberId;
    private UUID subscriptionId;
    private BigDecimal freezeAmount;

    @Override
    protected void executeInternal( JobExecutionContext context ) throws JobExecutionException
    {
        Subscription subscription = subscriptionRepository.findById( subscriptionId ).orElseThrow( () -> new ErrorResponse(
            new DataIntegrityViolationResponse( HttpStatus.NOT_FOUND.value(), Subscription.class, "Requested Subsdcription not found  with id " + subscriptionId ) ) );
        SubscriptionVO subscriptionVO = ModelMapperUtils.map( subscription, SubscriptionVO.class );
        List<UUID> memberIdList = new ArrayList<>();
        for( MemberSubscription memberSubscription : subscription.getMemberSubscriptionList() )
        {
            memberIdList.add( memberSubscription.getId().getMemId() );
        }

        subscriptionVO.setMemberIdList( memberIdList );
        subscriptionVO.setAccountId( subscription.getAccount().getAccountId() );
        subscriptionVO.setFreezeSubId( subscriptionId );
        List<SubscriptionItemVO> subscriptionItems = subscriptionVO.getItems();
        for( int index = 0; index < subscriptionItems.size(); index++ )
        {
            BigDecimal freeAmount = subscription.getFreezeAmount();
            if( null != freeAmount )
            {
                if( index == 0 )
                {
                    subscriptionItems.get( index ).setPrice( freeAmount );
                }
            }
            else
            {
                subscriptionItems.get( index ).setPrice( new BigDecimal( 0 ) );
            }
        }
        if( null != subscription.getFreezeAmount() )
        {
            subscriptionVO.setItems( subscriptionItems );
        }
        if( null != subscription.getFreezeEndDate() )
        {
            subscriptionVO.setExpirationDate( subscription.getFreezeEndDate().toLocalDate() );
        }
        subscriptionService.createSubscription( subscriptionVO, false );

    }
}
