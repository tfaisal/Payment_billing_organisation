package com.abcfinancial.api.billing.subscriptionmanagement.subscription

import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class SubscriptionNegativeTest extends BaseTest
{

    static def accountId
    def subscriptionMethod
    static def memberId = UUID.randomUUID()
    static def payorId = UUID.randomUUID();
    static def createMember
    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy');
    static def NEWExpireDate = new Date().minus(1).format('MM-dd-yyyy');
    static def RENEWDATE = new Date().plus(16).format('MM-dd-yyyy');
    static def RENEWEXPDATE = new Date().plus(20).format('MM-dd-yyyy');
    static def RENEWXPDATE = new Date().plus(15).format('MM-dd-yyyy');
    static def STARTBEFORE = new Date().minus(95).format('MM-dd-yyyy');
    static def STARTBEFOREDAYS = new Date().minus(90).format('MM-dd-yyyy');
    static def STARTBEFOREINVOICEDAYS = new Date().minus(60).format('MM-dd-yyyy');
    static def STARTBEFOREINVOICEDAY = new Date().minus(70).format('MM-dd-yyyy');
    static def PASTSTART = new Date().minus(91).format('MM-dd-yyyy');

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
        createMember = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createMemberAccountRequest())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

    }

    @Test(dataProvider = 'subscriptionVOFieldValidations', dependsOnMethods = 'createMember')
    void testSubscriptionVOValidator(HttpStatus expectedStatus, List<String> errorCodes, Map request) throws Exception
    {
        mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}").content(toJson(request)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath('errors').isArray())
                .andExpect(jsonPath('errors', hasSize(errorCodes.size())))
                .andExpect(jsonPath('errors[*].code', containsInAnyOrder(errorCodes.toArray())))
    }

    @Test(dependsOnMethods = 'testSubscriptionVOValidator')
    void getMemberSubscriptionTestWith_404_statusCode()
    {
        def memberIdData = UUID.randomUUID();
        mvc().perform(get('/subscription/member/' + memberIdData).header("Authorization", "Bearer ${bearerToken}")
        )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
    }

    @DataProvider
    static Object[][] subscriptionVOFieldValidations()
    {
        return [
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "autoRenew"      : true,
                                "renewType"      : "TERM",
                                "renewalOptions" : [
                                        "renewDate"      : RENEWDATE,
                                        "renewFrequency" : "DAILY",
                                        "renewDuration"  : "4",
                                        "renewType"      : "TERM",
                                        "renewAmount"    : 1002,
                                        "renewExpireDate": RENEWEXPDATE
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : STARTBEFOREDAYS,
                                "invoiceDate"    : STARTBEFOREINVOICEDAYS,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "autoRenew"      : true,
                                "renewType"      : "TERM",
                                "renewalOptions" : [
                                        "renewDate"       : currentDate,
                                        "renewInvoiceDate": STARTBEFORE,
                                        "renewFrequency"  : "DAILY",
                                        "renewDuration"   : "4",
                                        "renewType"       : "TERM",
                                        "renewAmount"     : 1002,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : STARTBEFOREDAYS,
                                "invoiceDate"    : STARTBEFOREINVOICEDAYS,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "autoRenew"      : true,
                                "renewType"      : "TERM",
                                "renewalOptions" : [
                                        "renewDate"       : STARTBEFOREDAYS,
                                        "renewFrequency"  : "DAILY",
                                        "renewInvoiceDate": STARTBEFOREINVOICEDAY,
                                        "renewDuration"   : "4",
                                        "renewType"       : "TERM",
                                        "renewAmount"     : 1002,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : currentDate,
                                "invoiceDate"    : currentDatePlus,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "autoRenew"      : true,
                                "renewType"      : "TERM",
                                "renewalOptions" : [
                                        "renewDate"       : STARTBEFORE,
                                        "renewFrequency"  : "DAILY",
                                        "renewInvoiceDate": STARTBEFOREINVOICEDAYS,
                                        "renewDuration"   : "4",
                                        "renewType"       : "TERM",
                                        "renewAmount"     : 1002,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : STARTBEFOREDAYS,
                                "invoiceDate"    : STARTBEFOREINVOICEDAYS,
                                "expirationDate" : currentDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "autoRenew"      : true,
                                "renewType"      : "TERM",
                                "renewalOptions" : [
                                        "renewDate"       : STARTBEFOREINVOICEDAYS,
                                        "renewFrequency"  : "DAILY",
                                        "renewInvoiceDate": STARTBEFOREINVOICEDAY,
                                        "renewDuration"   : "4",
                                        "renewType"       : "TERM",
                                        "renewAmount"     : 1002,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "autoRenew"      : true,
                                "renewType"      : "TERM",
                                "renewalOptions" : [
                                        "renewDate"      : RENEWDATE,
                                        "renewFrequency" : "DAILY",
                                        "renewDuration"  : "4",
                                        "renewType"      : "TERM",
                                        "renewAmount"    : 1002,
                                        "renewExpireDate": RENEWEXPDATE
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "autoRenew"      : true,
                                "renewType"      : "TERM",
                                "renewalOptions" : [
                                        "renewDate"       : RENEWDATE,
                                        "renewInvoiceDate": RENEWDATE,
                                        "renewDuration"   : "4",
                                        "renewType"       : "TERM",
                                        "renewAmount"     : 1003,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
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
                                        "renewAmount"     : 1004,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
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
                                        "renewDuration"   : "-4",
                                        "renewType"       : "TERM",
                                        "renewAmount"     : 1005,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
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
                                        "renewAmount"     : 1006,
                                        "renewExpireDate" : RENEWXPDATE
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
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
                                        "renewInvoiceDate": RENEWXPDATE,
                                        "renewDuration"   : "4",
                                        "renewType"       : "TERM",
                                        "renewAmount"     : 1007,
                                        "renewExpireDate" : RENEWXPDATE
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
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
                                        "renewType"       : "OPEN",
                                        "renewAmount"     : 1100
                                ],
                                "items"          : [[
                                                            "itemName"       : "adcds",
                                                            "itemId"         : UUID.randomUUID(),
                                                            "version"        : 1,
                                                            "price"          : 1008,
                                                            "quantity"       : "1",
                                                            "expirationStart": "PURCHASE",
                                                            "type"           : "PRODUCT",
                                                            "unlimited"      : "false"
                                                    ]]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
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
                                        "renewType"       : "OPEN",
                                ],
                                "items"          : [[
                                                            "itemName"       : "adcds",
                                                            "itemId"         : UUID.randomUUID(),
                                                            "version"        : 1,
                                                            "price"          : 1009,
                                                            "quantity"       : "1",
                                                            "expirationStart": "PURCHASE",
                                                            "type"           : "PRODUCT",
                                                            "unlimited"      : "false"
                                                    ]]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
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
                                        "renewType"       : "OPEN",
                                        "renewAmount"     : -1100
                                ],
                                "items"          : [[
                                                            "itemName"       : "adcds",
                                                            "itemId"         : UUID.randomUUID(),
                                                            "version"        : 1,
                                                            "price"          : 1010,
                                                            "quantity"       : "1",
                                                            "expirationStart": "PURCHASE",
                                                            "type"           : "PRODUCT",
                                                            "unlimited"      : "false"
                                                    ]]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
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
                                        "renewInvoiceDate": RENEWXPDATE,
                                        "renewDuration"   : "4",
                                        "renewType"       : "TERM",
                                        "renewAmount"     : 1011,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
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
                                        "renewAmount"     : 1012,
                                        "renewExpireDate" : RENEWEXPDATE
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
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start": ""
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"      : currentDate,
                                "invoiceDate": ""
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"      : currentDate,
                                "invoiceDate": currentDate
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": NEWExpireDate
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "openEnded"     : true,
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ""
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : ""
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "DAILY",
                                "duration"      : "4",
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "WEEKLY",
                                "duration"      : "4",
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "EVERY_OTHER_WEEK",
                                "duration"      : "4",
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "MONTHLY",
                                "duration"      : "4",
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "EVERY_OTHER_MONTH",
                                "duration"      : "4",
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "QUARTERLY",
                                "duration"      : "4",
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "SEMIANNUALLY",
                                "duration"      : "4",
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "ANNUALLY",
                                "duration"      : "4",
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "ANNUALLY",
                                "duration"      : "4",
                                "locationId"    : ""
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "ANNUALLY",
                                "duration"      : "4",
                                "locationId"    : createMember.locationId,
                                "memberId"      : "",
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "ANNUALLY",
                                "duration"      : "4",
                                "locationId"    : createMember.locationId,
                                "memberId"      : createMember.memberId,
                                "accountId"     : ""
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "start"         : currentDate,
                                "invoiceDate"   : currentDate,
                                "expirationDate": ExpireDate,
                                "frequency"     : "ANNUALLY",
                                "duration"      : "4",
                                "locationId"    : createMember.locationId,
                                "memberId"      : createMember.memberId,
                                "accountId"     : createMember.account.accountId,
                                "duration"      : "-4",
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "items"          : [[
                                                            "itemName"       : "",
                                                            "itemId"         : UUID.randomUUID(),
                                                            "version"        : 1,
                                                            "price"          : 2117,
                                                            "quantity"       : "1",
                                                            "expirationStart": "PURCHASE",
                                                            "type"           : "PRODUCT",
                                                            "unlimited"      : "false"
                                                    ]]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "items"          : [[
                                                            "itemName"       : "Yoga",
                                                            "itemId"         : "",
                                                            "version"        : 1,
                                                            "price"          : 2117,
                                                            "quantity"       : "1",
                                                            "expirationStart": "PURCHASE",
                                                            "type"           : "PRODUCT",
                                                            "unlimited"      : "false"
                                                    ]]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "items"          : [[
                                                            "itemName"       : "Yoga",
                                                            "itemId"         : UUID.randomUUID(),
                                                            "version"        : 1,
                                                            "quantity"       : "1",
                                                            "expirationStart": "PURCHASE",
                                                            "type"           : "PRODUCT",
                                                            "unlimited"      : "false"
                                                    ]]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "items"          : [[
                                                            "itemName"       : "Yoga",
                                                            "itemId"         : UUID.randomUUID(),
                                                            "version"        : 1,
                                                            "price"          : -2117,
                                                            "quantity"       : "1",
                                                            "expirationStart": "PURCHASE",
                                                            "type"           : "PRODUCT",
                                                            "unlimited"      : "false"
                                                    ]]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberId"       : createMember.memberId,
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "items"          : [[
                                                            "itemName"       : "Yoga",
                                                            "itemId"         : UUID.randomUUID(),
                                                            "version"        : 1,
                                                            "price"          : 2117,
                                                            "quantity"       : "1",
                                                            "expirationStart": "PURCHASE",
                                                            "type"           : "",
                                                            "unlimited"      : "false"
                                                    ]]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : UUID.randomUUID(),
                                "memberId"       : createMember.memberId,
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "autoRenew"      : true,
                                "renewType"      : "TERM",
                                "renewalOptions" : [
                                        "renewDate"       : "",
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
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
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
                                        "renewAmount"     : "",
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
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
                                        "renewAmount"     : -105,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
                                "frequency"      : "",
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
                                "frequency"      : "WEEKLY",
                                "duration"       : "-4",
                                "openEnded"      : true,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [""],
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
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
                                "frequency"      : "DAILY",
                                "start"          : "04-05-2019",
                                "invoiceDate"    : "04-01-2019",
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
                                "frequency"      : "WEEKLY",
                                "duration"       : "4",
                                "start"          : "04-05-2019",
                                "invoiceDate"    : "04-01-2019",
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
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
                                        "renewInvoiceDate": "05-04-2019",
                                        "renewDuration"   : "1",
                                        "renewType"       : "TERM",
                                        "renewAmount"     : 1000,
                                        "renewExpireDate" : "04-03-2019"
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
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
                                        "renewExpireDate" : ""
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
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

                                "items"          : [[]]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
                                "frequency"      : "DAILY",
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
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
                                                    ]],
                                "renewalOptions" : [
                                        "renewDate"       : RENEWDATE,
                                        "renewFrequency"  : "DAILY",
                                        "renewInvoiceDate": RENEWDATE,
                                        "renewDuration"   : "1",
                                        "renewType"       : "TERM",
                                        "renewAmount"     : 1000,
                                        "renewExpireDate" : RENEWEXPDATE
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
                                "frequency"      : "DAILY",
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "duration"       : 0,
                                "expirationDate" : "",
                                "openEnded"      : false,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "expirationDate" : ExpireDate,
                                "frequency"      : "DAILY",
                                "duration"       : "4",
                                "autoRenew"      : false,
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
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
                                "frequency"      : "DAILY",
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "openEnded"      : true,
                                "autoRenew"      : true,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
                                "frequency"      : "DAILY",
                                "start"          : PASTSTART,
                                "invoiceDate"    : currentDate,
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
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
                                        "renewInvoiceDate": RENEWEXPDATE,
                                        "renewDuration"   : "1",
                                        "renewType"       : "TERM",
                                        "renewAmount"     : 1000,
                                        "renewExpireDate" : RENEWDATE
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"     : createMember.locationId,
                                "salesEmployeeId": UUID.randomUUID(),
                                "accountId"      : createMember.account.accountId,
                                "memberIdList"   : [createMember.memberId],
                                "frequency"      : "DAILY",
                                "start"          : currentDate,
                                "invoiceDate"    : currentDate,
                                "expirationDate" : "",
                                "openEnded"      : false,
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
                        "name"       : "Ankit",
                        "email"      : "test@QA4LIFE.COM",
                        "phone"      : "19075526443",
                        "sevaluation": sEvaluation,
                        "billingDate": currentDate,
                        paymentMethod: [
                                "type"   : "CASH",
                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                        ]
                ]
        ]
    }

    def createProcessorRequest()
    {
        return [
                "accountId"     : "5b47534bf58fa7398df15d38",
                "organizationId": "5b115891b6cdfd755de3d4bd",
                "locationId"    : createMember.locationId
        ]
    }
}
