package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import lombok.Data;

import java.util.UUID;

@Data

public class BusinessContactVO
{
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String extention;
    private boolean active;
}
