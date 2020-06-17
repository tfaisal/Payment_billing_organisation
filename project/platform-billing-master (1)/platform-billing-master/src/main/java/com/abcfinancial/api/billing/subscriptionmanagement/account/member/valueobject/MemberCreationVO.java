package com.abcfinancial.api.billing.subscriptionmanagement.account.member.valueobject;

import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AccountVO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )
@NoArgsConstructor
public class MemberCreationVO
{
    /**
     * An unique id which behaves as the identification of a particular registered organization and its location.
     */

    @NotNull( message = "location id can not be null" )
    private UUID locationId;
    /**
     * An unique id  which behaves as the identification of member belongs to a particular Location ( particular registered organization and its location )
     */

    private UUID memberId;
    /**
     * An unique id  which behaves as the identification of payor belongs to a particular Location ( particular registered organization and its location )
     */

    @NotNull( message = "payor id can not be null" )
    private UUID payorId;
    /**
     * Account details to create Member
     */

    @NotNull( message = "Account details can not be null" )
    private AccountVO account;
}
