package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class ErrorResponseVO
{
    private String code;
    private String message;
}
