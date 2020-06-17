package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )
@NoArgsConstructor
public class UpdateAccountDetailVO
{
    /**
     * Account details
     */

    private UpdateAccountVO account;

}
