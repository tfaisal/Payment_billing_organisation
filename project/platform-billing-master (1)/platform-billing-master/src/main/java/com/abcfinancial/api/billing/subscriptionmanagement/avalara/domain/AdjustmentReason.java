package com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain;

public enum AdjustmentReason
{
    NotAdjusted,
    SourcingIssue,
    ReconciledWithGeneralLedger,
    ExemptCertApplied,
    PriceAdjusted,
    ProductReturned,
    ProductExchanged,
    BadDebt,
    Other,
    Offline
}
