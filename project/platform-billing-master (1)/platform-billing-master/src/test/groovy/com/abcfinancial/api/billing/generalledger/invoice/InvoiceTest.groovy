package com.abcfinancial.api.billing.generalledger.invoice

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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

class InvoiceTest extends BaseTest
{
    def static accountId;
    def subscriptionMethod
    static def memberId = UUID.randomUUID();
    static def payorId = UUID.randomUUID();
    def subscriptionId;
    static def memberIdNull = null;
    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy');
    def createdMember
    def startDate = new Date().minus(1).format('MM-dd-yyyy');
    def endDate = new Date().minus(2).format('MM-dd-yyyy');
    def futureEndDate = new Date().plus(3).format('MM-dd-yyyy');
    def futureStartDate = new Date().plus(1).format('MM-dd-yyyy');
    def invalidStartDate = '04-31-2019';
    def invalidendDate = new Date().minus(5).format('MM-dd-yyyy');

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
        def requestMemAccn = createMemberAccountRequest()

        createdMember = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestMemAccn)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())


        accountId = createdMember.account.accountId;
    }

    @Test(dependsOnMethods = 'createMember')
    void subscriptionTest()
    {
        def request = createSubscriptionRequest()

        subscriptionMethod = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

    }


    @Test(dependsOnMethods = 'subscriptionTest')
    void getInvoices()
    {
        mvc().perform(get('/invoice/member/' + memberId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("getInvoices"))
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test(dependsOnMethods = 'getInvoices')
    void getPayorInvoice()
    {

        def createPayorInvoiceRequest = createPayorInvoiceRequest()
        parseJson(mvc().perform(post('/account/payor/invoice').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createPayorInvoiceRequest)))
                .andExpect(status().isCreated())
                .andDo(print()).andDo(document("createPayorInvoice"))
                .andReturn())
    }

    @Test(dependsOnMethods = 'subscriptionTest')
    void getInvoicesWithoutMemberId()
    {
        mvc().perform(get('/invoice/member/' + memberIdNull).header("Authorization", "Bearer ${bearerToken}"))
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = 'subscriptionTest')
    void getInvoicesWithStartDate()
    {
        mvc().perform(get('/invoice/member/' + memberId).header("Authorization", "Bearer ${bearerToken}")
                .param("startDate", startDate))
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test(dependsOnMethods = 'subscriptionTest')
    void getInvoicesWithEndDate()
    {
        mvc().perform(get('/invoice/member/' + memberId).header("Authorization", "Bearer ${bearerToken}")
                .param("endDate", endDate))
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test(enabled = true, dependsOnMethods = 'getInvoices')
    void getInvoicesWithInvalidStartDate()
    {
        mvc().perform(get('/invoice/member/' + memberId).header("Authorization", "Bearer ${bearerToken}")
                .param("startDate", invalidStartDate)
                .param("endDate", endDate))
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(enabled = true, dependsOnMethods = 'getInvoices')
    void getInvoicesWithFutureStartDateAndEndDate()
    {
        mvc().perform(get('/invoice/member/' + memberId).header("Authorization", "Bearer ${bearerToken}")
                .param("startDate", futureStartDate)
                .param("endDate", futureEndDate))
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(enabled = true, dependsOnMethods = 'getInvoices')
    void getInvoicesWithFutureStartDateAndPastEndDate()
    {
        mvc().perform(get('/invoice/member/' + memberId).header("Authorization", "Bearer ${bearerToken}")
                .param("startDate", startDate)
                .param("endDate", endDate))
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dataProvider = 'payorInvoiceVOFieldValidations', dependsOnMethods = 'getPayorInvoice')
    void testAccountVOValidator(HttpStatus expectedStatus, List<String> errorCodes, Map request) throws Exception
    {
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        mvc().perform(post('/account/payor/invoice').headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath('errors').isArray())
                .andExpect(jsonPath('errors', hasSize(errorCodes.size())))
                .andExpect(jsonPath('errors[*].code', containsInAnyOrder(errorCodes.toArray())))
    }

    @DataProvider
    static Object[][] payorInvoiceVOFieldValidations()
    {
        return [
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": null
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": createdClientLocationId,
                                "memberId"  : memberId,
                                "accountId" : null
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": createdClientLocationId,
                                "memberId"  : memberId,
                                "accountId" : UUID.randomUUID(),
                                "totalTax"  : null
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": createdClientLocationId,
                                "memberId"  : memberId,
                                "accountId" : UUID.randomUUID(),
                                "totalTax"  : -30
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": null
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": -30
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": 30,
                                "totalNetPrice"      : null
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": 30,
                                "totalNetPrice"      : -30
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": 30,
                                "totalAmount"        : null
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": 30,
                                "totalAmount"        : -30
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": 30,
                                "totalNetPrice"      : 30,
                                "items"              : null

                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": 30,
                                "totalNetPrice"      : 30,
                                "items"              : [[

                                                        ]]

                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": 30,
                                "totalNetPrice"      : 30,
                                "items"              : [[
                                                                "itemName": ""
                                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": 30,
                                "totalNetPrice"      : 30,
                                "items"              : [[
                                                                "itemName": " "
                                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": 30,
                                "totalNetPrice"      : 30,
                                "items"              : [[
                                                                "itemName": name,
                                                                "itemId"  : null
                                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": 30,
                                "totalNetPrice"      : 30,
                                "items"              : [[
                                                                "itemName": name,
                                                                "itemId"  : "00000000-0000-0000-0000-000000000000"
                                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"         : createdClientLocationId,
                                "memberId"           : memberId,
                                "accountId"          : UUID.randomUUID(),
                                "totalTax"           : 30,
                                "totalDiscountAmount": 30,
                                "totalNetPrice"      : 30,
                                "items"              : [[
                                                                "itemName": name,
                                                                "itemId"  : "00000000-0000-0000-0000-000000000001"
                                                        ]
                                ]
                        ]
                ]

        ]
    }

    def createPayorInvoiceRequest()
    {
        return [
                "locationId"         : createdClientLocationId,
                "memberId"           : memberId,
                "accountId"          : accountId,
                "salesEmployeeId"    : "ad8ebc9e-5fab-140f-879f-9e930cc35c74",
                "totalTax"           : 30.0,
                "totalAmount"        : 300.0,
                "totalDiscountAmount": 20,
                "totalNetPrice"      : 310.0,
                "items"              : [
                        [
                                "itemName"       : "dance",
                                "itemId"         : "fd1ebc5e-1fab-120f-879f-4e930cc25c11",
                                "version"        : 1,
                                "price"          : 200,
                                "discountCode"   : 1234,
                                "discountAmount" : 10,
                                "amountRemaining": 200,
                                "taxAmount"      : 20,
                                "type"           : "PRODUCT",
                                "quantity"       : "1",
                                "itemCategoryId" : "fd1ebc5e-1fab-120f-879f-4e930cc25c12"
                        ], [
                                "itemName"       : "sing",
                                "itemId"         : "fd1ebc5e-1fab-120f-879f-4e930cc25c12",
                                "version"        : 1,
                                "price"          : 100,
                                "discountAmount" : 10,
                                "amountRemaining": 200,
                                "taxAmount"      : 10,
                                "type"           : "PRODUCT",
                                "quantity"       : "1",
                                "itemCategoryId" : "fd1ebc5e-1fab-120f-879f-4e930cc25c12"
                        ]
                ]
        ]
    }

    def createSubscriptionRequest()
    {
        return [
                "locationId"     : createdClientLocationId,
                "salesEmployeeId": "ad8ebc9e-5fab-140f-879f-9e930cc35c74",
                "accountId"      : accountId,
                "memberIdList"   : [createdMember.memberId],
                "start"          : currentDate,
                "invoiceDate"    : currentDate,
                "expirationDate" : ExpireDate,
                "frequency"      : "DAILY",
                "duration"       : "4",
                "items"          : [[
                                            "itemName"       : "adcds",
                                            "itemId"         : "fd1ebc5e-1fab-120f-879f-4e930cc25c11",
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
                "locationId": createdClientLocationId,
                "memberId"  : memberId,
                "payorId"   : payorId,
                account     : [
                        "name"       : "Test",
                        "email"      : "test@gmail.com",
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
}
