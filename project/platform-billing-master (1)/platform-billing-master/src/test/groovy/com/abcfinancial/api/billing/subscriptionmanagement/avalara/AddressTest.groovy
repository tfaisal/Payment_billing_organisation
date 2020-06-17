package com.abcfinancial.api.billing.subscriptionmanagement.avalara

import com.abcfinancial.api.billing.common.BaseTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static groovy.json.JsonOutput.toJson
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AddressTest extends BaseTest
{

    def avaAddressRequest = prepareAvaAddress()
    def locationId = getRandomNumber()

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }

    @Test
    void createAddressTest()
    {


        parseJson(mvc().perform(post('/createAddress/{locationId}', locationId).header("Authorization", "Bearer ${bearerToken}").contentType(MediaType.APPLICATION_JSON)
                .content(toJson(avaAddressRequest)))
                .andDo(document("createAddress"))
                .andExpect(status().isOk())
                .andDo(print()).andReturn())

    }

// Commenting out this test because it will reach out to call the Avalara service. Unit tests should be isolated to the code base.
    @Test(dependsOnMethods = 'createAddressTest')
    void resolveAddressTest()
    {
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.add("username", "2000325679")
        headers.add("password", "01902AD0EDDAD3E8")
        parseJson(mvc().perform(post('/resolve-address/157065', locationId).headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(avaAddressRequest)))
                .andDo(document("resolveAddress"))
                .andExpect(status().isOk())
                .andDo(print()).andReturn())

    }

    def prepareAvaAddress()
    {
        return [
                line      : "2000 MAIN Street",
                city      : "IRVINE",
                region    : "CA",
                country   : "US",
                postalCode: 92614


        ];
    }

    def getRandomNumber()
    {
        Random random = new Random();
        return random.nextInt(254657);
    }


}
