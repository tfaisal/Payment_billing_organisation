package com.abcfinancial.api.billing.subscriptionmanagement.agreement.service;

import com.abcfinancial.api.billing.scheduler.EventScheduler;
import com.abcfinancial.api.billing.scheduler.schedules.Schedule;
import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionCancel;
import com.abcfinancial.api.billing.scheduler.schedules.SubscriptionDeleteJob;
import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.domain.*;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.repository.AgreementMemberRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.repository.AgreementRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.repository.AgreementSubscriptionRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.repository.SubscriptionDocumentRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.validation.AgreementValidation;
import com.abcfinancial.api.billing.subscriptionmanagement.agreement.valueobject.*;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.MemberSubscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.SubscriptionTypeEnum;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionMembersRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.SubscriptionService;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.SubscriptionDue;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.valueobject.SubscriptionVO;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import com.abcfinancial.api.billing.utility.exception.CustomErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.common.domain.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

import static com.abcfinancial.api.billing.utility.common.AppConstants.*;

@Service
@Slf4j
public class AgreementService
{
    @Autowired
    SubscriptionService subscriptionService;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private AgreementRepository agreementRepository;
    @Autowired
    private AgreementSubscriptionRepository agreementSubscriptionRepository;
    @Autowired
    private SubscriptionDocumentRepository subscriptionDocumentRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionMembersRepository subscriptionMembersRepository;
    @Autowired
    private AgreementValidation agreementValidation;
    @Autowired
    private EventScheduler eventScheduler;
    @Autowired
    private AgreementMemberRepository agreementMemberRepository;
    @Autowired
    private SubscriptionDeleteJob subscriptionDeleteJob;

    @Transactional
    public AgreementRequestVO createAgreement( AgreementRequestVO agreementRequestVO )
    {
        log.debug( "Agreement Request {}", agreementRequestVO );
        boolean agreementFlag = true;
        agreementValidation.validateAgreement( agreementRequestVO );
        List<UUID> agreementMemberIdList = new ArrayList<>();
        List<Boolean> pAgmMemberList = new ArrayList<>();
        agreementRequestVO.getMemberIdList().forEach( agreementMemberRequestVO -> {
            agreementMemberIdList.add( agreementMemberRequestVO.getMemberId() );
            pAgmMemberList.add( agreementMemberRequestVO.isPrimary() );
        } );
        AgreementRequestVO agreementResponceVO = new AgreementRequestVO();
        List<SubscriptionVO> subscriptionVOResponseList = new ArrayList<>();
        log.debug( "Agreement Number {}", agreementRequestVO.getAgreementNumber() );
        Agreement agreement = new Agreement();
        Optional<Agreement> agreementPresent = agreementRepository.findByAgreementNumber( agreementRequestVO.getAgreementNumber() );
        if( agreementPresent.isPresent() && agreementPresent.get().getAgreementNumber().equals( agreementRequestVO.getAgreementNumber() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AGREEMENT_NUMBER_ALREADY_EXIST ) ) );
        }
        agreement.setAgreementNumber( agreementRequestVO.getAgreementNumber() );
        agreement.setLocationId( agreementRequestVO.getLocationId() );
        agreement.setCampaign( agreementRequestVO.getCampaign() );
        agreement = agreementRepository.save( agreement );
        saveAgreementMember( agreementRequestVO, agreement );
        existSubscription( agreementMemberIdList, agreementRequestVO, agreement, subscriptionVOResponseList );

        subscriptionVoList( agreementRequestVO, agreementFlag, agreement, subscriptionVOResponseList );

        agreementResponceVO.setAgreementId( agreement.getAgreementId() );
        agreementResponceVO.setCampaign( agreement.getCampaign() );
        log.debug( "Agreement Id {}", agreement.getAgreementId() );
        agreementResponceVO.setAgreementNumber( agreement.getAgreementNumber() );
        log.debug( "Agreement Id {}", agreement.getAgreementNumber() );
        agreementResponceVO.setLocationId( agreement.getLocationId() );
        agreementResponceVO.setDocumentIdList( agreementRequestVO.getDocumentIdList() );
        agreementResponceVO.setMemberIdList( agreementRequestVO.getMemberIdList() );
        agreementResponceVO.setSubscriptionList( subscriptionVOResponseList );
        Optional<SubscriptionVO> subscriptionVO = agreementResponceVO.getSubscriptionList().stream().filter( SubscriptionVO::isPrimary ).findFirst();
        if( subscriptionVO.isPresent() )
        {
            agreementResponceVO.setStatus( getAgreementStatus( subscriptionVO.get() ) );
        }
        log.debug( "Agreement Responce  {}", agreementResponceVO );
        return agreementResponceVO;
    }

    private void saveAgreementMember( AgreementRequestVO agreementRequestVO, Agreement agreement )
    {
        log.info( "**** saving  Agreement's members  {} ", agreementRequestVO.getMemberIdList() );
        agreementRequestVO.getMemberIdList().forEach( agreementMemberRequestVO -> {
            AgreementMember agreementMember = new AgreementMember();
            AgreementMemberId agreementMemberId = new AgreementMemberId();
            agreementMemberId.setMemId( agreementMemberRequestVO.getMemberId() );
            agreementMemberId.setAgreementId( agreement.getAgreementId() );
            agreementMember.setAgreementNumber( agreementRequestVO.getAgreementNumber() );
            agreementMember.setActive( true );
            agreementMember.setPrimary( agreementMemberRequestVO.isPrimary() );
            agreementMember.setAgrmMemId( agreementMemberId );
            agreementMemberRepository.save( agreementMember );
        } );

    }

    private void existSubscription( List<UUID> agreementMemberIdList, AgreementRequestVO agreementRequestVO, Agreement agreement, List<SubscriptionVO> subscriptionVOResponseList )
    {
        if( !CollectionUtils.isEmpty( agreementRequestVO.getSubscriptionIdList() ) )
        {
            agreementRequestVO.getSubscriptionIdList().forEach( subscriptionExisting -> {
                Subscription subscription = subscriptionRepository.findSubscription( agreementRequestVO.getLocationId(), subscriptionExisting.getSubId() );
                List<MemberSubscription> memberSubscriptionList = subscriptionMembersRepository.findListBySubId( subscriptionExisting.getSubId() );
                if( Objects.nonNull( subscription ) )
                {
                    if( !subscription.getLocationId().equals( agreementRequestVO.getLocationId() ) )
                    {
                        throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATIONID_SAME_IN_AGREEMENT ) ) );
                    }

                    if( agreementValidation.exsitingSubsMemListIntoAgreementMemlist( agreementMemberIdList, memberSubscriptionList ) )
                    {
                        throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                            applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MISSING_AGREEMENT_MEMBER_WITH_SUBSCRIPTION ) ) );
                    }

                    agreementRequestVO.getSubscriptionList().forEach( subsVO -> {
                        if( Objects.nonNull( subsVO ) && !subscription.getLocationId().equals( subsVO.getLocationId() ) )
                        {
                            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATIONID_SAME_IN_AGREEMENT ) ) );
                        }
                    } );
                    List<UUID> memberIdList = new ArrayList<>();
                    memberSubscriptionList.forEach( memberSubscription ->
                        memberIdList.add( memberSubscription.getId().getMemId() )
                    );
                    existSubscription( agreementRequestVO, memberIdList, subscriptionExisting, agreement, subscription, subscriptionVOResponseList );
                }
                else
                {
                    throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Subscription.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ONE_EXISTING_SUBSCRIPTION_NOT_PRESENT_AGREEMENT ) ) );
                }
            } );
        }
    }

    private void subscriptionVoList( AgreementRequestVO agreementRequestVO, boolean agreementFlag, Agreement agreement,
        List<SubscriptionVO> subscriptionVOResponseList )
    {
        agreementRequestVO.getSubscriptionList().forEach( subsVO -> {
            SubscriptionVO subscriptionVO = subscriptionService.createSubscription( subsVO, agreementFlag );
            AgreementSubscriptionId agreementSubscriptionId = new AgreementSubscriptionId();
            agreementSubscriptionId.setSubId( subscriptionVO.getSubId() );
            agreementSubscriptionId.setAgreementId( agreement.getAgreementId() );
            AgreementSubscription agreementSubscription = new AgreementSubscription();
            agreementSubscription.setAgrmSuId( agreementSubscriptionId );
            agreementSubscription.setPrimary( subsVO.isPrimary() );
            AgreementSubscription agreementSub = agreementSubscriptionRepository.save( agreementSubscription );
            subscriptionVO.setPrimary( agreementSub.isPrimary() );
            Optional<Subscription> subscription = subscriptionRepository.findBySubId( subscriptionVO.getSubId() );
            if( subscription.isPresent() && agreementSub.isPrimary() )
            {
                subscription.get().setSubscriptionTypeEnum( SubscriptionTypeEnum.PRIMARY );
                subscriptionRepository.save( subscription.get() );
            }
            log.debug( "Created Agreement Subscription : {}", agreementSub );

            if( subsVO.isPrimary() )
            {
                SubscriptionDocumentId subscriptionDocumentId = new SubscriptionDocumentId();
                subscriptionDocumentId.setDocumentId( agreementRequestVO.getDocumentIdList().get( 0 ) );
                subscriptionDocumentId.setSubId( subscriptionVO.getSubId() );
                SubscriptionDocuments subscriptionDocuments = new SubscriptionDocuments();
                subscriptionDocuments.setId( subscriptionDocumentId );
                SubscriptionDocuments subscriptionDocument = subscriptionDocumentRepository.save( subscriptionDocuments );
                log.debug( "Created Subscription Document: {}", subscriptionDocument );
            }
            subscriptionVOResponseList.add( subscriptionVO );
        } );
    }

    private String getAgreementStatus( SubscriptionVO subscriptionVO )
    {

        String status = null;

        Optional<Subscription> renewSubscription = subscriptionRepository.findBySubPrevRefId( subscriptionVO.getSubId() );

        if( subscriptionVO.getStart().isAfter( LocalDate.now() ) )
        {
            status = PENDING_ACTIVE;
        }
        else if( renewSubscription.isPresent() && subscriptionVO.getExpirationDate().isAfter( renewSubscription.get().getStart() ) )
        {
            status = EXPIRED;
        }

        else if( subscriptionVO.isActive() && Objects.isNull( subscriptionVO.getSubCancellationDate() ) )
        {
            status = ACTIVE;

        }
        else if( !subscriptionVO.isActive() && !subscriptionVO.isOpenEnded() && ( subscriptionVO.getExpirationDate().equals( LocalDate.now() )
                                                                                  || subscriptionVO.getExpirationDate().isBefore( LocalDate.now() ) ) &&
                 Objects.isNull( subscriptionVO.getSubCancellationDate() ) )
        {
            status = EXPIRED;
        }
        else if( !subscriptionVO.isActive() && Objects.nonNull( subscriptionVO.getSubCancellationDate() ) &&
                 ( subscriptionVO.getSubCancellationDate().toLocalDate().isBefore( LocalDate.now() )
                   || subscriptionVO.getSubCancellationDate().toLocalDate().isEqual( LocalDate.now() ) ) )
        {
            status = CANCELLED;
        }
        else if( subscriptionVO.isActive() && Objects.nonNull( subscriptionVO.getSubCancellationDate() ) && subscriptionVO.getSubCancellationDate()
                                                                                                                          .toLocalDate().isAfter( LocalDate.now() ) )
        {
            status = PENDING_CANCELLATION;
        }

        return status;
    }

    private void existSubscription( AgreementRequestVO agreementRequestVO, List<UUID> memberIdList,
        SubscriptionExistingPrimaryVO subscriptionExisting, Agreement agreement, Subscription subscription,
        List<SubscriptionVO> subscriptionVOResponseList )
    {
        if( !agreementValidation.pSubsMembertIntoPAgreementMember( agreementRequestVO, memberIdList, subscriptionExisting.isPrimary() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PRIMARY_SUBSCRIPTION_MEMBER_SAME_INAGREEMENT_PRIMARY_MEMBER ) ) );
        }
        AgreementSubscriptionId agreementSubscriptionId = new AgreementSubscriptionId();
        agreementSubscriptionId.setSubId( subscriptionExisting.getSubId() );
        agreementSubscriptionId.setAgreementId( agreement.getAgreementId() );
        AgreementSubscription agreementSubscription = new AgreementSubscription();
        agreementSubscription.setAgrmSuId( agreementSubscriptionId );
        agreementSubscription.setPrimary( subscriptionExisting.isPrimary() );
        AgreementSubscription agreementSub = agreementSubscriptionRepository.save( agreementSubscription );
        log.debug( "Created Agreement Subscription : {}", agreementSub );
        if( subscriptionExisting.isPrimary() )
        {
            SubscriptionDocumentId subscriptionDocumentId = new SubscriptionDocumentId();
            subscriptionDocumentId.setDocumentId( agreementRequestVO.getDocumentIdList().get( 0 ) );
            subscriptionDocumentId.setSubId( subscriptionExisting.getSubId() );
            SubscriptionDocuments subscriptionDocuments = new SubscriptionDocuments();
            subscriptionDocuments.setId( subscriptionDocumentId );
            SubscriptionDocuments subscriptionDocument = subscriptionDocumentRepository.save( subscriptionDocuments );
            log.debug( "Created Subscription Document: {}", subscriptionDocument );
        }
        SubscriptionVO subscriptionVO = ModelMapperUtils.map( subscription, SubscriptionVO.class );
        subscriptionVO.setPrimary( agreementSub.isPrimary() );
        subscriptionVO.setMemberIdList( memberIdList );
        subscriptionVOResponseList.add( subscriptionVO );
    }

    @Transactional
    public AgreementCancelResponseVO cancelAgreement( String agreementNumber, AgreementCancelVO agreementCancelVO )
    {
        log.info( "**** Cancellation of Agreement for agreement number {} ", agreementNumber );
        if( Objects.isNull( agreementCancelVO.getAgrmCancellationDate() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AgreementCancelVO.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CANCELDATE ) ) );
        }
        Optional<Agreement> agreement = agreementRepository.findByAgreementNumber( agreementNumber );
        isAgreementExist( agreementNumber, agreement );
        cancelValid( agreement, false );
        List<AgreementSubscription> agreementSubscriptions = agreementSubscriptionRepository.findByAgrmSuIdAgreementId( agreement.get().getAgreementId() );
        return cancelAgreement( agreementCancelVO, agreement, agreementSubscriptions );
    }

    private void isAgreementExist( String agreementNumber, Optional<Agreement> agreement )
    {
        if( !agreement.isPresent() )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), AgreementCancelVO.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AGREEMENT_ID ) + " " + agreementNumber ) );
        }
    }

    private void cancelValid( Optional<Agreement> agreement, boolean isValid )
    {
        if( agreement.isPresent() )
        {
            List<AgreementSubscription> agreementSubscriptions = agreementSubscriptionRepository.findByAgrmSuIdAgreementId( agreement.get().getAgreementId() );
            long activeCount = agreementSubscriptions.stream().filter( h -> h.getSubId().isActive() ).count();
            long cancelCount = agreementSubscriptions.stream().filter( h -> h.getSubId().getSubCancellationDate() != null ).count();
            if( activeCount == 0L )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AgreementCancelVO.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACTIVECOUNT ) ) );
            }
            else if( cancelCount == 0L && isValid )
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AgreementCancelVO.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_CANCELCOUNT ) ) );
            }
        }
    }

    private AgreementCancelResponseVO cancelAgreement( AgreementCancelVO agreementCancelVO, Optional<Agreement> agreement,
        List<AgreementSubscription> agreementSubscriptions )
    {
        AgreementCancelResponseVO agreementCancelResponseVO = new AgreementCancelResponseVO();
        if( ( agreementCancelVO.getAgrmCancellationDate().isAfter( LocalDate.now() ) || agreementCancelVO.getAgrmCancellationDate().isEqual( LocalDate.now() ) ) &&
            agreement.isPresent() )
        {
            List<UUID> subIdList = new ArrayList<>();
            isCancel( agreementSubscriptions, agreementCancelVO, subIdList );
            if( !subIdList.isEmpty() )
            {
                ModelMapperUtils.map( agreement.get(), agreementCancelResponseVO );
                agreementCancelResponseVO.setSubscriptionIdList( subIdList );
                agreementCancelResponseVO.setAgrmCancellationDate( agreementCancelVO.getAgrmCancellationDate() );
            }
            else
            {
                throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AgreementCancelVO.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_SUB_CANCEL_EXPIRED ) ) );
            }
        }
        else
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), AgreementCancelVO.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_REQUESTED_AGREEMENT_FUTURE ) ) );
        }
        return agreementCancelResponseVO;
    }

    private List<UUID> isCancel( List<AgreementSubscription> agreementSubscriptions,
        AgreementCancelVO agreementCancelVO, List<UUID> subIdList )
    {
        agreementSubscriptions.forEach( agreementSubscription -> {
            Subscription subscriptions = agreementSubscription.getSubId();

            if( subscriptions.isActive() && subscriptions.isOpenEnded() )
            {
                cancelAllSubscription( subscriptions, agreementCancelVO, subIdList );
                log.info( "Cancel open ended subscription" );
            }

            else if( subscriptions.isActive() && ( agreementCancelVO.getAgrmCancellationDate().isBefore( subscriptions.getExpirationDate() ) ) )
            {
                cancelAllSubscription( subscriptions, agreementCancelVO, subIdList );
                log.info( "Cancel term subscription" );
            }
        } );
        return subIdList;
    }

    private void cancelAllSubscription( Subscription subscriptions, AgreementCancelVO agreementCancelVO, List<UUID> subIdList )
    {
        if( null != subscriptions.getSubCancelEventId() )
        {
            eventScheduler.cancelSub( subscriptions.getSubCancelEventId() );
            subscriptions.setSubCancelEventId( null );
        }
        subscriptions.setSubCancellationDate( agreementCancelVO.getAgrmCancellationDate().atStartOfDay() );
        Optional<UUID> cancelId = cancelSubscription( subscriptions, agreementCancelVO.getAgrmCancellationDate() );
        if( cancelId.isPresent() )
        {
            subscriptions.setSubCancelEventId( cancelId.get() );
        }
        subIdList.add( subscriptions.getSubId() );
        subscriptionRepository.save( subscriptions );
    }

    public Optional<UUID> cancelSubscription( Subscription subscription, LocalDate cancellationDate )
    {
        return eventScheduler.scheduleSubscriptionCancelation( Schedule.<SubscriptionCancel>builder().cancel( cancellationDate ).properties(
            SubscriptionCancel.builder().subscriptionId( subscription.getSubId() ).scheduleInvoicesId( subscription.getScheduleInvoicesId() )
                              .subCancelDate( cancellationDate ).build() ).build() );
    }

    @Transactional
    public AgreementResponseVO addAgreementSubscription( String agreementNumber, UUID subscriptionId )
    {
        AgreementResponseVO agreementResponseVO = null;
        log.info( "**** Remove subscriptionId ****** agreementId {}, subscriptionId {}  ", agreementNumber, subscriptionId );
        agreementResponseVO = getAgreementByNumber( agreementNumber, null );

        SubscriptionVO subscriptionVO = subscriptionService.getSubscription( subscriptionId );

        agreementValidation.validateSubscriptionVOForAgreement( subscriptionVO, agreementResponseVO );

        AgreementSubscription agreementSubscriptionIdValidate =
            agreementSubscriptionRepository.findAgreementSubscriptionByAgrIdAndSubId( agreementResponseVO.getAgreementId(), subscriptionId );

        agreementValidation.validatedUplicateAgreementSubscription( agreementSubscriptionIdValidate );

        AgreementSubscription agreement = agreementSubscriptionRepository.findAgreementByAgrId( agreementResponseVO.getAgreementId() );

        agreementValidation.validatedAgreementId( agreement );

        AgreementSubscription agreementSubscription = new AgreementSubscription();
        AgreementSubscriptionId agreementSubscriptionId = new AgreementSubscriptionId();
        agreementSubscriptionId.setSubId( subscriptionId );
        agreementSubscriptionId.setAgreementId( agreementResponseVO.getAgreementId() );
        agreementSubscription.setAgrmSuId( agreementSubscriptionId );
        agreementSubscription.setCreated( LocalDateTime.now() );
        agreementSubscriptionRepository.save( agreementSubscription );

        List<SubscriptionVO> subscriptionVOList = agreementResponseVO.getSubscriptionList();
        subscriptionVO = subscriptionService.getSubscription( subscriptionId );

        subscriptionVOList.add( subscriptionVO );
        agreementResponseVO.setSubscriptionList( subscriptionVOList );
        log.info( "**** subscription save successfully  ****** ", agreementResponseVO );
        return agreementResponseVO;
    }

    @Transactional( readOnly = true )
    public AgreementResponseVO getAgreementByNumber( String agreementNumber, String currentDate )
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "MM-dd-yyyy" );
        LocalDate currentLocalDate = null;
        Predicate<String> localDatepredicate = Objects::isNull;
        if( !localDatepredicate.test( currentDate ) )
        {
            currentLocalDate = LocalDate.parse( currentDate, formatter );
        }
        AgreementResponseVO agreementResponseVO = new AgreementResponseVO();
        Optional<Agreement> agreementObject = agreementRepository.findByAgreementNumber( agreementNumber );
        if( !agreementObject.isPresent() )
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AGREEMENT_NOT_EXIST ) + "" + agreementNumber ) );
        }
        else
        {
            List<AgreementSubscription> agreementSubscriptionList = agreementSubscriptionRepository.findByAgrmSuIdAgreementId( agreementObject.get().getAgreementId() );
            getAgreementByNumber( agreementSubscriptionList, agreementResponseVO, agreementObject, currentLocalDate );
        }
        return agreementResponseVO;
    }

    private AgreementResponseVO getAgreementByNumber( List<AgreementSubscription> agreementSubscriptionList,
        AgreementResponseVO agreementResponseVO, Optional<Agreement> agreementObject, LocalDate curreDate )
    {
        List<UUID> documentIdList = new ArrayList<>();
        Set<AgreementMemberRequestVO> memberIdList = new HashSet<>();
        List<SubscriptionVO> subscriptionVOList = new ArrayList<>();
        List<SubscriptionVO> cancelSubscriptionVOList = new ArrayList<>();
        agreementSubscriptionList.forEach( agreementSubscription ->
            {
                SubscriptionVO subscriptionVO = subscriptionService.getSubscription( agreementSubscription.getSubId().getSubId() );
                List<SubscriptionDocuments> subscriptionDocumentList = subscriptionDocumentRepository.findByIdSubId( agreementSubscription.getSubId().getSubId() );
                if( !subscriptionDocumentList.isEmpty() )
                {
                    subscriptionDocumentList.forEach( subscriptionDocuments ->
                        documentIdList.add( subscriptionDocuments.getId().getDocumentId() )
                    );
                }
                subscriptionVO.setPrimary( agreementSubscription.isPrimary() );
                if( subscriptionVO.getSubCancellationDate() != null && null != curreDate && curreDate.isBefore( subscriptionVO.getSubCancellationDate().toLocalDate() ) )
                {
                    cancelSubscriptionVOList.add( subscriptionVO );
                    agreementResponseVO.setSubscriptionList( cancelSubscriptionVOList );
                }
                if( null == curreDate )
                {
                    subscriptionVOList.add( subscriptionVO );
                    agreementResponseVO.setSubscriptionList( subscriptionVOList );
                }

            }
        );
        if( cancelSubscriptionVOList.isEmpty() && ( null != curreDate ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(),
                Subscription.class, applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_FUTURE_CANCEL_DATE ) ) );
        }

        if( agreementObject.isPresent() )
        {
            List<AgreementMember> agreementMemberList = agreementMemberRepository.findByAgreementNumber( agreementObject.get().getAgreementNumber() );
            agreementMemberList.forEach( agreementMember ->
            {
                AgreementMemberRequestVO agreementMemberRequestVO = new AgreementMemberRequestVO();
                if( agreementMember.isActive() )
                {
                    agreementMemberRequestVO.setMemberId( agreementMember.getAgrmMemId().getMemId() );
                    agreementMemberRequestVO.setPrimary( agreementMember.isPrimary() );
                    memberIdList.add( agreementMemberRequestVO );
                }
            } );

            agreementResponseVO.setMemberIdList( new ArrayList<>( memberIdList ) );
            agreementResponseVO.setAgreementId( agreementObject.get().getAgreementId() );
            agreementResponseVO.setLocationId( agreementObject.get().getLocationId() );
            agreementResponseVO.setDocumentIdList( documentIdList );
            agreementResponseVO.setCampaign( agreementObject.get().getCampaign() );
            agreementResponseVO.setAgreementNumber( agreementObject.get().getAgreementNumber() );
            Optional<SubscriptionVO> subscriptionVO = agreementResponseVO.getSubscriptionList().stream().filter( SubscriptionVO::isPrimary ).findFirst();
            if( subscriptionVO.isPresent() )
            {
                agreementResponseVO.setStatus( getAgreementStatus( subscriptionVO.get() ) );
            }
        }
        return agreementResponseVO;
    }

    @Transactional
    public AgreementResponseVO removeSubscription( String agreementNumber, UUID subscriptionId )
    {
        log.debug( "**** Remove subscriptionId ****** agreementId {}, subscriptionId {}  ", agreementNumber, subscriptionId );
        AgreementResponseVO agreementResponseVO = getAgreementByNumber( agreementNumber, null );
        List<SubscriptionVO> subscriptionVOList = agreementResponseVO.getSubscriptionList();
        SubscriptionVO subscriptionVO = subscriptionService.getSubscription( subscriptionId );
        agreementValidation.validateSubscriptionVOForAgreement( subscriptionVO, agreementResponseVO );
        AgreementSubscription agreement = agreementSubscriptionRepository.findAgreementSubscriptionByAgrIdAndSubId( agreementResponseVO.getAgreementId(), subscriptionId );
        agreementValidation.validateAgreementSubscription( agreement );
        if( agreement.isPrimary() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUB_ISPRIMARY ) ) );
        }
        agreement.setDeactivated( LocalDateTime.now() );
        agreementSubscriptionRepository.save( agreement );
        subscriptionVOList.remove( subscriptionVO );
        agreementResponseVO.setSubscriptionList( subscriptionVOList );
        log.info( "**** subscription remove successfully  ****** ", agreementResponseVO );
        subscriptionDeleteJob.deleteJob( Optional.ofNullable( agreement.getSubId() ), true );
        return agreementResponseVO;
    }

    @Transactional
    public AgreementCancelResponseVO removeCancelAgreement( String agreementNumber )
    {
        Optional<Agreement> agreement = agreementRepository.findByAgreementNumber( agreementNumber );
        AgreementCancelResponseVO agreementCancelResponseVO = new AgreementCancelResponseVO();
        isAgreementExist( agreementNumber, agreement );
        cancelValid( agreement, true );
        List<AgreementSubscription> agreementSubscriptions = agreementSubscriptionRepository.findByAgrmSuIdAgreementId( agreement.get().getAgreementId() );
        List<UUID> subIdList = new ArrayList<>();
        agreementSubscriptions.forEach( agreementSubscription -> {
            Subscription subscription = agreementSubscription.getSubId();
            if( null != subscription.getSubCancelEventId() && subscription.isActive() )
            {
                eventScheduler.cancelSub( subscription.getSubCancelEventId() );
                subscription.setSubCancellationDate( null );
                subIdList.add( subscription.getSubId() );
                subscriptionRepository.save( subscription );
            }

        } );
        ModelMapperUtils.map( agreement.get(), agreementCancelResponseVO );
        agreementCancelResponseVO.setSubscriptionIdList( subIdList );
        return agreementCancelResponseVO;
    }

    @Transactional( readOnly = true )
    public AgreementDue getRemainingAgreementValue( String agreementNumber )
    {
        Long agreementInvoiceCount = Long.valueOf( 0 );
        BigDecimal agreementAmountDue = new BigDecimal( 0 );
        AgreementDue agreementDue = new AgreementDue();
        log.debug( "**** Get Remaining Agreement value for agreement Number {} ", agreementNumber );
        agreementDue.setAgreementNumber( agreementNumber );
        List<SubscriptionListDue> subscriptionListDues = new ArrayList<>();
        Optional<Agreement> optionalAgreement = agreementRepository.findByAgreementNumber( agreementNumber );
        if( optionalAgreement.isPresent() )
        {
            AgreementResponseVO agreementResponseVO = getAgreementByNumber( optionalAgreement.get().getAgreementNumber(), null );
            List<SubscriptionVO> subscriptionVOList = agreementResponseVO.getSubscriptionList();
            for( SubscriptionVO subscriptionVO : subscriptionVOList )
            {
                SubscriptionListDue subscriptionListDue = new SubscriptionListDue();
                try
                {
                    SubscriptionDue subscriptionDue = subscriptionService.getRemainingSubscriptionValue( subscriptionVO.getSubId() );
                    subscriptionListDue.setSubscriptionId( subscriptionVO.getSubId() );
                    subscriptionListDue.setInvoiceAmountDue( subscriptionDue.getInvoiceAmountDue() );
                    subscriptionListDue.setInvoiceCountDue( subscriptionDue.getInvoiceCountDue() );
                    agreementInvoiceCount = agreementInvoiceCount + subscriptionDue.getInvoiceCountDue();
                    agreementAmountDue = agreementAmountDue.add( subscriptionDue.getInvoiceAmountDue() );

                }
                catch( ErrorResponse errorResponse )
                {
                    subscriptionListDue.setSubscriptionId( subscriptionVO.getSubId() );
                    subscriptionListDue.setMessage( errorResponse.getErrors().get( 0 ).getMessage() );

                }
                catch( DataIntegrityViolationException exception )
                {

                    subscriptionListDue.setMessage( exception.getMessage() );

                }
                subscriptionListDues.add( subscriptionListDue );

            }
            agreementDue.setAgreementInvoiceCount( agreementInvoiceCount );
            agreementDue.setAgreementAmountDue( agreementAmountDue );
            agreementDue.setSubscriptionList( subscriptionListDues );
        }
        else
        {
            throw new ErrorResponse( new CustomErrorResponse( HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), AgreementService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AGREEMENT_NOT_EXIST ) + agreementNumber ) );
        }
        return agreementDue;
    }

    @Transactional
    public AgreementResponseVO addAgreementMember( String agreementNumber, AgreementMemberVO agreementMemberVO )
    {
        AgreementResponseVO agreementResponseVO = null;
        log.info( "**** Agreement Number {}, memberIdList {}  ", agreementNumber, agreementMemberVO.getMemberIdList() );
        agreementValidation.validateAgreementMember( agreementMemberVO );
        List<AgreementMemberRequestVO> memberList = new ArrayList<>();
        Optional<List<AgreementMember>> agreementMemberList = agreementMemberRepository.findByAgreementNumberAndIsActive( agreementNumber, true );
        if( agreementMemberList.isPresent() )
        {
            agreementMemberList.get().forEach( agreementMember -> {
                if( agreementMemberVO.getMemberIdList().contains( agreementMember.getAgrmMemId().getMemId() ) && agreementMember.isActive() )
                {
                    throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Agreement.class,
                        applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ID_ALREADY_EXIST ) ) );
                }
                AgreementMemberRequestVO agreementMemberRequestVO = new AgreementMemberRequestVO();
                agreementMemberRequestVO.setMemberId( agreementMember.getAgrmMemId().getMemId() );
                agreementMemberRequestVO.setPrimary( agreementMember.isPrimary() );
                memberList.add( agreementMemberRequestVO );
            } );
            updateAgreementMember( agreementMemberVO, agreementNumber, agreementMemberList.get().get( 0 ).getAgrmMemId().getAgreementId() );
            agreementResponseVO = getAgreementByAgreementId( agreementMemberList.get().get( 0 ).getAgrmMemId().getAgreementId(), agreementNumber );
            agreementMemberVO.getMemberIdList().forEach( memberId -> {
                AgreementMemberRequestVO agreementMemberRequestVO = new AgreementMemberRequestVO();
                agreementMemberRequestVO.setMemberId( memberId );
                memberList.add( agreementMemberRequestVO );
            } );
            agreementResponseVO.setMemberIdList( memberList );
        }
        else
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AGREEMENT_NUMBER_FIFTEEN_ALPHANUMERIC_VALID ) ) );
        }
        return agreementResponseVO;
    }

    private void updateAgreementMember( AgreementMemberVO agreementMemberVO, String agreementNumber, UUID agreementId )
    {
        log.info( "**** Adding members to Agreement  {} ", agreementMemberVO.getMemberIdList() );
        agreementMemberVO.getMemberIdList().forEach( memberId -> {
            AgreementMember agreementMember = new AgreementMember();
            AgreementMemberId agreementMemberId = new AgreementMemberId();
            agreementMemberId.setMemId( memberId );
            agreementMemberId.setAgreementId( agreementId );
            agreementMember.setActive( true );
            agreementMember.setAgreementNumber( agreementNumber );
            agreementMember.setAgrmMemId( agreementMemberId );
            agreementMemberRepository.save( agreementMember );
        } );

    }

    private AgreementResponseVO getAgreementByAgreementId( UUID agreementId, String agreementNumber )
    {
        List<UUID> documentIdList = new ArrayList<>();
        List<SubscriptionVO> activeSubscriptionVOList = new ArrayList<>();
        AgreementResponseVO agreementResponseVO = new AgreementResponseVO();
        Optional<Agreement> optionalAgreement = agreementRepository.findByAgreementNumber( agreementNumber );
        if( optionalAgreement.isPresent() )
        {
            agreementResponseVO = getAgreementByNumber( agreementNumber, null );
            for( SubscriptionVO subscriptionVO : agreementResponseVO.getSubscriptionList() )
            {
                List<SubscriptionDocuments> subscriptionDocumentList = subscriptionDocumentRepository.findByIdSubId( subscriptionVO.getSubId() );
                if( !subscriptionDocumentList.isEmpty() )
                {
                    subscriptionDocumentList.forEach( subscriptionDocuments ->
                        documentIdList.add( subscriptionDocuments.getId().getDocumentId() )
                    );
                }
                activeSubscriptionVOList.add( subscriptionVO );

            }
            agreementResponseVO.setLocationId( optionalAgreement.get().getLocationId() );
            agreementResponseVO.setCampaign( optionalAgreement.get().getCampaign() );
            agreementResponseVO.setAgreementNumber( optionalAgreement.get().getAgreementNumber() );
        }
        agreementResponseVO.setDocumentIdList( documentIdList );
        agreementResponseVO.setSubscriptionList( activeSubscriptionVOList );
        agreementResponseVO.setAgreementId( agreementId );
        return agreementResponseVO;
    }

    @Transactional
    public AgreementResponseVO removeAgreementMember( String agreementNumber, AgreementMemberVO agreementMemberVO )
    {
        AgreementResponseVO agreementResponseVO = null;
        log.info( "**** Agreement Number {}, memberIdList {}  ", agreementNumber, agreementMemberVO.getMemberIdList() );
        agreementValidation.validateAgreementMemberRemove( agreementMemberVO, agreementNumber );
        List<AgreementMemberRequestVO> argMemberList = new CopyOnWriteArrayList<>();
        List<SubscriptionVO> subscriptionVOList = new CopyOnWriteArrayList<>();
        Set<UUID> subPrimaryMemberList = new CopyOnWriteArraySet<>();
        Set<UUID> subNonPrimaryMemberList = new CopyOnWriteArraySet<>();
        Optional<List<AgreementMember>> agreementMemberList = agreementMemberRepository.findByAgreementNumberAndIsActive( agreementNumber, true );
        if( agreementMemberList.isPresent() )
        {
            agreementMemberList.get().forEach( agreementMember -> {
                AgreementMemberRequestVO agreementMemberRequestVO = new AgreementMemberRequestVO();
                agreementMemberRequestVO.setMemberId( agreementMember.getAgrmMemId().getMemId() );
                agreementMemberRequestVO.setPrimary( agreementMember.isPrimary() );
                argMemberList.add( agreementMemberRequestVO );
                argMemberList.stream().sorted();
            } );
            List<AgreementSubscription> agreementSubscriptionList =
                agreementSubscriptionRepository.findByAgrmSuIdAgreementIdOrderByPrimaryDesc( agreementMemberList.get().get( 0 ).getAgrmMemId().getAgreementId() );
            agreementSubscriptionList.forEach( agreementSubscription -> {
                if( agreementSubscription.isPrimary() )
                {
                    List<MemberSubscription> memberSubscriptionList =
                        subscriptionMembersRepository.findByIdSubIdAndDeactivated( agreementSubscription.getSubId().getSubId(), null );
                    SubscriptionVO subscriptionVO = null;
                    memberSubscriptionList.forEach( memberSubscription ->
                        subPrimaryMemberList.add( memberSubscription.getId().getMemId() )
                    );
                    subscriptionVO = removeMemberFromPrimarySubscription( agreementNumber, agreementSubscription, subPrimaryMemberList, agreementMemberVO );
                    if( Objects.isNull( subscriptionVO.getSubId() ) )
                    {
                        subscriptionVO = subscriptionService.getSubscription( agreementSubscription.getSubId().getSubId() );
                        subscriptionVO.setPrimary( agreementSubscription.isPrimary() );
                        subscriptionVOList.add( subscriptionVO );
                    }
                    else
                    {
                        subscriptionVOList.add( subscriptionVO );
                    }
                }
                else if( !agreementSubscription.isPrimary() )
                {
                    List<MemberSubscription> memberSubscriptionList =
                        subscriptionMembersRepository.findByIdSubIdAndDeactivated( agreementSubscription.getSubId().getSubId(), null );
                    SubscriptionVO subscriptionVO = new SubscriptionVO();
                    memberSubscriptionList.forEach( memberSubscription ->
                        subNonPrimaryMemberList.add( memberSubscription.getId().getMemId() )
                    );
                    if( matchMemberList( subNonPrimaryMemberList, agreementMemberVO.getMemberIdList() ) && !subNonPrimaryMemberList.isEmpty() )
                    {
                        for( UUID memberId : subNonPrimaryMemberList )
                        {
                            if( agreementMemberVO.getMemberIdList().contains( memberId ) && subNonPrimaryMemberList.stream().count() > 0 )
                            {
                                subscriptionVO = subscriptionService.removeMember( agreementSubscription.getSubId().getSubId(), memberId, false );
                                subNonPrimaryMemberList.remove( memberId );
                                subscriptionVO.setMemberIdList( new ArrayList<>( subNonPrimaryMemberList ) );
                            }

                        }
                        if( subNonPrimaryMemberList.stream().count() != 0 )
                        {
                            subscriptionVOList.add( subscriptionVO );
                        }
                        if( !Objects.isNull( subscriptionVO.getMemberIdList() ) && subscriptionVO.getMemberIdList().stream().count() == 0 )
                        {

                            Subscription subscription = ModelMapperUtils.map( subscriptionVO, Subscription.class );
                            Account acc = new Account();
                            acc.setAccountId( subscriptionVO.getAccountId() );
                            subscription.setAccount( acc );
                            subscriptionDeleteJob.deleteJob( Optional.of( subscription ), true );
                        }
                    }
                    else if( !subNonPrimaryMemberList.isEmpty() )
                    {
                        subscriptionVO = subscriptionService.getSubscription( agreementSubscription.getSubId().getSubId() );
                        subscriptionVOList.add( subscriptionVO );
                    }
                }

            } );
            List<AgreementMemberRequestVO> argMemberResponseList =
                inActiveAgreementMember( argMemberList, agreementMemberVO, agreementNumber, agreementMemberList.get().get( 0 ).getAgrmMemId().getAgreementId() );
            agreementResponseVO = getAgreementAfterRemoveMems( subscriptionVOList, agreementMemberList.get().get( 0 ).getAgrmMemId().getAgreementId() );
            agreementResponseVO.setMemberIdList( argMemberResponseList );
        }
        else
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Agreement.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_AGREEMENT_NUMBER_FIFTEEN_ALPHANUMERIC_VALID ) ) );
        }
        return agreementResponseVO;
    }

    private SubscriptionVO removeMemberFromPrimarySubscription( String agreementNumber, AgreementSubscription agreementSubscription,
        Set<UUID> subPrimaryMemberList, AgreementMemberVO agreementMemberVO )
    {
        SubscriptionVO subscriptionVO = new SubscriptionVO();
        Optional<AgreementMember> agreementMemberObject = agreementMemberRepository.findByAgreementNumberAndPrimary( agreementNumber, true );
        if( agreementMemberObject.isPresent() )
        {
            for( UUID subMemberId : subPrimaryMemberList )
            {
                for( UUID removeMemberId : agreementMemberVO.getMemberIdList() )
                {
                    if( agreementSubscription.isPrimary() && subMemberId.equals( removeMemberId ) &&
                        !subMemberId.equals( agreementMemberObject.get().getAgrmMemId().getMemId() ) &&
                        !removeMemberId.equals( agreementMemberObject.get().getAgrmMemId().getMemId() ) )
                    {
                        subscriptionVO = subscriptionService.removeMember( agreementSubscription.getSubId().getSubId(), subMemberId, false );
                        subscriptionVO.setPrimary( agreementSubscription.isPrimary() );
                        subPrimaryMemberList.remove( subMemberId );
                        subscriptionVO.setMemberIdList( new ArrayList<>( subPrimaryMemberList ) );
                    }
                }
            }
        }
        return subscriptionVO;
    }

    private boolean matchMemberList( Set<UUID> subNonPrimaryMemberList, List<UUID> memberIdList )
    {
        return subNonPrimaryMemberList.stream().anyMatch( memberIdList::contains );
    }

    private List<AgreementMemberRequestVO> inActiveAgreementMember( List<AgreementMemberRequestVO> argMemberList, AgreementMemberVO agreementMemberVO, String agreementNumber,
        UUID agreementId )
    {
        log.info( "**** Removing  members from Agreement {} ", agreementMemberVO.getMemberIdList() );
        for( AgreementMemberRequestVO agreementMemberRequestVO : argMemberList )
        {
            for( UUID memberId : agreementMemberVO.getMemberIdList() )
            {
                if( !agreementMemberRequestVO.isPrimary() && agreementMemberRequestVO.getMemberId().equals( memberId ) )
                {
                    AgreementMember agreementMember = new AgreementMember();
                    AgreementMemberId agreementMemberId = new AgreementMemberId();
                    agreementMemberId.setMemId( memberId );
                    agreementMemberId.setAgreementId( agreementId );
                    agreementMember.setAgrmMemId( agreementMemberId );
                    agreementMember.setActive( false );
                    agreementMember.setAgreementNumber( agreementNumber );
                    agreementMemberRepository.save( agreementMember );
                    argMemberList.remove( agreementMemberRequestVO );
                }
            }
        }
        return argMemberList;
    }

    private AgreementResponseVO getAgreementAfterRemoveMems( List<SubscriptionVO> subscriptionVOList,
        UUID agreementId )
    {
        AgreementResponseVO agreementResponseVO = new AgreementResponseVO();
        List<UUID> documentIdList = new ArrayList<>();
        List<SubscriptionVO> subscriptionList = new ArrayList<>();
        for( SubscriptionVO subscriptionVO : subscriptionVOList )
        {
            List<SubscriptionDocuments> subscriptionDocumentList = subscriptionDocumentRepository.findByIdSubId( subscriptionVO.getSubId() );
            if( !subscriptionDocumentList.isEmpty() )
            {
                subscriptionDocumentList.forEach( subscriptionDocuments ->
                    documentIdList.add( subscriptionDocuments.getId().getDocumentId() )
                );
            }
            subscriptionList.add( subscriptionVO );
        }
        Optional<Agreement> agreementObject = agreementRepository.findById( agreementId );
        if( agreementObject.isPresent() )
        {
            agreementResponseVO.setAgreementId( agreementObject.get().getAgreementId() );
            agreementResponseVO.setLocationId( agreementObject.get().getLocationId() );
            agreementResponseVO.setCampaign( agreementObject.get().getCampaign() );
            agreementResponseVO.setAgreementNumber( agreementObject.get().getAgreementNumber() );
        }
        agreementResponseVO.setDocumentIdList( documentIdList );
        agreementResponseVO.setSubscriptionList( subscriptionList );
        return agreementResponseVO;
    }
}
