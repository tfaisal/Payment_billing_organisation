package com.abcfinancial.api.billing.generalledger.kafka.consumer;

import com.abcfinancial.api.billing.subscriptionmanagement.account.service.AccountTransactionService;
import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.abcfinancial.api.billing.generalledger.invoice.service.InvoiceService;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.InvoiceItemRequestVO;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.PayorInvoiceRequestVO;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.statements.service.StatementService;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service

public class InvoiceListener
{
    @Autowired
    AccountTransactionService accountTransactionService;
    @Autowired
    SubscriptionRepository subscriptionRepository;
    @Autowired
    private StatementService statementService;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private InvoiceService invoiceService;

    @KafkaListener( topics = "invoice-created", groupId = "payorInvoice" )
    public void createPayorInvoice( @Payload Invoice invoice ) throws IOException {
            log.debug( "invoiceCreateStatement - receiving invoice-transaction  = '{}'" + invoice );
            PayorInvoiceRequestVO payorInvoiceRequestVO = ModelMapperUtils.map( invoice, PayorInvoiceRequestVO.class );
              if( Objects.nonNull( invoice.getPaymentMethodId() ) )
               {
                 payorInvoiceRequestVO.setAccountId( invoice.getPaymentMethodId() );
                }
            if( invoice.getSubscription( ).getSubId( ) != null ) {
                payorInvoiceRequestVO.setSubscriptionId( invoice.getSubscription( ).getSubId( ) );
            }
            List<InvoiceItemRequestVO> payorItems = new ArrayList<>( );
            invoice.getItems( ).forEach( invoiceItem -> {
                InvoiceItemRequestVO invoiceItemRequestVO = new InvoiceItemRequestVO( );
                if( invoiceItem.getId( ) != null ) {
                    invoiceItemRequestVO.setId( invoiceItem.getId( ) );
                }
                if( invoiceItem.getLocId( ) != null ) {
                    invoiceItemRequestVO.setLocId( invoiceItem.getLocId( ) );
                }
                if( invoiceItem.getDiscountCode( ) != null ) {
                    invoiceItemRequestVO.setDiscountCode( invoiceItem.getDiscountCode( ) );
                }
                if( invoiceItem.getDiscountAmount( ) != null ) {
                    invoiceItemRequestVO.setDiscountAmount( invoiceItem.getDiscountAmount( ) );
                }
                if( invoiceItem.getItemCategoryId( ) != null ) {
                    invoiceItemRequestVO.setItemCategoryId( invoiceItem.getItemCategoryId( ) );
                }
                invoiceItemRequestVO.setItemName( invoiceItem.getItemName( ) );
                invoiceItemRequestVO.setPrice( invoiceItem.getPrice( ) );
                invoiceItemRequestVO.setVersion( invoiceItem.getVersion( ) );
                invoiceItemRequestVO.setTaxAmount( invoiceItem.getTaxAmount( ) );
                invoiceItemRequestVO.setAmountRemaining( invoiceItem.getAmountRemaining( ) );
                invoiceItemRequestVO.setType( invoiceItem.getType( ) );
                invoiceItemRequestVO.setQuantity( invoiceItem.getQuantity( ) );
                invoiceItemRequestVO.setItemId( invoiceItem.getItemId( ) );
                invoiceItemRequestVO.setTaxCode( invoiceItem.getTaxCode( ) );
                payorItems.add( invoiceItemRequestVO );
            }
            );
            payorInvoiceRequestVO.setItems( payorItems );
            if( invoice.getTransactionId( ) != null ) {
                payorInvoiceRequestVO.setAvaTransactionId( invoice.getTransactionId( ) );
            }
            payorInvoiceRequestVO.setInvoiceDate( invoice.getInvoiceDate( ) );
            invoiceService.createPayorInvoice( payorInvoiceRequestVO );
        }
    }


