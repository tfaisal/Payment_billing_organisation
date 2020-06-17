package com.abcfinancial.api.billing.subscriptionmanagement.account.member;

import com.abcfinancial.api.billing.subscriptionmanagement.account.member.service.MemberAccountService;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.valueobject.GetPayorResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.valueobject.MemberCreationVO;
import com.abcfinancial.api.billing.subscriptionmanagement.kafka.producer.UpdateCard;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.common.annotation.EndpointDeprecated;
import com.google.i18n.phonenumbers.NumberParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static com.abcfinancial.api.billing.utility.common.AppConstants.*;

@Slf4j
@RestController

public class MemberAccountController
{
    @Autowired
    UpdateCard updateCard;
    @Autowired
    private MemberAccountService memberAccountService;
    @Value( "${dimebox.uri.updateCardDetails}" )
    private String updateCardURI;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    /**
     * @deprecated since sprint 8
     */
    @Deprecated()
    @PostMapping( "/account/member" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:write' )" )
    @EndpointDeprecated( replacement = "Please migrate to /account/payor", deprecatedDate = "2019-02-07", validFor = 30 )
    public ResponseEntity<MemberCreationVO> createMemberAccount( @RequestHeader HttpHeaders headers, @RequestBody MemberCreationVO memberVO ) throws NumberParseException
    {
        return ResponseEntity.status( HttpStatus.CREATED ).body( memberAccountService.createMember( memberVO ) );
    }

    @PostMapping( "/account/payor" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:write' )" )
    public ResponseEntity<MemberCreationVO> createPayorAccount( @RequestHeader HttpHeaders headers, @RequestBody MemberCreationVO memberVO ) throws NumberParseException
    {
        return ResponseEntity.status( HttpStatus.CREATED ).body( memberAccountService.createMember( memberVO ) );
    }

    /**
     * @param memberId An unique id  which behaves as the identification of member belongs to a particular Location ( particular registered organization and its location )
     */

    @GetMapping( "/account/member/{memberId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:read' )" )
    public MemberCreationVO getMemberAccountById( @PathVariable( "memberId" ) UUID memberId, @RequestHeader HttpHeaders headers )
    {
        return memberAccountService.getMember( memberId );
    }

    /**
     * @param accountId payor's accountId must be UUID format.
     */

    @GetMapping( "/account/payor/{accountId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:read' )" )
    public MemberCreationVO reviewPayorAccount( @PathVariable( "accountId" ) UUID accountId, @RequestHeader HttpHeaders headers )
    {
        return memberAccountService.reviewPayor( accountId );
    }

    /**
     * @param name Name of payor account holder
     * @param page Number of pages for respective result
     * @param size Number of records per page
     */

    @GetMapping( value = "/account/payor" )
    @PreAuthorize( "#oauth2.hasScope( 'application:read' )" )
    public Page<MemberCreationVO> reviewPayorAccounts( @RequestHeader HttpHeaders headers, @RequestParam( value = DEFAULT_NAME_VALUE, required = false ) String name,
        @RequestParam( value = DEFAULT_PAGE_VALUE, required = false, defaultValue = DEFAULT_PAGE ) int page,
        @RequestParam( value = DEFAULT_SIZE_VALUE, required = false, defaultValue = DEFAULT_SIZE ) int size )
    {
        Pageable pageable = PageRequest.of( ( page < 0 ) ? 20 : page, ( size <= 0 ) ? 20 : size );
        List<MemberCreationVO> locationAccountVO = memberAccountService.reviewPayorAccounts( name, pageable );
        return new PageImpl<>( locationAccountVO, pageable, locationAccountVO.size() );
    }

    /**
     * @param accountId payor's accountId must be UUID format.
     */

    @GetMapping( "payor/account/{accountId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:read' )" )
    public GetPayorResponse getPayorAccount( @PathVariable( "accountId" ) UUID accountId, @RequestHeader HttpHeaders headers )
    {
        return memberAccountService.getPayor( accountId );
    }

}
