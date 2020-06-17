package com.abcfinancial.api.billing.common

import com.abcfinancial.api.common.test.util.UuidMatcher
import groovy.json.JsonSlurper
import org.hamcrest.Matcher
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.web.servlet.MvcResult

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*

abstract class BaseTest extends com.abcfinancial.api.common.test.BaseTest
{
    // TODO use jsonSlurper and bearerToken via com.abcfinancial.api.common.test.crud.EndpointBase
    private JsonSlurper jsonSlurper = new JsonSlurper()
    public bearerToken

    def parseJson(MvcResult result)
    {
        return jsonSlurper.parse(result.getResponse().getContentAsByteArray())
    }

    def parseJson(String json)
    {
        return jsonSlurper.parse(json.getBytes())
    }

    static RestDocumentationResultHandler document(String identifier, Snippet... snippets)
    {
        return MockMvcRestDocumentation.document(identifier, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()), snippets)
    }

    static Matcher<Object> isUuid()
    {
        return new UuidMatcher()
    }

    final static Map<String, Object> TOKEN_PARAMS = [scope      : ['payment-account:read', 'payment-account:write', 'payment-account:protected', 'payment-account:admin', 'application:read', 'permission:write', 'tax-rate:write', 'pricing:write', 'subscription:write', 'subscription:read'],
                                                     authorities: ['payment-account:read', 'payment-account:write', 'payment-account:protected', 'payment-account:admin', 'application:read', 'permission:write', 'tax-rate:write', 'pricing:write', 'subscription:write', 'subscription:read'],
                                                     aud        : ['authorization', 'billing']]

    String getBearerToken()
    {
        this.bearerToken = tokenGenerator.generateClientToken(TOKEN_PARAMS)
        return this.bearerToken
    }
}

