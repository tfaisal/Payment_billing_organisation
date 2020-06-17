package com.abcfinancial.api.billing.generalledger.payment

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.utility.constant.Constant
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class PaymentStatusWebhookTest extends BaseTest
{

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }

    @Test
    void paymentStatusWebhookTest()
    {
        def paymentStatusWebhookRequest = createPaymentStatusWebhookRequest()
        def paymentStatusWebhook = parseJson(mvc().perform(post('/payment-status').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(paymentStatusWebhookRequest)))
                .andDo(print()).andDo(document("createPaymentStatus"))
                .andExpect(status().isCreated())
                .andReturn())
    }

    def createPaymentStatusWebhookRequest()
    {
        return [
                "companyId"  : "3280a71d-853b-4800-bbfe-76263e2950e8",
                "eventTime"  : "03-08-2019 09:00:01",
                "eventId"    : "f0745bdf-2c9d-4f01-a9d5-e4bb25d26524",
                "payloadList": [[
                                        "status"         : "SUCCESS",
                                        "message"        : "payment has been successed",
                                        "messageCode"    : "200",
                                        "transactionType": "DEBIT",
                                        "transactionId"  : "f0745bdf-2c9d-4f01-a9d5-e4bb25d26524",
                                        "referencedId"   : Constant.uuidNeverCreated,
                                        "requested"      : "03-08-2019 09:00:01",
                                        "source"         : "BILLING",
                                        "merchantId"     : "96d18cd8-eb6d-4393-a04f-628d15795657"
                                ]]
        ]
    }
}
