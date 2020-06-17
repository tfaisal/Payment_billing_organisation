package com.abcfinancial.api.billing.generalledger.statements;

import com.abcfinancial.api.billing.utility.common.AppConstants;
import com.abcfinancial.api.billing.generalledger.statements.service.StatementService;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.EvaluateStatementResponseVO;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.GetStatementVo;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.StatementRequestVO;
import com.abcfinancial.api.billing.generalledger.statements.valueobject.StatementResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@Slf4j

public class StatementController
{
    @Autowired
    private StatementService statementService;

    /**
     * @param accountId main or payment method account id
     */

    @GetMapping( "/evaluate-statement/account/{accountId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:write' )" )
    public ResponseEntity<EvaluateStatementResponseVO> evaluateStatement( @PathVariable UUID accountId, @RequestHeader HttpHeaders httpHeaders )
    {
        log.trace( "Statement evaluate start." );
        return ResponseEntity.status( HttpStatus.OK ).body( statementService.evaluateStatement( accountId ) );
    }

    /**
     * @param statementRequestVO Statement evaluate and generate request body.
     */

    @PostMapping( "/statement" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:write' )" )
    public ResponseEntity<StatementResponseVO> generateStatement( @RequestHeader HttpHeaders httpHeaders,
        @RequestBody StatementRequestVO statementRequestVO ) //todo MarkV why doesn't this just take an account id...what's with having an entire object???
    {
        log.trace( "Statement evaluate and generate start." );
        return ResponseEntity.status( HttpStatus.OK ).body( statementService.generateStatement( statementRequestVO.getAccountId(), httpHeaders ) );
    }

    /**
     * @param accountId  AccountId must be in java.util.UUID format
     * @param pageSize   Number of pages for respective result
     * @param pageNumber Number of records per page
     * @param sort       sorting order of data in ascending( ASC ) or descending( DESC )
     */

    @GetMapping( path = "/account/{accountId}/statement" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:read' )" )
    public Page<GetStatementVo> getStatementByAccountId( @PathVariable UUID accountId, @RequestParam( value = "page", required = false, defaultValue = "0" ) int pageSize,
        @RequestParam( value = "size",
                       required = false, defaultValue = "20" ) int pageNumber, @RequestParam( value = "sort", required = false, defaultValue = "desc" ) String sort,
        @RequestHeader HttpHeaders headers )
    {
        log.debug( "accountId {}   ", accountId );
        Pageable pageable = null;
        if( pageNumber <= 0 )
        {
            pageNumber = 20;
        }
        if( pageSize < 0 )
        {
            pageSize = 0;
        }
        if( ( sort.trim() ).equalsIgnoreCase( AppConstants.ASC ) )
        {
            pageable = PageRequest.of( pageSize, pageNumber, Sort.by( AppConstants.CREATED ).ascending() );
        }
        else
        {
            pageable = PageRequest.of( pageSize, pageNumber, Sort.by( AppConstants.CREATED ).descending() );
        }
        List<GetStatementVo> statementVos = statementService.getStatementByAccountId( accountId, pageable );
        return new PageImpl<>( statementVos, pageable, statementVos.size() );
    }

    /**
     * @param statementRequestVO Statement evaluate, generate and charge at payment-gateway request body.
     */

    @PostMapping( "/statement/charge" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:write' )" )
    public ResponseEntity<StatementResponseVO> evaluateStatementWithCharge( @RequestHeader HttpHeaders httpHeaders,
        @RequestBody StatementRequestVO statementRequestVO ) //todo MarkV why doesn't this just take an account id...what's with having an entire object???
    {
        log.trace( "Statement evaluate, generate and charge start." );
        return ResponseEntity.status( HttpStatus.OK ).body( statementService.generateStatement( statementRequestVO.getAccountId(), httpHeaders ) );
    }
}
