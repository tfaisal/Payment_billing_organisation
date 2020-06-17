package com.abcfinancial.api.billing.subscriptionmanagement.avalara.service;

import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AvalaraAccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.NexusResModel;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.NexusResponseModel;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaNexus;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaNexusRepository;
import com.abcfinancial.api.billing.utility.common.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AvaNexusService
{
    @Autowired
    private RestTemplate restTemplate;
    @Value( "${avalara.uri.getNexus}" )
    private String getNexusURL;
    @Autowired
    private AvaNexusRepository avaNexusRepository;

    public List<NexusResponseModel> getNexus( String username, String password, String companyId )
    {
        ResponseEntity<NexusResModel> nexusResModelResponseEntity;
        HttpEntity<AvalaraAccountVO> requestEntity = new HttpEntity<>( CommonUtil.createAuthorizationHeader( username, password ) );
        Map<String, String> params = new HashMap<>();
        params.put( "companyId", companyId );
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString( getNexusURL );
        nexusResModelResponseEntity = restTemplate.exchange( builder.buildAndExpand( params ).toUri(), HttpMethod.GET, requestEntity, NexusResModel.class );
        return nexusResModelResponseEntity.getBody().getValue();
    }

    public AvaNexus save( AvaNexus avaNexus )
    {
        return avaNexusRepository.save( avaNexus );
    }
}
