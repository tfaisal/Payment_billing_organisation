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

class CreateLocationTest extends BaseTest
{

    def avaCreateLocationRequest = prepareCreateLocationRequest()


    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken()

    }


    @Test
    void createLocationTest()
    {
        def headers = new HttpHeaders()
        def auth = "Bearer ${bearerToken}"
        headers.add("Authorization", auth)
        headers.add("username", "2000350169")
        headers.add("password", "8E3995246223E23F")
        parseJson(mvc().perform(post('/create-location/853098').headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(avaCreateLocationRequest)))
                .andDo(document("createAvalaraLocation"))
                .andExpect(status().isOk())
                .andDo(print()).andReturn())

    }

    def prepareCreateLocationRequest()
    {
        return [

                [
                        line1            : "2000 MAIN Street",
                        city             : "IRVINE",
                        region           : "CA",
                        country          : "US",
                        postalCode       : 92614,
                        locationCode     : "LocationTest",
                        addressTypeId    : "Location",
                        addressCategoryId: "MainOffice"
                ]


        ];
    }
}
