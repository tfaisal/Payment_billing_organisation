package com.abcfinancial.api.billing.subscriptionmanagement.subscription

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class SubscriptionExpireTest extends BaseTest
{

    def accountId;
    static def memberId = UUID.randomUUID();
    static def payorId = UUID.randomUUID();
    def sub
    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy');
    def createdMember

    @Autowired
    AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }

    @Test
    void ExpireSubscriptionTestingAPITest()
    {

        def requestMemAccn = createMemberAccountRequest()

        createdMember = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestMemAccn)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())


        accountId = createdMember.account.accountId;

        def request = createSubscriptionRequest()

        def subscriptionMethod = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        sub = subscriptionMethod
        def expireSubscriptionReq = expireSubscriptionRequest()

        parseJson(mvc().perform(put('/subscription/{subscriptionId}/expire', sub.subId).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(expireSubscriptionReq)))
                .andDo(print()).andDo(document("expireSubscription"))
                .andExpect(status().isOk())
                .andReturn())
    }

    def createSubscriptionRequest()
    {
        return [
                "locationId"     : createdClientLocationId,
                "salesEmployeeId": UUID.randomUUID(),
                "accountId"      : accountId,
                "memberIdList"   : [createdMember.memberId],
                "start"          : currentDate,
                "invoiceDate"    : currentDate,
                "expirationDate" : ExpireDate,
                "frequency"      : "DAILY",
                "duration"       : "4",
                "items"          : [[
                                            "itemName"       : "adcds",
                                            "itemId"         : UUID.randomUUID(),
                                            "version"        : 1,
                                            "price"          : 7894,
                                            "quantity"       : "1",
                                            "expirationStart": "PURCHASE",
                                            "type"           : "PRODUCT",
                                            "unlimited"      : "false"
                                    ]]
        ]
    }

    static def createProcessorRequest()
    {
        return [
                "accountId"     : "5b47534bf58fa7398df15d38",
                "organizationId": "5b115891b6cdfd755de3d4bd",
                "locationId"    : createdClientLocationId
        ]
    }

    static def createMemberAccountRequest()
    {
        return [
                "locationId": Constant.fourthCreatedClientLocationId,
                "memberId"  : memberId,
                "payorId"   : payorId,
                account     : [
                        "name"       : "SubscriptionExpire",
                        "email"      : "SubscriptionExpire@gmail.com",
                        "phone"      : "19075516443",
                        "sevaluation": sEvaluation,
                        "billingDate": currentDate,
                        paymentMethod: [
                                "type"   : "CASH",
                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                        ]
                ]
        ]
    }


    def expireSubscriptionRequest()
    {
        return [
                "memberId"      : memberId,
                "locationId"    : createdClientLocationId,
                "subId"         : sub.subId,
                "expirationDate": calculateDate()

        ]
    }

    def calculateDate()
    {
        LocalDateTime localDate = LocalDateTime.now()
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        return localDate.format(formatter)
    }
}
