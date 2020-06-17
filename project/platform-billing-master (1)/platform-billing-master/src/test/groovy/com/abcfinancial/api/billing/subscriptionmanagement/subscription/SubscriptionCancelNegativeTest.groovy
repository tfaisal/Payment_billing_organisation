package com.abcfinancial.api.billing.subscriptionmanagement.subscription

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.http.HttpStatus
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import static com.abcfinancial.api.billing.utility.constant.Constant.currentDate
import static com.abcfinancial.api.billing.utility.constant.Constant.sEvaluation
import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SubscriptionCancelNegativeTest extends BaseTest
{

    def accountId;
    static def subscriptionCreate
    static def memberId = UUID.randomUUID()
    static def payorId = UUID.randomUUID();
    static def createMem;
    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy');

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken();
    }

    @Test
    void createData()
    {
        createMem = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createMemberAccountRequest())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        subscriptionCreate = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createSubscriptionRequest())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

    }

    @Test(dataProvider = 'subscriptionCancelFieldValidations', dependsOnMethods = 'createData')
    void testSubscriptionCancelValidator(HttpStatus expectedStatus, List<String> errorCodes, Map request) throws Exception
    {

        mvc().perform(put('/subscription/{subscriptionId}/cancel', subscriptionCreate.subId).header("Authorization", "Bearer ${bearerToken}").content(toJson(request)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath('errors').isArray())
                .andExpect(jsonPath('errors', hasSize(errorCodes.size())))
                .andExpect(jsonPath('errors[*].code', containsInAnyOrder(errorCodes.toArray())))
    }

    @DataProvider
    static Object[][] subscriptionCancelFieldValidations()
    {
        return [
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "subCancellationDate": lastDate()
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "subCancellationDate": "15-18-2018"
                        ]
                ]
        ]
    }

    def createSubscriptionRequest()
    {
        return [
                "locationId"     : createMem.locationId,
                "salesEmployeeId": UUID.randomUUID(),
                "accountId"      : createMem.account.accountId,
                "memberIdList"   : [createMem.memberId],
                "start"          : currentDate,
                "invoiceDate"    : currentDate,
                "expirationDate" : ExpireDate,
                "frequency"      : "DAILY",
                "duration"       : "4",
                "items"          : [[
                                            "itemName"       : "Swimming",
                                            "itemId"         : UUID.randomUUID(),
                                            "version"        : 1,
                                            "price"          : 200,
                                            "quantity"       : "1",
                                            "expirationStart": "PURCHASE",
                                            "type"           : "PRODUCT",
                                            "unlimited"      : "false"
                                    ]]
        ]
    }

    def createProcessorRequest()
    {
        return [
                "accountId"     : "5b47534bf58fa7398df15d38",
                "organizationId": "5b115891b6cdfd755de3d4bd",
                "locationId"    : createMem.locationId
        ]
    }

    def createMemberAccountRequest()
    {

        return [
                "locationId": Constant.fourthCreatedClientLocationId,
                "memberId"  : memberId,
                "payorId"   : payorId,
                account     : [
                        "name"       : "SubscriptionCancel",
                        "email"      : "SubscriptionCancel@gmail.com",
                        "phone"      : "19175526443",
                        "sevaluation": sEvaluation,
                        "billingDate": currentDate,
                        paymentMethod: [
                                "type"   : "CASH",
                                "tokenId": "e048aa55-c4e6-44ea-8a40-5b6c2229fd2f"
                        ]
                ]
        ]
    }


    static def lastDate()
    {
        LocalDate today = LocalDate.now()
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        LocalDate yesterday = today.minusDays(1);
        return yesterday.format(formatter)
    }

    static def currentDate()
    {
        LocalDate localDate = LocalDate.now()
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        return localDate.format(formatter)
    }
}
