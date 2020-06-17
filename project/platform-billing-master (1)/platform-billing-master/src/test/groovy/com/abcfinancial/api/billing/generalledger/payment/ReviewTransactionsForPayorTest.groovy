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

import java.time.format.DateTimeFormatter

import static groovy.json.JsonOutput.toJson
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class ReviewTransactionsForPayorTest extends BaseTest
{
    static def memberId = UUID.randomUUID();
    def memberAccount
    static def name = "abcfinancial" + Constant.getRandomString
    @MockBean
    private MerchantService merchantService
    def locationAccount = null
    def statementId = null;
    static def randomNumber = null
    def formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    def startDate = (today).format(formatter);
    def endDate = (today).format(formatter);

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }

    def accountId
    def paymentMethodId
    static def clientAccnId = null

    @Test
    void createLocationAccountTest()
    {
        def request = createLocationAccountRequest()
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.add("AccountTest", "true")

        when(merchantService.createMerchant(any(HttpHeaders.class), any(String.class)))
                .thenReturn(new MerchantResponseVO(UUID.randomUUID()))

        locationAccount = parseJson(mvc().perform(post('/account/client').headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("\$.locationId").value(isUuid()))
                .andExpect(jsonPath("\$.account.name").value(name))
                .andExpect(jsonPath("\$.account.email").value(emailId))
                .andExpect(jsonPath("\$.account.phone").value("1234567890"))
                .andExpect(jsonPath("\$.account.paymentMethod.type").value("BANK_ACCOUNT"))
                .andExpect(jsonPath("\$.account.paymentMethod.bankAccountType").value("SAVING"))
                .andExpect(jsonPath("\$.account.paymentMethod.routingNumber").value("101000967"))
                .andExpect(jsonPath("\$.account.paymentMethod.accountNumber").value("454556"))
                .andExpect(jsonPath("\$.account.paymentMethod.alias").value("testalias2"))
                .andReturn())
        clientAccnId = locationAccount.account.accountId;
    }

    @Test(dependsOnMethods = "createLocationAccountTest")
    void createPayorAccount()
    {
        def payorRequest = createMemberAccountBankAccountRequest();
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.add("AccountTest", "true")
        memberAccount = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(payorRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        accountId = memberAccount.account.accountId
        paymentMethodId = memberAccount.account.paymentMethod.id;
    }

    @Test(dependsOnMethods = "createPayorAccount")
    void createPayorInvoice()
    {
        def createPayorInvoiceRequest = createPayorInvoiceRequest()
        parseJson(mvc().perform(post('/account/payor/invoice').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createPayorInvoiceRequest)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn())
    }

    @Test(dependsOnMethods = "createPayorInvoice")
    void createStatementForMainAccount()
    {
        def createStatementMainAccount = createStatementMainAccountRequest()
        def statement = parseJson(mvc().perform(post('/statement').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createStatementMainAccount)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn())

        statementId = statement.statementId
    }

    @Test(dependsOnMethods = "createStatementForMainAccount")
    void reviewPayorTransactionsForMainAccount()
    {
        mvc().perform(get('/transactions/account/' + accountId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("reviewPayorTransactions"))
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/account/' + accnId + '?endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
    }

    @Test(dependsOnMethods = "createStatementForMainAccount")
    void reviewPayorTransactionsWithStartDateForMainAccount()
    {
        mvc().perform(get('/transactions/account/' + accountId + '?startDate=' + startDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/account/' + accountId + '?startDate=' + invalidDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = "createStatementForMainAccount")
    void reviewPayorTransactionsWithEndDateForMainAccount()
    {
        mvc().perform(get('/transactions/account/' + accountId + '?endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/account/' + accountId + '?endDate=' + invalidDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = "createStatementForMainAccount")
    void reviewPayorTransactionsWithDateRangeForMainAccount()
    {
        mvc().perform(get('/transactions/account/' + accountId + '?startDate=' + startDate + '&endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/account/' + accountId + '?startDate=' + invalidDate + '&endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = "createStatementForMainAccount")
    void reviewPayorTransactionsWithTypeForMainAccount()
    {
        mvc().perform(get('/transactions/account/' + accountId + '?transactionType=' + transactionType).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/account/' + accountId + '?transactionType=' + invalidTransType).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = "createStatementForMainAccount")
    void reviewPayorTransactionsWithTypeAndDateRangeForMainAccount()
    {
        mvc().perform(get('/transactions/account/' + accountId + '?transactionType=' + transactionType + '&startDate=' + startDate + '&endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test(dependsOnMethods = "createPayorAccount")
    void createPayorInvoiceForPaymentMethodAccount()
    {
        def createPaymentMethodAccountInvoiceRequest = createPaymentMethodAccountInvoiceRequest()
        parseJson(mvc().perform(post('/account/payor/invoice').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createPaymentMethodAccountInvoiceRequest)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn())
    }

    @Test(dependsOnMethods = "createPayorInvoiceForPaymentMethodAccount")
    void createStatementForPaymentMethodAccount()
    {
        def createStatementPaymentMethodAccountRequest = createStatementPaymentMethodAccountRequest()
        def statement = parseJson(mvc().perform(post('/statement').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createStatementPaymentMethodAccountRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn())

        statementId = statement.statementId
    }

    @Test(dependsOnMethods = "createStatementForPaymentMethodAccount")
    void createPayorSecondInvoiceForPaymentMethodAccount()
    {
        def createPaymentMethodAccountInvoiceRequest = createPaymentMethodAccountInvoiceRequest()
        parseJson(mvc().perform(post("/account/payor/invoice").header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createPaymentMethodAccountInvoiceRequest)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn())
    }

    @Test(dependsOnMethods = "createStatementForPaymentMethodAccount")
    void reviewPayorTransactionsForPaymentMethodAccount()
    {
        mvc().perform(get('/transactions/account/' + paymentMethodId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/account/' + accnId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
    }

    @Test(dependsOnMethods = "createStatementForPaymentMethodAccount")
    void reviewPayorTransactionsWithStartDateForPaymentMethodAccount()
    {
        mvc().perform(get('/transactions/account/' + paymentMethodId + '?startDate=' + startDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/account/' + paymentMethodId + '?startDate=' + invalidDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()

    }

    @Test(dependsOnMethods = "createStatementForPaymentMethodAccount")
    void reviewPayorTransactionsWithEndDateForPaymentMethodAccount()
    {
        mvc().perform(get('/transactions/account/' + paymentMethodId + '?endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/account/' + paymentMethodId + '?endDate=' + invalidDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = "createStatementForPaymentMethodAccount")
    void reviewPayorTransactionsWithDateRangeForPaymentMethodAccount()
    {
        mvc().perform(get('/transactions/account/' + paymentMethodId + '?startDate=' + startDate + '&endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/account/' + paymentMethodId + '?startDate=' + invalidDate + '&endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = "createStatementForPaymentMethodAccount")
    void reviewPayorTransactionsWithTypeForPaymentMethodAccount()
    {
        mvc().perform(get('/transactions/account/' + paymentMethodId + '?transactionType=' + transactionType).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/account/' + paymentMethodId + '?transactionType=' + invalidTransType).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = "createStatementForPaymentMethodAccount")
    void reviewPayorTransactionsWithTypeAndDateRangeForPaymentMethodAccount()
    {
        mvc().perform(get('/transactions/account/' + paymentMethodId + '?transactionType=' + transactionType + '&startDate=' + startDate + '&endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
    }


    @Test(dependsOnMethods = "createStatementForPaymentMethodAccount")
    void reviewPayorTransactionsSinceLastStatement() throws Exception
    {
        mvc().perform(get("/transactions/statementData/paymentMethod/" + paymentMethodId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("reviewPayorTransactionsSinceLastStatement"))
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get("/transactions/statementData/paymentMethod/" + accnId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
    }

    @Test(dependsOnMethods = "createLocationAccountTest")
    void getEvaluateSettlementTest()
    {
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.add("AccountTest", "true")

        def getEvaluateSettlement = parseJson(mvc().perform(get('/evaluate-settlement/account/{accountId}', clientAccnId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn())
    }

    @Test(dependsOnMethods = 'getEvaluateSettlementTest')
    void generateSettlementTest()
    {
        def createSettlement = parseJson(mvc().perform(post('/settlement/account/{accountId}', clientAccnId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

    }

    @Test(dependsOnMethods = "createLocationAccountTest")
    void applyPaymentToAccountTest()
    {
        def applyDirectPaymentRequest = applyPaymentRequest();
        mvc().perform(post('/apply-payment/account/{accountId}', clientAccnId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(applyDirectPaymentRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()
    }

    @Test(dependsOnMethods = "generateSettlementTest")
    void reviewClientTransactions()
    {
        mvc().perform(get('/transactions/client/account/' + clientAccnId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("reviewClientTransactions"))
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/client/account/' + accnId + '?endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
    }

    @Test(dependsOnMethods = "generateSettlementTest")
    void reviewClientTransactionsWithStartDate()
    {
        mvc().perform(get('/transactions/client/account/' + clientAccnId + '?startDate=' + startDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
        mvc().perform(get('/transactions/client/account/' + clientAccnId + '?startDate=' + invalidDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = "generateSettlementTest")
    void reviewClientTransactionsWithEndDate()
    {
        mvc().perform(get('/transactions/client/account/' + clientAccnId + '?endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/client/account/' + clientAccnId + '?endDate=' + invalidDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = "generateSettlementTest")
    void reviewClientTransactionsWithDateRange()
    {
        mvc().perform(get('/transactions/client/account/' + clientAccnId + '?startDate=' + startDate + '&endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/client/account/' + clientAccnId + '?startDate=' + invalidDate + '&endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = "generateSettlementTest")
    void reviewClientTransactionsWithType()
    {
        mvc().perform(get('/transactions/client/account/' + clientAccnId + '?transactionType=' + transactionType).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/transactions/client/account/' + clientAccnId + '?transactionType=' + invalidTransType).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = "generateSettlementTest")
    void reviewClientTransactionsWithTypeAndDateRange()
    {
        mvc().perform(get('/transactions/client/account/' + clientAccnId + '?transactionType=' + transactionType + '&startDate=' + startDate + '&endDate=' + endDate).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
    }

    def createLocationAccountRequest()
    {
        randomNumber = getRandomNumber()
        emailId = "AbcTest" + randomNumber + "@qa4life.com"
        return [

                locationId: fifthcreatedClientLocationId,
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"         : name,
                        "email"        : emailId,
                        "phone"        : "1234567890",
                        "sevaluation"  : sEvaluation,
                        "billingDate"  : currentDate,
                        "paymentMethod": [
                                "type"           : "BANK_ACCOUNT",
                                "bankAccountType": "SAVING",
                                "routingNumber"  : "101000967",
                                "accountNumber"  : "454556",
                                "alias"          : "testalias2"
                        ]
                ]
        ]
    }

    static def getRandomNumber()
    {
        return (int) System.nanoTime()
    }

    def createPayorInvoiceRequest()
    {
        return [
                "locationId"         : fifthcreatedClientLocationId,
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

    def createPaymentMethodAccountInvoiceRequest()
    {
        return [
                "locationId"         : fifthcreatedClientLocationId,
                "memberId"           : memberId,
                "accountId"          : paymentMethodId,
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

    static def createMemberAccountBankAccountRequest()
    {
        return [
                "locationId": fifthcreatedClientLocationId,
                "memberId"  : UUID.randomUUID(),
                "payorId"   : UUID.randomUUID(),
                account     : [
                        "name"       : "Test1",
                        "email"      : emailId,
                        "phone"      : "19075526443",
                        "sevaluation": sEvaluation,
                        "billingDate": billingDate,
                        paymentMethod: [
                                "type"           : "BANK_ACCOUNT",
                                "bankAccountType": "SAVING",
                                "routingNumber"  : "101000967",
                                "accountNumber"  : "454123456"
                        ]
                ]

        ]
    }

    def createStatementMainAccountRequest()
    {
        return [
                "accountId" : accountId,
                "locationId": fifthcreatedClientLocationId
        ]
    }

    def createStatementPaymentMethodAccountRequest()
    {
        return [
                "accountId" : paymentMethodId,
                "locationId": fifthcreatedClientLocationId
        ]
    }

    def applyPaymentRequest()
    {

        return [
                "payAmount": "300"
        ]
    }
}

