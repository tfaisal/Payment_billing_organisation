package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )
@NoArgsConstructor
public class UpdateAccountResponseVO
{

    /**
     * account Id
     */
    private UUID accountId;

    /**
     * Account details
     */
    private UpdateAccountInfoVO account;

}
