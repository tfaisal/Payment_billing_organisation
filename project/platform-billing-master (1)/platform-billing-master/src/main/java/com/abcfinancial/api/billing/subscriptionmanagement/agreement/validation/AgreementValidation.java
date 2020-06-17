package com.abcfinancial.api.billing.subscriptionmanagement.agreement.validation;

import com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain.Agreement;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain.AgreementMember;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain.AgreementSubscription;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.repository.AgreementMemberRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.repository.AgreementRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject.AgreementMemberRequestVO;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject.AgreementMemberVO;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject.AgreementRequestVO;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject.AgreementResponseVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.MemberSubscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.SubscriptionVO;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.billing.utility.common.AppConstants;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.exception.CustomErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

@Component
public class AgreementValidation
{
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private AgreementRepository agreementRepository;
    @Autowired
    private AgreementMemberRepository agreementMemberRepository;

    public void validateAgreement( AgreementRequestVO agreementRequestVO )
    {

        List<UUID> agreementMemberIdList = new ArrayList<>();
        List<Boolean> pAgmMemberList = new ArrayList<>();
        if( !CollectionUtils.isEmpty( agreementRequestVO.getMemberIdList() ) )
        {
            agreementRequestVO.getMemberIdList().forEach( agreementMemberRequestVO -> {
                if( Objects.nonNull( agreementMemberRequestVO ) )
                {
                    if( Objects.isNull( agreementMemberRequestVO.getMemberId() ) )
                    {
                        throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Agreement.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBERLIST_BLANKIN_AGREEMENT ) ) );
                    }
                    agreementMemberIdList.add( agreementMemberRequestVO.getMemberId() );
                    pAgmMemberList.add( agreementMemberRequestVO.isPrimary() );

                }
                else
                {
                    throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Agreement.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBERLIST_BLANKIN_AGREEMENT ) ) );
                }
            } );
        }
        else
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBERLIST_BLANKIN_AGREEMENT ) ) );
        }
        if( !distinctMemberId( agreementMemberIdList ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ID_DISTINGT_IN_LIST ) ) );

        }
        if( CollectionUtils.isEmpty( agreementRequestVO.getSubscriptionIdList() ) && CollectionUtils.isEmpty( agreementRequestVO.getSubscriptionList() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MISSING_AGREEMENT_ATTACH_WITH_SUBSCRIPTIONS ) ) );
        }

        validateAgreement( agreementRequestVO, pAgmMemberList, agreementMemberIdList );
    }

    private boolean distinctMemberId( List<UUID> memberIdList )
    {
        Set<UUID> distinctMemSet = new HashSet<>( memberIdList );
        return ( distinctMemSet.size() == memberIdList.size() );
    }

    private void validateAgreement( AgreementRequestVO agreementRequestVO, List<Boolean> pAgmMemberList, List<UUID> agreementMemberIdList )
    {
        List<Boolean> primaryList = new ArrayList<>();
        agreementRequestVO.getSubscriptionList().forEach( subscriptionVO ->
            validateAgreement( subscriptionVO, agreementRequestVO, primaryList, pAgmMemberList, agreementMemberIdList )
        );

        if( Objects.isNull( agreementRequestVO.getLocationId() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATION_ID ) ) );
        }
        if( CollectionUtils.isEmpty( agreementRequestVO.getDocumentIdList() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DOCUMENT_SAME_IN_AGREEMENT ) ) );
        }

        agreementRequestVO.getDocumentIdList().forEach( documentId ->
        {
            if( Objects.isNull( documentId ) )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DOCUMENT_SAME_IN_AGREEMENT ) ) );
            }
        } );
        if( !CollectionUtils.isEmpty( agreementRequestVO.getSubscriptionIdList() ) )
        {
            agreementRequestVO.getSubscriptionIdList().forEach( subscriptionExisting ->
            {
                if( Objects.isNull( subscriptionExisting ) && CollectionUtils.isEmpty( agreementRequestVO.getSubscriptionList() ) )
                {
                    throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ONE_EXISTING_SUBSCRIPTIONID_CORRECT ) ) );
                }

                if( Objects.nonNull( subscriptionExisting.getSubId() ) )
                {
                    primaryList.add( subscriptionExisting.isPrimary() );
                }
            } );
        }
        validateAgreement( primaryList, agreementRequestVO );
    }

    private void validateAgreement( SubscriptionVO subscriptionVO, AgreementRequestVO agreementRequestVO, List<Boolean> primaryList,
        List<Boolean> pAgmMemberList, List<UUID> agreementMemberIdList )
    {
        if( Objects.isNull( subscriptionVO ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ONE_EXISTING_SUBSCRIPTIONLIST_FORMAT ) ) );
        }
        if( !subscriptionVO.getLocationId().equals( agreementRequestVO.getLocationId() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATIONID_SAME_IN_AGREEMENT ) ) );
        }
        List<UUID> memberIdListStore = subscriptionVO.getMemberIdList();
        List<UUID> memberIdList = new ArrayList<>();
        if( !CollectionUtils.isEmpty( subscriptionVO.getMemberIdList() ) )
        {
            subscriptionVO.getMemberIdList().stream().forEach( memberIdList::add );
        }
        primaryList.add( subscriptionVO.isPrimary() );
        if( Objects.nonNull( subscriptionVO.getMemberId() ) )
        {
            memberIdList.add( subscriptionVO.getMemberId() );
        }
        if( !CollectionUtils.isEmpty( memberIdListStore ) && Objects.nonNull( subscriptionVO.getMemberId() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ID_OR_MEMBERLIST ) ) );

        }

        if( primaryChecking( pAgmMemberList ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ONE_PRIMARY_MEMBER_AGREEMENT ) ) );
        }
        if( !pSubsMembertIntoPAgreementMember( agreementRequestVO, memberIdList, subscriptionVO.isPrimary() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PRIMARY_SUBSCRIPTION_MEMBER_SAME_INAGREEMENT_PRIMARY_MEMBER ) ) );
        }
        if( subsMemListIntoAgreementMemList( agreementMemberIdList, memberIdList ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUSBMEMBERLIST_PRESENT_AGREEMENTLISTLIST ) ) );
        }
    }

    private void validateAgreement( List<Boolean> primaryList, AgreementRequestVO agreementRequestVO )
    {
        if( primaryChecking( primaryList ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ONE_PRIMARY_AGREEMENT ) ) );
        }
        if( !CollectionUtils.isEmpty( agreementRequestVO.getSubscriptionList() ) && checkLocationId( agreementRequestVO.getSubscriptionList() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATIONID_SAME_IN_AGREEMENT ) ) );

        }
        if( StringUtils.isBlank( agreementRequestVO.getAgreementNumber() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AGREEMENT_NUMBER ) ) );
        }
        else if( agreementRequestVO.getAgreementNumber().length() > 15 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AGREEMENT_NUMBER_FIFTEEN_ALPHANUMERIC ) ) );
        }
        if( !StringUtils.isAlphanumeric( agreementRequestVO.getAgreementNumber() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AGREEMENT_NUMBER_ALPHANUMERIC ) ) );
        }
        if( StringUtils.isNotBlank( agreementRequestVO.getCampaign() ) )
        {
            if( agreementRequestVO.getCampaign().length() > 100 )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CAMPAIGN_LENGTH ) ) );
            }

            if( !( StringUtils.isAlphanumeric( agreementRequestVO.getCampaign().replaceAll( " ", "" ) ) ) )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CAMPAIGN_ALPHANUMERIC ) ) );
            }
        }
    }

    private boolean primaryChecking( List<Boolean> primaryList )
    {
        LongPredicate primaryPredicate = isPrimaryCount -> isPrimaryCount > 1 || isPrimaryCount == 0;
        long isPrimaryCount = primaryList.stream().filter( Boolean::booleanValue ).count();
        return primaryPredicate.test( isPrimaryCount );
    }

    public boolean pSubsMembertIntoPAgreementMember( AgreementRequestVO agreementRequestVO, List<UUID> memberSubList, boolean susPMember )
    {
        for( AgreementMemberRequestVO agreementMemberRequestVO : agreementRequestVO.getMemberIdList() )
        {
            if( agreementMemberRequestVO.isPrimary() == AppConstants.ISTRUE && susPMember == AppConstants.ISTRUE )
            {
                Predicate<UUID> memberPredicate = memberId -> memberId.equals( agreementMemberRequestVO.getMemberId() );
                boolean pMemCheck = memberSubList.stream().noneMatch( memberPredicate );
                if( pMemCheck )
                {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean subsMemListIntoAgreementMemList( List<UUID> agreementMemberIdList, List<UUID> memberSubList )
    {
        boolean memberflag = true;
        if( ( agreementMemberIdList ).containsAll( memberSubList ) )
        {
            memberflag = false;
            return memberflag;
        }
        return memberflag;
    }

    private boolean checkLocationId( List<SubscriptionVO> subscriptionList )
    {
        if( subscriptionList.size() == 1 )
        {
            return false;
        }
        Set<UUID> locationIdSet = new HashSet<>();
        for( SubscriptionVO subscriptionVO : subscriptionList )
        {
            if( locationIdSet.contains( subscriptionVO.getLocationId() ) )
            {
                return false;
            }
            locationIdSet.add( subscriptionVO.getLocationId() );
        }
        return true;
    }

    public boolean exsitingSubsMemListIntoAgreementMemlist( List<UUID> agreementMemberIdList, List<MemberSubscription> memberSubscriptionList )
    {
        List<UUID> memberIdList = new ArrayList<>();
        boolean memberflag = true;
        if( !CollectionUtils.isEmpty( agreementMemberIdList ) )
        {
            memberSubscriptionList.forEach( memberSubscription ->
                memberIdList.add( memberSubscription.getId().getMemId() )
            );
        }
        if( agreementMemberIdList.containsAll( memberIdList ) )
        {
            memberflag = false;
            return memberflag;
        }
        return memberflag;
    }

    public void validateAgreementSubscription( AgreementSubscription agreementSubscription )
    {
        if( agreementSubscription == null )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AGREEMENT_ID_NOT_EXIST ) ) );
        }
        if( agreementSubscription.getDeactivated() != null )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_ALREADY_DELETED_FROM_AGREEMENT ) ) );
        }

    }

    public void validatedUplicateAgreementSubscription( AgreementSubscription agreementSubscription )
    {
        if( agreementSubscription != null )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_ID_ALREADY_EXIST_WITH_AGRID ) ) );
        }
    }

    public void validatedAgreementId( AgreementSubscription agreementSubscription )
    {
        if( agreementSubscription == null )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AGREEMENT_ID_NOT_EXIST ) ) );
        }
    }

    public void validateSubscriptionVOForAgreement( SubscriptionVO subscriptionVO, AgreementResponseVO agreementResponseVO )
    {
        if( subscriptionVO == null )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_NOT_EXIST ) ) );
        }
        int size = agreementResponseVO.getSubscriptionList().size();

        if( size < 2 )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_DELETE_SUBSCRIPTION_ID ) ) );
        }

    }

    public void validateAgreementMember( AgreementMemberVO agreementMemberVO )
    {
        Predicate<UUID> memberPredicte = Objects::isNull;
        if( CollectionUtils.isEmpty( agreementMemberVO.getMemberIdList() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), AgreementMemberVO.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBERLIST_BLANKIN_AGREEMENT ) ) );
        }
        agreementMemberVO.getMemberIdList().forEach( memberId -> {

            if( memberPredicte.test( memberId ) )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), AgreementMemberVO.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBERLIST_BLANKIN_AGREEMENT ) ) );
            }

        } );
    }

    public void validateAgreementMemberRemove( AgreementMemberVO agreementMemberVO, String agreementNumber )
    {
        List<UUID> argMemberIdList = new ArrayList<>();
        Predicate<UUID> memberPredicte = Objects::isNull;
        Optional<AgreementMember> agreementMemberObject = agreementMemberRepository.findByAgreementNumberAndPrimary( agreementNumber, true );
        if( CollectionUtils.isEmpty( agreementMemberVO.getMemberIdList() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), AgreementMemberVO.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBERLIST_BLANKIN_AGREEMENT ) ) );
        }
        agreementMemberVO.getMemberIdList().forEach( memberId -> {

            if( memberPredicte.test( memberId ) )
            {
                throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), AgreementMemberVO.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBERLIST_BLANKIN_AGREEMENT ) ) );
            }

        } );

        List<AgreementMember> agreementMemberList = agreementMemberRepository.findByAgreementNumber( agreementNumber );
        agreementMemberList.forEach( agreementMember -> {
            argMemberIdList.add( agreementMember.getAgrmMemId().getMemId() );
            agreementMemberVO.getMemberIdList().forEach( memberId -> {

                if( agreementMember.getAgrmMemId().getMemId().equals( memberId ) && !agreementMember.isActive() )
                {
                    throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), AgreementMemberVO.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ID_REMOVE_FROM_AGREEMENT ) ) );
                }

            } );
        } );
        if( argMemberIdList.stream().noneMatch( agreementMemberVO.getMemberIdList()::contains ) )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), AgreementMemberVO.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ID_NOT_ASSOCIATED_WITH_AGREEMENT ) ) );
        }
        if( agreementMemberVO.getMemberIdList().contains( agreementMemberObject.get().getAgrmMemId().getMemId() ) )
        {
            throw new ErrorResponse( new CustomErrorResponse(
                HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), AgreementMemberVO.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PRIMARY_MEMBER_CAN_NOT_REMOVE_FROM_AGREEMENT ) +
                agreementMemberObject.get().getAgrmMemId().getMemId() ) );
        }
    }

}
