package com.abcfinancial.api.billing.generalledger.fee.service;

import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.LocationAccountRepository;
import com.abcfinancial.api.billing.generalledger.common.validations.CommonValidation;
import com.abcfinancial.api.billing.generalledger.fee.domain.Fee;
import com.abcfinancial.api.billing.generalledger.lookup.domain.FeeMode;
import com.abcfinancial.api.billing.generalledger.lookup.domain.FeeTransactionType;
import com.abcfinancial.api.billing.generalledger.lookup.domain.FeeType;
import com.abcfinancial.api.billing.generalledger.lookup.domain.FeeValueType;
import com.abcfinancial.api.billing.generalledger.lookup.repository.FeeModeRepository;
import com.abcfinancial.api.billing.generalledger.lookup.repository.FeeTransactionTypeRepository;
import com.abcfinancial.api.billing.generalledger.lookup.repository.FeeTypeRepository;
import com.abcfinancial.api.billing.generalledger.lookup.repository.FeeValueTypeRepository;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.common.domain.NotFoundResponseError;
import com.abcfinancial.api.billing.generalledger.fee.repository.FeeRepository;
import com.abcfinancial.api.billing.generalledger.fee.valueobject.FeeRequestVO;
import com.abcfinancial.api.billing.generalledger.fee.valueobject.FeeResponseVO;
import com.abcfinancial.api.billing.generalledger.fee.valueobject.UpdateFeeRequestVO;
import com.abcfinancial.api.billing.generalledger.fee.valueobject.UpdateFeeVO;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.CommonUtil;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j

public class FeeService
{
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private FeeRepository feeRepository;
    @Autowired
    private FeeModeRepository feeModeRepository;
    @Autowired
    private FeeTypeRepository feeTypeRepository;
    @Autowired
    private FeeTransactionTypeRepository feeTransactionTypeRepository;
    @Autowired
    private FeeValueTypeRepository feeValueTypeRepository;
    @Autowired
    private LocationAccountRepository locationAccountRepository;
    @Autowired
    private CommonValidation commonValidation;

    @Transactional
    public FeeResponseVO createFee( FeeRequestVO feeRequestVO )
    {

        log.debug( "Start createFee." );
        validateMandatoryFieldsForFee( feeRequestVO );
        if( feeRequestVO.getAccountId() == null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NULL ) ) );
        }
        Optional<LocationAccount> locationAccount = locationAccountRepository.getDetailsByAccountId( feeRequestVO.getAccountId() );
        if( !locationAccount.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + feeRequestVO.getAccountId() ) );
        }
        Optional<Fee> feeOptional =
            feeRepository
                .findFeeByAccountIdAndFeeTypeAndFeeTransactionTypeAndFeeValueTypeAndDeactivated( feeRequestVO.getAccountId(), feeRequestVO.getFeeType(),
                    feeRequestVO.getFeeTransactionType(),
                    feeRequestVO.getFeeValueType(), null );
        if( feeOptional.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_ALREADY_EXIST ) + feeRequestVO.getFeeType() + " and feeTransactionType " +
                feeRequestVO.getFeeTransactionType() + " and feeValueType " + feeRequestVO.getFeeValueType() ) );
        }

        Fee fee = ModelMapperUtils.map( feeRequestVO, Fee.class );

        feeRepository.save( fee );

        FeeResponseVO feeResponseVO = ModelMapperUtils.map( fee, FeeResponseVO.class );

        log.debug( "End createFee." );
        return feeResponseVO;
    }

    private void validateMandatoryFieldsForFee( FeeRequestVO feeRequestVO )
    {

        if( feeRequestVO.getFeeMode() == null || feeRequestVO.getFeeMode().trim().length() <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_MODE_NULL ) ) );
        }

        feeRequestVO.setFeeMode( feeRequestVO.getFeeMode().trim().toUpperCase() );

        Optional<FeeMode> feeMode = feeModeRepository.findById( feeRequestVO.getFeeMode() );
        if( !feeMode.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_MODE_INVALID ) ) );
        }

        if( feeRequestVO.getFeeType() == null || feeRequestVO.getFeeType().trim().length() <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_TYPE_NULL ) ) );
        }

        feeRequestVO.setFeeType( feeRequestVO.getFeeType().trim().toUpperCase() );

        Optional<FeeType> feeType = feeTypeRepository.findById( feeRequestVO.getFeeType() );

        if( !feeType.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_TYPE_INVALID ) ) );
        }

        validateMandatoryFields( feeRequestVO );
    }

    private void validateMandatoryFields( FeeRequestVO feeRequestVO )
    {
        if( feeRequestVO.getFeeTransactionType() == null || feeRequestVO.getFeeTransactionType().trim().length() <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_TRANSACTION_TYPE_NULL ) ) );
        }

        feeRequestVO.setFeeTransactionType( feeRequestVO.getFeeTransactionType().trim().toUpperCase() );

        Optional<FeeTransactionType> feeTransactionType = feeTransactionTypeRepository.findById( feeRequestVO.getFeeTransactionType() );
        if( !feeTransactionType.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_TRANSACTION_TYPE_INVALID ) ) );
        }

        if( feeRequestVO.getFeeValueType() == null || feeRequestVO.getFeeValueType().trim().length() <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_VALUE_TYPE_NULL ) ) );
        }

        feeRequestVO.setFeeValueType( feeRequestVO.getFeeValueType().trim().toUpperCase() );

        Optional<FeeValueType> feeValueType = feeValueTypeRepository.findById( feeRequestVO.getFeeValueType() );

        if( !feeValueType.isPresent() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_VALUE_TYPE_INVALID ) ) );
        }

        if( feeRequestVO.getFeeValue() == null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_VALUE_NULL ) ) );
        }

        feeRequestVO.setFeeValue( feeRequestVO.getFeeValue().setScale( 4, RoundingMode.HALF_UP ) );

        if( feeRequestVO.getFeeValue().compareTo( BigDecimal.ZERO ) < 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_VALUE_NEGATIVE ) ) );
        }

        if( feeRequestVO.getFeeValueType().equalsIgnoreCase( "PERCENTAGE" ) && feeRequestVO.getFeeValue().compareTo( BigDecimal.valueOf( 100 ) ) > 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_VALUE_PERCENTAGE ) ) );
        }

        if( feeRequestVO.getFeeValueType().equalsIgnoreCase( "FLAT" ) && feeRequestVO.getFeeValue().compareTo( BigDecimal.valueOf( 999999999999999999L ) ) > 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_VALUE_INVALID ) ) );
        }
    }

    public List<BigDecimal> calculateDeductionFee( BigDecimal payAmount, UUID clientAccountId )
    {
        List<BigDecimal> calculatedAmounts = new ArrayList<>();
        List<Fee> feeList = feeRepository.findFeeByAccountIdAndDeactivatedIsNull( clientAccountId );

        feeList.parallelStream().forEach( fee ->
            calculatedAmounts.add( calculateFee( payAmount, fee ) )
        );

        return calculatedAmounts;
    }

    private BigDecimal calculateFee( BigDecimal payAmount, Fee fee )
    {
        BigDecimal calculatedAmount = BigDecimal.ZERO;
        if( "FLAT".equals( fee.getFeeValueType() ) )
        {
            calculatedAmount = fee.getFeeValue();
        }
        if( "PERCENTAGE".equals( fee.getFeeValueType() ) )
        {
            calculatedAmount = payAmount.multiply( fee.getFeeValue() ).divide( BigDecimal.valueOf( 100 ) );
        }
        return calculatedAmount.setScale( 2, RoundingMode.HALF_UP );
    }

    @Transactional
    public UpdateFeeVO updateFeeDetails( UpdateFeeRequestVO updateFeeVO, UUID feeId )
    {
        log.trace( "Update Fee: " + feeId );

        Optional<Fee> feeOptional = feeRepository.getDetailsByFeeId( feeId );

        commonValidation.validateFeeId( feeOptional );

        if( feeOptional.isPresent() )
        {
            Fee fee = ModelMapperUtils.map( updateFeeVO, Fee.class );
            commonValidation.validateFeeTrimUpperCase( fee );

            log.debug( "values: {}", updateFeeVO );
            String feeType = Objects.isNull( updateFeeVO.getFeeType() ) ? feeOptional.get().getFeeType() : updateFeeVO.getFeeType();
            String feeTransactionType = Objects.isNull( updateFeeVO.getFeeTransactionType() ) ? feeOptional.get().getFeeTransactionType() : updateFeeVO.getFeeTransactionType();
            String feeValueType = Objects.isNull( updateFeeVO.getFeeValueType() ) ? feeOptional.get().getFeeValueType() : updateFeeVO.getFeeValueType();

            Optional<Fee> feeOptional1 =
                feeRepository
                    .findFeeByAccountIdAndFeeTypeAndFeeTransactionTypeAndFeeValueTypeAndDeactivated( feeOptional.get().getAccountId(), feeType, feeTransactionType, feeValueType,
                        null );
            log.info( "fee: {}", feeOptional.get() );
            if( feeOptional1.isPresent() &&
                ( Objects.nonNull( updateFeeVO.getFeeType() ) && Objects.nonNull( updateFeeVO.getFeeTransactionType() ) && Objects.nonNull( updateFeeVO.getFeeValueType() ) ) )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), FeeService.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FEE_ALREADY_EXIST ) + updateFeeVO.getFeeType() + " and feeTransactionType " +
                    updateFeeVO.getFeeTransactionType() + " and feeValueType " + updateFeeVO.getFeeValueType() ) );
            }
            BeanUtils.copyProperties( fee, feeOptional.get(), CommonUtil.getNullPropertyNames( fee ) );
            feeRepository.save( feeOptional.get() );
            UpdateFeeVO updateFeeVO1 = ModelMapperUtils.map( feeOptional.get(), UpdateFeeVO.class );
            FeeRequestVO updateFeeVO2 = ModelMapperUtils.map( updateFeeVO1, FeeRequestVO.class );
            validateMandatoryFieldsForFee( updateFeeVO2 );
            log.trace( "Update Fee End " );
            return updateFeeVO1;
        }
        return null;
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public FeeResponseVO deleteFee( UUID feeId )
    {
        Fee fee = null;

        if( Objects.nonNull( feeId ) )
        {
            Optional<Fee> feeOptional = feeRepository.getDetailsByFeeId( feeId );
            commonValidation.validateFeeId( feeOptional );
            if( feeOptional.isPresent() )
            {
                fee = feeOptional.get();
                if( Objects.isNull( feeOptional.get().getDeactivated() ) )
                {
                    fee.setDeactivated( LocalDateTime.now() );
                    feeRepository.save( fee );
                }
                else
                {
                    commonValidation.validateFeeId( feeOptional );
                }
            }
        }
        FeeResponseVO fees = ModelMapperUtils.map( fee, FeeResponseVO.class );

        log.trace( "Delete Fee End " );
        return fees;
    }

    @Transactional( readOnly = true )
    public List<FeeResponseVO> getFeeByAccountId( UUID accountId, String feeTransactionType, Pageable pageable )
    {
        log.trace( "FeeByAccountId Start " );
        commonValidation.validateClientAccountId( accountId );

        commonValidation.validateFeeTransactionType( commonValidation.validateTrimUpperCase( feeTransactionType ) );
        List<Fee> feeList = null;

        if( Strings.isBlank( feeTransactionType ) )
        {
            feeList = feeRepository.findFeeByAccountId( accountId, pageable );
        }
        else
        {
            feeList = feeRepository.findFeeByAccountIdAndFeeTransactionType( accountId, feeTransactionType, pageable );

        }
        if( feeList.isEmpty() )
        {
            throw new ErrorResponse( new NotFoundResponseError( Fee.class, accountId ) );
        }

        List<FeeResponseVO> fees = ModelMapperUtils.mapAll( feeList, FeeResponseVO.class );
        log.trace( "FeeByAccountId End " );
        return fees;
    }
}

