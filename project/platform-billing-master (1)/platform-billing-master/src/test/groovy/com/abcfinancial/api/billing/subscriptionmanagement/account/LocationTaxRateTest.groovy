package com.abcfinancial.api.billing.subscriptionmanagement.account

import com.abcfinancial.api.billing.common.BaseTest
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationTaxRate
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LocationTaxRateTest extends BaseTest
{


    static def locId = UUID.randomUUID()
    static def locationId = UUID.randomUUID()

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()
    }

    @Test
    void createLocationTaxRate()
    {
        def locationTaxRateRequest = prepareLocationTaxRate()
        mvc().perform(post('/tax-rate').header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(locationTaxRateRequest)))
                .andDo(print())
                .andDo(document("createLocationTaxRate"))
                .andExpect(status().isCreated())
                .andReturn()
    }

    @Test(dependsOnMethods = 'createLocationTaxRate')
    void getLocationTaxRate()
    {
        mvc().perform(get('/tax-rate/{locationId}', locId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getLocationTaxRate"))
                .andExpect(status().isOk())
    }

    def prepareLocationTaxRate()
    {
        return [
                "locationId"      : locId,
                "empId"           : UUID.randomUUID(),
                "taxRate"         : 20.25745,
                "taxCode"         : "rtr",
                "isOverriden"     : true,
                "suggestedTaxRate": 100

        ]
    }

    @Test(dependsOnMethods = 'createLocationTaxRate')
    void updateLocationTaxRate()
    {
        def updateLocationpayload = prepareupdateLocationTaxRate();
        mvc().perform(put('/tax-rate/{locationId}', locId).header("Authorization", "Bearer ${bearerToken}")
                .content(toJson(updateLocationpayload)))
                .andDo(print())
                .andDo(document("updateLocationTaxRate"))
                .andExpect(status().isOk())
                .andReturn()

    }

    @Test(dependsOnMethods = 'updateLocationTaxRate')
    void deleteLocationTaxRate()
    {
        mvc().perform(delete('/tax-rate/{locationId}', locId).header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("deleteLocationTaxRate"))
                .andExpect(status().isOk())
                .andReturn()
    }

    @Test
    void getAvalaraMastertaxCode()
    {
        mvc().perform(get('/avalara/master/taxcode').header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andDo(document("getAvalaraMastertaxCode"))
                .andExpect(status().isOk())
    }


    def prepareupdateLocationTaxRate()
    {
        return [
                "empId"           : "b79cac77-5c00-4169-a88e-2937de561528",
                "taxRate"         : 20.25745,
                "isOverriden"     : true,
                "suggestedTaxRate": 10
        ]
    }


}
