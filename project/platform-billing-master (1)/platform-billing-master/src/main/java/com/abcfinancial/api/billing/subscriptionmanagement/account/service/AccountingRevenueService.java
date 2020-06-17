package com.abcfinancial.api.billing.subscriptionmanagement.account.service;

import com.abcfinancial.api.billing.generalledger.payment.service.PaymentService;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.AccountingRevenue;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountingRevenueRepository;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import com.abcfinancial.api.common.domain.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.abcfinancial.api.billing.utility.common.MessageUtils.ERROR_MESSAGE_ACCOUNTING_REVENUE_NOT_FOUND;

@Slf4j
@Service

public class AccountingRevenueService
{
    @Autowired
    private AccountingRevenueRepository accountingRevenueRepository;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @Transactional( propagation = Propagation.REQUIRED )
    public AccountingRevenue save( AccountingRevenue accountingRevenue )
    {
        return accountingRevenueRepository.save( accountingRevenue );
    }

    @Transactional( readOnly = true )
    public List<AccountingRevenue> findByInvoiceId( UUID invoiceId )
    {
        List<AccountingRevenue> accountingRevenue = accountingRevenueRepository.findAccountingRevenueByinvoiceId( invoiceId );
        log.debug( "Accounting Revenue: {}", accountingRevenue );
        if( accountingRevenue == null )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentService.class,
                applicationConfiguration.getValue( ERROR_MESSAGE_ACCOUNTING_REVENUE_NOT_FOUND ) ) );
        }
        return accountingRevenue;
    }
}
