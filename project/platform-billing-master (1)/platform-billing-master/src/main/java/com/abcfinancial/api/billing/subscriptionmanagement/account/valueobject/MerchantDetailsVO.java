package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.account.enums.ServiceClassCode;
import com.abcfinancial.api.billing.subscriptionmanagement.account.enums.StandardEntryClass;
import lombok.Data;

import java.util.List;

@Data

public class MerchantDetailsVO
{
    private String companyName;
    private String companyDiscretionInfo = "501-515-5000"; //todo MarkV why are we exposing this outside the gateway???
    private ServiceClassCode serviceClassCode = ServiceClassCode.MIXED;
    private StandardEntryClass standardEntryClassCode = StandardEntryClass.PPD;
    private Boolean active;
    private String companyEntryDescription = "CLUB FEES";
    private List<String> processors;
}
