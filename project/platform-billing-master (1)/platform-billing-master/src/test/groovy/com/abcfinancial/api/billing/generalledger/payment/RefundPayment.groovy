package com.abcfinancial.api.billing.generalledger.payment
//package com.abcfinancial.api.generalledger.invoice.payment
//
//import AccountRepository
//import com.abcfinancial.api.common.test.BaseTest
//import org.springframework.beans.factory.annotation.Autowired
//import org.testng.annotations.BeforeMethod
//import org.testng.annotations.Test
//
//import static groovy.json.JsonOutput.toJson
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//
//class RefundPayment extends BaseTest
//{
//
//    def accountId;
//    def subscriptionMethod
//    static def memberId = UUID.randomUUID();
//    static def locId = UUID.randomUUID();
//    static def payorId = UUID.randomUUID();
//    static def currentDate = new Date().format('MM-dd-yyyy');
//
//    @Autowired
//    AccountRepository accountRepository
//
//    @BeforeMethod
//    void setToken()
//    {
//        bearerToken = generateBearerToken()
//    }
//
//
//    @Test(priority = 1, enabled = true)
//    void refundPaymentTest()
//    {
//
//        def requestMemAccount = createMemberAccountRequestData()
//
//        def createMem = parseJson(mvc().perform(post('/account/member').header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(requestMemAccount)))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andReturn())
//
//
//        accountId = createMem.account.accountId;
//
//        def request = createSubscriptionRequest()
//
//        def requestProcessorData = createProcessorRequestData()
//
//        parseJson(mvc().perform(post('/processor').header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(requestProcessorData)))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andReturn())
//
//        def createSubscriptions = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(request)))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andReturn())
//
//        def refundRequest = refundPayment()
//
//        def refundPayment = parseJson(mvc().perform(post('/payment/{paymentId}/refund', createSubscriptions.subId).header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(request)))
//                .andDo(print()).andDo(document("refundPayment"))
//                .andExpect(status().isOk())
//                .andReturn())
//    }
//
//
//    def createSubscriptionRequest()
//    {
//        return [
//                "planId"         : "fd8ebc9e-5fab-440f-879f-9e930cc35c74",
//                "locationId"     : locId,
//                "salesEmployeeId": "ad8ebc9e-5fab-140f-879f-9e930cc35c74",
//                "accountId"      : accountId,
//                "memberId"       : memberId,
//                "start"          : currentDate,
//                "invoiceDate"    : currentDate,
//                "planVersion"    : 99,
//                "frequency"      : "DAILY",
//                "duration"       : "2",
//                "name"           : "arvind",
//                "test"           : true,
//                "items"          : [[
//                                            "itemName"       : "Test",
//                                            "itemId"         : "fd1ebc5e-1fab-120f-879f-4e930cc25d11",
//                                            "version"        : 1,
//                                            "price"          : 2,
//                                            "quantity"       : "1",
//                                            "expirationStart": "PURCHASE",
//                                            "type"           : "PRODUCT",
//                                            "unlimited"      : "false"
//                                    ]]
//        ]
//    }
//
//    static def createProcessorRequestData()
//    {
//        return [
//                "accountId"     : "5b47534bf58fa7398df15d38",
//                "organizationId": "5b115891b6cdfd755de3d4bd",
//                "locationId"    : locId
//        ]
//    }
//
//    static def refundPayment()
//    {
//        return [
//
//        ]
//    }
//
//    static def createMemberAccountRequestData()
//    {
//        return [
//                "locationId": locId,
//                "memberId"  : memberId,
//                "payorId"   : payorId,
//                account     : [
//                        "name"       : "RefundPayment",
//                        "email"      : "RefundPayment@gmail.com",
//                        "phone"      : "19075526544",
//                        "sevaluation": sEvaluation,
//                        paymentMethod: [
//                                "type" : "CREDIT_CARD",
//                                "token": "d5ba5ebad4006c7b2a2f144f"
//                        ]
//                ]
//        ]
//    }
//}
