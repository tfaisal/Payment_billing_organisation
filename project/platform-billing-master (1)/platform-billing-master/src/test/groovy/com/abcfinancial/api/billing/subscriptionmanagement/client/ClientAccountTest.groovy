package com.abcfinancial.api.billing.subscriptionmanagement.client

import com.abcfinancial.api.billing.subscriptionmanagement.account.service.MerchantService
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.MerchantResponseVO
import com.abcfinancial.api.billing.common.BaseTest
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*
class ClientAccountTest extends BaseTest
{
    @MockBean
    private MerchantService merchantService

    static def randomNumber = null
    static def emailId = null
    def locationAccount = null;
    static def accountName = name + getRandomAccountCreationString

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()
    }

    @Test(groups = 'onboarding')
    //todo should probably expand this to properly encompass all onboarding tests
    void getClientAccounts()
    {
        locationAccount = createLocationAccount()
        mvc().perform(get('/account/client?name=american', locationAccount.locationId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andDo(document("getClientAccounts"))
                .andExpect(status().isOk())
    }

    @Test(dependsOnMethods = 'getClientAccounts')
    void getClientAccount()
    {

        mvc().perform(get('/account/client/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("\$.account.name").value(accountName))
                .andExpect(jsonPath("\$.account.email").value(emailId))
                .andDo(document("getClientAccount"))

        mvc().perform(get('/account/client/' + UUID.randomUUID(), locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())
    }

    def createLocationAccount()
    {
        def request = createLocationAccountRequest()
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.add("AccountTest", "true")

        when(merchantService.createMerchant(any(HttpHeaders.class), any(String.class)))
                .thenReturn(new MerchantResponseVO(UUID.randomUUID()))

        parseJson(mvc().perform(post('/account/client').headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("\$.locationId").value(isUuid()))
                .andExpect(jsonPath("\$.account.name").value(accountName))
                .andExpect(jsonPath("\$.account.email").value(emailId))
                .andExpect(jsonPath("\$.account.phone").value("1234567890"))
                .andExpect(jsonPath("\$.account.paymentMethod.type").value("BANK_ACCOUNT"))
                .andExpect(jsonPath("\$.account.paymentMethod.bankAccountType").value("SAVING"))
                .andExpect(jsonPath("\$.account.paymentMethod.routingNumber").value("101000967"))
                .andExpect(jsonPath("\$.account.paymentMethod.accountNumber").value("454556"))
                .andExpect(jsonPath("\$.account.paymentMethod.alias").value("testalias2"))
                .andReturn())
    }

    static def createLocationAccountRequest()
    {
        randomNumber = getRandomNumber()
        emailId = "Abc" + randomNumber + "Test@qa4life.com"
        return [

                locationId      : fourthCreatedClientLocationId,
                clientId        : UUID.randomUUID(),
                account         : [
                        "name"         : accountName,
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
}
