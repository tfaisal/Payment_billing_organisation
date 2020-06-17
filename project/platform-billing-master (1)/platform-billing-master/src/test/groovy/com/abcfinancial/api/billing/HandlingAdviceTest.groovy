package com.abcfinancial.api.billing

import com.abcfinancial.api.billing.common.BaseTest
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

public class HandlingAdviceTest extends BaseTest
{

    @BeforeMethod
    void setToken()
    {
        bearerToken = getBearerToken();
    }


    @Test
    void urlNotExist()
    {

        mvc().perform(get('/invalidrequest').header("Authorization", "Bearer ${bearerToken}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()

    }

}
