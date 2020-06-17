package com.abcfinancial.api.billing.subscriptionmanagement.subscription.restutil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service

public class HttpBillingService
{
    @Autowired
    private HttpBillingRepository httpRepository;
    private static String responseMessage = "Requested API Response  {}";

    public <REQ, RES> RES callApi( String uri, REQ entity, Class<RES> resClass )
    {
        HttpEntity<REQ> requestEntity = new HttpEntity<>( entity );
        ResponseEntity<RES> responseEntity = httpRepository.callApiRepo( uri, requestEntity, resClass );
        log.debug( responseMessage, responseEntity.getBody() );
        return responseEntity.getBody();
    }

    public <REQ, RES> RES callApi( String uri, MultiValueMap<String, String> headers, REQ entity, Class<RES> resClass )
    {
        HttpEntity<REQ> requestEntity = new HttpEntity<>( entity, headers );
        ResponseEntity<RES> responseEntity = httpRepository.callApiRepo( uri, requestEntity, resClass );
        log.debug( responseMessage, responseEntity.getBody() );
        return responseEntity.getBody();
    }

    public <REQ, RES> RES callApiCustom( String uri, MultiValueMap<String, String> headers, REQ entity, Class<RES> resClass )
    {
        HttpEntity<REQ> requestEntity = new HttpEntity<>( entity, headers );
        ResponseEntity<RES> responseEntity = httpRepository.callApiRepoCustom( uri, requestEntity, resClass );
        log.debug( responseMessage, responseEntity.getBody() );
        return responseEntity.getBody();
    }

    public <REQ, RES> RES callPutApi( String uri, MultiValueMap<String, String> headers, REQ entity, Class<RES> resClass )
    {
        HttpEntity<REQ> requestEntity = new HttpEntity<>( entity, headers );
        ResponseEntity<RES> responseEntity = httpRepository.callApiPut( uri, requestEntity, resClass );
        log.debug( responseMessage, responseEntity.getBody() );
        return responseEntity.getBody();
    }

    public <RES> RES callGetApi( String uri, Class<RES> resClass )
    {
        HttpEntity entity = new HttpEntity( "" );
        ResponseEntity<RES> responseEntity = httpRepository.callGetAPI( uri, entity, resClass );
        log.debug( responseMessage, responseEntity.getBody() );
        return responseEntity.getBody();
    }

    public <REQ, RES> RES callApi( String uri, REQ entity, Class<RES> resClass, MultiValueMap<String, String> headers )
    {
        HttpEntity<REQ> requestEntity = new HttpEntity<>( entity, headers );
        ResponseEntity<RES> responseEntity = httpRepository.callApiRepo( uri, requestEntity, resClass );
        log.trace( responseMessage, responseEntity.getBody() );
        return responseEntity.getBody();
    }
}
