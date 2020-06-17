package com.abcfinancial.api.billing.subscriptionmanagement.subscription

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.utility.constant.Constant
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class SubscriptionUpdateTest extends BaseTest
{

    def accountId;
    static def memberId = UUID.randomUUID();
    static def payorId = UUID.randomUUID();
    static def salesEmpId = UUID.randomUUID();
    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy');
    def createNewMembers

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken();

    }

    @Test
    void updateSubscriptionTest()
    {

        def requestMemAccn = createMemberAccountRequest()

        createNewMembers = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestMemAccn)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())


        accountId = createNewMembers.account.accountId;

        def request = createSubscriptionRequest()

        def subscriptionMethod = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        def updateSubscriptionReq = updateSubscriptionRequest()
        mvc().perform(put('/subscription/{subscriptionId}', subscriptionMethod.subId).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(updateSubscriptionReq)))
                .andDo(print()).andDo(document("updateSubscription"))
                .andExpect(status().isOk())
                .andReturn()
    }

    def createSubscriptionRequest()
    {
        return [
                "locationId"     : createdClientLocationId,
                "salesEmployeeId": UUID.randomUUID(),
                "accountId"      : accountId,
                "memberIdList"   : [createNewMembers.memberId],
                "frequency"      : "DAILY",
                "duration"       : "4",
                "start"          : currentDate,
                "invoiceDate"    : currentDate,
                "expirationDate" : ExpireDate,
                "items"          : [[
                                            "itemName"       : "adcds",
                                            "itemId"         : UUID.randomUUID(),
                                            "version"        : 1,
                                            "price"          : 2117,
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
                        "name"       : "SubscriptionUpdate",
                        "email"      : "ravishjain@QA4LIFE.COM",
                        "phone"      : "19175526446",
                        "sevaluation": sEvaluation,
                        "billingDate": currentDate,
                        paymentMethod: [
                                "type"   : "CASH",
                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                        ]
                ]

        ]
    }

    static def updateSubscriptionRequest()
    {
        return [
                "salesEmployeeId": salesEmpId

        ]
    }
}
