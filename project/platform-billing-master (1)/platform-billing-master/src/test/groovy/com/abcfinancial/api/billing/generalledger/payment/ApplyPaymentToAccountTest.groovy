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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ApplyPaymentToAccountTest extends BaseTest
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
    def paymentMethodId

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
        accountId = memberAccount.account.accountId;
        def applyDirectPaymentRequest = applyPaymentRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andDo(document("applyPayment"))
                .andExpect(status().isCreated())
                .andReturn()

        def applyPaymentWithNegativeAmountRequest = applyPaymentWithNegativeAmountRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyPaymentWithNegativeAmountRequest)))
                .andDo(print())
                .andDo(document("applyPayment"))
                .andExpect(status().isBadRequest())
                .andReturn()

        def applyPaymentWithExceededAmountRequest = applyPaymentWithExceededAmountRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyPaymentWithExceededAmountRequest)))
                .andDo(print())
                .andDo(document("applyPayment"))
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = 'applyPaymentToAccountTest')
    void addPaymentMethodTest()
    {
        def paymentMethodRequest = addPaymentMethodRequest()
        def paymentMethod = parseJson(mvc().perform(post('/paymentMethod').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(paymentMethodRequest)))
                .andDo(print()).andDo(document("addPaymentMethod"))
                .andExpect(status().isCreated())
                .andReturn())

        paymentMethodId = paymentMethod.id;

        mvc().perform(get('/payor/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())

        mvc().perform(get('/paymentMethod/{paymentMethodId}', paymentMethodId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("reviewPaymentMethod"))
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/paymentMethod/{paymentMethodId}', accountId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())
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
    void applyPaymentToAccountAgainstSameInvoiceAgainTest()
    {
        def applyDirectPaymentRequest = applyPaymentWithInvoiceRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andReturn()
    }

    @Test(dependsOnMethods = 'applyPaymentToAccountAgainstInvoiceTest')
    void applyPaymentToAccountWithoutAmountTest()
    {

        def applyDirectPaymentRequest = withoutAmountApplyPaymentRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()

    }

    @Test(dependsOnMethods = 'applyPaymentToAccountWithoutAmountTest')
    void applyingPartialPaymentAgainstInvoiceTest()
    {
        def createPayorInvoiceRequest = createPayorInvoiceRequest();

        invoice = parseJson(mvc().perform(post('/account/payor/invoice').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createPayorInvoiceRequest)))
                .andExpect(status().isCreated())
                .andReturn())

        def applyDirectPaymentRequest = applyingPartialPaymentAgainstInvoiceRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()
    }

    @Test(dependsOnMethods = 'applyingPartialPaymentAgainstInvoiceTest')
    void applyPaymentToAccountWithoutAmountAgainstInvoiceTest()
    {

        def applyDirectPaymentRequest = withoutAmountAgainstInvoiceApplyPaymentRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()

    }

    @Test(dependsOnMethods = 'applyPaymentToAccountWithoutAmountAgainstInvoiceTest')
    void applyPaymentInvalidAccountTest()
    {

        def applyDirectPaymentRequest = applyPaymentRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accnId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()

    }

    @Test(dependsOnMethods = 'applyPaymentInvalidAccountTest')
    void applyPaymentInvalidInvoiceTest()
    {

        def applyDirectPaymentRequest = applyPaymentInvalidInvoiceRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()

    }

    @Test(dependsOnMethods = 'applyPaymentInvalidInvoiceTest')
    void applyPaymentAgainstStatementTest()
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
                .andExpect(status().isCreated())
                .andReturn()


    }

    @Test(dependsOnMethods = 'applyPaymentInvalidInvoiceTest')
    void applyPaymentWithNegativeStatementAmountTest()
    {

        def applyDirectPaymentRequest = applyPaymentWithHigherAmountRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andDo(document("applyPayment"))
                .andExpect(status().isCreated())
                .andReturn()

        def createEvaluationStatementRequest = createEvaluationStatementRequest()
        def statement = parseJson(mvc().perform(post('/statement').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createEvaluationStatementRequest)))
                .andDo(print()).andDo(document("generateStatement"))
                .andExpect(status().isOk())
                .andReturn())

        statementId = statement.statementId

        def applyPaymentWithNegativeStatementRequest = applyPaymentWithStatementRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyPaymentWithNegativeStatementRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()


    }

    @Test(dependsOnMethods = 'applyPaymentToAccountTest')
    void applyPaymentAgainstStatementAndInvoiceTest()
    {
        def createPayorInvoiceRequest = createPayorInvoiceRequest();

        invoice = parseJson(mvc().perform(post('/account/payor/invoice').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createPayorInvoiceRequest)))
                .andExpect(status().isCreated())
                .andReturn())

        def createEvaluationStatementRequest = createEvaluationStatementRequest()
        def statement = parseJson(mvc().perform(post('/statement').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createEvaluationStatementRequest)))
                .andDo(print()).andDo(document("generateStatement"))
                .andExpect(status().isOk())
                .andReturn())

        statementId = statement.statementId

        def applyPaymentWithStatementAndInvoiceRequest = applyPaymentWithStatementAndInvoiceRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyPaymentWithStatementAndInvoiceRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = 'applyPaymentAgainstStatementTest')
    void applyPaymentAgainstStatementAgainTest()
    {
        def applyDirectPaymentRequest = applyPaymentWithStatementRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andReturn()
    }

    @Test(dependsOnMethods = 'applyPaymentAgainstStatementTest')
    void applyPaymentAgainstStatementWithoutAmountTest()
    {
        def applyDirectPaymentRequest = applyPaymentWithoutAmountWithStatementRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = 'applyPaymentAgainstStatementWithoutAmountTest')
    void applyPaymentAgainstInvalidStatementTest()
    {
        def applyDirectPaymentRequest = applyPaymentWithInvalidStatementRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
    }

    def addPaymentMethodRequest()
    {
        return [
                "accountId"        : accountId,
                "type"             : "BANK_ACCOUNT",
                "bankAccountType"  : "SAVING",
                "routingNumber"    : "101000967",
                "alias"            : "testalias6",
                "tokenId"          : "f0745bdf-2c9d-4f01-a9d5-e4bb25d26524",
                "accountHolderName": "Abhinay",
                "sevaluation": Constant.sEvaluation,
                "billingDate": Constant.currentDate

        ]
    }
    static def createMemberAccountBankAccountRequest()
    {
        return [
                "locationId": Constant.createdClientLocationId,
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
                "locationId"         : Constant.createdClientLocationId,
                "memberId"           : memberId,
                "accountId"          : accountId,
                "salesEmployeeId"    : "ad8ebc9e-5fab-140f-879f-9e930cc35c74",
                "totalTax"           : 30.0,
                "totalAmount"        : 1000.0,
                "totalDiscountAmount": 20,
                "totalNetPrice"      : 1100.0,
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

    def applyPaymentWithHigherAmountRequest()
    {

        return [
                "payAmount": "1300"
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
                "payAmount": "1200",
                "invoiceId": invoice.id
        ]
    }

    def applyingPartialPaymentAgainstInvoiceRequest()
    {

        return [
                "payAmount": "200",
                "invoiceId": invoice.id
        ]
    }

    def withoutAmountAgainstInvoiceApplyPaymentRequest()
    {

        return [

                "invoiceId": invoice.id
        ]
    }

    def applyPaymentInvalidInvoiceRequest()
    {

        return [
                "payAmount": "500",
                "invoiceId": invoiceId
        ]
    }

    static def createLocationAccountRequest()
    {
        def randomNumber = getRandomNumber()
        Constant.emailId = "Abc" + randomNumber + "Test@qa4life.com"
        return [

                locationId      : Constant.createdClientLocationId,
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
                "locationId": Constant.createdClientLocationId
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
                "payAmount"  : "400",
                "statementId": stmtId
        ]
    }

    def applyPaymentWithStatementAndInvoiceRequest()
    {

        return [
                "payAmount"  : "400",
                "statementId": statementId,
                "invoiceId"  : invoice.id
        ]
    }

    def applyPaymentWithNegativeAmountRequest()
    {
        return [
                "payAmount": "-300"
        ]
    }

    def applyPaymentWithExceededAmountRequest()
    {
        return [
                "payAmount": "999999999999999"
        ]
    }
}


