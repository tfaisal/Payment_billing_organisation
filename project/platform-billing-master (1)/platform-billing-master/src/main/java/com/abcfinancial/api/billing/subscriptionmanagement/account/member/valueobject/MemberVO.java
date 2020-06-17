package com.abcfinancial.api.billing.subscriptionmanagement.account.member.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )

public class MemberVO
{
    /**
     * Updated locationId of MemberId
     */

    @NotNull
    private UUID locationId;
}
