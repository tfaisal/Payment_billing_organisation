package com.abcfinancial.api.billing.generalledger.payment

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.MerchantService
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.MerchantResponseVO
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ApplyPaymentToPaymentMethodAccountTest extends BaseTest
{
    static def memberId = UUID.randomUUID();
    static def accnId = UUID.randomUUID();
    static def invoiceId = UUID.randomUUID();
    def memberAccount
    def invoice
    static def name = "abcfinancial" + Constant.getRandomString
    @MockBean
    private MerchantService merchantService
    def locationAccount = null
    def statementId = null;
    def stmtId = UUID.randomUUID();

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }

    def accountId

    @Test
    void createLocationAccount()
    {
        def request = createLocationAccountRequest()
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        // headers.setContentType(MediaType.APPLICATION_JSON)
        headers.add("AccountTest", "true")

        when(merchantService.createMerchant(any(HttpHeaders.class), any(String.class)))
                .thenReturn(new MerchantResponseVO(UUID.randomUUID()))

        locationAccount = parseJson(mvc().perform(post('/account/client').headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

    }

    @Test(dependsOnMethods = 'createLocationAccount')
    void applyPaymentToAccountTest()
    {

        def bankAccountRequest = createMemberAccountBankAccountRequest()

        memberAccount = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(bankAccountRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())
        accountId = memberAccount.account.paymentMethod.id;
        def applyDirectPaymentRequest = applyPaymentRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andDo(document("applyPayment"))
                .andExpect(status().isBadRequest())
                .andReturn()

        accountId = memberAccount.account.paymentMethod.id;
        def applyPaymentWithoutInvoiceAndStatementRequest = applyPaymentWithoutInvoiceAndStatementRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyPaymentWithoutInvoiceAndStatementRequest)))
                .andDo(print())
                .andDo(document("applyPayment"))
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = 'applyPaymentToAccountTest')
    void applyPaymentToAccountAgainstInvoiceTest()
    {
        def createPayorInvoiceRequest = createPayorInvoiceRequest();

        invoice = parseJson(mvc().perform(post('/account/payor/invoice').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createPayorInvoiceRequest)))
                .andExpect(status().isCreated())
                .andReturn())

        def applyDirectPaymentRequest = applyPaymentWithInvoiceRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()

    }

    @Test(dependsOnMethods = 'applyPaymentToAccountAgainstInvoiceTest')
    void applyPaymentToAccountWithAmountAgainstInvoiceTest()
    {

        def applyDirectPaymentRequest = withAmountAgainstInvoiceApplyPaymentRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()

    }

    @Test(dependsOnMethods = 'applyPaymentToAccountWithAmountAgainstInvoiceTest')
    void applyPaymentInvalidAccountTest()
    {

        def applyDirectPaymentRequest = applyPaymentRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accnId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()

    }

    @Test(dependsOnMethods = 'applyPaymentToAccountWithAmountAgainstInvoiceTest')
    void applyPaymentInvalidInvoiceTest()
    {

        def applyDirectPaymentRequest = applyPaymentInvalidInvoiceRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()

    }

    @Test(dependsOnMethods = 'applyPaymentToAccountAgainstInvoiceTest')
    void applyPaymentAgainstStatementWithAmountTest()
    {

        def createEvaluationStatementRequest = createEvaluationStatementRequest()
        def statement = parseJson(mvc().perform(post('/statement').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createEvaluationStatementRequest)))
                .andDo(print()).andDo(document("generateStatement"))
                .andExpect(status().isOk())
                .andReturn())

        statementId = statement.statementId
        def applyDirectPaymentRequest = applyPaymentWithStatementRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()


    }

    @Test(dependsOnMethods = 'applyPaymentAgainstStatementWithAmountTest')
    void applyPaymentAgainstStatementTest()
    {
        def applyDirectPaymentRequest = applyPaymentWithoutAmountWithStatementRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()
    }

    @Test(dependsOnMethods = 'applyPaymentAgainstStatementTest')
    void applyPaymentAgainstInvalidStatementTest()
    {
        def applyDirectPaymentRequest = applyPaymentWithInvalidStatementRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
    }

    static def createMemberAccountBankAccountRequest()
    {
        return [
                "locationId": Constant.thirdCreatedClientLocationId,
                "memberId"  : UUID.randomUUID(),
                "payorId"   : UUID.randomUUID(),
                account     : [
                        "name"       : "Test1",
                        "email"      : "ravishjain@QA4LIFE.COM",
                        "phone"      : "19075526443",
                        "sevaluation": Constant.sEvaluation,
                        "billingDate": Constant.currentDate,
                        paymentMethod: [
                                "type"           : "BANK_ACCOUNT",
                                "bankAccountType": "SAVING",
                                "routingNumber"  : "101000967",
                                "accountNumber"  : "454123456"
                        ]
                ]

        ]
    }


    def createPayorInvoiceRequest()
    {
        return [
                "locationId"         : Constant.thirdCreatedClientLocationId,
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


    def applyPaymentRequest()
    {

        return [
                "payAmount": "300"
        ]
    }

    def withoutAmountApplyPaymentRequest()
    {

        return [
                "payAmount": ""
        ]
    }

    def applyPaymentWithInvoiceRequest()
    {

        return [
                "invoiceId": invoice.id
        ]
    }

    def withAmountAgainstInvoiceApplyPaymentRequest()
    {

        return [

                "invoiceId": invoice.id,
                "payAmount": "200"
        ]
    }

    def applyPaymentInvalidInvoiceRequest()
    {

        return [
                "invoiceId": invoiceId
        ]
    }

    static def createLocationAccountRequest()
    {
        def randomNumber = getRandomNumber()
        Constant.emailId = "Abc" + randomNumber + "Test@qa4life.com"
        return [

                locationId      : Constant.thirdCreatedClientLocationId,
                clientId        : UUID.randomUUID(),
                account         : [
                        "name"         : name,
                        "email"        : Constant.emailId,
                        "phone"        : "1234567890",
                        "sevaluation"  : Constant.sEvaluation,
                        "billingDate"  : Constant.currentDate,
                        "paymentMethod": [
                                "type"           : "BANK_ACCOUNT",
                                "bankAccountType": "SAVING",
                                "routingNumber"  : "101000967",
                                "accountNumber"  : "454556",
                                "alias"          : "testalias2"
                        ]
                ],
                "avalaraAccount": [
                        "organizationId"                   : "fd1ebc1e-0fab-440f-879f-9e930cc97298",
                        "accountName"                      : "ABCBHAVNA Test00" + randomNumber,
                        "website"                          : "bhavnacorp.com",
                        "lastName"                         : "sharma",
                        "welcomeEmail"                     : "Normal",
                        "avaCompanyCode"                   : "BHAVNACOMPONE4",
                        "companyAddress"                   : [
                                "line"      : "2000 Main Street",
                                "region"    : "CA",
                                "city"      : "Irvine",
                                "country"   : "US",
                                "postalCode": "92614"
                        ],
                        "acceptAvalaraTermsAndConditions"  : true,
                        "haveReadAvalaraTermsAndConditions": true
                ]
        ]
    }

    static def getRandomNumber()
    {
        return (int) System.nanoTime()
    }

    def createEvaluationStatementRequest()
    {
        return [
                "accountId" : accountId,
                "locationId": Constant.thirdCreatedClientLocationId
        ]
    }

    def applyPaymentWithStatementRequest()
    {

        return [
                "payAmount"  : "400",
                "statementId": statementId
        ]
    }

    def applyPaymentWithoutAmountWithStatementRequest()
    {

        return [
                "statementId": statementId
        ]
    }

    def applyPaymentWithInvalidStatementRequest()
    {

        return [
                "statementId": stmtId
        ]
    }

    def applyPaymentWithoutInvoiceAndStatementRequest()
    {

        return [
                "statementId": "",
                "invoiceId"  : ""
        ]
    }
}


