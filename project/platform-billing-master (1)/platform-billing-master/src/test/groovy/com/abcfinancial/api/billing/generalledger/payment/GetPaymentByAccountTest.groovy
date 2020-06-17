package com.abcfinancial.api.billing.generalledger.payment

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class GetPaymentByAccountTest extends BaseTest
{

    def accountId;
    def subscriptionMethod
    static def memberId = UUID.randomUUID();
    static def payorId = UUID.randomUUID();
    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy');
    def createMemb

    @Autowired
    AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }

    @Test
    void createPaymentBySubscriptionTest()
    {

        def requestMemAccn = createMemberAccountRequest()

        createMemb = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestMemAccn)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        accountId = createMemb.account.accountId;

        def request = createSubscriptionRequest()

        subscriptionMethod = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

    }


    @Test(dependsOnMethods = 'createPaymentBySubscriptionTest')
    void getPaymentByAccountTest()
    {

        mvc().perform(get('/account/' + accountId + '/payment').header("Authorization", "Bearer ${bearerToken}")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
    }


    @Test(dependsOnMethods = 'getPaymentByAccountTest')
    void getPaymentByAccountNotFoundTest()
    {

        mvc().perform(get('/account/' + accountId + '/payment/aaa').header("Authorization", "Bearer ${bearerToken}")
        )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
    }

    @Test(dependsOnMethods = 'getPaymentByAccountNotFoundTest')
    void getPaymentByAccountInvalidAccountIdTest()
    {
        def accountId1 = "fd8ebc9e-5fab-440f-879f"
        mvc().perform(get('/account/' + accountId1 + '/payment').header("Authorization", "Bearer ${bearerToken}")
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn()
    }

    @Test(dependsOnMethods = 'getPaymentByAccountInvalidAccountIdTest')
    void getPaymentByAccountPaginationTest()
    {

        mvc().perform(get('/account/' + accountId + '/payment' + '?page=' + 0 + '&size=' + 20).header("Authorization", "Bearer ${bearerToken}")
        )

                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test(dependsOnMethods = 'getPaymentByAccountPaginationTest')
    void getPaymentByAccountPaginationAndSortingWithDescTest()
    {
        def sort = 'created,desc'
        mvc().perform(get('/account/' + accountId + '/payment' + '?page=' + 0 + '&size=' + 20 + '&sort=' + sort).header("Authorization", "Bearer ${bearerToken}")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test(dependsOnMethods = 'getPaymentByAccountPaginationAndSortingWithDescTest')
    void getPaymentByAccountPaginationAndSortingWithascTest()
    {
        def sort = 'created,asc'
        mvc().perform(get('/account/' + accountId + '/payment' + '?page=' + 0 + '&size=' + 20 + '&sort=' + sort)
                .header("Authorization", "Bearer ${bearerToken}")
        )
                .andDo(print()).andDo(document("getPaymentByAccount"))
                .andExpect(status().isOk())
                .andReturn()
    }

    def createSubscriptionRequest()
    {
        return [
                "locationId"     : createdClientLocationId,
                "salesEmployeeId": "ad8ebc9e-5fab-140f-879f-9e930cc35c74",
                "accountId"      : accountId,
                "memberIdList"   : [createMemb.memberId],
                "start"          : currentDate,
                "invoiceDate"    : currentDate,
                "expirationDate" : ExpireDate,
                "frequency"      : "DAILY",
                "duration"       : "4",
                "items"          : [[
                                            "itemName"       : "adcds",
                                            "itemId"         : "fd1ebc5e-1fab-120f-879f-4e930cc25c11",
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
                "locationId"    : createdClientLocationId
        ]
    }

    static def createMemberAccountRequest()
    {
        return [
                "locationId": createdClientLocationId,
                "memberId"  : memberId,
                "payorId"   : payorId,
                account     : [
                        "name"       : "Test",
                        "email"      : "test@gmail.com",
                        "phone"      : "19075526443",
                        "sevaluation": sEvaluation,
                        "billingDate": currentDate,
                        paymentMethod: [
                                "type"   : "CASH",
                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                        ]
                ]
        ]
    }
}
