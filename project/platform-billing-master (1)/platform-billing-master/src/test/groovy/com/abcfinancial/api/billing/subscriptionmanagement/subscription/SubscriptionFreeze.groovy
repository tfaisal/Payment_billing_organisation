package com.abcfinancial.api.billing.subscriptionmanagement.subscription

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class SubscriptionFreeze extends BaseTest
{

    def accountId;
    def subscriptionMethod
    static def memberId = UUID.randomUUID();
    static def payorId = UUID.randomUUID();
    List invList = null;
    def freezeSubscription

    static def ExpireDate = new Date().plus(2).format('MM-dd-yyyy');
    static def InvoiceDate = new Date().plus(1).format('MM-dd-yyyy');
    def createMem
    static def STARTBEFORE = new Date().minus(1).format('MM-dd-yyyy');
    static def FREEZESTARTDATE = new Date().plus(20).format('MM-dd-yyyy');

    @Autowired
    AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()
    }

    @Test
    void createSubscriptiontest()
    {
        def requestMemAccn = createMemberAccountRequest()

        createMem = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestMemAccn)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())


        accountId = createMem.account.accountId;

        def request = createSubscriptionRequest()

        subscriptionMethod = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())
    }

    @Test(dependsOnMethods = 'createSubscriptiontest')
    void freezeSubscriptionTest()
    {


        def freezeRequest = createFreezeSubscriptionRequest()


        freezeSubscription = parseJson(mvc().perform(post('/subscription/freeze/' + subscriptionMethod.subId).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(freezeRequest)))
                .andDo(print()).andDo(document("freezeSubscription"))
                .andExpect(status().isCreated())
                .andReturn())

    }


    @Test(dependsOnMethods = 'freezeSubscriptionTest')
    void getfreezeSubscriptionTest()
    {
        mvc().perform(get('/subscription/freeze/' + subscriptionMethod.subId + '/location/' + createdClientLocationId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("getFreezeSubscription"))
                .andExpect(status().isOk())
                .andReturn()
    }


    @Test(dependsOnMethods = 'freezeSubscriptionTest')
    void updateFreezeSubscriptionTest()
    {
        def freezeRequest = updateFreezeSubscriptionRequest()

        freezeSubscription = parseJson(mvc().perform(put('/subscription/freeze/' + freezeSubscription.id).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(freezeRequest)))
                .andDo(print()).andDo(document("updatefreezeSubscription"))
                .andExpect(status().isOk())
                .andReturn())

    }

    @Test(dependsOnMethods = 'freezeSubscriptionTest')
    void removeFreezeSubscriptionTest()
    {
        def freezeRequest = removeFreezeSubscriptionRequest()

        freezeSubscription = parseJson(mvc().perform(put('/subscription/unfreeze/' + freezeSubscription.id).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(freezeRequest)))
                .andDo(print()).andDo(document("removeFreezeSubscription"))
                .andExpect(status().isOk())
                .andReturn())

    }


    @Test(dataProvider = 'subscriptionVOFieldValidations', dependsOnMethods = 'createSubscriptiontest')
    void testSubscriptionVOValidator(HttpStatus expectedStatus, List<String> errorCodes, Map request) throws Exception
    {
        mvc().perform(post('/subscription/freeze/' + subscriptionMethod.subId).header("Authorization", "Bearer ${bearerToken}").content(toJson(request)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath('errors').isArray())
                .andExpect(jsonPath('errors', hasSize(errorCodes.size())))
                .andExpect(jsonPath('errors[*].code', containsInAnyOrder(errorCodes.toArray())))
    }

    @DataProvider
    static Object[][] subscriptionVOFieldValidations()
    {
        return [
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "freezeStartDate": STARTBEFORE,
                                "freezeEndDate"  : futureDateTime()
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "freezeStartDate": currentDateTime(),
                                "freezeEndDate"  : ""
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "freezeStartDate": FREEZESTARTDATE,
                                "freezeEndDate"  : futureDateTime()
                        ]
                ]
        ]
    }

    def createSubscriptionRequest()
    {
        return [
                "locationId"     : createdClientLocationId,
                "salesEmployeeId": "ad8ebc9e-5fab-140f-879f-9e930cc35c74",
                "accountId"      : accountId,
                "memberIdList"   : [createMem.memberId],
                "frequency"      : "DAILY",
                "duration"       : "3",
                "start"          : currentDate,
                "invoiceDate"    : InvoiceDate,
                "expirationDate" : ExpireDate,
                "items"          : [[
                                            "itemName"       : "adcds",
                                            "itemId"         : "fd1ebc5e-1fab-120f-879f-4e930cc25c11",
                                            "version"        : 1,
                                            "price"          : 2,
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
                "locationId"    : createdClientLocationId
        ]
    }

    static def createMemberAccountRequest()
    {
        return [
                "locationId": Constant.fourthCreatedClientLocationId,
                "memberId"  : memberId,
                "payorId"   : payorId,
                account     : [
                        "name"       : "SubscriptionFreeze",
                        "email"      : "SubscriptionFreeze@gmail.com",
                        "phone"      : "19075526544",
                        "sevaluation": sEvaluation,
                        "billingDate": currentDate,
                        paymentMethod: [
                                "type"   : "CASH",
                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                        ]
                ]
        ]
    }


    def expireSubscriptionRequest()
    {
        return [
                "memberId"      : memberId,
                "locationId"    : createdClientLocationId,
                "subId"         : sub.subscription.subId,
                "expirationDate": currentDate()

        ]
    }


    def createFreezeSubscriptionRequest()
    {

        return [
                "freezeStartDate"  : currentDateTime(),
                "freezeEndDate"    : futureDateTime(),
                "subExpirationDate": updateFutureDateTime()
        ]
    }

    def updateFreezeSubscriptionRequest()
    {

        return [
                "freezeStartDate"  : futureDateTime(),
                "freezeEndDate"    : updateFutureDateTime(),
                "subExpirationDate": expireDateTime()

        ]
    }

    def removeFreezeSubscriptionRequest()
    {
        return [
                "subExpirationDate": expireDateTimeforRemove()
        ]
    }

    static def currentDateTime()
    {
        LocalDate localDate = LocalDate.now()
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        return localDate.format(formatter)
    }

    static def futureDateTime()
    {
        LocalDate localDate = LocalDate.now().plusDays(1L)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        return localDate.format(formatter)
    }

    static def updateFutureDateTime()
    {
        LocalDate localDate = LocalDate.now().plusDays(2L)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        return localDate.format(formatter)
    }

    static def expireDateTime()
    {
        LocalDate localDate = LocalDate.now().plusDays(5L)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        return localDate.format(formatter)
    }

    static def expireDateTimeforRemove()
    {
        LocalDate localDate = LocalDate.now().plusDays(8L)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        return localDate.format(formatter)
    }
}
