package com.abcfinancial.api.billing.generalledger.statements.valueobject;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.UUID;

@Data

@JsonPropertyOrder( { "organizationId", "accountId", "locationId", "paymentId" } )

public class AdditionalInformationVO
{
    private UUID organizationId;
    private UUID accountId;
    private UUID locationId;
    private UUID paymentId;
    private UUID settlementId;
}
