package com.abcfinancial.api.billing.subscriptionmanagement.subscription

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class UpdateMemberSubscriptionTest extends BaseTest
{
    def accountId;
    def subscriptionMethod
    static def memberId = UUID.randomUUID();
    static def payorId = UUID.randomUUID();
    static def memberId2 = UUID.randomUUID();
    static def payorId2 = UUID.randomUUID();
    List invList = null;
    def addMemberSubscription
    def removeMemberSubscription
    static def ExpireDate = new Date().plus(2).format('MM-dd-yyyy');
    static def InvoiceDate = new Date().plus(1).format('MM-dd-yyyy');
    def createMem
    def createMem2


    @Autowired
    AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()
    }

    @Test
    void createSubscriptionTest()
    {
        def requestMember = createMemberAccountRequest()

        createMem = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestMember)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())


        accountId = createMem.account.accountId;

        def requestMember2 = createMemberAccountRequest2()
        createMem2 = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(requestMember2)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        def request = createSubscriptionRequest()
        subscriptionMethod = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())
    }

    @Test(dependsOnMethods = 'createSubscriptionTest')
    void addMemberSubscriptionTest()
    {
        def request = addMemberSubscriptionRequest()
        addMemberSubscription = parseJson(mvc().perform(post('/subscription/' + subscriptionMethod.subId + '/member').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print()).andDo(document("addMemberSubscription"))
                .andExpect(status().isCreated())
                .andReturn())
    }

    @Test(dependsOnMethods = 'createSubscriptionTest')
    void removeMemberSubscriptionTest()
    {
        removeMemberSubscription = parseJson(mvc().perform(delete('/subscription/' + subscriptionMethod.subId + '/member/' + memberId2).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("removeMemberSubscription"))
                .andExpect(status().isOk())
                .andReturn())
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

    static def createMemberAccountRequest2()
    {
        return [
                "locationId": createdClientLocationId,
                "memberId"  : memberId2,
                "payorId"   : payorId2,
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


    def addMemberSubscriptionRequest()
    {
        return [
                "memberId": createMem2.memberId
        ]
    }
}
