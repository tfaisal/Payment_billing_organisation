package com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AgreementDue
{
    /**
     * Agreement number for Requested Agreement of Type String
     */
    @NotNull
    private String agreementNumber;
    /**
     * List of Subscriptions involved in an agreement
     */
    @NotNull
    private List<SubscriptionListDue> subscriptionList;
    /**
     * Total Invoice count due for an agreement should be integer.
     */
    @NotNull
    private Long agreementInvoiceCount;
    /**
     * Total Invoice amount due  for an agreement
     */
    @NotNull
    private BigDecimal agreementAmountDue;

}
