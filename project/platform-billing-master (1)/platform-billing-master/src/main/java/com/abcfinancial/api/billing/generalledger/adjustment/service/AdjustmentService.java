package com.abcfinancial.api.billing.generalledger.adjustment.service;

import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.generalledger.adjustment.domain.Adjustment;
import com.abcfinancial.api.billing.generalledger.adjustment.enums.AdjustmentType;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.adjustment.repository.AdjustmentRepository;
import com.abcfinancial.api.billing.generalledger.adjustment.valueobject.AdjustmentRequestVO;
import com.abcfinancial.api.billing.generalledger.adjustment.valueobject.AdjustmentResponseVO;
import com.abcfinancial.api.billing.generalledger.statements.repository.PaymentMethodAccountRepository;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.abcfinancial.api.billing.utility.common.AppConstants.ADJUSTMENT_FIELD;
import static com.abcfinancial.api.billing.utility.common.AppConstants.FEE_MODE;

@Service
@Slf4j
@RequiredArgsConstructor

public class AdjustmentService
{
    @Autowired
    private AdjustmentRepository adjustmentRepository;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @Autowired
    private PaymentMethodAccountRepository paymentMethodAccountRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional( propagation = Propagation.REQUIRED )
    public AdjustmentResponseVO createAdjustment( AdjustmentRequestVO adjustmentRequestVO )
    {
        log.debug( "Inside createAdjustment( AdjustmentRequestVO adjustmentRequestVO )" );

        Adjustment adjustment = ModelMapperUtils.map( adjustmentRequestVO, Adjustment.class );
        adjustment.setAccountId( adjustmentRequestVO.getAccountId() );
        adjustment.setAmount( BigDecimal.ZERO.subtract( adjustmentRequestVO.getAmount() ) );
        adjustment.setAdjustmentType( AdjustmentType.SERVICE_FEE );
        adjustment.setFeeMode( FEE_MODE );
        adjustment.setAdjustmentField( ADJUSTMENT_FIELD );
        adjustmentRepository.save( adjustment );

        return ModelMapperUtils.map( adjustment, AdjustmentResponseVO.class );
    }

}
