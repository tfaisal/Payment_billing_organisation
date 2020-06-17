package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class AccountVO
{
    /**
     * Account Id( Read Only )
     * It should be in java.util.UUID format
     */

    private UUID accountId;
    /**
     * Name for Account.
     */
    @Size( min = 1, max = 100 )
    @NotNull( message = "not allowed for name" )
    private String name;
    /**
     * Email Id for Account.
     */

    @Size( min = 1, max = 254 )
    private String email;
    /**
     * Phone Number for Account.
     */

    private String phone;
    /**
     * sEvaluation - refers to the cycle ( can be settlement ( frequency / threshold ( to be used in future ) ) - in case of client account / statement ( frequency ) - in case
     * of payor account  ) for Account.
     * frequency can be -
     * 1. DAILY  -  daily
     * 2. WEEKLY -   weekly
     * 3. MONTHLY -   monthly
     * 4. ANNUALLY -  yearly
     * 5. QUARTERLY -  3 months
     * 6. SEMIANNUALLY -  6 months
     * 7. EVERY_OTHER_MONTH -  2 months
     * 8. EVERY_OTHER_WEEK -  2 weeks
     */

    @NotNull( message = "not allowed for sevaluation" )
    private String sevaluation;
    /**
     * Date refers to client Settlement Date and payor Billing Date
     */

    @NotNull( message = "not allowed for billingDate" )
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private LocalDate billingDate;
    /**
     * Payment Method Details
     */

    @NotNull( message = "payment method details cannot be null" )
    private PaymentMethodVO paymentMethod;

    public void setPaymentMethod( PaymentMethodVO paymentMethod )
    {
        paymentMethod.setBillingDate( paymentMethod.getBillingDate() == null ? LocalDate.now() : paymentMethod.getBillingDate() );
        paymentMethod.setSevaluation( paymentMethod.getSevaluation() == null ? Frequency.ANNUALLY : paymentMethod.getSevaluation() );
        this.paymentMethod = paymentMethod;
    }
}
