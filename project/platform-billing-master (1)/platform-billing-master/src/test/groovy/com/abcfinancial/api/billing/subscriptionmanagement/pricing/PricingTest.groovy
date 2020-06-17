package com.abcfinancial.api.billing.subscriptionmanagement.pricing

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class PricingTest extends BaseTest
{
    static def locationId = UUID.randomUUID()
    @Autowired
    private AccountRepository accountRepository

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }

    @Test(priority = 1, enabled = true)
    void calculatePricingTest()
    {
        def request = calculatePricingRequest()
        def requestUpdate = createLocationTaxRateRequest()
        parseJson(mvc().perform(post('/pricing').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print()).andDo(document("calculatePricing"))
                .andExpect(status().isCreated())
                .andReturn())


    }

    @Test(priority = 2, enabled = true)
    void calculatedPriceWithInvalidLocationTest()
    {
        def invalidLocationrequest = calculatePricingWithInvalidLocationRequest()

        parseJson(mvc().perform(post('/pricing').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(invalidLocationrequest)))
                .andDo(print()).andDo(document("calculatedPriceWithInvalidLocation"))
                .andExpect(status().isBadRequest())
                .andReturn())

    }

    @Test(priority = 2, enabled = true)
    void calculatedPricefailWithInvalidItemTest()
    {
        def invalidItemRequest = calculatePricingWithInvalidItemsRequest()

        parseJson(mvc().perform(post('/pricing').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(invalidItemRequest)))
                .andDo(print()).andDo(document("calculatedPricefailWithInvalidItem"))
                .andExpect(status().isBadRequest())
                .andReturn())

    }

    @Test(priority = 2, enabled = true)
    void calculatePricingWithEmptyLocationTest()
    {
        def emptyItemRequest = calculatePricingWithEmptyLocationRequest()

        parseJson(mvc().perform(post('/pricing').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(emptyItemRequest)))
                .andDo(print()).andDo(document("calculatePricingWithEmptyLocation"))
                .andExpect(status().isBadRequest())
                .andReturn())

    }

    @Test(priority = 2, enabled = true)
    void calculatePricingWithEmptyItemTest()
    {
        def emptyItemRequest = calculatePricingWithEmptyItemRequest()

        parseJson(mvc().perform(post('/pricing').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(emptyItemRequest)))
                .andDo(print()).andDo(document("calculatePricingWithEmptyItem"))
                .andExpect(status().isBadRequest())
                .andReturn())

    }

    @Test(priority = 2, enabled = true)
    void calculatedPriceWithnotFoundTest()
    {
        def request = calculatePricingRequest()

        mvc().perform(post('/pricing/as').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(request)))
                .andDo(print()).andDo(document("calculatedPriceWithnotFound"))
                .andExpect(status().isNotFound())
                .andReturn()

    }

    static def createLocationTaxRateRequest()
    {
        return [
                "location": locationId,
                "taxRate" : "4.3"
        ]
    }

    static def calculatePricingRequest()
    {
        return [
                "locationId"    : locationId,
                "items"         : [
                        200.2,
                        500
                ],
                "itemCategoryId": [
                        "c6626502-cd5b-4c3f-9023-4a436282a75d"
                ]
        ]
    }

    static def calculatePricingWithInvalidLocationRequest()
    {
        return [
                "locationId": "a16e#365-d@c7-4afc-92d1-2bb105068d}c",
                "items"     : [
                        200,
                        500
                ]
        ]
    }

    static def calculatePricingWithEmptyLocationRequest()
    {
        return [
                "items": [
                        200,
                        100,
                        500
                ]
        ]
    }

    static def calculatePricingWithInvalidItemsRequest()
    {
        return [
                "locationId": locationId,
                "items"     : [
                        'abc',
                        100,
                        500
                ]
        ]
    }

    static def calculatePricingWithEmptyItemRequest()
    {
        return [
                "locationId": locationId,

        ]
    }

}

