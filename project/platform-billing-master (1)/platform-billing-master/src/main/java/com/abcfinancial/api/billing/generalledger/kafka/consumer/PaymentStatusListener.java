package com.abcfinancial.api.billing.generalledger.kafka.consumer;

import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.payment.service.PaymentService;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentApprovedVO;
import com.abcfinancial.api.billing.generalledger.statements.repository.AccountSummaryRepository;
import com.abcfinancial.api.billing.generalledger.statements.repository.PaymentMethodAccountRepository;
import com.abcfinancial.api.billing.generalledger.statements.repository.StatementRepository;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service

public class PaymentStatusListener
{
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private StatementRepository statementRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private AccountSummaryRepository accountSummaryRepository;
    @Autowired
    private PaymentMethodAccountRepository paymentMethodAccountRepository;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @KafkaListener( topics = "payment-status" )
    public void paymentStatusApprovedListener( PaymentApprovedVO paymentApprovedVO )
    {
        log.debug( "paymentStatusApprovedListener start paymentApprovedVO Data{}", paymentApprovedVO );
        paymentService.consumePaymentStatus( paymentApprovedVO.getReferenceId() );
    }
}
