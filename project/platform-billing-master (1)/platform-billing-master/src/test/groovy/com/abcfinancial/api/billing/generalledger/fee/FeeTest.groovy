package com.abcfinancial.api.billing.generalledger.fee

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.MerchantService
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.MerchantResponseVO
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.hasSize
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class FeeTest extends BaseTest
{
    @MockBean
    private MerchantService merchantService

    def locationAccount
    static def name = "Abhinay" + Constant.getRandomString
    static def clientAccountId_FeeTest
    def feeId
    @Autowired
    AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()
    }


    @Test(dataProvider = 'feeVOFieldValidations')
    void testFeeVOValidator(HttpStatus expectedStatus, List<String> errorCodes, Map request) throws Exception
    {
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.add("FeeTest", "true")

        mvc().perform(post('/configure-fee').headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath('errors').isArray())
                .andExpect(jsonPath('errors', hasSize(errorCodes.size())))
                .andExpect(jsonPath('errors[*].code', containsInAnyOrder(errorCodes.toArray())))
    }


    @Test
    void createFeeTest()
    {
        def request = createLocationAccount()

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

        clientAccountId_FeeTest = locationAccount.account.accountId;

        def requestFee = createFee()
        def createFee = parseJson(mvc().perform(post('/configure-fee').headers(headers)
                .content(toJson(requestFee)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("createConfigureFee"))
                .andReturn())

        feeId = createFee.feeId

    }

    @Test(dependsOnMethods = 'createFeeTest')
    void getFeeByAccountId()
    {
        def getConfigureFee = parseJson(mvc().perform(get('/fee/account/{accountId}?feeTransactionType=AMEX', clientAccountId_FeeTest).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("getFeeByAccountId"))
                .andReturn())

        parseJson(mvc().perform(get('/fee/account/{accountId}', clientAccountId_FeeTest).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn())
        parseJson(mvc().perform(get('/fee/account/').header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn())
        parseJson(mvc().perform(get('/fee/account/{accountId}?feeTransactionType=AMEXs', clientAccountId_FeeTest).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn())
        parseJson(mvc().perform(get('/fee/account/{accountId}?feeTransactionType=AMEXs', Constant.uuidNeverCreated).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())
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

    def createFee()
    {

        return [
                accountId         : clientAccountId_FeeTest,
                feeMode           : "TRANSACTION  ",
                feeType           : "PASS_THROUGH_FEE  ",
                feeTransactionType: "AMEX  ",
                feeValueType      : "FLAT  ",
                feeValue          : "100  "
        ]

    }

    @DataProvider
    static Object[][] feeVOFieldValidations()
    {
        return [
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : "",
                                feeMode           : "TRANSACTION  ",
                                feeType           : "PASS_THROUGH_FEE  ",
                                feeTransactionType: "MC  ",
                                feeValueType      : "FLAT  ",
                                feeValue          : "9999999  "
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION  ",
                                feeType           : "PASS_THROUGH_FEE  ",
                                feeTransactionType: "MC  ",
                                feeValueType      : "FLAT  ",
                                feeValue          : "99999999999999999999999  "
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION  ",
                                feeType           : "PASS_THROUGH_FEE  ",
                                feeTransactionType: "MC  ",
                                feeValueType      : "FLAT  ",
                                feeValue          : "-1  "
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION  ",
                                feeType           : "PASS_THROUGH_FEE  ",
                                feeTransactionType: "AMEX  ",
                                feeValueType      : "FLAT  ",
                                feeValue          : "100  "
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTIONa",
                                feeType           : "PASS_THROUGH_FEE",
                                feeTransactionType: "AMEX",
                                feeValueType      : "FLAT",
                                feeValue          : "100"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION",
                                feeType           : "PASS_THROUGH_FEEA",
                                feeTransactionType: "AMEX",
                                feeValueType      : "FLAT",
                                feeValue          : "100"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION",
                                feeType           : "PASS_THROUGH_FEE",
                                feeTransactionType: "AMEXA",
                                feeValueType      : "FLAT",
                                feeValue          : "100"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION",
                                feeType           : "PASS_THROUGH_FEE",
                                feeTransactionType: "AMEX",
                                feeValueType      : "FLATA",
                                feeValue          : "100"

                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION",
                                feeType           : "PASS_THROUGH_FEE",
                                feeTransactionType: "AMEX",
                                feeValueType      : "PERCENTAGE",
                                feeValue          : "1001"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION",
                                feeType           : "PASS_THROUGH_FEE",
                                feeTransactionType: "AMEX",
                                feeValueType      : "FLATA",
                                feeValue          : "99999999999999999999"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "    ",
                                feeType           : "PASS_THROUGH_FEE",
                                feeTransactionType: "AMEX",
                                feeValueType      : "FLATA",
                                feeValue          : "99999999999999999999"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION",
                                feeType           : "     ",
                                feeTransactionType: "AMEX",
                                feeValueType      : "FLATA",
                                feeValue          : "99999999999999999999"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION",
                                feeType           : "PASS_THROUGH_FEE",
                                feeTransactionType: "      ",
                                feeValueType      : "FLATA",
                                feeValue          : "99999999999999999999"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION",
                                feeType           : "PASS_THROUGH_FEE",
                                feeTransactionType: "AMEX",
                                feeValueType      : "      ",
                                feeValue          : "99999999999999999999"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                accountId         : clientAccountId_FeeTest,
                                feeMode           : "TRANSACTION",
                                feeType           : "PASS_THROUGH_FEE",
                                feeTransactionType: "AMEX",
                                feeValueType      : " FLAT     ",
                                feeValue          : "  "
                        ]
                ]

        ]
    }
}
