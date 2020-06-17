package com.abcfinancial.api.billing.generalledger.statements.service;

import com.abcfinancial.api.billing.generalledger.statements.domain.Balance;
import com.abcfinancial.api.billing.generalledger.statements.repository.BalanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service

public class BalanceService
{
    @Autowired
    private BalanceRepository balanceRepository;

    @Transactional
    public void updateBalance( UUID accountId, UUID paymentMethodId, BigDecimal newBalance )
    {
        Balance balance;
        if( paymentMethodId == null )
        {
            balance = balanceRepository.findByAccountIdAndDeactivatedAndPaymentMethodIdNull( accountId, null );
        }
        else
        {
            balance = balanceRepository.findByPaymentMethodIdAndDeactivated( paymentMethodId, null );
        }
        if( newBalance.compareTo( balance.getAmount() ) != 0 )
        {
            Balance newBalanceObj = balance.clone();
            newBalanceObj.setAmount( newBalance );
            newBalanceObj.setBalanceDate( LocalDateTime.now( Clock.systemUTC() ) );
            balanceRepository.delete( balance );
            newBalanceObj.setId( null );
            balanceRepository.save( newBalanceObj );
        }
    }

    @Transactional
    public Balance createBalance( UUID accountId, BigDecimal balanceAmt )
    {
        Balance balance = new Balance();
        balance.setAccountId( accountId );
        balance.setAmount( balanceAmt );
        balance.setBalanceDate( LocalDateTime.now( Clock.systemUTC() ) );
        balance.setCreated( LocalDateTime.now( Clock.systemUTC() ) );
        balance.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        return balanceRepository.save( balance );
    }

    @Transactional
    public Balance createPaymentMethodBalance( UUID accountId, UUID paymentMethodId, BigDecimal balanceAmt )
    {
        Balance balance = new Balance();
        balance.setAccountId( accountId );
        balance.setAmount( balanceAmt );
        balance.setPaymentMethodId( paymentMethodId );
        balance.setBalanceDate( LocalDateTime.now( Clock.systemUTC() ) );
        balance.setCreated( LocalDateTime.now( Clock.systemUTC() ) );
        balance.setModified( LocalDateTime.now( Clock.systemUTC() ) );
        return balanceRepository.save( balance );
    }
}
