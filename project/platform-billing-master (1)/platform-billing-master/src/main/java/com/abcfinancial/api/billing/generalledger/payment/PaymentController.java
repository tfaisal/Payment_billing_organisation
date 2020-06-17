package com.abcfinancial.api.billing.generalledger.payment;

import com.abcfinancial.api.billing.generalledger.enums.PaymentType;
import com.abcfinancial.api.billing.generalledger.enums.Status;
import com.abcfinancial.api.billing.generalledger.payment.service.PaymentService;
import com.abcfinancial.api.billing.generalledger.payment.valueobject.*;
import com.abcfinancial.api.billing.generalledger.transaction.helper.TransactionPage;
import com.abcfinancial.api.billing.generalledger.transaction.valueobject.ClientAccountTransactionResponseVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.*;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.PayStatus;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.PaymentVO;
import com.abcfinancial.api.billing.utility.common.AppConstants;
import com.abcfinancial.api.common.annotation.EndpointDeprecated;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.common.domain.ResponseErrorImpl;
import com.google.i18n.phonenumbers.NumberParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

import static com.abcfinancial.api.billing.utility.common.AppConstants.*;

@Slf4j
@Validated
@RestController

public class PaymentController
{
    @Autowired
    private PaymentService paymentService;

    /**
     * @param accountId Account id to update account and Payment method
     */

    @PutMapping( value = "/account/{accountId}" )
    @PreAuthorize( "#oauth2.hasAnyScope( 'payment-account:write' )" )
    public ResponseEntity<UpdateAccountDetailVO> updateAccountWithPaymentMethod( @RequestHeader HttpHeaders headers, @Valid @RequestBody UpdateAccountDetailVO updateAccountVO,
        @PathVariable UUID accountId ) throws NumberParseException
    {
        return paymentService.updateAccountDetails( updateAccountVO, accountId );
    }

    /**
     * @param accountId Account id to update account details.
     */

    @PutMapping( value = "/account-details/account/{accountId}" )
    @PreAuthorize( "#oauth2.hasAnyScope( 'payment-account:write' )" )
    public ResponseEntity<UpdateAccountResponseVO> updateAccountDetails( @RequestHeader HttpHeaders headers, @Valid @RequestBody UpdateAccountRequestVO updateAccountRequestVO,
        @PathVariable UUID accountId ) throws NumberParseException
    {
        return paymentService.updateAccount( updateAccountRequestVO, accountId );
    }

    /**
     * @param id paymentMethodId must be in java.util.UUID format
     */

    @PutMapping( value = "/paymentMethod/{id}" )
    @PreAuthorize( "#oauth2.hasAnyScope( 'payment-account:write' )" )
    public ResponseEntity<PaymentMethodPaymentResponseVO> updatePaymentMethodDetails( @RequestHeader HttpHeaders headers,
        @Valid @RequestBody PaymentMethodPaymentVO paymentMethodVO,
        @PathVariable UUID id ) throws NumberParseException
    {
        return paymentService.updatePaymentMethodDetails( paymentMethodVO, id );
    }

    /**
     * @param accountId  AccountId  must be in java.util.UUID format
     * @param pageSize   Number of pages for respective result
     * @param pageNumber Number of records per page
     * @param sort       sorting order of data in ascending( ASC ) or descending( DESC )
     */

    @GetMapping( value = "account/{id}/payment" )
    public Page<PaymentVO> getPaymentByAccount( @PathVariable( "id" ) UUID accountId, @RequestParam( value = "page", required = false, defaultValue = "0" ) int pageSize,
        @RequestParam( value = "size", required = false, defaultValue = "20" ) int pageNumber, @RequestParam( value = "sort", required = false, defaultValue = "desc" ) String sort,
        @RequestHeader HttpHeaders headers )
    {
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
        List<PaymentVO> paymentVO = paymentService.getPaymentByAccount( accountId, pageable );
        return new PageImpl<>( paymentVO, pageable, paymentVO.size() );
    }

    /**
     * @param paymentId PaymentId must be in java.util.UUID format
     */

    @PostMapping( value = "/payment/{paymentId}/refund" )
    @PreAuthorize( "#oauth2.hasScope( 'permission:write' )" )
    public RefundPaymentVO refundPayment( @RequestHeader HttpHeaders headers, @PathVariable UUID paymentId, @Valid @RequestBody RefundPaymentVO refundPaymentVO )
    {
        try
        {
            return paymentService.refundPayment( paymentId, refundPaymentVO );
        }
        catch( Exception exception )
        {
            log.debug( "Unable to refund payment {}", paymentId, exception );
            ResponseErrorImpl error = new ResponseErrorImpl();
            error.setHttpStatus( HttpStatus.I_AM_A_TEAPOT );
            error.setCode( "dimebox.is.dead" );
            error.setMessageKey( "dimebox.is.dead" );
            error.setMessage( "This API is still wired up to dimebox, please don't expect much from it" );
            throw new ErrorResponse( error );
        }
    }

    /**
     * @param accountId AccountId  must be in java.util.UUID format
     */
    @PostMapping( value = "/apply-payment/account/{accountId}" )
    @PreAuthorize( "#oauth2.hasScope( 'permission:write' )" )
    public ResponseEntity<ApplyPaymentResponseVO> applyPayment( @RequestHeader HttpHeaders headers, @PathVariable UUID accountId,
        @RequestBody ApplyPaymentRequestVO applyPaymentRequestVO )
    {
        return paymentService.updateAccountSummaryInfo( headers, accountId, applyPaymentRequestVO );
    }

    /**
     *      
     *
     * @deprecated
     */
    @Deprecated
    @PostMapping( value = "/payment/{paymentId}" )
    @PreAuthorize( "#oauth2.hasScope( 'permission:write' )" )
    @EndpointDeprecated( replacement = "Please migrate to /apply-payment/account/{accountId}", deprecatedDate = "2019-01-11", validFor = 30 )
    public ResponseEntity<AccountSummaryResponseVO> applyPayment( @PathVariable UUID paymentId, @RequestBody AccountSummaryVO accountSummaryVO )
    {
        if( paymentId.compareTo( UUID.fromString( "00000000-0000-0000-0000-000000000001" ) ) == 0 )
        {
            AccountSummaryResponseVO accountSummaryResponseVO = new AccountSummaryResponseVO();
            accountSummaryResponseVO.setPayStatus( PayStatus.APPROVED );
            return ResponseEntity.status( HttpStatus.CREATED ).body( accountSummaryResponseVO );
        }

        return paymentService.updateAccountSummaryDetails( paymentId, accountSummaryVO );
    }

    @PostMapping( "/payment-status" )
    public ResponseEntity<WebhookPayloadResponseVO> createPaymentStatus( @RequestHeader HttpHeaders headers, @Valid @RequestBody WebhookPayloadRequestVO webhookPayloadRequestVO )
    {
        log.info( "createPaymentStatus WebhookPayloadRequestVO {}", webhookPayloadRequestVO );
        if( webhookPayloadRequestVO.getPayloadList().get( 0 ).getReferencedId().equalsIgnoreCase( TEST_UUID ) )
        {
            List<Payload> payloadList = new ArrayList<>();
            payloadList.add( Payload.builder().referencedId( TEST_UUID ).status( Status.SUCCESS ).transactionType( PaymentType.DEBIT ).build() );
            return ResponseEntity.status( HttpStatus.CREATED ).body( WebhookPayloadResponseVO.builder().payloadList( payloadList ).build() );
        }
        return ResponseEntity.status( HttpStatus.CREATED ).body( paymentService.createPaymentStatus( webhookPayloadRequestVO ) );
    }

    /**
     * @param accountId AccountId  must be in java.util.UUID format. It must be payor main account id or payor payment method id.
     * @param type      Transaction type must be one of [STATEMENT, PAYMENT, INVOICE].
     * @param startDate must be in mm-dd-yyyy format
     * @param endDate   must be in mm-dd-yyyy format
     * @return If main account id is provided, both main account transactions and payment method account transactions provided(if exist). if payment method id provided, only
     * payment method content will be in response.
     */
    @GetMapping( value = "/transactions/account/{accountId}" )
    public TransactionPage reviewPayorTransactions( @PathVariable UUID accountId,
        @RequestParam( value = "transactionType", required = false ) Optional<String> type,
        @RequestParam( value = "startDate", required = false ) Optional<String> startDate,
        @RequestParam( value = "endDate", required = false ) Optional<String> endDate,
        @RequestParam( value = DEFAULT_PAGE_VALUE, required = false, defaultValue = DEFAULT_PAGE ) int page,
        @RequestParam( value = DEFAULT_SIZE_VALUE, required = false, defaultValue = DEFAULT_SIZE ) int size )

    {
        Pageable pageable = PageRequest.of( ( page < ZERO ) ? TWENTY : page, ( size <= ZERO ) ? TWENTY : size, Sort.by( SUMMARY_DATE ) );
        List<PayorTransactionVO> payorTransactionVOList = paymentService.getPayorTransactions( accountId, startDate, endDate, type, pageable );
        boolean mainAccountIsNonNull = Objects.nonNull( payorTransactionVOList.get( ZERO ).getMainAccountTransactions() );
        boolean paymentMethodAccountIsNonNull = Objects.nonNull( payorTransactionVOList.get( ZERO ).getPaymentMethodTransactions() );
        return TransactionPage.builder()
                              .mainAccountTransactions(
                                  new PageImpl<>( mainAccountIsNonNull ? payorTransactionVOList.get( ZERO ).getMainAccountTransactions() : new ArrayList<>(), pageable,
                                      mainAccountIsNonNull ? payorTransactionVOList.get( ZERO ).getMainAccountTransactions().size() : 0 ) )
                              .paymentMethodTransactions(
                                  new PageImpl<>( paymentMethodAccountIsNonNull ? payorTransactionVOList.get( ZERO ).getPaymentMethodTransactions() : new ArrayList<>(), pageable,
                                      paymentMethodAccountIsNonNull ? payorTransactionVOList.get( ZERO ).getPaymentMethodTransactions().size() : 0 ) )
                              .build();
    }

    /**
     * @param paymentMethodId PaymentMethodId  must be in java.util.UUID format. It must be  payor payment method id.
     */
    @GetMapping( value = "/transactions/statementData/paymentMethod/{paymentMethodId}" )
    public Page<PaymentMethodTransactionVO> reviewPayorTransactionsSinceLastStatement( @PathVariable UUID paymentMethodId,
        @RequestParam( value = DEFAULT_PAGE_VALUE, required = false, defaultValue = DEFAULT_PAGE ) int page,
        @RequestParam( value = DEFAULT_SIZE_VALUE, required = false, defaultValue = DEFAULT_SIZE ) int size )

    {
        Pageable pageable = PageRequest.of( ( page < ZERO ) ? TWENTY : page, ( size <= ZERO ) ? TWENTY : size );
        List<PaymentMethodTransactionVO> paymentMethodTransactionVOList = paymentService.getPaymentMethodAccountTransactions( paymentMethodId, pageable );
        return new PageImpl<>( paymentMethodTransactionVOList, pageable, paymentMethodTransactionVOList.size() );
    }

    @PostMapping( "/paymentMethod" )
    @PreAuthorize( "#oauth2.hasScope( 'permission:write' )" )
    public ResponseEntity<PaymentMethodResponseVO> createPaymentMethod( @Valid @RequestBody PaymentMethodRequestVO paymentMethodRequestVO )
    {
        log.info( "createPaymentMethod PaymentMethodRequestVO {}", paymentMethodRequestVO );
        return ResponseEntity.status( HttpStatus.CREATED ).body( paymentService.createPaymentMethod( paymentMethodRequestVO ) );

    }

    /**
     * @param accountId AccountId  must be in java.util.UUID format. It must be payor main account id or payor payment method id.
     * @param type      Transaction type must be one of [  STATEMENT, SETTLEMENT, PAYMENT, ADJUSTMENT, INVOICE, DEPOSIT].
     * @param startDate must be in mm-dd-yyyy format
     * @param endDate   must be in mm-dd-yyyy format     *
     */
    @GetMapping( value = "/transactions/client/account/{accountId}" )
    public Page<ClientAccountTransactionResponseVO> reviewClientTransactions( @PathVariable UUID accountId,
        @RequestParam( value = "transactionType", required = false ) Optional<String> type,
        @RequestParam( value = "startDate", required = false ) Optional<String> startDate,
        @RequestParam( value = "endDate", required = false ) Optional<String> endDate,
        @RequestParam( value = DEFAULT_PAGE_VALUE, required = false, defaultValue = DEFAULT_PAGE ) int page,
        @RequestParam( value = DEFAULT_SIZE_VALUE, required = false, defaultValue = DEFAULT_SIZE ) int size )
    {
        Pageable pageable = PageRequest.of( ( page < ZERO ) ? TWENTY : page, ( size <= ZERO ) ? TWENTY : size, Sort.by( SUMMARY_DATE ) );
        List<ClientAccountTransactionResponseVO> clientAccountTransactions = paymentService.getClientAccountTransactions( accountId, startDate, endDate, type, pageable );
        return new PageImpl<>( clientAccountTransactions, pageable, clientAccountTransactions.size() );
    }

    /**
     * @param paymentMethodId PaymentMethodId  must be in java.util.UUID format.It must be  payor's payment method id .
     *                        (as we are doing add payment method for Payor only for now).
     */
    @GetMapping( value = "/paymentMethod/{paymentMethodId}" )
    @PreAuthorize( "#oauth2.hasScope( 'payment-account:read' )" )
    public ResponseEntity<PaymentMethodResponseVO> reviewPaymentMethod( @PathVariable( "paymentMethodId" ) UUID paymentMethodId, @RequestHeader HttpHeaders headers )
    {
        return ResponseEntity.status( HttpStatus.OK ).body( paymentService.getPaymentMethod( paymentMethodId ) );
    }
}
