package com.abcfinancial.api.billing.subscriptionmanagement.account;

import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationAccountResponseVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationAccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.MemberCreation;
import com.abcfinancial.api.billing.subscriptionmanagement.account.service.AccountService;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AccountStatementVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.LocationAccountRequest;
import com.abcfinancial.api.billing.utility.common.AppConstants;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.common.annotation.EndpointDeprecated;
import com.abcfinancial.api.common.domain.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Validated
@RestController

public class AccountController
{
    String testAccount = "AccountTest";
    @Autowired
    private AccountService accountService;

    @Deprecated
    @PostMapping( value = "/account/location" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:write' )" )
    @EndpointDeprecated( replacement = "Please migrate to /account/client", deprecatedDate = "2019-02-06", validFor = 30 )
    public ResponseEntity<LocationAccountResponseVO> createLocationAccount( @RequestHeader HttpHeaders headers, @RequestBody LocationAccountRequest locationAccountVO )
    {
        try
        {
            boolean isAccountTest;
            if( ( headers.get( testAccount ) ) != null )
            {
                isAccountTest = Boolean.valueOf( headers.get( testAccount ).get( 0 ) );
            }
            else
            {
                isAccountTest = false;
            }
            return ResponseEntity.status( HttpStatus.CREATED ).body( accountService.createLocationAccount( headers, locationAccountVO, isAccountTest ) );
        }
        catch( TransactionSystemException exception )
        {
            log.debug( "Unable to create location account", exception );
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class, exception.getCause().getCause().getMessage() ) );
        }
    }

    @PostMapping( value = "/account/client" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:write' )" )
    public ResponseEntity<LocationAccountResponseVO> createClientAccount( @RequestHeader HttpHeaders headers, @Valid @RequestBody LocationAccountRequest locationAccountVO )
    {
        try
        {
            boolean isAccountTest;
            if( ( headers.get( testAccount ) ) != null )
            {
                isAccountTest = Boolean.valueOf( headers.get( testAccount ).get( 0 ) );
            }
            else
            {
                isAccountTest = false;
            }
            return ResponseEntity.status( HttpStatus.CREATED ).body( accountService.createLocationAccount( headers, locationAccountVO, isAccountTest ) );
        }
        catch( TransactionSystemException exception )
        {
            log.debug( "Unable to create location account", exception );
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class, exception.getCause().getCause().getMessage() ) );
        }
    }

    /**
     * @param locationId LocationId must be in java.util.UUID format
     */

    @GetMapping( value = "/account/location/{locationId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:read' )" )
    public LocationAccountVO getClientByLocationId( @RequestHeader HttpHeaders headers, @Valid @PathVariable UUID locationId )
    {
        return accountService.getClientByLocation( locationId );
    }

    /**
     * @param headers
     * @param statementId StatementId must be in java.util.UUID format
     * @return
     */

    @GetMapping( value = "/account/statement/{statementId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:read' )" )
    public ResponseEntity<AccountStatementVO> getStatementByStatementId( @RequestHeader HttpHeaders headers, @Valid @PathVariable UUID statementId )
    {
        return ResponseEntity.status( HttpStatus.OK ).body( accountService.getStatementByStatementId( statementId ) );
    }

    /**
     * @param headers
     * @param accountId accountId must be in java.util.UUID format
     * @param fromDate  It is optional ( If fromDate is persent then toDate will be mandatory vice versa )
     * @param toDate    It is optional
     * @param page      It is optional ( Only Integer value )
     * @param size      It is optional ( Only Integer value )
     * @return
     */

    @GetMapping( value = "/account/statement/account/{accountId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:read' )" )
    public ResponseEntity<Page<AccountStatementVO>> getStatementByAccountId( @RequestHeader HttpHeaders headers, @Valid @PathVariable( "accountId" ) UUID accountId,
        @RequestParam( value = "fromDate", required = false ) Optional<String> fromDate,
        @RequestParam( value = "toDate", required = false ) Optional<String> toDate,
        @RequestParam( value = "page", required = false, defaultValue = "0" ) int page,
        @RequestParam( value = "size", required = false, defaultValue = "20" ) int size )
    {
        if( page < 0 )
        {
            page = AppConstants.TWENTY;
        }
        if( size <= 0 )
        {
            size = AppConstants.TWENTY;
        }
        Pageable pageable = null;
        pageable = PageRequest.of( page, size, Sort.by( "stmtDate" ).descending() );
        int accountStatementVoListSize = accountService.getStatementByAccountId( accountId, fromDate, toDate, pageable ).size();
        List<AccountStatementVO> accountStatementVO = accountService.getStatementByAccountId( accountId, fromDate, toDate, pageable );
        return ResponseEntity.status( HttpStatus.OK ).body( new PageImpl<AccountStatementVO>( accountStatementVO, pageable, accountStatementVoListSize ) );
    }
}
