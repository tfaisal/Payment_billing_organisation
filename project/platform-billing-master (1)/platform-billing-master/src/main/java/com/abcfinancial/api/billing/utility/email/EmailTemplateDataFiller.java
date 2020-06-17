package com.abcfinancial.api.billing.utility.email;

import com.abcfinancial.api.billing.utility.email.valueobject.PurchaseEmailVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.SubscriptionItem;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;

@Service
@RequiredArgsConstructor

public class EmailTemplateDataFiller
{
    @Value( "${abcfinancial.email.template.purchase-request}" )
    private String purchaseRequestEmailTemplate;
    private final SubscriptionRepository subscriptionRepository;

    public void fillPurchaseEmailData( PurchaseEmailVO purchaseEmailVO )
    {
        purchaseEmailVO.setTemplate( purchaseRequestEmailTemplate );
        purchaseEmailVO.setToAddress( purchaseEmailVO.getUserEmail( ) );
        subscriptionRepository.findByIdFetchItemsEagerly( purchaseEmailVO.getSubscriptionId( ) ).ifPresent( subscription -> {
            purchaseEmailVO.setMembership( subscription.getName( ) );
            BigDecimal installmentPrice = subscription.getItems( ).stream( )
                                                      .map( SubscriptionItem::getPrice )
                                                      .reduce( BigDecimal.ZERO, BigDecimal::add );
            purchaseEmailVO.setInstallmentPrice( NumberFormat.getCurrencyInstance( ).format( installmentPrice ) );
            purchaseEmailVO.setFrequency( subscription.getFrequency( ).getStringRepresentation( ) );
            purchaseEmailVO.setBillingCycles( String.valueOf( subscription.getDuration( ) ) );
        } );
    }
}
