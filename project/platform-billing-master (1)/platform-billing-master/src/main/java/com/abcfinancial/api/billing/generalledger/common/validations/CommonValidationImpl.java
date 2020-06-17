package com.abcfinancial.api.billing.generalledger.common.validations;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.LocationAccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.repository.MemberCreationRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Payment;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.PaymentRepository;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.common.domain.NotFoundResponseError;
import com.abcfinancial.api.common.domain.ValidationError;
import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.generalledger.fee.domain.Fee;
import com.abcfinancial.api.billing.generalledger.lookup.repository.FeeTransactionTypeRepository;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.domain.Type;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodRequestVO;
import com.abcfinancial.api.billing.generalledger.settlement.domain.Settlement;
import com.abcfinancial.api.billing.generalledger.settlement.repository.SettlementRepository;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.CommonUtil;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import com.google.common.base.Enums;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.abcfinancial.api.billing.utility.common.AppConstants.*;

@Slf4j
@Component
public class CommonValidationImpl implements CommonValidation
{
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    SettlementRepository settlementRepository;
    @Autowired
    ApplicationConfiguration applicationConfiguration;
    @Autowired
    MemberCreationRepository memberCreationRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private LocationAccountRepository locationAccountRepository;
    @Autowired
    private FeeTransactionTypeRepository feeTransactionTypeRepository;

    @Override
    public void validateName( String name )
    {
        if( !Objects.isNull( name ) && name.isEmpty() )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NAME_BLANK ) );
        }
        if( null != name && Strings.isNotEmpty( name ) )
        {
            if( name.length() > 100 )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NAME_SIZE ) );
            }
            if( StringUtils.isNumeric( name ) )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NAME_NUMERIC ) );
            }
        }
    }

    @Override
    public void validateSevaluation( String sevaluation )
    {
        if( !Objects.isNull( sevaluation ) && StringUtils.isWhitespace( sevaluation ) )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_S_EVALUATION ) );
        }
        if( !Objects.isNull( sevaluation ) )
        {
            if( sevaluation.toUpperCase().trim().equals( Frequency.DAILY.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.WEEKLY.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.EVERY_OTHER_WEEK.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.MONTHLY.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.EVERY_OTHER_MONTH.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.QUARTERLY.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.SEMIANNUALLY.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.ANNUALLY.toString() ) )
            {
                log.trace( "updateAccountInfoVo.getSevaluation( ) is." + sevaluation );
            }
            else
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_CYCLE_CHECK ) );
            }
        }
    }

    @Override
    public void validateEmail( String email )
    {
        if( !Objects.isNull( email ) )
        {
            boolean result = CommonUtil.isValidEmailAddress( email );
            if( !result )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_EMAIL_INVALID ) );
            }
        }
    }

    @Override
    public void validatePhone( String phone )
    {
        if( !Objects.isNull( phone ) )
        {
            String regex = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
            Pattern pattern = Pattern.compile( regex );
            Matcher matcher = pattern.matcher( phone );
            if( !matcher.matches() )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_PHONE_VALID ) );
            }
        }
    }

    @Override
    public void validateBillingDateForClient( LocalDate billingDate )
    {
        if( billingDate.isBefore( LocalDate.now() ) )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_BILLINGDATE_PASTDATECHECK ) );
        }
    }

    @Override
    public void validateBillingDateForPayor( LocalDate updatedPayorBillingDate, LocalDate givenPayorBillingDate, String sevaluation )
    {
        if( !Objects.isNull( updatedPayorBillingDate ) && !Objects.isNull( givenPayorBillingDate ) )
        {
            LocalDate currentDate = LocalDate.now();
            long noOfDaysBetween = 0;
            if( Objects.isNull( sevaluation ) || sevaluation.toUpperCase().trim().equals( "" ) )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_CYCLE_CHECK_FOR_BILLINGDATE ) );
            }
            if( sevaluation.toUpperCase().trim().equals( Frequency.DAILY.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.WEEKLY.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.EVERY_OTHER_WEEK.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.MONTHLY.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.EVERY_OTHER_MONTH.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.QUARTERLY.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.SEMIANNUALLY.toString() ) ||
                sevaluation.toUpperCase().trim().equals( Frequency.ANNUALLY.toString() ) )
            {
                log.trace( "updateAccountInfoVO.getSevaluation( ) is." + sevaluation );
            }
            else
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_CYCLE_CHECK ) );
            }
            if( updatedPayorBillingDate.isBefore( givenPayorBillingDate ) )
            {
                noOfDaysBetween = ChronoUnit.DAYS.between( updatedPayorBillingDate, givenPayorBillingDate );
            }
            else if( updatedPayorBillingDate.isAfter( givenPayorBillingDate ) )
            {
                noOfDaysBetween = ChronoUnit.DAYS.between( givenPayorBillingDate, updatedPayorBillingDate );
            }
            else if( updatedPayorBillingDate.isEqual( givenPayorBillingDate ) )
            {
                noOfDaysBetween = ChronoUnit.DAYS.between( givenPayorBillingDate, updatedPayorBillingDate );
            }
            if( sevaluation.toUpperCase().trim().equals( Frequency.DAILY.toString() ) )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_CYCLE_DATE_FOR_DAILY ) );
            }
            if( sevaluation.toUpperCase().trim().equals( Frequency.WEEKLY.toString() ) )
            {
                // Current Date restricted
                if( updatedPayorBillingDate.equals( givenPayorBillingDate ) || currentDate.equals( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( currentDate.isBefore( givenPayorBillingDate ) && updatedPayorBillingDate.isAfter( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( updatedPayorBillingDate.isBefore( givenPayorBillingDate ) && currentDate.isAfter( updatedPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( noOfDaysBetween > 7 )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_CYCLE_DATE_FOR_WEEKLY ) );
                }
            }
            if( sevaluation.toUpperCase().trim().equals( Frequency.EVERY_OTHER_WEEK.toString() ) )
            {
                // Current Date restricted
                if( updatedPayorBillingDate.equals( givenPayorBillingDate ) || currentDate.equals( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( currentDate.isBefore( givenPayorBillingDate ) && updatedPayorBillingDate.isAfter( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( updatedPayorBillingDate.isBefore( givenPayorBillingDate ) && currentDate.isAfter( updatedPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( noOfDaysBetween > 14 )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_CYCLE_DATE_FOR_EVERY_OTHER_WEEK ) );
                }
            }
            if( sevaluation.toUpperCase().trim().equals( Frequency.MONTHLY.toString() ) )
            {
                // Current Date restricted
                if( updatedPayorBillingDate.equals( givenPayorBillingDate ) || currentDate.equals( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( currentDate.isBefore( givenPayorBillingDate ) && updatedPayorBillingDate.isAfter( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( updatedPayorBillingDate.isBefore( givenPayorBillingDate ) && currentDate.isAfter( updatedPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( noOfDaysBetween > 30 )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_CYCLE_DATE_FOR_MONTHLY ) );
                }
            }
            if( sevaluation.toUpperCase().trim().equals( Frequency.EVERY_OTHER_MONTH.toString() ) )
            {
                // Current Date restricted
                if( updatedPayorBillingDate.equals( givenPayorBillingDate ) || currentDate.equals( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( currentDate.isBefore( givenPayorBillingDate ) && updatedPayorBillingDate.isAfter( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( updatedPayorBillingDate.isBefore( givenPayorBillingDate ) && currentDate.isAfter( updatedPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( noOfDaysBetween > 60 )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_CYCLE_DATE_FOR_EVERY_OTHER_MONTH ) );
                }
            }
            if( sevaluation.toUpperCase().trim().equals( Frequency.QUARTERLY.toString() ) )
            {
                // Current Date restricted
                if( updatedPayorBillingDate.equals( givenPayorBillingDate ) || currentDate.equals( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( currentDate.isBefore( givenPayorBillingDate ) && updatedPayorBillingDate.isAfter( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( updatedPayorBillingDate.isBefore( givenPayorBillingDate ) && currentDate.isAfter( updatedPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( noOfDaysBetween > 90 )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_CYCLE_DATE_FOR_QUARTERLY ) );
                }
            }
            if( sevaluation.toUpperCase().trim().equals( Frequency.SEMIANNUALLY.toString() ) )
            {
                // Current Date restricted
                if( updatedPayorBillingDate.equals( givenPayorBillingDate ) || currentDate.equals( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( currentDate.isBefore( givenPayorBillingDate ) && updatedPayorBillingDate.isAfter( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( updatedPayorBillingDate.isBefore( givenPayorBillingDate ) && currentDate.isAfter( updatedPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( noOfDaysBetween > 180 )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_CYCLE_DATE_FOR_SEMIANNUALLY ) );
                }
            }
            if( sevaluation.toUpperCase().trim().equals( Frequency.ANNUALLY.toString() ) )
            {
                // Current Date restricted
                if( updatedPayorBillingDate.equals( givenPayorBillingDate ) || currentDate.equals( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( currentDate.isBefore( givenPayorBillingDate ) && updatedPayorBillingDate.isAfter( givenPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( updatedPayorBillingDate.isBefore( givenPayorBillingDate ) && currentDate.isAfter( updatedPayorBillingDate ) )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NEW_CYCLE_DATE_NOT_GREATERTHAN_UPCOMING_CYCLE ) );
                }
                if( noOfDaysBetween > 365 )
                {
                    throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_CYCLE_DATE_FOR_ANNUALLY ) );
                }
            }
        }
    }

    @Override
    public void validateFeeId( Optional<Fee> feeOptional )
    {
        if( !feeOptional.isPresent() )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_FEE_ID_INVALID ) );

        }
        if( Objects.nonNull( feeOptional.get().getDeactivated() ) )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_FEE_ID_DEACTIVATED ) );

        }
    }

    @Override
    public Fee validateFeeTrimUpperCase( Fee fee )
    {
        if( !Objects.isNull( fee.getFeeType() ) )
        {
            fee.setFeeType( fee.getFeeType().trim().toUpperCase() );
        }
        if( !Objects.isNull( fee.getFeeValueType() ) )
        {
            fee.setFeeValueType( fee.getFeeValueType().trim().toUpperCase() );
        }
        if( !Objects.isNull( fee.getFeeTransactionType() ) )
        {
            fee.setFeeTransactionType( fee.getFeeTransactionType().trim().toUpperCase() );
        }
        if( !Objects.isNull( fee.getFeeMode() ) )
        {
            fee.setFeeMode( fee.getFeeMode().trim().toUpperCase() );
        }
        if( !Objects.isNull( fee.getFeeValue() ) )
        {
            fee.setFeeValue( fee.getFeeValue() );
        }

        return fee;
    }

    public Settlement validateSettlement( UUID settlementId )
    {
        return settlementRepository.findById( settlementId ).orElseThrow(
            () -> new ErrorResponse( new NotFoundResponseError( Settlement.class, settlementId ) ) );
    }

    @Override
    public Payment validateSettlementAccount( UUID settlementId, UUID accountId )
    {
        return paymentRepository.findBySettlementIdAndAccountAccountId( settlementId, accountId ).orElseThrow(
            () -> new ErrorResponse( new NotFoundResponseError( Payment.class, accountId ) ) );
    }

    @Override
    public Account validateClientAccountId( UUID accountId )
    {
        locationAccountRepository.getDetailsByAccountId( accountId ).orElseThrow(
            () -> new ErrorResponse( new NotFoundResponseError( Account.class, accountId ) ) );

        return accountRepository.findById( accountId ).orElseThrow(
            () -> new ErrorResponse( new NotFoundResponseError( Account.class, accountId ) ) );
    }

    @Override
    public Account validatePayorAccountId( UUID accountId )
    {
        memberCreationRepository.getDetailsByAccountId( accountId ).orElseThrow(
            () -> new ErrorResponse( new NotFoundResponseError( Account.class, accountId ) ) );

        return accountRepository.findById( accountId ).orElseThrow(
            () -> new ErrorResponse( new NotFoundResponseError( Account.class, accountId ) ) );
    }

    @Override
    public void validatePaymentMethodRequestVO( PaymentMethodRequestVO paymentMethodRequestVO )
    {
        if( paymentMethodRequestVO.getType().equals( Type.BANK_ACCOUNT ) )
        {
            if( Objects.nonNull( paymentMethodRequestVO.getBrand() ) || paymentMethodRequestVO.getExpiryYear() != ZERO || paymentMethodRequestVO.getExpiryMonth() != ZERO )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NOT_ALLOW_BANK ) );
            }
            if( Objects.isNull( paymentMethodRequestVO.getBankAccountType() ) )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NOT_NULL_BANK_TYPE ) );
            }
        }
        if( paymentMethodRequestVO.getType().equals( Type.CREDIT_CARD ) )
        {
            if( Objects.nonNull( paymentMethodRequestVO.getBankAccountType() ) || Objects.nonNull( paymentMethodRequestVO.getRoutingNumber() ) )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NOT_ALLOW_CREDIT_CARD ) );
            }
        }

        if( paymentMethodRequestVO.getType().equals( Type.CASH ) )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_NOT_SUPPORTED ) );
        }

        if( paymentMethodRequestVO.getExpiryYear() != ZERO && paymentMethodRequestVO.getExpiryMonth() != ZERO )
        {

            if( paymentMethodRequestVO.getExpiryMonth() > TOTAL_MONTH )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_EXP_INVALID ) );
            }
            if( paymentMethodRequestVO.getExpiryYear() > TOTAL_YEAR )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_EXP_INVALID ) );
            }

            if( ( paymentMethodRequestVO.getExpiryMonth() < YearMonth.now().getMonthValue() && paymentMethodRequestVO.getExpiryYear() == YearMonth.now().getYear() ) ||
                ( paymentMethodRequestVO.getExpiryYear() < YearMonth.now().getYear() ) )
            {
                throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_EXP_CURRENT_FUTURE ) );
            }
        }
        if( ( paymentMethodRequestVO.getExpiryYear() > ONE && paymentMethodRequestVO.getExpiryMonth() < ONE ) ||
            ( paymentMethodRequestVO.getExpiryYear() < ONE && paymentMethodRequestVO.getExpiryMonth() > ONE ) )
        {
            throw new ErrorResponse( new ValidationError( "400", "", MessageUtils.ERROR_MESSAGE_EXP_BOTH ) );
        }
    }

    @Override
    public String validateTrimUpperCase( String str )
    {
        if( !Objects.isNull( str ) )
        {
            str = str.trim().toUpperCase();
        }

        return str;
    }

    @Override
    public String validateFeeTransactionType( String str )
    {
        if( !Objects.isNull( str ) )
        {
            feeTransactionTypeRepository.findById( str ).orElseThrow(
                () -> new ErrorResponse( new NotFoundResponseError( Account.class, str ) ) );
        }
        return str;
    }

    public void validateAccountId( Optional<Account> accountOptional, Optional<PaymentMethod> paymentMethodOptional, UUID accnId )
    {
        if( !accountOptional.isPresent() && !paymentMethodOptional.isPresent() )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), CommonValidation.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_NOT_EXIST ) + "" + accnId ) );
        }
    }

    public TransactionType validateTransactionType( String type )
    {
        if( !Enums.getIfPresent( TransactionType.class, type.toUpperCase() ).isPresent() )
        {
            throw new ErrorResponse( new ValidationError( HttpStatus.BAD_REQUEST.toString(), "", MessageUtils.ERROR_MESSAGE_TRANSACTION_TYPE_INVALID ) );
        }
        return TransactionType.valueOf( type.toUpperCase() );

    }

    public void validateDateRange( LocalDate startDate, LocalDate endDate )
    {
        if( startDate.isAfter( LocalDate.now( Clock.systemUTC() ) ) )
        {
            throw new ErrorResponse( new ValidationError( HttpStatus.BAD_REQUEST.toString(), "", MessageUtils.ERROR_MESSAGE_START_DATE_FUTURE_DATE_NOT_ALLOWED ) );
        }

        if( endDate.isAfter( LocalDate.now( Clock.systemUTC() ) ) )
        {
            throw new ErrorResponse( new ValidationError( HttpStatus.BAD_REQUEST.toString(), "", MessageUtils.ERROR_MESSAGE_END_DATE_FUTURE_DATE_NOT_ALLOWED ) );
        }

        if( startDate.isAfter( endDate ) )
        {
            throw new ErrorResponse( new ValidationError( HttpStatus.BAD_REQUEST.toString(), "", MessageUtils.ERROR_MESSAGE_START_DATE_AFTER_END_DATE_INVALID ) );
        }
    }

    public void validatePaymentMethodId( UUID paymentMethodId )
    {
        throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), CommonValidation.class,
            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PAYOR_ACCOUNT_NOT_EXIST ) + "" + paymentMethodId ) );
    }
}
