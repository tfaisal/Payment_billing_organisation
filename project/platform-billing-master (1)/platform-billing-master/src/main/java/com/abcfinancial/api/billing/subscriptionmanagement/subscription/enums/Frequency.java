package com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums;

import lombok.Getter;

import java.time.Period;

@Getter
public enum Frequency
{
    DAILY( Period.ofDays( 1 ), "Daily" ),
    WEEKLY( Period.ofWeeks( 1 ), "Weekly" ),
    EVERY_OTHER_WEEK( Period.ofWeeks( 2 ), "Every other week" ),
    MONTHLY( Period.ofMonths( 1 ), "Monthly" ),
    EVERY_OTHER_MONTH( Period.ofMonths( 2 ), "Every other month" ),
    ANNUALLY( Period.ofYears( 1 ), "Annually" ),
    QUARTERLY( Period.ofMonths( 3 ), "Quarterly" ),
    SEMIANNUALLY( Period.ofMonths( 6 ), "Semiannually" );

    private final Period period;
    private final String stringRepresentation;

    Frequency( Period period, String stringRepresentation )
    {
        this.period = period;
        this.stringRepresentation = stringRepresentation;
    }

    public String getStringRepresentation()
    {
        return stringRepresentation;
    }
}
