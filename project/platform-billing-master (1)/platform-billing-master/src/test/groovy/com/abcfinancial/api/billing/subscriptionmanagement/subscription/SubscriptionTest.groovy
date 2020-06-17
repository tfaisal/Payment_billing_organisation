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

class SubscriptionTest extends BaseTest
{

    static def accountId
    def subscriptionMethod
    static def memberId = UUID.randomUUID()
    static def payorId = UUID.randomUUID()
    static def createdMember
    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy');
    static def EXPIRERENEWDATE = new Date().minus(1).format('MM-dd-yyyy');
    static def PASTRENEWDATE = new Date().plus(2).format('MM-dd-yyyy');
    static def PASTSTART = new Date().minus(15).format('MM-dd-yyyy');
    static def PASTINVOICE = new Date().minus(15).format('MM-dd-yyyy');
    static def RENEWDATE = new Date().plus(16).format('MM-dd-yyyy');
    static def RENEWEXPDATE = new Date().plus(20).format('MM-dd-yyyy');
    static def START = new Date().plus(1).format('MM-dd-yyyy');
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
                    .andDo(print()).andDo(document("createSubscription"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("\$.subId").value(isUuid()))
                    .andExpect(jsonPath("\$.locationId").value(isUuid()))
                    .andExpect(jsonPath("\$.accountId").value(isUuid()))
                    .andReturn())
        }

    }

    @Test(dependsOnMethods = 'subscriptionTest')
    void getSubscription()
    {
        mvc().perform(get('/subscription/{subscriptionId}', subscriptionMethod.subId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getSubscription"))
                .andExpect(status().isOk())
    }


    @Test(dependsOnMethods = 'subscriptionTest')
    void getRemainingSubscriptionValue()
    {
        mvc().perform(get('/subscription/remaining-subscription-value/{subId}', subscriptionMethod.subId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getRemainingSubscriptionValue"))
                .andExpect(status().isOk())
    }

    @Test(dependsOnMethods = 'subscriptionTest')
    void getRemainingSubscriptionValueWithUnknownSubscriptionId()
    {
        mvc().perform(get('/subscription/remaining-subscription-value/{subId}', UUID.randomUUID()).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
    }

    @Test(dependsOnMethods = 'subscriptionTest')
    void getRemainingSubscriptionValueWithRenewSubscriptionId()
    {
        mvc().perform(get('/subscription/remaining-subscription-value/{subId}', subscriptionMethod.renewalOptions.renewSubId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
    }

    @Test(dependsOnMethods = 'getSubscription')
    void getMemberSubscriptions()
    {
        mvc().perform(get('/subscription/member/{memberId}', createdMember.memberId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getMemberSubscriptions"))
                .andExpect(status().isOk())
    }

    @Test(dependsOnMethods = 'getMemberSubscriptions')
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
                        "duration"       : "1",
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
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
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
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "frequency"      : "DAILY",
                        "start"          : START,
                        "invoiceDate"    : START,
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "frequency"      : "DAILY",
                        "start"          : START,
                        "invoiceDate"    : START,
                        "openEnded"      : true,
                        "items"          : [[
                                                    "itemName"       : "adcds",
                                                    "itemId"         : UUID.randomUUID(),
                                                    "version"        : 1,
                                                    "price"          : 2117,
                                                    "quantity"       : "1",
                                                    "expirationStart": "",
                                                    "type"           : "PRODUCT",
                                                    "unlimited"      : "true"
                                            ]]
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "frequency"      : "DAILY",
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "frequency"      : "WEEKLY",
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "frequency"      : "EVERY_OTHER_WEEK",
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "frequency"      : "MONTHLY",
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "frequency"      : "EVERY_OTHER_MONTH",
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : currentDate,
                        "invoiceDate"    : currentDate,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "ANNUALLY",
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : currentDate,
                        "invoiceDate"    : currentDate,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "SEMIANNUALLY",
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
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberId"       : createdMember.memberId,
                        "start"          : currentDate,
                        "invoiceDate"    : currentDate,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "SEMIANNUALLY",
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
                                "renewType"       : "OPEN",
                                "renewAmount"     : 1100
                        ],
                        "items"          : [[
                                                    "itemName"       : "adcds",
                                                    "itemId"         : UUID.randomUUID(),
                                                    "version"        : 1,
                                                    "price"          : 1000,
                                                    "quantity"       : "1",
                                                    "expirationStart": "PURCHASE",
                                                    "type"           : "PRODUCT",
                                                    "unlimited"      : "false"
                                            ]]
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "DAILY",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : currentDate,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": currentDate,
                                "renewType"       : "OPEN",
                                "renewAmount"     : 1100
                        ],
                        "items"          : [[
                                                    "itemName"       : "adcds",
                                                    "itemId"         : UUID.randomUUID(),
                                                    "version"        : 1,
                                                    "price"          : 1000,
                                                    "quantity"       : "1",
                                                    "expirationStart": "PURCHASE",
                                                    "type"           : "PRODUCT",
                                                    "unlimited"      : "false"
                                            ]]
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "DAILY",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : currentDate,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": currentDate,
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "DAILY",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : currentDate,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": currentDate,
                                "renewDuration"   : "4",
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "DAILY",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : EXPIRERENEWDATE,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": EXPIRERENEWDATE,
                                "renewDuration"   : "4",
                                "renewType"       : "TERM",
                                "renewAmount"     : 1000,
                                "renewExpireDate" : currentDate
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "WEEKLY",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : EXPIRERENEWDATE,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": EXPIRERENEWDATE,
                                "renewDuration"   : "4",
                                "renewType"       : "TERM",
                                "renewAmount"     : 1000,
                                "renewExpireDate" : currentDate
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "EVERY_OTHER_WEEK",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : EXPIRERENEWDATE,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": EXPIRERENEWDATE,
                                "renewDuration"   : "4",
                                "renewType"       : "TERM",
                                "renewAmount"     : 1000,
                                "renewExpireDate" : currentDate
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "MONTHLY",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : EXPIRERENEWDATE,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": EXPIRERENEWDATE,
                                "renewDuration"   : "4",
                                "renewType"       : "TERM",
                                "renewAmount"     : 1000,
                                "renewExpireDate" : currentDate
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "EVERY_OTHER_MONTH",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : EXPIRERENEWDATE,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": EXPIRERENEWDATE,
                                "renewDuration"   : "4",
                                "renewType"       : "TERM",
                                "renewAmount"     : 1000,
                                "renewExpireDate" : currentDate
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "ANNUALLY",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : EXPIRERENEWDATE,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": EXPIRERENEWDATE,
                                "renewDuration"   : "4",
                                "renewType"       : "TERM",
                                "renewAmount"     : 1000,
                                "renewExpireDate" : currentDate
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "QUARTERLY",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : EXPIRERENEWDATE,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": EXPIRERENEWDATE,
                                "renewDuration"   : "4",
                                "renewType"       : "TERM",
                                "renewAmount"     : 1000,
                                "renewExpireDate" : currentDate
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
                ],
                [
                        "locationId"     : createdMember.locationId,
                        "salesEmployeeId": UUID.randomUUID(),
                        "accountId"      : createdMember.account.accountId,
                        "memberIdList"   : [createdMember.memberId],
                        "start"          : PASTSTART,
                        "invoiceDate"    : PASTINVOICE,
                        "expirationDate" : ExpireDate,
                        "frequency"      : "SEMIANNUALLY",
                        "duration"       : "4",
                        "autoRenew"      : true,
                        "renewType"      : "TERM",
                        "renewalOptions" : [
                                "renewDate"       : EXPIRERENEWDATE,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": EXPIRERENEWDATE,
                                "renewDuration"   : "4",
                                "renewType"       : "TERM",
                                "renewAmount"     : 1000,
                                "renewExpireDate" : currentDate
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
                ],
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
                                "renewDate"       : PASTRENEWDATE,
                                "renewFrequency"  : "DAILY",
                                "renewInvoiceDate": RENEWDATE,
                                "renewType"       : "OPEN",
                                "renewAmount"     : 1599
                        ],
                        "items"          : [[
                                                    "itemName"       : "adcds",
                                                    "itemId"         : UUID.randomUUID(),
                                                    "version"        : 1,
                                                    "price"          : 1000,
                                                    "quantity"       : "1",
                                                    "expirationStart": "PURCHASE",
                                                    "type"           : "PRODUCT",
                                                    "unlimited"      : "false"
                                            ]]
                ],
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
                                "renewDuration"   : "4",
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
                ],
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

    static def createProcessorRequest()
    {
        return [
                "accountId"     : "5b47534bf58fa7398df15d38",
                "organizationId": "5b115891b6cdfd755de3d4bd",
                "locationId"    : createdMember.locationId
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
