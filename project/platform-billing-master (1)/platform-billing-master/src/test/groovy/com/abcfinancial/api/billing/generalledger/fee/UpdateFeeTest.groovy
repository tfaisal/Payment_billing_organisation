package com.abcfinancial.api.billing.generalledger.fee

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.MerchantService
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.MerchantResponseVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class UpdateFeeTest extends BaseTest
{
    @MockBean
    private MerchantService merchantService

    def locationAccount
    static def name = "Abhiveg" + getRandomString
    def feeId
    @Autowired
    AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()
    }

    @Test
    void createFeeTest()
    {
        def request = createLocationAccount()
        def updateRequest = updateFeeRequest()

        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.setContentType(MediaType.APPLICATION_JSON)

        when(merchantService.createMerchant(any(HttpHeaders.class), any(String.class)))
                .thenReturn(new MerchantResponseVO(UUID.randomUUID()))

        locationAccount = parseJson(mvc().perform(post('/account/client').headers(headers)
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        clientAccountId = locationAccount.account.accountId;

        def requestFee = createFee()
        def createFee = parseJson(mvc().perform(post('/configure-fee').headers(headers)
                .content(toJson(requestFee)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("createConfigureFee"))
                .andReturn())

        feeId = createFee.feeId
        mvc().perform(put('/configure-fee/{feeId}', feeId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateConfigureFee"))
                .andExpect(status().isOk())
                .andReturn()

    }


    @Test(dependsOnMethods = 'createFeeTest')
    void updateFeeWithBlankValueTypeTest()
    {

        def updateRequest = updateFeeWithBlankValueTypeRequest();

        mvc().perform(put('/configure-fee/{feeId}', feeId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateFeeWithBlankValueType"))
                .andExpect(status().isOk())
                .andReturn()

    }

    @Test
    void updateFeeWithBlankValueTest()
    {

        def updateRequest = updateFeeWithBlankValueRequest();

        mvc().perform(put('/configure-fee/{feeId}', feeId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateFeeWithBlankValue"))
                .andExpect(status().isOk())
                .andReturn()

    }

    @Test(dependsOnMethods = 'createFeeTest', priority = 2)
    void updateFeeWithBlankTransactionTypeTest()
    {

        def updateRequest = updateFeeWithBlankTransactionTypeRequest();

        mvc().perform(put('/configure-fee/{feeId}', feeId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateFeeWithBlankTransactionType"))
                .andExpect(status().isOk())
                .andReturn()

    }

    @Test(dependsOnMethods = 'createFeeTest', priority = 2)
    void updateFeeWithBlankFeeModeTest()
    {

        def updateRequest = updateFeeWithBlankFeeModeRequest();

        mvc().perform(put('/configure-fee/{feeId}', feeId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateFeeWithBlankFeeMode"))
                .andExpect(status().isOk())
                .andReturn()

    }

    @Test(dependsOnMethods = 'createFeeTest')
    void updateFeeWithBlankFeeTypeTest()
    {

        def updateRequest = updateFeeWithBlankFeeTypeRequest();

        mvc().perform(put('/configure-fee/{feeId}', feeId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateFeeWithBlankFeeType"))
                .andExpect(status().isOk())
                .andReturn()

    }


    def createLocationAccount()
    {
        def randomNumber = getRandomString
        def emailId = "AbcTest" + getRandomString + "@qa4life.com"
        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"         : name,
                        "email"        : emailId,
                        "phone"        : "1234567890",
                        "sevaluation"  : sEvaluation,
                        "billingDate"  : currentDate,
                        "paymentMethod": [
                                "type"   : "BANK_ACCOUNT",
                                "tokenId": "b4deef2b-e4f3-481a-a5ed-488b6e773f63"
                        ]
                ]
        ]

    }

    def createFee()
    {

        return [
                accountId         : clientAccountId,
                feeMode           : "TRANSACTION",
                feeType           : "PASS_THROUGH_FEE",
                feeTransactionType: "AMEX",
                feeValueType      : "FLAT",
                feeValue          : "100"
        ]

    }

    static def updateFeeWithBlankValueRequest()
    {

        return [

                accountId         : clientAccountId,
                feeMode           : "TRANSACTION",
                feeType           : "PASS_THROUGH_FEE",
                feeTransactionType: "VISA",
                feeValueType      : "PERCENTAGE",


        ]

    }

    static def updateFeeWithBlankTransactionTypeRequest()
    {

        return [

                accountId   : clientAccountId,
                feeMode     : "TRANSACTION",
                feeType     : "PASS_THROUGH_FEE",
                feeValueType: "FLAT",
                feeValue    : "50"
        ]

    }

    static def updateFeeWithBlankFeeModeRequest()
    {

        return [

                accountId         : clientAccountId,
                feeType           : "PASS_THROUGH_FEE",
                feeTransactionType: "DISCOVER",
                feeValueType      : "FLAT",
                feeValue          : "50"
        ]

    }

    static def updateFeeWithBlankFeeTypeRequest()
    {

        return [

                accountId         : clientAccountId,
                feeMode           : "TRANSACTION",
                feeTransactionType: "MC",
                feeValueType      : "FLAT",
                feeValue          : "50"

        ]

    }

    static def updateFeeWithBlankValueTypeRequest()
    {

        return [

                accountId         : clientAccountId,
                feeMode           : "TRANSACTION",
                feeType           : "PASS_THROUGH_FEE",
                feeTransactionType: "MC",
                feeValue          : "50"

        ]

    }

    def updateFeeRequest()
    {

        return [

                feeMode           : "TRANSACTION",
                feeType           : "PASS_THROUGH_FEE",
                feeTransactionType: "MC",
                feeValueType      : "FLAT",
                feeValue          : "50"
        ]

    }
}
