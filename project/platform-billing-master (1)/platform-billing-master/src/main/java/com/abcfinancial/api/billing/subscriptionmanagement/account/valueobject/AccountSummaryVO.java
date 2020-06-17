package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class AccountSummaryVO
{
    /**
     * Invoice Id.
     */

    private UUID invoiceId;
    /**
     * Statement Id.
     */

    private UUID statementId;
}
