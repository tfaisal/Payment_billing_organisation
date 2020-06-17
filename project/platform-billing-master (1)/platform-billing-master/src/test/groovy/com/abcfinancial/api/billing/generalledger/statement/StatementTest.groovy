package com.abcfinancial.api.billing.generalledger.statement

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class StatementTest extends BaseTest
{
    def accountId
    static def memberId = UUID.randomUUID()
    static def payorId = UUID.randomUUID()
    def statementId
    def statement
    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy')

    @Autowired
    AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }

    @Test(dependsOnMethods = 'createEvaluationStatement')
    void getStatementTest()
    {
        def url = "/account/" + accountId + "/statement"
        mvc().perform(get(url).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("getStatement"))
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test
    void createEvaluationStatement()
    {

        def requestMemAccn = createMemberAccountRequest()

        def createdMember = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestMemAccn)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())


        accountId = createdMember.account.accountId

        def createPayorInvoiceRequest = createPayorInvoiceRequest()
        parseJson(mvc().perform(post('/account/payor/invoice').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createPayorInvoiceRequest)))
                .andExpect(status().isCreated())
                .andDo(print()).andDo(document("createPayorInvoice"))
                .andReturn())

        def createEvaluationStatementRequest = createEvaluationStatementRequest()
        statement = parseJson(mvc().perform(post('/statement').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createEvaluationStatementRequest)))
                .andDo(print()).andDo(document("generateStatement"))
                .andExpect(status().isOk())
                .andReturn())

        statementId = statement.statementId
        accountId = statement.accountId
    }

    @Test(dependsOnMethods = 'getStatementTest')
    void getStatementByStatementId()
    {
        mvc().perform(get('/account/statement/{statementId}', statementId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getStatementByStatementId"))
                .andExpect(status().isOk())
    }

    @Test(dependsOnMethods = 'getStatementByStatementId')
    void getStatementByAccountId()
    {
        mvc().perform(get('/account/statement/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getStatementByAccountId"))
                .andExpect(status().isOk())
    }

    @Test(dependsOnMethods = 'getStatementByAccountId')
    void getEvaluateStatementTest()
    {
        mvc().perform(get('/evaluate-statement/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("evaluateStatement"))
                .andExpect(status().isOk())
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
                "totalNetPrice"      : 100.0,
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


    def createEvaluationStatementRequest()
    {
        return [
                "locationId": createdClientLocationId,
                "accountId" : accountId
        ]
    }

    def createSubscriptionRequest()
    {
        return [
                "locationId"     : createdClientLocationId,
                "salesEmployeeId": "ad8ebc9e-5fab-140f-879f-9e930cc35c74",
                "accountId"      : accountId,
                "memberId"       : memberId,
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
