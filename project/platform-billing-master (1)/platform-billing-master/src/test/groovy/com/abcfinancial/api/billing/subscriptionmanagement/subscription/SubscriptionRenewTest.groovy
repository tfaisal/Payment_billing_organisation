package com.abcfinancial.api.billing.subscriptionmanagement.subscription

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static com.abcfinancial.api.billing.utility.constant.Constant.currentDate
import static com.abcfinancial.api.billing.utility.constant.Constant.sEvaluation
import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SubscriptionRenewTest extends BaseTest
{

    static def accountId
    def subscriptionMethod
    static def memberId = UUID.randomUUID()
    static def payorId = UUID.randomUUID()
    static def createdMember
    static def ExpireDate = new Date().plus(15).format('MM-dd-yyyy');
    static def RENEWDATE = new Date().plus(16).format('MM-dd-yyyy');
    static def RENEWEXPDATE = new Date().plus(20).format('MM-dd-yyyy');

    @Autowired
    AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }


    @Test
    void createMember()
    {
        createdMember = parseJson(mvc().perform(post('/account/member').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createMemberAccountRequest())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

    }

    @Test(dependsOnMethods = 'createMember')
    void subscriptionTest()
    {
        def request = createSubscriptionRequest()

        request.collect { requests ->
            subscriptionMethod = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                    .content(toJson(requests)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("\$.subId").value(isUuid()))
                    .andExpect(jsonPath("\$.locationId").value(isUuid()))
                    .andExpect(jsonPath("\$.accountId").value(isUuid()))
                    .andReturn())
        }

    }

    @Test(dependsOnMethods = 'subscriptionTest')
    void getMemberSubscription()
    {
        mvc().perform(get('/subscription/member/{memberId}', createdMember.memberId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getMemberSubscription"))
                .andExpect(status().isOk())
    }

    @Test(dependsOnMethods = 'getMemberSubscription')
    void renewSubscription()
    {
        mvc().perform(put('/subscription/renew/{subscriptionId}', subscriptionMethod.subId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest())))
                .andDo(print())
                .andDo(document("renewSubscription"))
                .andExpect(status().isOk())
                .andReturn()
    }

    def createSubscriptionRequest()
    {
        return [
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : currentDate,
                        "invoiceDate"    : currentDate,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "DAILY",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : RENEWDATE,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": RENEWDATE,
                                "renewDuration"   : "1",
                                "renewType"       : "TERM",
                                "renewAmount"     : 1000,
                                "renewExpireDate" : RENEWEXPDATE
                        ],

                        "items"          : [[
                                                    "itemName"       : "adcds",
                                                    "itemId"         : UUID.randomUUID(),
                                                    "version"        : 1,
                                                    "price"          : 900,
                                                    "quantity"       : "1",
                                                    "expirationStart": "PURCHASE",
                                                    "type"           : "PRODUCT",
                                                    "unlimited"      : "false"
                                            ]]
                ]
        ]
    }

    static def updateRequest()
    {
        return [
                "locationId"     : createdMember.locationId,
                "salesEmployeeId": UUID.randomUUID(),
                "accountId"      : createdMember.account.accountId,
                "memberIdList"   : [createdMember.memberId],
                "start"          : currentDate,
                "invoiceDate"    : currentDate,
                "expirationDate" : ExpireDate,
                "frequency"      : "QUARTERLY",
                "duration"       : "4",
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

    static def createMemberAccountRequest()
    {
        return [
                "locationId": Constant.fourthCreatedClientLocationId,
                "memberId"  : memberId,
                "payorId"   : payorId,
                account     : [
                        "name"       : "SubscriptionService",
                        "email"      : "SubscriptionService@QA4LIFE.COM",
                        "phone"      : "19075526943",
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
