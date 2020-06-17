package com.abcfinancial.api.billing.generalledger.settlement

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.MerchantService
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.MerchantResponseVO
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.beans.factory.annotation.Autowired
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

class SettlementTest extends BaseTest
{
    @MockBean
    private MerchantService merchantService

    def locationAccount = null
    static def name = "Abhinay" + Constant.getRandomString
    static def clientAccountId = null
    @Autowired
    AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()
    }

    @Test
    void getEvaluateSettlementTest()
    {
        def request = createLocationAccount()

        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.setContentType(MediaType.APPLICATION_JSON)

        when(merchantService.createMerchant(any(HttpHeaders.class), any(String.class)))
                .thenReturn(new MerchantResponseVO(UUID.randomUUID()))

        locationAccount = parseJson(mvc().perform(post('/account/location').headers(headers)
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        clientAccountId = locationAccount.account.accountId;

        def getEvaluateSettlement = parseJson(mvc().perform(get('/evaluate-settlement/account/{accountId}', clientAccountId).headers(headers))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("evaluateSettlement"))
                .andReturn())


    }

    @Test(dependsOnMethods = 'getEvaluateSettlementTest')
    void generateSettlementTest()
    {
        def createSettlement = parseJson(mvc().perform(post('/settlement/account/{accountId}', clientAccountId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("generateSettlement"))
                .andReturn())

    }

    def createLocationAccount()
    {
        def randomNumber = Constant.getRandomString
        def emailId = "AbcTest" + Constant.getRandomString + "@qa4life.com"
        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"         : name,
                        "email"        : emailId,
                        "phone"        : "1234567890",
                        "sevaluation"  : Constant.sEvaluation,
                        "billingDate"  : Constant.currentDate,
                        "paymentMethod": [
                                "type"   : "BANK_ACCOUNT",
                                "tokenId": "b4deef2b-e4f3-481a-a5ed-488b6e773f63"
                        ]
                ]
        ]

    }
}
