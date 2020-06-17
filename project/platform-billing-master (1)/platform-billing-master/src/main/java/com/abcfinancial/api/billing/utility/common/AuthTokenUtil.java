package com.abcfinancial.api.billing.utility.common;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.restutil.HttpBillingService;
import com.abcfinancial.api.billing.utility.common.valueobject.AuthorisationTokenVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import static com.abcfinancial.api.billing.utility.common.AppConstants.HEADER_AUTHORISATION;

@Slf4j
@Service
public final class AuthTokenUtil {

    @Autowired
    private HttpBillingService httpService;
    @Value( "${authorisation.uri.getAccessTokenUsingOath2}" )
    private String accessTokenUsingOath2;
    @Value( "${authorisation.uri.client-id}" )
    private String clientId;
    @Value( "${authorisation.uri.client-secret}" )
    private String clientSecret;

    public String getUserToken( )
    {
        HttpHeaders headers = createAuthorizationHeader( clientId, clientSecret );
        log.trace( "Invoke Authorization API to get Token." );
        AuthorisationTokenVO authorisationTokenVO = httpService.callApi( accessTokenUsingOath2, headers, "", AuthorisationTokenVO.class );
        return authorisationTokenVO.getAccessToken();
    }

    public HttpHeaders createAuthorizationHeader( String username, String password ) {
        String authHeader = null;
        String auth = username + ":" + password;
        try {
            authHeader = "Basic " + Base64.getEncoder().encodeToString( auth.getBytes( "utf-8" ) );
        } catch ( UnsupportedEncodingException exception ) {
            log.error( exception.getMessage( ) );
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add( HEADER_AUTHORISATION, authHeader );
        headers.setContentType( MediaType.APPLICATION_JSON );
        return headers;
    }
}
