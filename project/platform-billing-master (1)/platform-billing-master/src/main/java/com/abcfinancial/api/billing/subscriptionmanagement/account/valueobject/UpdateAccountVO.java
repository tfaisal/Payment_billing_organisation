package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.abcfinancial.api.billing.generalledger.payment.valueobject.PaymentMethodVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class UpdateAccountVO
{
    /**
     * account Id
     */

    private UUID accountId;
    /**
     * Name for Account
     */

    @Size( min = 1, max = 100, message = "name must be between 1 and 100 characters" )
    private String name;
    /**
     * Email Id
     */

    private String email;
    /**
     * Phone Number
     */

    private String phone;
    /**
     * sEvaluation - refers to the cycle ( can be settlement ( frequency / threshold ( to be used in future ) ) - in case of client account / statement ( frequency ) - in case of payor account  ) for Account.
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

    private String sevaluation;
    /**
     * Payment Method Details
     */

    private PaymentMethodVO paymentMethod;

    @JsonIgnore
    public boolean isValid( )
    {
        return Objects.isNull( accountId ) && Objects.isNull( name ) && Objects.isNull( email ) && Objects.isNull( phone ) && Objects.isNull( paymentMethod );
    }
}
