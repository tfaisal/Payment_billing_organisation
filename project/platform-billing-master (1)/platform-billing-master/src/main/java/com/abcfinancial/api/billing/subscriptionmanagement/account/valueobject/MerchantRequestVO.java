package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@JsonPropertyOrder( { "companyId", "id", "name", "merchantAccountToken", "businessContact", "technicalContact", "merchantDetails" } )

public class MerchantRequestVO
{
    private String companyId;
    private UUID id;
    private String name;
    private UUID merchantAccountToken;
    private MerchantDetailsVO merchantDetails = new MerchantDetailsVO( );
    private BusinessContactVO businessContact = new BusinessContactVO( );
    private TechnicalContactVO technicalContact = new TechnicalContactVO( );
}
