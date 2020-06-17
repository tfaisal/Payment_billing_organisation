package com.abcfinancial.api.billing.generalledger.statements.valueobject;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data

public class StatementRequestVO
{
    /**
     * Location Id.
     */

    private UUID locationId;
    /**
     * Payor's account id for created statement. This account denotes to payment method account and main account.
     */

    @NotNull
    private UUID accountId;
}
