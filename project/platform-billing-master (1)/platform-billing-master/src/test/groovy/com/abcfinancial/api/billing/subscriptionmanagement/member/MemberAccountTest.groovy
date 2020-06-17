package com.abcfinancial.api.billing.subscriptionmanagement.member

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.valueobject.MemberCreationVO
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.MerchantService
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class MemberAccountTest extends BaseTest
{
    @MockBean
    private MerchantService merchantService

    def accountId;
    def subscriptionMethod
    static def memberId = UUID.randomUUID();
    static def locationId = UUID.randomUUID();
    static def payorId = UUID.randomUUID();
    static def tokenId = UUID.randomUUID();
    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy');
    def clientAccount
    static def randomNumber = null
    static def accountName = name + getRandomString
    def payorAccount
    def payorAccountWithTokenId

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }

    @Test(dependsOnGroups = 'onboarding')
    void createMemberAccountTest()
    {

        def requestPayor = createMemberAccountCreditRequest()
        def requestPayorWithTokenId = createPayorAccountWithTokenIdRequest()


        payorAccount = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestPayor)))
                .andDo(print()).andDo(document("createPayorAccount" ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("\$.locationId").value(isUuid()))
                .andExpect(jsonPath("\$.payorId").value(isUuid()))
                .andReturn())

        payorAccountWithTokenId = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestPayorWithTokenId)))
                .andDo(print()).andDo(document("createPayorAccount"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("\$.locationId").value(isUuid()))
                .andExpect(jsonPath("\$.payorId").value(isUuid()))
                .andReturn())


        mvc().perform(get('/account/member/{memberId}', payorAccount.memberId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("getMemberAccount"))
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/account/payor?name=' + name).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("reviewPayorAccounts"))
                .andExpect(status().isOk())
                .andReturn()


        mvc().perform(get('/account/payor/{accountId}', payorAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("reviewPayorAccount"))
                .andExpect(status().isOk())
                .andReturn()

        mvc().perform(get('/account/payor/' + UUID.randomUUID(), payorAccount.account.accountId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())

        mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createSubscriptionRequest())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("\$.subId").value(isUuid()))
                .andExpect(jsonPath("\$.locationId").value(isUuid()))
                .andExpect(jsonPath("\$.accountId").value(isUuid()))
                .andReturn()
    }

    @Test(dataProvider = 'memberAccountVOFieldValidations')
    void testmemberAccountVOValidator(HttpStatus expectedStatus, List<String> errorCodes, Map request) throws Exception
    {
        mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}").content(toJson(request)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath('errors').isArray())
                .andExpect(jsonPath('errors', hasSize(errorCodes.size())))
                .andExpect(jsonPath('errors[*].code', containsInAnyOrder(errorCodes.toArray())))
    }

    def createMemberAccountCreditRequest()
    {
        return [
                "locationId": secCreatedClientLocationId,
                "memberId"  : UUID.randomUUID(),
                "payorId"   : UUID.randomUUID(),
                account     : [
                        "name"       : name,
                        "email"      : emailId,
                        "phone"      : "19075526443",
                        "sevaluation": sEvaluation,
                        "billingDate": currentDate,
                        paymentMethod: [
                                "type"           : bankType,
                                "bankAccountType": bankAccountType,
                                "routingNumber"  : routingNumber,
                                "accountNumber"  : accountNumber,
                                "alias"          : "testalias2"
                        ]
                ]
        ]
    }

    def createPayorAccountWithTokenIdRequest()
    {
        return ["locationId": secCreatedClientLocationId,
                "memberId"  : UUID.randomUUID(),
                "payorId"   : UUID.randomUUID(),
                account     : [
                        "name"       : name,
                        "email"      : emailId,
                        "phone"      : "19075526443",
                        "sevaluation": sEvaluation,
                        "billingDate": currentDate,
                        paymentMethod: [
                                "type"   : bankType,
                                "tokenId": tokenId
                        ]
                ]
        ]
    }

    def createSubscriptionRequest()
    {
        return [
                "locationId"     : locationId,
                "salesEmployeeId": UUID.randomUUID(),
                "accountId"      : payorAccount.account.accountId,
                "memberIdList"   : [payorAccount.memberId],
                "start"          : currentDate,
                "invoiceDate"    : currentDate,
                "expirationDate" : ExpireDate,
                "frequency"      : "MONTHLY",
                "duration"       : "4",
                "items"          : [[
                                            "itemName"       : "adcds",
                                            "itemId"         : UUID.randomUUID(),
                                            "version"        : 1,
                                            "price"          : 2117,
                                            "quantity"       : "1",
                                            "expirationStart": "PURCHASE",
                                            "type"           : "PRODUCT",
                                            "unlimited"      : "false"
                                    ]]
        ]
    }

    static def createProcessorRequest()
    {
        return [
                "accountId"     : "5b47534bf58fa7398df15d38",
                "organizationId": "5b115891b6cdfd755de3d4bd",
                "locationId"    : locationId
        ]
    }

    def updatelocationofmemberReq()
    {
        return [
                "locationId": locationId,

        ]
    }

    @DataProvider
    static Object[][] memberAccountVOFieldValidations()
    {
        return [
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": "a16e#365-d@c7-4afc-92d1-2bb105068d}c"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": UUID.randomUUID(),
                                "memberId"  : "a16e#365-d@c7-4afc-92d1-2bb105068d}c"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": UUID.randomUUID(),
                                "memberId"  : UUID.randomUUID(),
                                account     : [
                                        "name"       : "an@",
                                        paymentMethod: [
                                                "type"           : "BANK_ACCOUNT",
                                                "bankAccountType": "SAVING"
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": UUID.randomUUID(),
                                "memberId"  : UUID.randomUUID(),
                                account     : [
                                        "name"       : "Test",
                                        "email"      : "ta.com",
                                        paymentMethod: [
                                                "type"           : "BANK_ACCOUNT",
                                                "bankAccountType": "SAVING"
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": UUID.randomUUID(),
                                "memberId"  : UUID.randomUUID(),
                                account     : [
                                        "name"       : "Ravi",
                                        "email"      : "ravishjain@QA4LIFE.COM",
                                        "phone"      : "19075526443",
                                        paymentMethod: [
                                                "type"           : "BANK_ACCOUNT",
                                                "bankAccountType": "SAVING"
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": UUID.randomUUID(),
                                "memberId"  : UUID.randomUUID(),
                                account     : [
                                        "name"       : "Tarique",
                                        "email"      : "ravishjain@QA4LIFE.COM",
                                        "phone"      : "19075526443",
                                        paymentMethod: [
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
                                "locationId": UUID.randomUUID(),
                                "memberId"  : UUID.randomUUID(),
                                account     : [
                                        "name"       : "Manoj",
                                        "email"      : "ravishjain@QA4LIFE.COM",
                                        "phone"      : "19075526443",
                                        paymentMethod: [
                                                "type"           : "BANK_ACCOUNT",
                                                "bankAccountType": "SAVING",
                                                "routingNumber"  : "101000967",
                                                "accountNumber"  : ""
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": UUID.randomUUID(),
                                "memberId"  : UUID.randomUUID(),
                                account     : [
                                        "name"       : "Test",
                                        "email"      : "Manoj@QA4LIFE.COM",
                                        "phone"      : "19075526443",
                                        paymentMethod: [
                                                "type"           : "BANK_ACCOUNT",
                                                "bankAccountType": "SAVING",
                                                "routingNumber"  : "123456711111111",
                                                "accountNumber"  : "333333"
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": UUID.randomUUID(),
                                "memberId"  : UUID.randomUUID(),
                                account     : [
                                        "name"       : "Test",
                                        "email"      : "Vikas@QA4LIFE.COM",
                                        "phone"      : "19075526443",
                                        paymentMethod: [
                                                "type"           : "BANK_ACCOUNT",
                                                "bankAccountType": "SAVING",
                                                "routingNumber"  : "101000967",
                                                "accountNumber"  : "1245678963110"
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": UUID.randomUUID(),
                                "memberId"  : UUID.randomUUID(),
                                account     : [
                                        "name"       : "Test",
                                        "email"      : "Deepak@QA4LIFE.COM",
                                        "phone"      : "19075526443",
                                        "sevaluation": sEvaluation,
                                        paymentMethod: [
                                                "type"   : "CASH",
                                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": UUID.randomUUID(),
                                "memberId"  : UUID.randomUUID(),
                                account     : [
                                        "name"       : "Test",
                                        "email"      : "Anurag@QA4LIFE.COM",
                                        "phone"      : "19075526443",
                                        "sevaluation": sEvaluation,
                                        paymentMethod: [
                                                "type"   : "CASH",
                                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                                        ]
                                ]
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId": UUID.randomUUID(),
                                "memberId"  : UUID.randomUUID(),
                                account     : [
                                        "name"       : name,
                                        "email"      : emailId,
                                        "phone"      : "19075526443",
                                        "sevaluation": sEvaluation,
                                        "billingDate": currentDate,
                                        paymentMethod: [
                                                "type"           : bankType,
                                                "bankAccountType": bankAccountType,
                                                "routingNumber"  : routingNumber,
                                                "accountNumber"  : accountNumber
                                        ]
                                ]

                        ]
                ]
        ]
    }
}
