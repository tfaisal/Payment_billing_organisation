package com.abcfinancial.api.billing.generalledger.settlement;

import com.abcfinancial.api.billing.generalledger.settlement.service.SettlementService;
import com.abcfinancial.api.billing.generalledger.settlement.valueobject.EvaluateSettlementResponseVO;
import com.abcfinancial.api.billing.generalledger.settlement.valueobject.SettlementResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@Slf4j

public class SettlementController
{
    @Autowired
    private SettlementService settlementService;

    /**
     * @param accountId client's account Id( UUID )
     */

    @PostMapping( "/settlement/account/{accountId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:write' )" )
    public ResponseEntity<SettlementResponseVO> generateSettlement( @PathVariable( "accountId" ) UUID accountId, @RequestHeader HttpHeaders headers )
    {
        return ResponseEntity.status( HttpStatus.CREATED ).body( settlementService.generateSettlement( headers, accountId ) );
    }

    /**
     * @param accountId client's account Id( UUID )
     */

    @GetMapping( "/evaluate-settlement/account/{accountId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:read' )" )
    public ResponseEntity<EvaluateSettlementResponseVO> evaluateSettlement( @PathVariable( "accountId" ) UUID accountId, @RequestHeader HttpHeaders headers )

    {
        return ResponseEntity.status( HttpStatus.OK ).body( settlementService.evaluateSettlement( accountId ) );
    }
}
