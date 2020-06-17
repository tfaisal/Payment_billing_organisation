package com.abcfinancial.api.billing.generalledger.common.validations;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Payment;
import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.generalledger.fee.domain.Fee;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodRequestVO;
import com.abcfinancial.api.billing.generalledger.settlement.domain.Settlement;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public interface CommonValidation
{
    public void validateName( final String name );

    public void validateSevaluation( final String sevaluation );

    public void validateEmail( final String email );

    public void validatePhone( final String phone );

    public void validateBillingDateForClient( final LocalDate clientBillingDate );

    public void validateBillingDateForPayor( final LocalDate updatedPayorBillingDate, final LocalDate givenPayorBillingDate, final String sevaluation );

    public void validateFeeId( Optional<Fee> feeOptional );

    public Fee validateFeeTrimUpperCase( Fee fee );

    Settlement validateSettlement( UUID settlementId );

    Payment validateSettlementAccount( UUID settlementId, UUID account );

    Account validateClientAccountId( UUID accountId );

    Account validatePayorAccountId( UUID accountId );

    public void validatePaymentMethodRequestVO( PaymentMethodRequestVO paymentMethodRequestVO );

    String validateTrimUpperCase( String str );

    String validateFeeTransactionType( String str );

    public void validateAccountId( Optional<Account> accountOptional, Optional<PaymentMethod> paymentMethodOptional, UUID accnId );

    public TransactionType validateTransactionType( String type );

    public void validateDateRange( LocalDate startDate, LocalDate endDate );

    public void validatePaymentMethodId( UUID paymentMethodId );
}
