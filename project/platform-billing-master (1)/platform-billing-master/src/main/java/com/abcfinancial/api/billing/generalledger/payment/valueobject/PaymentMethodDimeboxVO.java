package com.abcfinancial.api.billing.generalledger.payment.valueobject;

import lombok.Data;

import java.util.List;

@Data

public class PaymentMethodDimeboxVO
{
    private int code;
    private String message;
    private long timestamp;
    private String details;
    private String type;
    private String brand;
    private String lastFour;
    private int expiryYear;
    private int expiryMonth;
    private String organisation;
    private List<String> processors;
}

