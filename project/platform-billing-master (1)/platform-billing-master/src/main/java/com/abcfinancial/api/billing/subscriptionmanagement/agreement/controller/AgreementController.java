package com.abcfinancial.api.billing.subscriptionmanagement.agreement.controller;

import com.abcfinancial.api.billing.subscriptionmanagement.agreement.service.AgreementService;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController

public class AgreementController
{
    @Autowired
    private AgreementService agreementService;

    @PostMapping( value = "/agreement" )
    public ResponseEntity<AgreementRequestVO> createAgreement( @RequestHeader HttpHeaders headers, @RequestBody AgreementRequestVO agreementRequestVO )
    {
        log.trace( "create Agreement start. {}", agreementRequestVO );
        AgreementRequestVO agreementResponceVO = agreementService.createAgreement( agreementRequestVO );
        return ResponseEntity.status( HttpStatus.CREATED ).body( agreementResponceVO );
    }

    /**
     * @param agreementNumber agreementNumber Agreement number up to 15 alphanumeric
     * @param cancelDate      Specify current date to get list of all future pending cancel subscription.
     * @return
     */
    @GetMapping( value = "/agreement/{agreementNumber}" )
    public ResponseEntity<AgreementResponseVO> viewAgreement( @PathVariable String agreementNumber,
        @RequestParam( value = "cancelDate", required = false ) String cancelDate )
    {
        AgreementResponseVO agreementResponseVO = agreementService.getAgreementByNumber( agreementNumber, cancelDate );
        return ResponseEntity.status( HttpStatus.OK ).body( agreementResponseVO );
    }

    /**
     * @param agreementNumber   Agreement number
     * @param agreementCancelVO
     * @return
     */
    @PutMapping( value = "/cancel-agreement/{agreementNumber}" )
    public ResponseEntity<AgreementCancelResponseVO> cancelAgreement( @PathVariable String agreementNumber, @RequestBody AgreementCancelVO agreementCancelVO )
    {
        return ResponseEntity.ok().body( agreementService.cancelAgreement( agreementNumber, agreementCancelVO ) );
    }

    /**
     * @param headers
     * @param agreementNumber Agreement Number
     * @param subscriptionId  Subscription Id
     * @return
     */
    @DeleteMapping( "/agreement/{agreementNumber}/subscription/{subscriptionId}" )
    public ResponseEntity<AgreementResponseVO> removeAgreementSubscription( @RequestHeader HttpHeaders headers, @PathVariable String agreementNumber,
        @PathVariable UUID subscriptionId )
    {
        AgreementResponseVO agreementResponseVO = agreementService.removeSubscription( agreementNumber, subscriptionId );
        return ResponseEntity.status( HttpStatus.OK ).body( agreementResponseVO );
    }

    /**
     * @param headers
     * @param agreementNumber Agreement Number
     * @param subscriptionId  Subscription Id
     * @return
     */
    @PostMapping( "/agreement/{agreementNumber}/subscription/{subscriptionId}" )
    public ResponseEntity<AgreementResponseVO> addAgreementSubscription( @RequestHeader HttpHeaders headers, @PathVariable String agreementNumber,
        @PathVariable UUID subscriptionId )
    {
        AgreementResponseVO agreementResponseVO = agreementService.addAgreementSubscription( agreementNumber, subscriptionId );

        return ResponseEntity.status( HttpStatus.CREATED ).body( agreementResponseVO );
    }

    /**
     * @param agreementNumber Agreement Number
     * @return
     */
    @PutMapping( value = "/agreement/remove-cancel-agreement/{agreementNumber}" )
    public ResponseEntity<AgreementCancelResponseVO> removeCancelAgreement( @PathVariable String agreementNumber )
    {
        return ResponseEntity.ok().body( agreementService.removeCancelAgreement( agreementNumber ) );
    }

    /**
     * @param agreementNumber agreementNumber must in java.lang.String format
     */
    @GetMapping( value = "/agreement/remaining-agreement-value/{agreementNumber}" )
    public ResponseEntity<AgreementDue> getRemainingAgreementDue( @PathVariable String agreementNumber )
    {
        return ResponseEntity.ok().body( agreementService.getRemainingAgreementValue( agreementNumber ) );

    }

    /**
     * @param agreementNumber   Agreement number
     * @param agreementMemberVO
     * @return
     */
    @PutMapping( value = "/agreement/{agreementNumber}" )
    public ResponseEntity<AgreementResponseVO> addAgreementMembersApi( @PathVariable String agreementNumber, @RequestBody AgreementMemberVO agreementMemberVO )
    {
        return ResponseEntity.ok().body( agreementService.addAgreementMember( agreementNumber, agreementMemberVO ) );
    }

    /**
     * @param agreementNumber   Agreement number
     * @param agreementMemberVO
     * @return
     */
    @DeleteMapping( value = "/agreement/{agreementNumber}" )
    public ResponseEntity<AgreementResponseVO> removeAgreementMembersApi( @PathVariable String agreementNumber, @RequestBody AgreementMemberVO agreementMemberVO )
    {
        return ResponseEntity.ok().body( agreementService.removeAgreementMember( agreementNumber, agreementMemberVO ) );
    }
}
