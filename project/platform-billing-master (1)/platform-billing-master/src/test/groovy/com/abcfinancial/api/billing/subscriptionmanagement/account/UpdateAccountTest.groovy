package com.abcfinancial.api.billing.subscriptionmanagement.account

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.MerchantService
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.MerchantResponseVO
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.Frequency
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class UpdateAccountTest extends BaseTest
{
    @MockBean
    private MerchantService merchantService

    static def memberId = UUID.randomUUID();
    static def payorId = UUID.randomUUID();
    static def invoiceId = UUID.randomUUID();
    def locationAccount
    def memberAccount
    def accountId
    def paymentMethodId
    static def randomNumber = null
    static def emailId = null
    static def accountName = name + getRandomString
    @Autowired
    private AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()
    }


    @Test
    void updateAccountUsingLocationAccountTest()
    {
        def request = createLocationAccountRequest();
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.add("AccountTest", "true")
        def updateRequest = updateClientAccountRequest();

        when(merchantService.createMerchant(any(HttpHeaders.class), any(String.class)))
                .thenReturn(new MerchantResponseVO(UUID.randomUUID()))

        locationAccount = parseJson(mvc().perform(post('/account/client').headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())
        mvc().perform(put('/account-details/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountUsingLocationAccount"))
                .andExpect(status().isOk())
                .andReturn()
        mvc().perform(put('/account-details/account/{accountId}', uuidNeverCreated).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
    }

    @Test(dataProvider = 'accountVOFieldValidations', priority = 2)
    void updateAccountValidation(HttpStatus expectedStatus, List<String> errorCodes, Map requests)
    {
        mvc().perform(put('/account-details/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(requests)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andReturn()
    }

    @Test
    void updateAccountUsingPayorAccountTest()
    {
        def payorRequest = createMemberAccountBankAccountRequest();
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.add("AccountTest", "true")
        def updatePayorRequest = updatePayorAccountRequest();
        memberAccount = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(payorRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())
        mvc().perform(put('/account-details/account/{accountId}', memberAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updatePayorRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

    }


    @Test(dataProvider = 'payorAccountVOFieldValidations', priority = 4)
    void updatePayorAccountValidation(HttpStatus expectedStatus, List<String> errorCodes, Map requests)
    {

        mvc().perform(put('/account-details/account/{accountId}', memberAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(requests)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andReturn()
    }


    @Test(dependsOnMethods = 'updateAccountUsingLocationAccountTest')
    void updateAccountUsingMemberBankAccountWithNameTest()
    {
        def bankAccountRequest = createMemberAccountBankAccountRequest()
        def updateRequest = updateAccountWithNameRequest();
        memberAccount = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(bankAccountRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())
        mvc().perform(put('/account-details/account/{accountId}', memberAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountUsingMemberBankAccountWithName"))
                .andExpect(status().isOk())
                .andReturn()

    }


    @Test(dependsOnMethods = 'updateAccountUsingMemberBankAccountWithNameTest')
    void updateAccountWithBlankNameTest()
    {

        def updateRequest = updateAccountWithBlankNameRequest();

        parseJson(mvc().perform(put('/account-details/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountWithBlankName"))
                .andExpect(status().isOk())
                .andReturn())

    }

    @Test(dependsOnMethods = 'updateAccountWithBlankNameTest')
    void updateAccountWithBlankEmailTest()
    {

        def updateRequest = updateAccountWithBlankEmailRequest();

        parseJson(mvc().perform(put('/account-details/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountWithBlankEmail"))
                .andExpect(status().isOk())
                .andReturn())

    }

    @Test(dependsOnMethods = 'updateAccountWithBlankEmailTest')
    void updateAccountWithBlankPhoneTest()
    {

        def updateRequest = updateAccountWithBlankPhoneRequest();
        parseJson(mvc().perform(put('/account-details/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountWithBlankPhone"))
                .andExpect(status().isOk())
                .andReturn())

    }

    @Test(dependsOnMethods = 'updateAccountWithBlankPhoneTest')
    void updateAccountWithBlankLocationTest()
    {

        def updateRequest = updateAccountWithBlankLocationRequest();
        mvc().perform(put('/account-details/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountWithBlankLocation"))
                .andExpect(status().isOk())
                .andReturn()

    }

    static def createLocationAccountRequest()
    {
        randomNumber = getRandomNumber()
        emailId = "Abc" + randomNumber + "Test2@qa4life.com"
        return [
                locationId      : secCreatedClientLocationId,
                clientId        : UUID.randomUUID(),
                account         : [
                        "name"         : accountName,
                        "email"        : emailId,
                        "phone"        : "1234567890",
                        "sevaluation"  : sEvaluation,
                        "billingDate"  : currentDatePlus,
                        "paymentMethod": [
                                "type"           : "BANK_ACCOUNT",
                                "bankAccountType": "SAVING",
                                "routingNumber"  : "101000187",
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


    static def updateAccountWithBlankNameRequest()
    {

        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "email"      : "kandi@qa4life.com",
                        "phone"      : "1234567890",
                        "sevaluation": sEvaluation
                ]
        ]

    }

    static def updateAccountWithBlankEmailRequest()
    {

        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"       : "Arvind Kandi",
                        "phone"      : "1234567890",
                        "sevaluation": sEvaluation
                ]
        ]

    }

    static def updateAccountWithBlankPhoneRequest()
    {

        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"       : "Arvind Kandi",
                        "email"      : "kandi@qa4life.com",
                        "sevaluation": sEvaluation
                ]
        ]

    }

    static def createLocationAccountRequestRoutingBlank()
    {

        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"         : "Arvind Kandi",
                        "email"        : "kandi@qa4life.com",
                        "phone"        : "1234567890",
                        "sevaluation"  : sEvaluation,
                        "billingDate"  : currentDate,
                        "paymentMethod": [
                                "type"           : "BANK_ACCOUNT",
                                "bankAccountType": "SAVING",
                                "routingNumber"  : "",
                                "accountNumber"  : "454556",
                                "alias"          : "testalias2"
                        ]
                ]
        ]

    }

    static def updateAccountWithBlankLocationRequest()
    {

        return [

                account: [
                        "name"       : "Arvind Kandi",
                        "email"      : "kandi@qa4life.com",
                        "phone"      : "1234567890",
                        "sevaluation": sEvaluation
                ]
        ]

    }

    static def updateClientAccountRequest()
    {

        return [
                account: [
                        "name"       : "Arvind Kandi",
                        "email"      : emailId,
                        "phone"      : "1234567890",
                        "sevaluation": "WEEKLY",
                        "billingDate": currentDate
                ]
        ]

    }

    static def updatePayorAccountRequest()
    {

        return [
                account: [
                        "name"       : "Arvind Kandi",
                        "email"      : emailId,
                        "phone"      : "1234567890",
                        "sevaluation": "WEEKLY"
                ]
        ]

    }

    static def createMemberAccountCreditRequest()
    {
        return [
                "locationId": createdClientLocationId,
                "memberId"  : UUID.randomUUID(),
                "payorId"   : UUID.randomUUID(),
                account     : [
                        "name"       : "Test",
                        "email"      : emailId,
                        "phone"      : "19075526446",
                        "sevaluation": sEvaluation,
                        "billingDate": currentDate,
                        paymentMethod: [
                                "type"   : "CASH",
                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                        ]
                ]
        ]

    }

    static def updateAccountWithMemberAccountCreditRequest()
    {
        return [
                account: [
                        "name"       : "Rajib Ranjan",
                        "email"      : emailId,
                        "phone"      : "1234567890",
                        "sevaluation": sEvaluation,
                        paymentMethod: [

                                "type"   : "CASH",
                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"

                        ]
                ]

        ]
    }

    static def createMemberAccountBankAccountRequest()
    {
        return [
                "locationId": secCreatedClientLocationId,
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

    static def updateAccountWithNameRequest()
    {

        return [

                locationId: UUID.randomUUID(),
                account   : [
                        "name"       : "Rajib Ranjan",
                        "email"      : emailId,
                        "phone"      : "1234567890",
                        "sevaluation": sEvaluation
                ]
        ]
    }

    @Test(dependsOnMethods = 'updateAccountWithBlankLocationTest')
    void updatePaymentMethodDetailsTest()
    {

        def updateRequest = updatePaymentMethodDetailsRequest();

        mvc().perform(put('/paymentMethod/{id}', locationAccount.account.paymentMethod.id).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updatePaymentMethodDetails"))
                .andExpect(status().isOk())
                .andReturn()
    }

    static def updatePaymentMethodDetailsRequest()
    {

        return [

                "type"           : "BANK_ACCOUNT",
                "bankAccountType": "SAVING",
                "routingNumber"  : "101000187",
                "accountNumber"  : "454556",
                "alias"          : "testalias2"


        ]

    }

    static def getRandomNumber()
    {
        return (int) System.nanoTime()
    }

    @DataProvider
    static Object[][] accountVOFieldValidations()
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
                                        "name": "AVCJVJCVKCkeclKCQkecAKCNQacklqacKBLQkclqkCBVNVSCGHCSBBCXHdcSLJcdHJGDJjcdbCBHJCVCVJVCHDCDBSDBJCsdckhvdscncdvdsncvscvnnnnnnnnnnnnnnnnnnvchjbncxbhdcbhdbchdchvdcj"
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "name": "12345"
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
                                        "sevaluation": "SHWETA123"
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "billingDate": beforeBillingDate
                                ]
                        ]
                ]
        ]
    }

    @DataProvider
    static Object[][] payorAccountVOFieldValidations()
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
                                        "name": "AVCJVJCVKCkeclKCQkecAKCNQacklqacKBLQkclqkCBVNVSCGHCSBBCXHdcSLJcdHJGDJjcdbCBHJCVCVJVCHDCDBSDBJCsdckhvdscncdvdsncvscvnnnnnnnnnnnnnnnnnnvchjbncxbhdcbhdbchdchvdcj"
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "name": "12345"
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
                                        "sevaluation": "SHWETA123"
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
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "billingDate": currentDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": "SHWETA123",
                                        "billingDate": currentDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.DAILY,
                                        "billingDate": currentDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.WEEKLY,
                                        "billingDate": billingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.WEEKLY,
                                        "billingDate": afterBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.WEEKLY,
                                        "billingDate": beforeBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.WEEKLY,
                                        "billingDate": currentDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.EVERY_OTHER_WEEK,
                                        "billingDate": billingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.EVERY_OTHER_WEEK,
                                        "billingDate": afterBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.EVERY_OTHER_WEEK,
                                        "billingDate": beforeBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.EVERY_OTHER_WEEK,
                                        "billingDate": currentDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.MONTHLY,
                                        "billingDate": billingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.MONTHLY,
                                        "billingDate": afterBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.MONTHLY,
                                        "billingDate": beforeBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.MONTHLY,
                                        "billingDate": currentDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.EVERY_OTHER_MONTH,
                                        "billingDate": billingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.EVERY_OTHER_MONTH,
                                        "billingDate": afterBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.EVERY_OTHER_MONTH,
                                        "billingDate": beforeBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.EVERY_OTHER_MONTH,
                                        "billingDate": currentDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.QUARTERLY,
                                        "billingDate": billingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.QUARTERLY,
                                        "billingDate": afterBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.QUARTERLY,
                                        "billingDate": beforeBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.QUARTERLY,
                                        "billingDate": currentDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.SEMIANNUALLY,
                                        "billingDate": billingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.SEMIANNUALLY,
                                        "billingDate": afterBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.SEMIANNUALLY,
                                        "billingDate": beforeBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.SEMIANNUALLY,
                                        "billingDate": currentDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.ANNUALLY,
                                        "billingDate": billingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.ANNUALLY,
                                        "billingDate": afterBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.ANNUALLY,
                                        "billingDate": beforeBillingDate
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                account: [
                                        "sevaluation": Frequency.ANNUALLY,
                                        "billingDate": currentDate
                                ]
                        ]
                ]
        ]
    }

}



