package com.abcfinancial.api.billing.generalledger.payment

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

class UpdateAccountWithPaymentTest extends BaseTest
{
    @MockBean
    private MerchantService merchantService

    static def memberId = UUID.randomUUID();
    static def payorId = UUID.randomUUID();
    static def statementId = UUID.randomUUID();
    static def invoiceId = UUID.randomUUID();
    def locationAccount
    def memberAccount
    def accountId
    static def randomNumber = null
    static def emailId = null
    static def accountName = name + getRandomAccountUpdationString

    @Autowired
    private AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }


    @Test
    void updatePaymentMethodDetailsTest()
    {

        def request = createLocationAccountRequest();
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.add("AccountTest", "true")
        def updateRequest = updatePaymentMethodDetailsRequest();

        when(merchantService.createMerchant(any(HttpHeaders.class), any(String.class)))
                .thenReturn(new MerchantResponseVO(UUID.randomUUID()))

        locationAccount = parseJson(mvc().perform(post('/account/client').headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        mvc().perform(put('/paymentMethod/{id}', locationAccount.account.paymentMethod.id).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updatePaymentMethodDetails"))
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test(dependsOnMethods = 'updatePaymentMethodDetailsTest')
    void updateAccountUsingLocationAccountTest()
    {
        def updateRequest = updateAccountRequest();

        mvc().perform(put('/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountUsingLocationAccount"))
                .andExpect(status().isOk())
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
        mvc().perform(put('/account/{accountId}', memberAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
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

        parseJson(mvc().perform(put('/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
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

        parseJson(mvc().perform(put('/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
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
        parseJson(mvc().perform(put('/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountWithBlankPhone"))
                .andExpect(status().isOk())
                .andReturn())

    }

    @Test(dependsOnMethods = 'updateAccountWithBlankPhoneTest')
    void updateAccountWithPaymentMethodInvalidTypeTest()
    {

        def updateRequest = updateAccountWithPaymentMethodInvalidTypeRequest();
        parseJson(mvc().perform(put('/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountWithPaymentMethodInvalidType"))
                .andExpect(status().isBadRequest())
                .andReturn())

    }

    @Test(dependsOnMethods = 'updateAccountWithPaymentMethodInvalidTypeTest')
    void updateAccountWithPaymentMethodBlankTypeTest()
    {

        def updateRequest = updateAccountWithPaymentMethodBlankTypeRequest();
        mvc().perform(put('/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountWithPaymentMethodBlankType"))
                .andExpect(status().isBadRequest())
                .andReturn()

    }

    @Test(dependsOnMethods = 'updateAccountWithPaymentMethodBlankTypeTest')
    void updateAccountDetailsWithPaymentInvalidAccountTest()
    {
        def updateRequest = createLocationAccountRequestInvalidAccountNumber();
        mvc().perform(put('/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountDetailsWithPaymentInvalidAccount"))
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = 'updateAccountDetailsWithPaymentInvalidAccountTest')
    void updateAccountDetailsWithPaymentInvalidRoutingTest()
    {

        def updateRequest = createLocationAccountRequestRoutingBlank();
        mvc().perform(put('/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountDetailsWithPaymentInvalidRouting"))
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = 'updateAccountDetailsWithPaymentInvalidRoutingTest')
    void updateAccountWithBlankLocationTest()
    {

        def updateRequest = updateAccountWithBlankLocationRequest();
        mvc().perform(put('/account/{accountId}', locationAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountWithBlankLocation"))
                .andExpect(status().isOk())
                .andReturn()

    }

    /*  @Test(dependsOnMethods = 'updateAccountWithBlankLocationTest')
      void updateAccountWithMemberAccountInvalidCreditTypeTest()
      {
  
           def updateRequest = updateAccountWithMemberAccountInvalidCreditTypeRequest();
           mvc().perform(put('/account/{accountId}',memberAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType( MediaType.APPLICATION_JSON )
                  .content(toJson(updateRequest)))
                  .andDo(print())
                  .andDo(document("updateAccountWithMemberAccountInvalidCreditType"))
                  .andExpect(status().isBadRequest())
                  .andReturn()
  
      }*/

//    @Test(dependsOnMethods = 'updateAccountWithMemberAccountInvalidCreditTypeTest') //todo MarkV don't make http calls in unit tests...kill dimebox...
    void updateAccountWithMemberAccountCreditWithInvalidTokenTest()
    {

        def updateRequest = updateAccountWithMemberAccountCreditWithInvalidTokenRequest();
        mvc().perform(put('/account/{accountId}', memberAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest)))
                .andDo(print())
                .andDo(document("updateAccountWithMemberAccountCreditWithInvalidToken"))
                .andExpect(status().isBadRequest())
                .andReturn()

    }

    static def createLocationAccountRequest()
    {
        randomNumber = getRandomNumber()
        emailId = "Abc" + randomNumber + "Test1@qa4life.com"
        return [

                locationId      : UUID.randomUUID(),
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
                                "routingNumber"  : "101000187",
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


    static def updateAccountWithBlankNameRequest()
    {

        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "email"        : emailId,
                        "phone"        : "1234567890",
                        "sevaluation"  : sEvaluation,
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

    static def updateAccountWithBlankEmailRequest()
    {

        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"         : name,
                        "phone"        : "1234567890",
                        "sevaluation"  : sEvaluation,
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

    static def updateAccountWithBlankPhoneRequest()
    {

        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"         : "Arvind Kandi",
                        "email"        : emailId,
                        "sevaluation"  : sEvaluation,
                        //"phone":"1234567890",
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


    static def updateAccountWithPaymentMethodInvalidTypeRequest()
    {

        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"         : "Arvind Kandi",
                        "email"        : emailId,
                        "phone"        : "1234567890",
                        "sevaluation"  : sEvaluation,
                        "paymentMethod": [
                                "type"           : "BANK_ACCOUNT12",
                                "bankAccountType": "SAVING",
                                "routingNumber"  : "101000967",
                                "accountNumber"  : "454556",
                                "alias"          : "testalias2"
                        ]
                ]
        ]

    }

    static def updateAccountWithPaymentMethodBlankTypeRequest()
    {

        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"         : "Arvind Kandi",
                        "email"        : emailId,
                        "phone"        : "1234567890",
                        "sevaluation"  : sEvaluation,
                        "paymentMethod": [
                                "routingNumber": "101000967",
                                "accountNumber": "454556",
                                "alias"        : "testalias2"
                        ]
                ]
        ]

    }

    static def createLocationAccountRequestInvalidAccountNumber()
    {

        return [
                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"         : "Arvind Kandi",
                        "email"        : emailId,
                        "phone"        : "19075526443",
                        "sevaluation"  : sEvaluation,
                        "billingDate"  : currentDate,
                        "paymentMethod": [
                                "type"           : "BANK_ACCOUNT",
                                "bankAccountType": "SAVING",
                                "routingNumber"  : "101000967",
                                "accountNumber"  : "454556ed",
                                "alias"          : "testalias2"
                        ]
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
                        "name"         : "Arvind Kandi",
                        "email"        : "kandi@qa4life.com",
                        "phone"        : "1234567890",
                        "sevaluation"  : sEvaluation,
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

    static def updateAccountRequest()
    {

        return [

                locationId: UUID.randomUUID(),
                clientId  : UUID.randomUUID(),
                account   : [
                        "name"         : "Arvind Kandi",
                        "email"        : "kandi@qa4life.com",
                        "phone"        : "1234567890",
                        "sevaluation"  : sEvaluation,
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

    static def createMemberAccountCreditRequest()
    {
        return [
                "locationId": createdClientLocationId,
                "memberId"  : UUID.randomUUID(),
                "payorId"   : UUID.randomUUID(),
                account     : [
                        "name"       : "Test",
                        "email"      : "test@qa4life.com",
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

    static def updateAccountWithMemberAccountInvalidCreditTypeRequest()
    {
        return [
                account: [
                        "name"       : "Test1",
                        "email"      : "ravishjain@QA4LIFE.COM",
                        "phone"      : "1234567890",
                        "sevaluation": sEvaluation,
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
                        "email"      : "ravishjain@QA4LIFE.COM",
                        "phone"      : "1234567890",
                        "sevaluation": sEvaluation,
                        paymentMethod: [
                                "type"   : "CASH",
                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                        ]
                ]

        ]
    }

    static def updateAccountWithMemberAccountCreditWithInvalidTokenRequest()
    {
        return [
                account: [
                        "name"       : "Test1",
                        "email"      : "ravishjain@QA4LIFE.COM",
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
                "locationId": createdClientLocationId,
                "memberId"  : UUID.randomUUID(),
                "payorId"   : UUID.randomUUID(),
                account     : [
                        "name"       : "Test1",
                        "email"      : "ravishjain@QA4LIFE.COM",
                        "phone"      : "19075526443",
                        "sevaluation": sEvaluation,
                        "billingDate": currentDate,
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
                        "name"         : "Rajib Ranjan",
                        "email"        : "kandi@qa4life.com",
                        "phone"        : "1234567890",
                        "sevaluation"  : sEvaluation,
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

}



