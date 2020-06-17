package com.abcfinancial.api.billing.subscriptionmanagement.account

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationAccountResponseVO
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.MerchantService
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.MerchantResponseVO
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.UpdateAccountDetailVO
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.UpdateAccountResponseVO
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class AccountTest extends BaseTest
{
    @MockBean
    private MerchantService merchantService

    static def randomNumber = null
    def locationAccount = null
    static def emailId = null
    static def name = "Abhinay" + getRandomString
    static def tokenId = UUID.randomUUID();

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }

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
                .andExpect(jsonPath("\$.onboardingAccountResponse.companyId").value("842855"))
                .andExpect(jsonPath("\$.onboardingAccountResponse.licenseKey").value("9959E096E9345B8A"))
                .andExpect(jsonPath("\$.onboardingAccountResponse.avaLocation.avaLocationId").value("354319"))
                .andExpect(jsonPath("\$.onboardingAccountResponse.avaLocation.avaAddress.line").value("2000 Main Street"))
                .andExpect(jsonPath("\$.onboardingAccountResponse.avaLocation.avaAddress.city").value("Irvine"))
                .andExpect(jsonPath("\$.onboardingAccountResponse.avaLocation.avaAddress.region").value("CA"))
                .andExpect(jsonPath("\$.onboardingAccountResponse.avaLocation.avaAddress.country").value("US"))
                .andExpect(jsonPath("\$.onboardingAccountResponse.avaLocation.avaAddress.postalCode").value("92614"))
                .andExpect(jsonPath("\$.onboardingAccountResponse.avaLocation.avaAddress.addressId").value("d1d298f9-f286-4055-ba5e-41abb2484fb8"))
                .andExpect(jsonPath("\$.onboardingAccountResponse.avaLocation.avaAddress.isValidated").value(true))
                .andDo(document("createLocationAccount"))
                .andReturn())


    }


    @Test
    void createLocationAccountTestDemo()
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
                .andDo(document("createClientAccount"))
                .andReturn())


    }


    @Test(dependsOnMethods = 'createLocationAccountTest')
    void updateAccountDetailsWithPayment()
    {
        mvc().perform(put('/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateAccountWithPaymentMethodRequest())))
                .andDo(print())
                .andDo(document("updateLocationAccount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("\$.account.accountId").value(isUuid()))
                .andExpect(jsonPath("\$.account.name").value("test"))
                .andExpect(jsonPath("\$.account.phone").value("8009578015"))
                .andExpect(jsonPath("\$.account.paymentMethod.id").value(isUuid()))
                .andExpect(jsonPath("\$.account.paymentMethod.type").value("BANK_ACCOUNT"))
                .andExpect(jsonPath("\$.account.paymentMethod.display").value("5455"))
                .andExpect(jsonPath("\$.account.paymentMethod.routingNumber").value("101000967"))
                .andExpect(jsonPath("\$.account.paymentMethod.accountNumber").value("45455"))
                .andExpect(jsonPath("\$.account.paymentMethod.alias").value("testalias6"))
                .andExpect(jsonPath("\$.account.paymentMethod.active").value(true))
                .andReturn()

        mvc().perform(put('/account/{accountId}', uuidNeverCreated).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateAccountWithPaymentMethodRequest())))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
    }

    @Test(dataProvider = 'updateAcccountValidations')
    void validateUpdateAccount(HttpStatus expectedStatus, List<String> errorCodes, Map requests)
    {
        mvc().perform(put('/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(requests)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andReturn()
    }

    @Test(dependsOnMethods = 'createLocationAccountTest')
    void updateAccountDetails()
    {

        mvc().perform(put('/account-details/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateAccountRequest())))
                .andDo(print())
                .andDo(document("updateAccount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("\$.accountId").value(isUuid()))
                .andExpect(jsonPath("\$.account.name").value("test"))
        // .andExpect(jsonPath("\$.account.email").value("test@qa4life.com"))
                .andExpect(jsonPath("\$.account.phone").value("8009578015"))
                .andReturn()
    }

    @Test(dependsOnMethods = 'updateAccountDetails')
    void getClientAccountByLocation()
    {

        mvc().perform(get('/account/location/{locationId}', locationAccount.locationId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getClientAccountByLocation"))
                .andExpect(status().isOk())

        mvc().perform(get('/account/location/{locationId}', uuidNeverCreated).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()

    }

    @Test(dataProvider = 'accountVOFieldValidations')
    void testAccountVOValidator(HttpStatus expectedStatus, List<String> errorCodes, Map request) throws Exception
    {
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.add("AccountTest", "true")

        when(merchantService.createMerchant(any(HttpHeaders.class), any(String.class)))
                .thenReturn(new MerchantResponseVO(UUID.randomUUID()))

        mvc().perform(post('/account/client').headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath('errors').isArray())
                .andExpect(jsonPath('errors', hasSize(errorCodes.size())))
                .andExpect(jsonPath('errors[*].code', containsInAnyOrder(errorCodes.toArray())))
    }

    @Test(dependsOnMethods = 'testAccountVOValidator')
    void createLocationAccountWithTokenizedPaymentMethodTest()
    {
        def request = createLocationAccountWithTokenizedPaymentMethodRequest()
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
                .andDo(document("createLocationAccount"))
                .andReturn())
    }

    @DataProvider
    static Object[][] accountVOFieldValidations()
    {
        return [
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                locationId: "a16e#365-d@c7-4afc-92d1-2bb105068d}c"

                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                locationId: UUID.randomUUID(),
                                account   : [
                                        "name": ""
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                locationId: UUID.randomUUID(),
                                account   : [
                                        "name" : "Arvind Kandi",
                                        "email": ""
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                locationId: UUID.randomUUID(),
                                account   : [
                                        "name" : "Arvind Kandi",
                                        "email": emailId,
                                        "phone": ""
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                locationId: UUID.randomUUID(),
                                account   : [
                                        "name"         : "Arvind Kandi",
                                        "email"        : emailId,
                                        "phone"        : "1234567890",
                                        "paymentMethod": [
                                                "type": "",
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                locationId: UUID.randomUUID(),
                                account   : [
                                        "name"         : "Arvind Kandi",
                                        "email"        : emailId,
                                        "phone"        : "1234567890",
                                        "paymentMethod": [
                                                "type"           : "BANK_ACCOUNT",
                                                "bankAccountType": "SAVING",
                                                "routingNumber"  : ""
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                locationId: UUID.randomUUID(),
                                account   : [
                                        "name"         : "Arvind Kandi",
                                        "email"        : emailId,
                                        "phone"        : "1234567890",
                                        "paymentMethod": [
                                                "type"           : "BANK_ACCOUNT",
                                                "bankAccountType": "SAVING",
                                                "routingNumber"  : "101000967",
                                                "accountNumber"  : "",
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                locationId: UUID.randomUUID(),
                                account   : [
                                        "name"         : "Arvind Kandi",
                                        "email"        : emailId,
                                        "phone"        : "1234567890",
                                        "paymentMethod": [
                                                "type"           : "BANK_ACCOUNT",
                                                "bankAccountType": "SAVING",
                                                "routingNumber"  : "101000967",
                                                "accountNumber"  : "117895633218795",
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                locationId: UUID.randomUUID(),
                                account   : [
                                        "name"         : "Arvind Kandi",
                                        "email"        : emailId,
                                        "phone"        : "1234567890",
                                        "paymentMethod": [
                                                "type"           : "BANK_ACCOUNT",
                                                "bankAccountType": "SAVING",
                                                "routingNumber"  : "789598745662",
                                                "accountNumber"  : "78956",
                                        ]
                                ]
                        ]
                ]
        ]
    }

    def createLocationAccountRequest()
    {
        randomNumber = getRandomNumber()
        emailId = "AbcTest" + randomNumber + "@qa4life.com"
        return [

                locationId      : UUID.randomUUID(),
                clientId        : UUID.randomUUID(),
                account         : [
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
                ],
                "avalaraAccount": [
                        "organizationId"                   : "fd1ebc1e-0fab-440f-879f-9e930cc97298",
                        "accountName"                      : "ABCTest" + randomNumber,
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

    static def updateAccountWithPaymentMethodRequest()
    {

        return [
                account: [
                        "name"         : "test",
                        "email"        : emailId,
                        "phone"        : "8009578015",
                        "sevaluation"  : sEvaluation,
                        "paymentMethod": [
                                "type"           : "BANK_ACCOUNT",
                                "bankAccountType": "SAVING",
                                "routingNumber"  : "101000967",
                                "accountNumber"  : "45455",
                                "alias"          : "testalias6"
                        ]
                ]
        ]

    }

    static def updateAccountRequest()
    {

        return [
                account: [
                        "name"       : "test",
                        "email"      : emailId,
                        "phone"      : "8009578015",
                        "sevaluation": sEvaluation
                ]
        ]

    }

    static def getRandomNumber()
    {
        return (int) System.nanoTime()
    }

    static def createLocationAccountWithTokenizedPaymentMethodRequest()
    {
        randomNumber = getRandomNumber()
        emailId = "AbcTest" + randomNumber + "@qa4life.com"
        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"          : testName + name,
                        "email"         : emailId,
                        "phone"         : "1234567890",
                        "sevaluation"   : sEvaluation,
                        "billingDate"   : currentDate,
                        "paymentMethod" : [
                                "type"   : "BANK_ACCOUNT",
                                "tokenId": tokenId
                        ],
                        "avalaraAccount": [
                                "organizationId"                   : "fd1ebc1e-0fab-440f-879f-9e930cc97298",
                                "accountName"                      : "ABCTest" + randomNumber,
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
        ]
    }

    @DataProvider
    static Object[][] updateAcccountValidations()
    {
        return [
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "name": ""
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": ""
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "email": "Ullman@qa4life.123"
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "phone": "12345678909"
                                ]
                        ]
                ]
        ]

    }
}
