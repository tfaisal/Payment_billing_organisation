package com.abcfinancial.api.billing.subscriptionmanagement.kafka.consumer;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.SubscriptionService;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.SubscriptionVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.AccountingRevenue;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.AccountingTransaction;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Status;
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.AccountTransactionService;
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.AccountingRevenueService;
import com.abcfinancial.api.billing.utility.common.CommonUtil;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.AccountingTransactionVO;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.InvoiceItemVO;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.InvoiceVO;
import com.abcfinancial.api.billing.generalledger.payment.domain.Brand;
import com.abcfinancial.api.billing.generalledger.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service

public class AccountUpdateListner
{
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private AccountTransactionService accountTransactionService;
    @Autowired
    private AccountingRevenueService accountingRevenueService;
    @Autowired
    SubscriptionService subscriptionService;

    @KafkaListener( topics = "payment-approved" )
    public void receivePaymentApprDetails( @Payload PaymentVO paymentVo ) throws Exception
    {
        log.info( "Receiving Payment Approved Data {}", paymentVo );
        UUID paymentId = paymentVo.getId();
        UUID locationId = paymentVo.getLocationId();
        Brand paymentType = paymentVo.getPaymentType();
        LocalDate paymentReceivedDate = paymentVo.getPayReceivedDate().toLocalDate();
        LocalDateTime paymentReceivedDateTime = paymentReceivedDate.atStartOfDay();
        List<AccountingTransaction> accountingTransactions = null;
        List<InvoiceVO> invoicesList = paymentVo.getInvoices();
        AccountingTransaction accountingTransaction = null;
        for( InvoiceVO invoice : invoicesList )
        {
            UUID invoiceId = invoice.getId();
            accountingTransactions = accountTransactionService.findAccountingTransactionByInvoiceId( invoiceId );
            if( accountingTransactions.size() > 0 )
            {
                List<InvoiceItemVO> invoiceItemVOS = invoice.getItems();
                AccountingRevenue accountingRevenue;
                for( int i = 0; i < invoiceItemVOS.size(); i++ )
                {
                    InvoiceItemVO invoiceItemVO = invoiceItemVOS.get( i );
                    log.debug( "Saving Acccounting TransactionData Recurring" );
                    accountingTransaction = accountingTransactions.get( i );
                    accountingTransaction.setStatus( Status.COMPLETED );
                    accountingTransaction.setPaymentId( paymentId );
                    accountingTransaction.setPaymentDestination( "ABC" );
                    accountingTransaction.setPaymentType( paymentType );
                    accountTransactionService.updateAccountTransaction( accountingTransaction );
                    accountingRevenue = new AccountingRevenue();
                    accountingRevenue.setLocationId( locationId );
                    accountingRevenue.setNetPrice( invoiceItemVO.getPrice() );
                    accountingRevenue.setItemType( invoiceItemVO.getType().name() );
                    accountingRevenue.setItemId( invoiceItemVO.getItemId() );
                    accountingRevenue.setItemVersion( invoiceItemVO.getVersion() );
                    accountingRevenue.setQuantitySold( invoiceItemVO.getQuantity() );
                    accountingRevenue.setRevStartDate( paymentReceivedDateTime );
                    accountingRevenue.setMId( invoice.getMemberId() );
                    accountingRevenue.setInvoiceId( invoiceId );
                    accountingRevenue.setAccId( invoice.getAccountId() );
                    accountingRevenue.setInviId( invoiceItemVO.getId() );
                    accountingRevenueService.save( accountingRevenue );
                    log.debug( "Accounting Revenue Updated !" );
                }
            }
        }
    }

    @KafkaListener( topics = "payment-approved-purchase" )
    public void receivePurchasePaymentApprDetail( @Payload PaymentVO paymentApproved )
    {
        log.debug( "Receiving Purchase Payment Approved Data {}", paymentApproved );
        UUID paymentId = paymentApproved.getId();
        UUID locationId = paymentApproved.getLocationId();
        Brand paymentType = paymentApproved.getPaymentType();
        LocalDate paymentReceivedDate = paymentApproved.getPayReceivedDate().toLocalDate();
        LocalDateTime paymentReceivedDateTime = paymentReceivedDate.atStartOfDay();
        List<InvoiceVO> invoicesList = paymentApproved.getInvoices();
        for( InvoiceVO invoice : invoicesList )
        {
            UUID invoiceId = invoice.getId();
            AccountingRevenue accountingRevenue = null;
            AccountingTransactionVO accountingTransactionVO = null;
            SubscriptionVO subscriptionVO = invoice.getSubscription();
            List<InvoiceItemVO> invoiceItemVOS = invoice.getItems();
            for( InvoiceItemVO invoiceItemVO : invoiceItemVOS )
            {
                log.debug( "Saving Acccounting TransactionData" );
                accountingTransactionVO = new AccountingTransactionVO();
                accountingTransactionVO.setLocationId( invoice.getLocationId() );
                accountingTransactionVO.setTotalPrice( invoice.getTotalAmount() );
                accountingTransactionVO.setQuantity( invoiceItemVO.getQuantity() );
                accountingTransactionVO.setTotalDiscountAmount( invoiceItemVO.getDiscountAmount() );
                accountingTransactionVO.setTotalNetPrice( invoiceItemVO.getPrice() );
                accountingTransactionVO.setTotalTax( invoiceItemVO.getTaxAmount() );
                accountingTransactionVO.setInvoiceId( invoice.getId() );
                accountingTransactionVO.setInvoiceItemId( invoiceItemVO.getItemId() );
                accountingTransactionVO.setItemId( invoiceItemVO.getItemId() );
                accountingTransactionVO.setVersion( invoiceItemVO.getVersion() );
                accountingTransactionVO.setMemberId( invoice.getMemberId() );
                accountingTransactionVO.setAccountId( invoice.getAccountId() );
                accountingTransactionVO.setEmployeeId( invoice.getSalesEmployeeId() );
                accountingTransactionVO.setStatus( Status.COMPLETED );
                accountingTransactionVO.setPaymentId( paymentId );
                accountingTransactionVO.setPaymentDestination( "ABC" );
                accountingTransactionVO.setPaymentType( paymentType );
                accountTransactionService.saveAccountTransaction( accountingTransactionVO );
                accountingRevenue = new AccountingRevenue();
                accountingRevenue.setLocationId( locationId );
                accountingRevenue.setNetPrice( invoiceItemVO.getPrice() );
                accountingRevenue.setItemType( invoiceItemVO.getType().name() );
                accountingRevenue.setItemId( invoiceItemVO.getItemId() );
                accountingRevenue.setItemVersion( invoiceItemVO.getVersion() );
                accountingRevenue.setQuantitySold( invoiceItemVO.getQuantity() );
                accountingRevenue.setRevStartDate( paymentReceivedDateTime );
                if( !subscriptionVO.isOpenEnded() )
                {
                    accountingRevenue.setRevExpirationDate( CommonUtil.convertLocDateToLocDateTime( subscriptionService.calculateSubExpirationDate( subscriptionVO ) ) );
                    LocalDate startDate = paymentReceivedDate,
                        calculateExpiredDate = subscriptionService.calculateSubExpirationDate( subscriptionVO );
                    Period period = Period.between( startDate, calculateExpiredDate );
                    accountingRevenue.setRevPeriod( period.getDays() );
                }
                accountingRevenue.setMId( invoice.getMemberId() );
                accountingRevenue.setInvoiceId( invoiceId );
                accountingRevenue.setAccId( invoice.getAccountId() );
                accountingRevenue.setInviId( invoiceItemVO.getId() );
                accountingRevenue.setLocationId( locationId );
                accountingRevenueService.save( accountingRevenue );
                log.debug( "Accounting Revenue Updated !" );
            }
        }
    }
}
