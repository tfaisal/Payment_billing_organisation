package com.abcfinancial.api.billing.subscriptionmanagement.subscription;

import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.FreezeSubscriptionService;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.SubscriptionService;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.*;
import com.google.i18n.phonenumbers.NumberParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping( "/subscription" )

public class SubscriptionController
{
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private FreezeSubscriptionService freezeSubscriptionService;

    /**
     * @param subscription Details to create Subscription.
     */

    @PostMapping( "/" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<SubscriptionVO> createSubscription( @RequestHeader HttpHeaders headers, @RequestBody SubscriptionVO subscription )
    {
        log.trace( "create subscription start. {}", subscription );
        SubscriptionVO subscriptionVO = subscriptionService.createSubscription( subscription, false );
        return ResponseEntity.status( HttpStatus.CREATED ).body( subscriptionVO );
    }

    /**
     * @param subscriptionId SubscriptionId must in java.util.UUID format
     */

    @GetMapping( value = "/{subscriptionId}" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:read' )" )
    public ResponseEntity<SubscriptionVO> getSubscriptionById( @RequestHeader HttpHeaders headers, @Valid @PathVariable UUID subscriptionId
    )
    {
        log.trace( "get subscription by pameId: {}", subscriptionId );
        return ResponseEntity.ok().body( subscriptionService.getSubscription( subscriptionId ) );
    }

    /**
     * @param memberId MemberId must in java.util.UUID format
     */

    @GetMapping( value = "/member/{memberId}" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:read' )" )
    public ResponseEntity<List<SubscriptionVO>> getMemberSubscriptions( @Valid @PathVariable UUID memberId,
        @RequestHeader HttpHeaders headers )
    {
        return ResponseEntity.ok().body( subscriptionService.getMemberSubscriptions( memberId ) );
    }

    /**
     * @param subscriptionExpireVO Details to expire subscription.
     */

    @PutMapping( "/{subscriptionId}/expire" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<SubscriptionExpireVO> expireSubscription( @RequestHeader HttpHeaders headers, @PathVariable UUID subscriptionId,
        @Valid @RequestBody SubscriptionExpireVO subscriptionExpireVO )
    {
        return ResponseEntity.ok().body( subscriptionService.expireSubscription( subscriptionId, subscriptionExpireVO ) );
    }

    /**
     * @param subscriptionId SubscriptionId must in java.util.UUID format
     */

    @PutMapping( value = "/{subscriptionId}/cancel" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<CancelSubscriptionVO> cancelSubscription( @PathVariable UUID subscriptionId, @RequestBody SubscriptionCancelVO subscriptionCancelVO )

    {
        return ResponseEntity.ok().body( subscriptionService.cancelSubscription( subscriptionId, subscriptionCancelVO ) );
    }

    /**
     * @param subscriptionId SubscriptionId must in java.util.UUID format
     */

    @PutMapping( value = "/{subscriptionId}/removeCancel" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<RemovedCancelSubscriptionVO> removeCancelSubscription( @PathVariable UUID subscriptionId )
    {
        return ResponseEntity.ok().body( subscriptionService.removeSubscriptionCancel( subscriptionId ) );
    }

    /**
     * @param accountId Member Account Id to get the cancelled subscription list. It must in java.util.UUID format
     */

    @GetMapping( value = "/{accountId}/cancel" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<List<CancelSubscriptionVO>> getCanceledSubscriptions( @PathVariable UUID accountId )
    {
        return ResponseEntity.ok().body( subscriptionService.getCanceledSubscriptions( accountId ) );
    }

    /**
     * @param subscriptionId SubscriptionId must in java.util.UUID format
     * @param subscriptionVO Details to renew Subscription
     */

    @PutMapping( value = "/renew/{subscriptionId}" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<SubscriptionVO> renewSubscription( @PathVariable UUID subscriptionId, @Valid @RequestBody SubscriptionVO subscriptionVO )
    {
        return ResponseEntity.ok().body( subscriptionService.renewSubscription( subscriptionId, subscriptionVO, false ) );
    }

    /**
     * @param subscriptionId subscriptionId must in java.util.UUID format
     */

    @PostMapping( "/freeze/{subscriptionId}" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<FreezeSubscriptionVO> createFreezeSubscription( @RequestHeader HttpHeaders headers, @RequestBody FreezeSubscriptionVO freezeSubscriptionVO,
        @PathVariable UUID subscriptionId ) throws CloneNotSupportedException, NumberParseException
    {
        log.trace( "freeze subscription start. {}", freezeSubscriptionVO );
        FreezeSubscriptionVO subscriptionVO = freezeSubscriptionService.freezeSubscription( subscriptionId, freezeSubscriptionVO );
        return ResponseEntity.status( HttpStatus.CREATED ).body( subscriptionVO );
    }

    /**
     * @param subscriptionId SubscriptionId must in java.util.UUID format
     */

    @PutMapping( value = "/{subscriptionId}" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<SubscriptionUpdateVO> updateSubscription( @PathVariable UUID subscriptionId, @RequestBody SubscriptionUpdateVO subscriptionUpdateVO )
    {
        return ResponseEntity.ok().body( subscriptionService.updateSubscription( subscriptionId, subscriptionUpdateVO ) );
    }

    /**
     * @param subscriptionId subscriptionId must in java.util.UUID format
     * @param locationId     locationId must in java.util.UUID format
     */

    @GetMapping( value = "freeze/{subscriptionId}/location/{locationId}" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:read' )" )
    public ResponseEntity<FreezeSubscriptionResponseVo> getFreezeSubscriptionById( @RequestHeader HttpHeaders headers, @PathVariable UUID subscriptionId,
        @PathVariable UUID locationId )
    {
        log.trace( " Freeze SubscriptionId {} LocationId {}", subscriptionId, locationId );
        FreezeSubscriptionResponseVo freezeSubscriptionVO = freezeSubscriptionService.getFreezeSubscriptionById( subscriptionId, locationId );
        if( null != freezeSubscriptionVO )
        {
            return ResponseEntity.status( HttpStatus.OK ).body( freezeSubscriptionVO );
        }
        else
        {
            return ResponseEntity.status( HttpStatus.NO_CONTENT ).body( freezeSubscriptionVO );
        }
    }

    /**
     * @param subFreezeId subFreezeId must in java.util.UUID format
     */

    @PutMapping( value = "/unfreeze/{subFreezeId}" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public RemoveFreezeSubscriptionVO removeFreezeSubscription( @RequestHeader HttpHeaders headers, @PathVariable UUID subFreezeId,
        @RequestBody RemoveFreezeSubscriptionVO freezeSubscriptionVO )
    {
        return freezeSubscriptionService.removeFreeze( subFreezeId, freezeSubscriptionVO );
    }

    /**
     * @param subscriptionId subscriptionId must in java.util.UUID format
     */

    @PutMapping( "/freeze/{subscriptionId}" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<UpdateFreezeSubscriptionVO> updateFreezeSubscription( @RequestHeader HttpHeaders headers,
        @RequestBody UpdateFreezeSubscriptionRequestVO freezeSubscriptionVO,
        @PathVariable UUID subscriptionId )
    {
        log.trace( "Update freeze subscription {} freezeSubscriptionId {}", freezeSubscriptionVO, subscriptionId );
        freezeSubscriptionVO.setSubId( subscriptionId );
        UpdateFreezeSubscriptionVO subscriptionVO = freezeSubscriptionService.updateFreezeSubscription( freezeSubscriptionVO );
        return ResponseEntity.status( HttpStatus.OK ).body( subscriptionVO );
    }

    /**
     * @param subscriptionId SubscriptionId must in java.util.UUID format
     */

    @PutMapping( value = "/{subscriptionId}/updateCancel" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<CancelSubscriptionVO> updateCancelSubscription( @PathVariable UUID subscriptionId, @Valid @RequestBody SubscriptionCancelVO subscriptionCancelVO )
    {
        return ResponseEntity.ok().body( subscriptionService.updateCancelSubscription( subscriptionId, subscriptionCancelVO ) );
    }

    @PostMapping( "/{subscriptionId}/member" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<SubscriptionVO> addMemberSubscription( @RequestHeader HttpHeaders headers, @RequestBody UpdateMemberSubscriptionVO updateMemberSubscriptionVO,
        @PathVariable UUID subscriptionId )
    {
        SubscriptionVO subscriptionVO = subscriptionService.addMember( updateMemberSubscriptionVO, subscriptionId );
        return ResponseEntity.status( HttpStatus.CREATED ).body( subscriptionVO );
    }

    @DeleteMapping( "/{subscriptionId}/member/{memberId}" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
    public ResponseEntity<SubscriptionVO> removeMemberSubscription( @RequestHeader HttpHeaders headers, @PathVariable UUID memberId, @PathVariable UUID subscriptionId )
    {

        SubscriptionVO subscriptionVO = subscriptionService.removeMember( subscriptionId, memberId, true );
        return ResponseEntity.status( HttpStatus.OK ).body( subscriptionVO );
    }

    /**
     * @param subId SubscriptionId must in java.util.UUID format
     */
    @GetMapping( value = "/remaining-subscription-value/{subId}" )
    @PreAuthorize( "#oauth2.hasScope( 'subscription:read' )" )
    public ResponseEntity<SubscriptionDue> getRemainingSubscriptionDue( @Valid @PathVariable UUID subId )
    {
        return ResponseEntity.ok().body( subscriptionService.getRemainingSubscriptionValue( subId ) );
    }
}
