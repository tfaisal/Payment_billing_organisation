package com.abcfinancial.api.billing.subscriptionmanagement.avalara.functional;

import com.abcfinancial.api.billing.subscriptionmanagement.avalara.constants.AVALARAAPI;

import java.time.LocalTime;

public interface AvalaraRetryService<T>
{
    /**
     * This method always try for the
     *
     * @param localTime
     * @param estimatedMinutes
     * @return
     */

    T nextCall( LocalTime localTime, int estimatedMinutes, AVALARAAPI avalaraapi, String username, String licenceKey );
}
