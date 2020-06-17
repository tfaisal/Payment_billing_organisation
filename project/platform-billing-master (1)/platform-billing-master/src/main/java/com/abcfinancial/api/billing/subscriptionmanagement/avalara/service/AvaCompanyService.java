package com.abcfinancial.api.billing.subscriptionmanagement.avalara.service;

import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AvalaraAccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.CompanyResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaCompany;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaCompanyRepository;
import com.abcfinancial.api.billing.utility.common.CommonUtil;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.common.domain.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service

public class AvaCompanyService
{
    @Autowired
    private RestTemplate restTemplate;
    @Value( "${avalara.uri.queryCompanies}" )
    private String queryCompaniesURI;
    @Autowired
    private AvaCompanyRepository avaCompanyRepository;

    public CompanyResponse queryCompanies( String username, String password )
    {
        try
        {
            log.debug( "Requesting  Query Company by username {}", username );
            HttpEntity<AvalaraAccountVO> requestEntity = new HttpEntity<>( CommonUtil.createAuthorizationHeader( username, password ) );
            return restTemplate.exchange( queryCompaniesURI, HttpMethod.GET, requestEntity, CompanyResponse.class ).getBody();
        }
        catch( HttpClientErrorException | HttpServerErrorException exception )
        {
            String message = null;
            if( exception instanceof HttpServerErrorException )
            {
                message = CommonUtil.buildExceptionMessage( ( (HttpServerErrorException) exception ).getResponseBodyAsString() );
            }
            else
            {
                message = CommonUtil.buildExceptionMessage( ( (HttpClientErrorException) exception ).getResponseBodyAsString() );
            }
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AvaCompanyService.class,
                message ) );
        }
    }

    public AvaCompany save( AvaCompany avaCompany )
    {
        return avaCompanyRepository.save( avaCompany );
    }
}
