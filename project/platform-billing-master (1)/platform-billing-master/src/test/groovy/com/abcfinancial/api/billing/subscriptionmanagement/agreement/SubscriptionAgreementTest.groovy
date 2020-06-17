package com.abcfinancial.api.billing.subscriptionmanagement.agreement

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SubscriptionAgreementTest extends BaseTest
{
    static def accountId
    def subscriptionMethod
    static def memberId = UUID.randomUUID()
    static def payorId = UUID.randomUUID()
    static def createdMember
    static def ExpireDate = new Date().plus(5).format('MM-dd-yyyy');
    static def Expire = new Date().minus(5).format('MM-dd-yyyy');
    static def startPast = new Date().minus(6).format('MM-dd-yyyy');
    static def agreement
    static def agreementNumber = "CBDEFG123456786"
    def subscription_2
    static def cancelDate = new Date().format('MM-dd-yyyy');
    static def agrMemberId = UUID.randomUUID()


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
    void cancelSubscription()
    {
        def cancelSubscriptionReq = cancelSubscriptionRequest()
        mvc().perform(put('/subscription/{subscriptionId}/cancel', subscriptionMethod.subId).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(cancelSubscriptionReq)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test(dependsOnMethods = 'subscriptionTest')
    void subscriptionAgreementTest()
    {
        def request = subscriptionAgreementRequest()
        agreement = parseJson(mvc().perform(post('/agreement').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print())
                .andDo(document("createAgreement"))
                .andExpect(status().isCreated())
                .andReturn())
    }

    @Test(dependsOnMethods = 'createMember')
    void subscriptionTest_2()
    {
        def request = createSubscriptionRequest()

        request.collect { requests ->
            subscription_2 = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                    .content(toJson(requests)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("\$.subId").value(isUuid()))
                    .andExpect(jsonPath("\$.locationId").value(isUuid()))
                    .andExpect(jsonPath("\$.accountId").value(isUuid()))
                    .andReturn())
        }
    }

    @Test(dependsOnMethods = 'subscriptionAgreementTest')
    void getRemainingSubscriptionValue()
    {
        mvc().perform(get('/agreement/remaining-agreement-value/{agreementNumber}', agreement.agreementNumber).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getRemainingAgreementValue"))
                .andExpect(status().isOk())
    }


    @Test(dependsOnMethods = 'subscriptionAgreementTest')
    void getAgreementByNumber()
    {
        mvc().perform(get('/agreement/{agreementNumber}', agreement.agreementNumber).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getAgreementByNumber"))
                .andExpect(status().isOk())
    }

    @Test(dependsOnMethods = 'subscriptionAgreementTest')
    void getAgreementByNumberWithParameter()
    {
        mvc().perform(get('/agreement/{agreementNumber}?cancelDate={cancelDate}', agreement.agreementNumber, cancelDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getAgreementByNumber"))
                .andExpect(status().isOk())
    }

    @Test(dependsOnMethods = 'subscriptionAgreementTest')
    void addAgreementSubscription()
    {
        def url = '/agreement/' + agreement.agreementNumber + '/subscription/' + subscription_2.subId
        parseJson(mvc().perform(post(url).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("addAgreementSubscription"))
                .andExpect(status().isCreated())
                .andReturn())
    }

    @Test(dependsOnMethods = 'subscriptionAgreementTest')
    void removeAgreementSubscription()
    {
        def url = '/agreement/' + agreement.agreementNumber + '/subscription/' + subscription_2.subId
        parseJson(mvc().perform(delete(url).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("removeAgreementSubscription"))
                .andExpect(status().isOk())
                .andReturn())
    }

    @Test(dependsOnMethods = 'getAgreementByNumber')
    void getAgreementNegative()
    {
        mvc().perform(get('/agreement/{agreementNumber}', UUID.randomUUID()).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())
    }

    @Test(dependsOnMethods = 'getAgreementNegative')
    void subscriptionAgreementTests()
    {
        def agreements = parseJson(mvc().perform(post('/agreement').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(subscriptionAgreement())))
                .andDo(print())

                .andExpect(status().isCreated())
                .andReturn())
    }

    @Test(dependsOnMethods = 'subscriptionAgreementTest')
    void addMembersAgreement()
    {
        parseJson(mvc().perform(put('/agreement/{agreementNumber}', agreement.agreementNumber).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(addMembersAgreementRequest())))
                .andDo(print()).andDo(document("addAgreementMembersApi"))
                .andExpect(status().isOk())
                .andReturn());
    }

    @Test(dependsOnMethods = 'subscriptionAgreementTest')
    void removeMembersAgreement()
    {
        parseJson(mvc().perform(delete('/agreement/{agreementNumber}', agreement.agreementNumber).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(removeMembersAgreementRequest())))
                .andDo(print()).andDo(document("removeAgreementMembersApi"))
                .andExpect(status().isOk())
                .andReturn());
    }

    @Test(dependsOnMethods = 'subscriptionAgreementTest')
    void removeMembersAgreementNegativeTest()
    {
        parseJson(mvc().perform(delete('/agreement/{agreementNumber}', agreement.agreementNumber).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(removeMembersAgreementNegative01Request())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn());
    }

    def addMembersAgreementRequest()
    {
        return [
                "memberIdList": [agrMemberId],
        ]
    }

    def removeMembersAgreementRequest()
    {
        return [
                "memberIdList": [agrMemberId],
        ]
    }

    def removeMembersAgreementNegative01Request()
    {
        return [
                "memberIdList": [createdMember.memberId],
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
                "campaign"          : "Agreement Campaign",
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
                                               "frequency"      : "DAILY",
                                               "start"          : Constant.currentDate,
                                               "invoiceDate"    : Constant.currentDate,
                                               "openEnded"      : true,
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

                ]

        ]
    }

    def subscriptionAgreement()
    {
        return [

                "locationId"        : Constant.fourthCreatedClientLocationId,
                "documentIdList"    : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                "memberIdList"      : [
                        ["memberId": createdMember.memberId, "primary": true]
                ],
                "agreementNumber"   : "14792",
                "campaign"          : "Agreement Campaign",
                "subscriptionIdList": [
                        ["subId": subscriptionMethod.subId, "primary": false]

                ],
                "subscriptionList"  : [[
                                               "primary"        : true,
                                               "locationId"     : Constant.fourthCreatedClientLocationId,
                                               "salesEmployeeId": UUID.randomUUID(),
                                               "accountId"      : createdMember.account.accountId,
                                               "memberIdList"   : [createdMember.memberId],
                                               "start"          : startPast,
                                               "invoiceDate"    : startPast,
                                               "expirationDate" : Expire,
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
                                               "frequency"      : "DAILY",
                                               "start"          : Constant.currentDate,
                                               "invoiceDate"    : Constant.currentDate,
                                               "openEnded"      : true,
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

    static def cancelSubscriptionRequest()
    {
        return [
                "subCancellationDate": calculateDate()
        ]
    }

    static def calculateDate()
    {
        LocalDate localDate = LocalDate.now().plusDays(2L)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        return localDate.format(formatter)
    }

}
