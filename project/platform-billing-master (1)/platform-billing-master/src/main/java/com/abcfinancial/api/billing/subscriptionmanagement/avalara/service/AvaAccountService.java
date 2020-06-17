package com.abcfinancial.api.billing.subscriptionmanagement.avalara.service;

import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AvalaraAccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.OnboardingAccountResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.LocationResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaAccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaLocationRepository;
import com.abcfinancial.api.billing.utility.common.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service

public class AvaAccountService
{
    @Value( "${account.username}" )
    private String masterUsername;
    @Value( "${account.password}" )
    private String masterPassword;
    @Autowired
    private RestTemplate restTemplate;
    @Value( "${avalara.uri.requestNewAccount}" )
    private String requestNewAccountURI;
    @Value( "${avalara.uri.queryLocations}" )
    private String queryLocationsURI;
    @Autowired
    private AvaAccountRepository avaAccountRepository;

    @Autowired
    private AvaLocationRepository avaLocationRepository;

    public OnboardingAccountResponse requestNewAccount( AvalaraAccountVO avalaraAccountVO )
    {
        log.debug( "Requesting New Avalara Account {}", avalaraAccountVO );
        HttpEntity<AvalaraAccountVO> requestEntity = new HttpEntity<>( avalaraAccountVO, CommonUtil.createAuthorizationHeader( masterUsername, "#ABC_avalara!8320" ) );
        return restTemplate.exchange( requestNewAccountURI, HttpMethod.POST, requestEntity, OnboardingAccountResponse.class ).getBody();
    }

    public LocationResponse queryLocation( String userName, String password )
    {
        log.debug( "Avalara Query Location Request accountId {}", userName );
        HttpEntity<AvalaraAccountVO> requestEntity = new HttpEntity<>( CommonUtil.createAuthorizationHeader( userName, password ) );
        return restTemplate.exchange( queryLocationsURI, HttpMethod.GET, requestEntity, LocationResponse.class ).getBody();
    }

    public AvaAccount save( AvaAccount avaAccount )
    {
        log.debug( "Saving Avalara Account to the DB." );
        return avaAccountRepository.save( avaAccount );
    }
}
