package com.abcfinancial.api.billing.generalledger.fee;

import com.abcfinancial.api.billing.generalledger.fee.service.FeeService;
import com.abcfinancial.api.billing.generalledger.fee.valueobject.FeeRequestVO;
import com.abcfinancial.api.billing.generalledger.fee.valueobject.FeeResponseVO;
import com.abcfinancial.api.billing.generalledger.fee.valueobject.UpdateFeeRequestVO;
import com.abcfinancial.api.billing.generalledger.fee.valueobject.UpdateFeeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static com.abcfinancial.api.billing.utility.common.AppConstants.*;

@Validated
@RestController
@Slf4j

public class FeeController
{
    @Autowired
    private FeeService feeService;

    /**
     * @param feeRequestVO
     */
    @PostMapping( "/configure-fee" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:write' )" )
    public ResponseEntity<FeeResponseVO> createFee( @RequestHeader HttpHeaders headers, @RequestBody FeeRequestVO feeRequestVO )
    {
        return ResponseEntity.status( HttpStatus.CREATED ).body( feeService.createFee( feeRequestVO ) );
    }

    /**
     * @param feeId feeId must be in java.util.UUID format
     */
    @PutMapping( value = "/configure-fee/{feeId}" )
    @PreAuthorize( "#oauth2.hasAnyScope( 'payment-account:write' )" )
    public ResponseEntity<UpdateFeeVO> updateFee( @RequestHeader HttpHeaders headers, @Valid @RequestBody UpdateFeeRequestVO updateFeeVO,
        @PathVariable( "feeId" ) UUID feeId )
    {
        return ResponseEntity.status( HttpStatus.OK ).body( feeService.updateFeeDetails( updateFeeVO, feeId ) );

    }

    /**
     * @param feeId feeId must be in java.util.UUID format
     */
    @DeleteMapping( value = "/fee/{feeId}" )
    @PreAuthorize( "#oauth2.hasAnyScope( 'payment-account:write' )" )
    public ResponseEntity<FeeResponseVO> deleteFee( @RequestHeader HttpHeaders headers, @Valid @PathVariable( "feeId" ) UUID feeId )
    {
        return ResponseEntity.status( HttpStatus.OK ).body( feeService.deleteFee( feeId ) );

    }

    /**
     * @param accountId          Client’s account Id( UUID ).
     * @param feeTransactionType Must be one of [EFT, MC, VISA, DISCOVER, AMEX].
     * @param page               Number of pages for respective result.
     * @param size               Number of records per page.
     */
    @GetMapping( value = "/fee/account/{accountId}" )
    @PreAuthorize( "#oauth2.hasScope( 'application:read' )" )
    public Page<FeeResponseVO> getFeeByAccountId( @RequestHeader HttpHeaders headers, @Valid @PathVariable( "accountId" ) UUID accountId,
        @RequestParam( value = DEFAULT_FEE_TRANSACTION_TYPE, required = false ) String feeTransactionType,
        @RequestParam( value = DEFAULT_PAGE_VALUE, required = false, defaultValue = DEFAULT_PAGE ) int page,
        @RequestParam( value = DEFAULT_SIZE_VALUE, required = false, defaultValue = DEFAULT_SIZE ) int size )
    {
        Pageable pageable = PageRequest.of( ( page < ZERO ) ? TWENTY : page, ( size <= ZERO ) ? TWENTY : size );
        List<FeeResponseVO> feeResponseVOList = feeService.getFeeByAccountId( accountId, feeTransactionType, pageable );
        return new PageImpl<>( feeResponseVOList, pageable, feeResponseVOList.size() );
    }

}
