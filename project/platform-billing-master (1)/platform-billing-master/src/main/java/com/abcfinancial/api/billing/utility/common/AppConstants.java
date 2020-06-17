package com.abcfinancial.api.billing.utility.common;

public final class AppConstants
{
    public static final String ERRORCAUSE = "Exception Message{} Cause{} ";
    public static final int ZERO = 0;
    public static final String COMMA = ", ";
    public static final String CREATED = "created";
    public static final String DESC = "desc";
    public static final String ASC = "asc";
    public static final String DEFAULT_SIZE = "20";
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_PAGE_VALUE = "page";
    public static final String DEFAULT_NAME_VALUE = "name";
    public static final String DEFAULT_FEE_TRANSACTION_TYPE = "feeTransactionType";
    public static final String DEFAULT_SIZE_VALUE = "size";
    public static final String HEADER_AUTHORISATION = "Authorization";
    public static final String HEADER_AUTHORISATION_BEARER = "Bearer ";
    public static final int TWENTY = 20;
    public static final int MAXCONNECTIONCOUNT = 200;
    public static final int HUNDRED = 100;
    public static final int THIRTY = 30;
    public static final boolean ISTRUE = true;
    public static final boolean ISFALSE = false;
    public static final String FEE_MODE = "TRANSACTION";
    public static final String ADJUSTMENT_FIELD = "Payment Deduction";
    public static final Long BALANCE_AMOUNT = 99999999999999999L;
    public static final Long PAY_AMOUNT = 99999999999999L;
    public static final String TEST_UUID = "00000000-0000-0000-0000-000000000001";
    public static final String BILLING = "BILLING";
    public static final String EXCEPTION_VALIDATING_INPUTS = "Exception occurred while validating inputs, Exception :: ";
    public static final String SUMMARY_DATE = "summaryDate";
    public static final String ACTIVE = "Active";
    public static final String CANCELLED = "Cancelled";
    public static final String PENDING_CANCELLATION = "Pending Cancellation";
    public static final String EXPIRED = "Expired";
    public static final String PENDING_ACTIVE = "Pending Active";
    public static final int ONE = 1;
    public static final int TOTAL_MONTH = 12;
    public static final int TOTAL_YEAR = 9999;
    public static final String ACCOUNT_SUMMARY = "Account summary updated. ";

    private AppConstants()
    {
        throw new AssertionError();
    }
}
