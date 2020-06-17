package com.abcfinancial.api.billing.subscriptionmanagement.subscription

import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.utility.constant.Constant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.LocalDateTime
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

class SubscriptionExpireNegativeTest extends BaseTest
{

    def accountId;
    static def subscriptionCreated
    static def memberId = UUID.randomUUID()
    static def payorId = UUID.randomUUID();
    static def createdMembers
    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy');

    @Autowired
    AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()
    }

    @Test
    void createData()
    {
        createdMembers = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createMemberAccountRequest())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

        subscriptionCreated = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(createSubscriptionRequest())))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn())

    }

    @Test(dataProvider = 'subscriptionExpireFieldValidations', dependsOnMethods = 'createData')
    void testSubscriptionExpireValidator(HttpStatus expectedStatus, List<String> errorCodes, Map request) throws Exception
    {
        mvc().perform(put('/subscription/{subscriptionId}/expire', subscriptionCreated.subId).header("Authorization", "Bearer ${bearerToken}").content(toJson(request)))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath('errors').isArray())
                .andExpect(jsonPath('errors', hasSize(errorCodes.size())))
                .andExpect(jsonPath('errors[*].code', containsInAnyOrder(errorCodes.toArray())))
    }

    @DataProvider
    static Object[][] subscriptionExpireFieldValidations()
    {
        return [
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "memberIdList"  : [createdMembers.memberId],
                                "locationId"    : createdMembers.locationId,
                                "subId"         : "fd8ebc9e-5fab-440f-879f-9e930cc35c74",
                                "expirationDate": currentDatePlus30Days()
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "memberIdList"  : [createdMembers.memberId],
                                "locationId"    : "fd8ebc9e-5fab-440f-879f-9e930cc33c74",
                                "subId"         : subscriptionCreated.subId,
                                "expirationDate": currentDatePlus30Days()
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "memberIdList"  : [createdMembers.memberId],
                                "locationId"    : createdMembers.locationId,
                                "subId"         : subscriptionCreated.subId,
                                "expirationDate": currentDatePlus30Days()
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "memberId"      : memberId,
                                "locationId"    : createdMembers.locationId,
                                "subId"         : subscriptionCreated.subId,
                                "expirationDate": "15-12-2018"
                        ]
                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "locationId"    : createdMembers.locationId,
                                "subId"         : subscriptionCreated.subId,
                                "expirationDate": "15-12-2018"
                        ]

                ],
                [
                        HttpStatus.BAD_REQUEST, ['400'],
                        [
                                "expirationDate": currentDatePlus30Days()
                        ]
                ]
        ]

    }


    def createSubscriptionRequest()
    {
        return [
                "locationId"     : createdMembers.locationId,
                "salesEmployeeId": UUID.randomUUID(),
                "accountId"      : createdMembers.account.accountId,
                "memberIdList"   : [createdMembers.memberId],
                "frequency"      : "DAILY",
                "duration"       : "4",
                "start"          : currentDate,
                "invoiceDate"    : currentDate,
                "expirationDate" : ExpireDate,
                "items"          : [[
                                            "itemName"       : "adcds",
                                            "itemId"         : UUID.randomUUID(),
                                            "version"        : 1,
                                            "price"          : 5000,
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
                "locationId"    : createdMembers.locationId
        ]
    }

    def createMemberAccountRequest()
    {

        return [
                "locationId": Constant.fourthCreatedClientLocationId,
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


    static def currentDatePlus30Days()
    {
        LocalDateTime today = LocalDateTime.now()
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        LocalDateTime yesterday = today.plusDays(30);
        return yesterday.format(formatter)
    }
}
