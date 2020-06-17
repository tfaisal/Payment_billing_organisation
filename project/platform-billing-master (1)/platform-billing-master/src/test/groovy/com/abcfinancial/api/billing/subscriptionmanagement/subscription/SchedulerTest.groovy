package com.abcfinancial.api.billing.subscriptionmanagement.subscription

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class SchedulerTest extends BaseTest
{


    def subscription
    def accountId;
    static def memberId = UUID.randomUUID();
    static def payorId = UUID.randomUUID();
    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy');
    def createNewMem

    @Autowired
    AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }

    @Test(dependsOnGroups = 'onboarding')
    void schedulerTriggerTest()
    {

        def requestMemAccn = createMemberAccountRequest()

        createNewMem = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestMemAccn)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())


        accountId = createNewMem.account.accountId;

        def requestPayload = createSubscriptionRequestPayload()

        subscription = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestPayload)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())
    }

    def createSubscriptionRequestPayload()
    {
        return [
                "locationId"     : createdClientLocationId,
                "salesEmployeeId": "ad8ebc9e-5fab-140f-879f-9e930cc35c74",
                "accountId"      : accountId,
                "memberIdList"   : [createNewMem.memberId],
                "start"          : currentDate,
                "invoiceDate"    : currentDate,
                "expirationDate" : ExpireDate,
                "frequency"      : "DAILY",
                "duration"       : "4",
                "items"          : [[
                                            "itemName"       : "Cycle",
                                            "itemId"         : "fd1ebc5e-1fab-120f-879f-4e930cc25c11",
                                            "version"        : 1,
                                            "price"          : 3000,
                                            "quantity"       : "1",
                                            "expirationStart": "PURCHASE",
                                            "type"           : "PRODUCT",
                                            "unlimited"      : "false"
                                    ]]
        ]
    }

    static def createMemberAccountRequest()
    {
        return [
                "locationId": Constant.fourthCreatedClientLocationId,
                "memberId"  : memberId,
                "payorId"   : payorId,
                account     : [
                        "name"       : "SchedulerTest",
                        "email"      : "schedulerTest@gmail.com",
                        "phone"      : "19075526043",
                        "sevaluation": sEvaluation,
                        "billingDate": currentDate,
                        paymentMethod: [
                                "type"   : "CASH",
                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                        ]
                ]
        ]
    }
}
