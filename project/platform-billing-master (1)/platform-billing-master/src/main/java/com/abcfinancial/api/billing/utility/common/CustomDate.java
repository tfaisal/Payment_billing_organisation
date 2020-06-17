package com.abcfinancial.api.billing.utility.common;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data

@Component

public class CustomDate
{
    long years;
    long months;
    long days;
    long hours;
    long minutes;
    long seconds;
}
