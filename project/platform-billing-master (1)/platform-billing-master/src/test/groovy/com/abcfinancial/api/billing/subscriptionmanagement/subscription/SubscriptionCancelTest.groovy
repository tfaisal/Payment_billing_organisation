package com.abcfinancial.api.billing.subscriptionmanagement.subscription

import com.abcfinancial.api.billing.common.BaseTest
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static com.abcfinancial.api.billing.utility.constant.Constant.*

class SubscriptionCancelTest extends BaseTest
{

    def accountId;
    static def memberId = UUID.randomUUID();
    static def payorId = UUID.randomUUID();
    static def subscription;
    static def ExpireDate = new Date().plus(4).format('MM-dd-yyyy');
    def createMembers
    static def newSubscriptionMethod
    static def RENEWDATE = new Date().plus(16).format('MM-dd-yyyy');
    static def RENEWEXPDATE = new Date().plus(20).format('MM-dd-yyyy');

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken();

    }

    @Test
    void cancelSubscriptionTest()
    {

        createMembers = parseJson(mvc().perform(post('/account/member').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createMemberAccountRequest())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())


        accountId = createMembers.account.accountId;

        def request = createSubscriptionRequest()

        subscription = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        newSubscriptionMethod = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        def cancelSubscriptionReq = cancelSubscriptionRequest()
        mvc().perform(put('/subscription/{subscriptionId}/cancel', subscription.subId).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(cancelSubscriptionReq)))
                .andDo(print()).andDo(document("cancelSubscription"))
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test(dependsOnMethods = 'cancelSubscriptionTest')
    void updateCancelSubscriptionTest()
    {

        def cancelSubscriptionReq = updateCancelSubscriptionRequest()
        mvc().perform(put('/subscription/{subscriptionId}/updateCancel', subscription.subId).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(cancelSubscriptionReq)))
                .andDo(print()).andDo(document("updateCancelSubscription"))
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test(dependsOnMethods = 'updateCancelSubscriptionTest')
    void getCancelSubscriptionTest()
    {
        mvc().perform(get('/subscription/{accountId}/cancel', subscription.accountId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print()).andDo(document("getCancelSubscription"))
                .andExpect(status().isOk())
                .andReturn()
    }

    def createSubscriptionRequest()
    {
        return [
                "locationId"     : createMembers.locationId,
                "salesEmployeeId": UUID.randomUUID(),
                "accountId"      : createMembers.account.accountId,
                "memberIdList"   : [createMembers.memberId],
                "start"          : currentDate,
                "invoiceDate"    : currentDate,
                "expirationDate" : ExpireDate,
                "frequency"      : "DAILY",
                "duration"       : "4",
                "autoRenew"      : true,
                "renewType"      : "TERM",
                "renewalOptions" : [
                        "renewDate"       : RENEWDATE,
                        "renewFrequency"  : "DAILY",
                        "renewInvoiceDate": RENEWDATE,
                        "renewDuration"   : "1",
                        "renewType"       : "TERM",
                        "renewAmount"     : 10000.21,
                        "renewExpireDate" : RENEWEXPDATE
                ],
                "items"          : [[
                                            "itemName"       : "Swim",
                                            "itemId"         : UUID.randomUUID(),
                                            "version"        : 1,
                                            "price"          : 900.2,
                                            "quantity"       : "1",
                                            "expirationStart": "PURCHASE",
                                            "type"           : "PRODUCT",
                                            "unlimited"      : "false"
                                    ]]
        ]
    }

    static def createMemberAccountRequest()
    {
        return [
                "locationId": fourthCreatedClientLocationId,
                "memberId"  : memberId,
                "payorId"   : payorId,
                account     : [
                        "name"       : "SubscriptionCancelTest",
                        "email"      : "ravishjain@QA4LIFE.COM",
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

    static def cancelSubscriptionRequest()
    {
        return [
                "subCancellationDate": calculateDate()
        ]
    }

    static def calculateDate()
    {
        LocalDate localDate = LocalDate.now().plusDays(2L)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        return localDate.format(formatter)
    }

    static def updateCancelSubscriptionRequest()
    {
        return [
                "subCancellationDate": updateCurrentDate()
        ]
    }

    static def updateCurrentDate()
    {
        LocalDate localDate = LocalDate.now()
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        return localDate.format(formatter)
    }
}
