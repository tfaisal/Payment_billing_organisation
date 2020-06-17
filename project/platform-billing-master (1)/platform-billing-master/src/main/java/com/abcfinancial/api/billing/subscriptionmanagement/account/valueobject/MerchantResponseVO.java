package com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@JsonPropertyOrder( { "companyId", "id", "name", "merchantAccountToken", "active", "businessContact", "technicalContact", "merchantDetails" } )

public class MerchantResponseVO
{
    private UUID companyId;
    private UUID id;
    private String name;
    private UUID merchantAccountToken;
    private MerchantDetailsVO merchantDetails;
    private BusinessContactVO businessContact;
    private TechnicalContactVO technicalContact;
    private boolean active;
    private List<ErrorResponseVO> errors;
    private String statusCode;

    public MerchantResponseVO( )
    {
    }

    public MerchantResponseVO( UUID id )
    {
        this.id = id;
    }
}
