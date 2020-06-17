package com.abcfinancial.api.billing.subscriptionmanagement.agreement

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.utility.constant.Constant
import org.apache.commons.lang.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SubscriptionAgreementNegativeTest extends BaseTest
{
    static def accountId
    def subscriptionMethod
    static def memberId = UUID.randomUUID()
    static def payorId = UUID.randomUUID()
    static def createdMember
    static def ExpireDate = new Date().plus(5).format('MM-dd-yyyy');
    static def agreement
    static def cancelDate = new Date().format('MM-dd-yyyy');
    public static def campaign = RandomStringUtils.randomAlphabetic(101);

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
                .andDo(document("createAgreement"))
                .andExpect(status().isCreated())
                .andReturn())
    }

    @Test(dataProvider = 'agreementVOFieldValidations', dependsOnMethods = 'createMember')
    void testAgreementVOValidator(HttpStatus expectedStatus, List<String> errorCodes, Map request) throws Exception
    {
        mvc().perform(post('/agreement').header("Authorization", "Bearer ${bearerToken}").content(toJson(request)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath('errors').isArray())
                .andExpect(jsonPath('errors', hasSize(errorCodes.size())))
                .andExpect(jsonPath('errors[*].code', containsInAnyOrder(errorCodes.toArray())))
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

    @Test(dependsOnMethods = 'subscriptionAgreementTest')
    void getAgreementByNumberWithParameter()
    {
        mvc().perform(get('/agreement/{agreementNumber}?cancelDate={cancelDate}', agreement.agreementNumber, cancelDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
    }

    @Test(dependsOnMethods = 'subscriptionAgreementTest')
    void getAgreementByNumberWithoutParameter()
    {
        mvc().perform(get('/agreement/{agreementNumber}', agreement.agreementNumber).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
    }

    def subscriptionAgreementRequest()
    {
        return [

                "locationId"        : Constant.fourthCreatedClientLocationId,
                "documentIdList"    : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                "memberIdList"      : [
                        ["memberId": createdMember.memberId, "primary": true]
                ],
                "agreementNumber"   : "ABDEFG123456789",
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

    def createSubscriptionRequest()
    {
        return [
                [
                        "locationId"     : createdMember.locationId,
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

    @DataProvider
    static Object[][] agreementVOFieldValidations()
    {
        return [
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : createdMember.locationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [createdMember.memberId],
                                "agreementNumber" : "ABDEFG12345678@",
                                "subscriptionList": [[
                                                             "primary"        : true,
                                                             "locationId"     : createdMember.locationId,
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
                                                             "locationId"     : createdMember.locationId,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : createdMember.locationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [createdMember.memberId],
                                "agreementNumber" : "",
                                "subscriptionList": [[
                                                             "primary"        : true,
                                                             "locationId"     : createdMember.locationId,
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
                                                     ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : createdMember.locationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [""],
                                "agreementNumber" : "ABDEFG123456789",
                                "subscriptionList": [[
                                                             "primary"        : true,
                                                             "locationId"     : createdMember.locationId,
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
                                                     ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : createdMember.locationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [createdMember.memberId],
                                "agreementNumber" : "ABDEFG1234567891450",
                                "subscriptionList": [[
                                                             "primary"        : true,
                                                             "locationId"     : createdMember.locationId,
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
                                                     ]

                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : createdMember.locationId,
                                "documentIdList"  : [" "],
                                "memberIdList"    : [createdMember.memberId],
                                "agreementNumber" : "ABDEFG123456789",
                                "subscriptionList": [[
                                                             "primary"        : true,
                                                             "locationId"     : createdMember.locationId,
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
                                                     ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createdMember.locationId,
                                "documentIdList" : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"   : [createdMember.memberId],
                                "agreementNumber": "ABDEFG123456789",

                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : "",
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [createdMember.memberId],
                                "agreementNumber" : "ABDEFG123456789",
                                "subscriptionList": [[
                                                             "primary"        : true,
                                                             "locationId"     : createdMember.locationId,
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
                                                     ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : createdMember.locationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [createdMember.memberId],
                                "agreementNumber" : "ABDEFG123456780",
                                "subscriptionList": [[
                                                             "primary"        : false,
                                                             "locationId"     : createdMember.locationId,
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
                                                     ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : createdMember.locationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [createdMember.memberId],
                                "agreementNumber" : "ABDEFG123456783",
                                "subscriptionList": [[
                                                             "primary"        : true,
                                                             "locationId"     : createdMember.locationId,
                                                             "salesEmployeeId": UUID.randomUUID(),
                                                             "accountId"      : createdMember.account.accountId,
                                                             "memberIdList"   : ["2efffd77-12ec-46eb-98a2-82990d4c5f51"],
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
                                                     ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : createdMember.locationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [createdMember.memberId],
                                "agreementNumber" : "ABDEFG123456781",
                                "subscriptionList": [[
                                                             "primary"        : true,
                                                             "locationId"     : createdMember.locationId,
                                                             "salesEmployeeId": UUID.randomUUID(),
                                                             "accountId"      : createdMember.account.accountId,
                                                             "memberIdList"   : [""],
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
                                                     ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : createdMember.locationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [UUID.randomUUID()],
                                "agreementNumber" : "ABDEFG123456782",
                                "subscriptionList": [[
                                                             "primary"        : true,
                                                             "locationId"     : createdMember.locationId,
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
                                                     ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createdMember.locationId,
                                "documentIdList" : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"   : [createdMember.memberId, ""],
                                "agreementNumber": "ABDEFG123456789",

                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createdMember.locationId,
                                "documentIdList" : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"   : [],
                                "agreementNumber": "ABDEFG123456789",

                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createdMember.locationId,
                                "documentIdList" : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"   : [createdMember.memberId, createdMember.memberId],
                                "agreementNumber": "ABDEFG123456789",

                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : "",
                                "documentIdList" : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"   : [createdMember.memberId],
                                "agreementNumber": "ABDEFG123456789",

                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createdMember.locationId,
                                "documentIdList" : [""],
                                "memberIdList"   : [createdMember.memberId],
                                "agreementNumber": "ABDEFG123456789",

                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createdMember.locationId,
                                "documentIdList" : ["eaca4410-3686-4eda-87c3-1320d15e4b94", ""],
                                "memberIdList"   : [createdMember.memberId],
                                "agreementNumber": "ABDEFG123456789",

                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : Constant.fourthCreatedClientLocationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [
                                        ["memberId": createdMember.memberId, "primary": true]
                                ],
                                "agreementNumber" : "awgb",
                                "campaign"        : "Agreement Campaign",
                                "subscriptionList": [[
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

                                                     ]

                                ]
                        ]

                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [

                                "locationId"      : Constant.fourthCreatedClientLocationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [
                                        ["memberId": createdMember.memberId, "primary": true]
                                ],
                                "agreementNumber" : "78945j",
                                "campaign"        : "Agreement Campaign",

                                "subscriptionList": [[
                                                             "primary"        : true,
                                                             "locationId"     : UUID.randomUUID(),
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"      : Constant.fourthCreatedClientLocationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [
                                        ["memberId": createdMember.memberId, "primary": true]
                                ],
                                "agreementNumber" : "14561",
                                "campaign"        : "Agreement Campaign",

                                "subscriptionList": [[
                                                             "primary"        : true,
                                                             "locationId"     : Constant.fourthCreatedClientLocationId,
                                                             "salesEmployeeId": UUID.randomUUID(),
                                                             "accountId"      : createdMember.account.accountId,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [


                                "locationId"      : Constant.fourthCreatedClientLocationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [
                                        ["memberId": createdMember.memberId, "primary": true]
                                ],
                                "agreementNumber" : "12gr",
                                "campaign"        : "Agreement Campaign",
                                "subscriptionList": [[
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
                                                             "primary"        : true,
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

                ],

                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [


                                "locationId"      : Constant.fourthCreatedClientLocationId,
                                "documentIdList"  : ["eaca4410-3686-4eda-87c3-1320d15e4b94"],
                                "memberIdList"    : [
                                        ["memberId": createdMember.memberId, "primary": true]
                                ],
                                "agreementNumber" : "12qwery",
                                "campaign"        : campaign,
                                "subscriptionList": [[
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
                ],

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
