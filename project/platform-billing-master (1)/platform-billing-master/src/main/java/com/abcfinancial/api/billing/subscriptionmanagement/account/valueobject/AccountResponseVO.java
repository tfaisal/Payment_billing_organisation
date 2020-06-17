package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodResponseVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class AccountResponseVO
{
    /**
     * Account Id( Read Only )
     * It should be in java.util.UUID format
     */

    private UUID accountId;
    /**
     * Name for Account.
     * Must be between 10 and 100 character
     */

    @NotNull( message = "Name cannot be null" )
    private String name;
    /**
     * Email Id for Account.
     */

    @Size( min = 1, max = 254 )
    @NotNull( message = "Email should not be null" )
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

    @NotNull
    private String sevaluation;
    /**
     * Date refers to client Settlement Date and payor Billing Date
     */

    @NotNull
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE )
    @JsonFormat( pattern = "MM-dd-yyyy" )
    @JsonInclude( JsonInclude.Include.NON_DEFAULT )
    private LocalDate billingDate;
    /**
     * Payment Method Details
     */

    @NotNull( message = "payment method details cannot be null" )
    private List<PaymentMethodResponseVO> paymentMethod;
}
