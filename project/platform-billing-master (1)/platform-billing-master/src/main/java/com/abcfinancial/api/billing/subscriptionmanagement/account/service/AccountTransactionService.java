package com.abcfinancial.api.billing.subscriptionmanagement.account.service;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.AccountingTransaction;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountTransactionRepository;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.AccountingTransactionVO;
import com.abcfinancial.api.billing.generalledger.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.abcfinancial.api.billing.utility.common.MessageUtils.ERROR_MESSAGE_ACCOUNTING_TRANSACTION_NOT_FOUND;

@Service

@Slf4j
@Transactional( propagation = Propagation.REQUIRED )

public class AccountTransactionService
{
    @Autowired
    private AccountTransactionRepository accountTransactionRepository;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @Transactional( propagation = Propagation.REQUIRED )
    public AccountingTransactionVO saveAccountTransaction( AccountingTransactionVO accountingTransactionVO )
    {
        validateMendatoryField( accountingTransactionVO );
        AccountingTransaction accountingTransaction = ModelMapperUtils.map( accountingTransactionVO, AccountingTransaction.class );
        accountingTransaction = accountTransactionRepository.save( accountingTransaction );
        return ModelMapperUtils.map( accountingTransaction, AccountingTransactionVO.class );
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public AccountingTransactionVO updateAccountTransaction( AccountingTransaction accountingTransactionVO )
    {
        validateUpdateField( accountingTransactionVO );
        accountTransactionRepository.save( accountingTransactionVO );
        return ModelMapperUtils.map( accountingTransactionVO, AccountingTransactionVO.class );
    }

    @Transactional( readOnly = true )
    public List<AccountingTransaction> findAccountingTransactionByInvoiceId( UUID invoiceId )
    {
        List<AccountingTransaction> accountingTransactions = accountTransactionRepository.findAccountingTransactionByinvoiceId( invoiceId );
        log.debug( "account transactions: {}", accountingTransactions );
        if( accountingTransactions.isEmpty() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), PaymentService.class,
                applicationConfiguration.getValue( ERROR_MESSAGE_ACCOUNTING_TRANSACTION_NOT_FOUND ) ) );
        }
        return accountingTransactions;
    }

    private void validateMendatoryField( AccountingTransactionVO accountingTransactionVO )
    {
        if( null == accountingTransactionVO.getItemId() || accountingTransactionVO.getItemId().toString().length() <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AccountingTransactionVO.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNTING_ITEMID ) ) );
        }
    }

    private void validateUpdateField( AccountingTransaction accountingTransactionVO )
    {
        if( null != accountingTransactionVO.getItemId() && accountingTransactionVO.getItemId().toString().length() <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AccountingTransactionVO.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNTING_ITEMID ) ) );
        }
    }
}
