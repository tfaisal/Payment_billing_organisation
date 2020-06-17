package com.abcfinancial.api.billing.subscriptionmanagement.agreement

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SubscriptionAgreementRemoveCancelTest extends BaseTest
{
    static def accountId
    def subscriptionMethod
    static def memberId = UUID.randomUUID()
    static def payorId = UUID.randomUUID()
    static def createdMember
    static def ExpireDate = new Date().plus(16).format('MM-dd-yyyy');
    static def agreement
    static def cancelDate = new Date().plus(15).format('MM-dd-yyyy');
    static def RENEWDATE = new Date().plus(16).format('MM-dd-yyyy');
    static def RENEWEXPDATE = new Date().plus(20).format('MM-dd-yyyy');
    static def agreementNumber = "CBDEFG123123456"
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
        createdMember = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
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
    void subscriptionAgreementTest()
    {
        def request = subscriptionAgreementRequest()
        agreement = parseJson(mvc().perform(post('/agreement').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())
    }

    @Test(dependsOnMethods = 'subscriptionAgreementTest')
    void cancelAgreement()
    {
        mvc().perform(put('/cancel-agreement/{agreementNumber}', agreement.agreementNumber).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(cancelAgrementReq())))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test(dependsOnMethods = 'cancelAgreement')
    void removeCancelAgreement()
    {
        mvc().perform(put('/agreement/remove-cancel-agreement/{agreementNumber}', agreement.agreementNumber).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("removeCancelAgreement"))
                .andExpect(status().isOk())
                .andReturn()
    }

    def cancelAgrementReq()
    {
        return [
                "agrmCancellationDate": cancelDate
        ]
    }

    def subscriptionAgreementRequest()
    {
        return [
                "locationId"        : Constant.fourthCreatedClientLocationId,
                "documentIdList"    : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                "memberIdList"      : [
                        ["memberId": createdMember.memberId, "primary": true]
                ],
                "agreementNumber"   : agreementNumber,
                "subscriptionIdList": [
                        ["subId": subscriptionMethod.subId, "primary": false]

                ],
                "subscriptionList"  : [[
                                               "primary"        : true,
                                               "locationId"     : Constant.fourthCreatedClientLocationId,
                                               "salesEmployeeId": UUID.randomUUID(),
                                               "accountId"      : createdMember.account.accountId,
                                               "memberIdList"   : [createdMember.memberId],
                                               "start"          : Constant.currentDate,
                                               "invoiceDate"    : Constant.currentDate,
                                               "expirationDate" : ExpireDate,
                                               "frequency"      : "DAILY",
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
                                       ],
                                       [
                                               "primary"        : false,
                                               "locationId"     : Constant.fourthCreatedClientLocationId,
                                               "salesEmployeeId": UUID.randomUUID(),
                                               "accountId"      : createdMember.account.accountId,
                                               "memberIdList"   : [createdMember.memberId],
                                               "start"          : Constant.currentDate,
                                               "invoiceDate"    : Constant.currentDate,
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
                                                                           "itemName"       : "Yoga",
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

        ]
    }

    def createSubscriptionRequest()
    {
        return [
                [
                        "locationId"     : Constant.fourthCreatedClientLocationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : Constant.currentDate,
                        "invoiceDate"    : Constant.currentDate,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "DAILY",
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
                ],


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
                        "sevaluation": Constant.sEvaluation,
                        "billingDate": Constant.currentDate,
                        paymentMethod: [
                                "type"   : "CASH",
                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                        ]
                ]
        ]
    }

}
