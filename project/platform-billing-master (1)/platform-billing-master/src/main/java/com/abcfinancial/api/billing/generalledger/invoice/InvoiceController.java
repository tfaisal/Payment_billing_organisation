package com.abcfinancial.api.billing.generalledger.invoice;

import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.abcfinancial.api.billing.generalledger.invoice.service.InvoiceService;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.InvoiceWithoutSubscriptionVO;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.PayorInvoiceRequestVO;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.PayorInvoiceResponseVO;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
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
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController

@Slf4j

public class InvoiceController
{
    @Autowired
    InvoiceService invoiceService;

    /**
     * @param memberId  Member Id  which behaves as the identification of member
     *                  belongs to a particular Location ( particular registered
     *                  organization and its location ). It will be in UUID format.
     * @param startDate It is String type Object in MM-DD-YYYY format.
     *                  In case of not given the value it will be 30 days prior to current date.
     * @param endDate   It is String type Object in MM-DD-YYYY format.
     *                  In case of not given the value it will be current date.
     * @param page      Number of pages for respective result
     * @param size      Number of records per page
     * @deprecated as retrieving transaction history should be via payor account (and/or payment method) instead of member.
     */

    @GetMapping( value = "/invoice/member/{memberId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:read' )" )
    @Deprecated
    public Page<InvoiceWithoutSubscriptionVO> getInvoices( @RequestHeader HttpHeaders headers, @Valid @PathVariable( "memberId" ) UUID memberId,
        @RequestParam( value = "startDate", required = false ) Optional<String> startDate,
        @RequestParam( value = "endDate", required = false ) Optional<String> endDate,
        @RequestParam( value = "page", required = false, defaultValue = "0" ) int page,
        @RequestParam( value = "size", required = false, defaultValue = "20" ) int size )
    {
        Pageable pageable = null;
        int invoiceVOSize = invoiceService.findRecentInvoiceByStartDateAndEndDate( memberId, startDate, endDate, pageable ).size();
        if( page < 0 )
        {
            page = 0;
        }
        if( size <= 0 )
        {
            size = 20;
        }
        pageable = PageRequest.of( page, size );
        List<InvoiceWithoutSubscriptionVO> invoiceVO = invoiceService.findRecentInvoiceByStartDateAndEndDate( memberId, startDate, endDate, pageable );
        return new PageImpl<>( invoiceVO, pageable, invoiceVOSize );
    }

    /**
     * @param payorInvoiceRequestVO Details to create Invoice.
     */

    @PostMapping( value = "/account/payor/invoice" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:write' )" )
    public ResponseEntity<PayorInvoiceResponseVO> createPayorInvoice( @RequestHeader HttpHeaders headers, @RequestBody PayorInvoiceRequestVO payorInvoiceRequestVO )
    {
        try
        {
            return ResponseEntity.status( HttpStatus.CREATED ).body( invoiceService.createPayorInvoice( payorInvoiceRequestVO ) );
        }
        catch( TransactionSystemException exception )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Invoice.class, exception.getCause().getCause().getMessage() ) );
        }
    }
}
