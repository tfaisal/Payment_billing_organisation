package com.abcfinancial.api.billing.generalledger.invoice.service;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.AvalaraMasterTaxCode;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationTaxRate;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.AvalaraMasterTaxCodeRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.repository.LocationTaxRateRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.service.LocationService;
import com.abcfinancial.api.billing.subscriptionmanagement.account.location.valueobject.LocationTaxVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.MemberCreation;
import com.abcfinancial.api.billing.subscriptionmanagement.account.member.repository.MemberCreationRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.*;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAddress;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaCompany;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaNexus;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaAccountRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaAddressRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaCompanyRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.repository.AvaNexusRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.service.AvaCompanyService;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.service.PricingService;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject.ItemVO;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject.ItemsVO;
import com.abcfinancial.api.billing.subscriptionmanagement.pricing.valueobject.PricingDetailsVO;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.SubscriptionItem;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.enums.InvoiceTypeEnum;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.repository.SubscriptionRepository;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.service.SubscriptionService;
import com.abcfinancial.api.billing.generalledger.invoice.InvoiceServiceHelper;
import com.abcfinancial.api.billing.generalledger.invoice.domain.Invoice;
import com.abcfinancial.api.billing.generalledger.invoice.domain.InvoiceItem;
import com.abcfinancial.api.billing.generalledger.invoice.domain.LineItemModel;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.generalledger.invoice.repository.InvoiceRepository;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.InvoiceItemRequestVO;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.InvoiceWithoutSubscriptionVO;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.PayorInvoiceRequestVO;
import com.abcfinancial.api.billing.generalledger.invoice.valueobject.PayorInvoiceResponseVO;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import com.abcfinancial.api.billing.generalledger.payment.repository.PaymentMethodRepository;
import com.abcfinancial.api.billing.generalledger.statements.domain.PaymentMethodAccount;
import com.abcfinancial.api.billing.generalledger.statements.domain.Summary;
import com.abcfinancial.api.billing.generalledger.statements.domain.Type;
import com.abcfinancial.api.billing.generalledger.statements.repository.AccountSummaryRepository;
import com.abcfinancial.api.billing.generalledger.statements.repository.PaymentMethodAccountRepository;
import com.abcfinancial.api.billing.utility.common.ApplicationConfiguration;
import com.abcfinancial.api.billing.utility.common.CommonUtil;
import com.abcfinancial.api.billing.utility.common.MessageUtils;
import com.abcfinancial.api.billing.utility.common.ModelMapperUtils;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.abcfinancial.api.billing.utility.exception.EntityNotFoundResponseError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NoHttpResponseException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor

public class InvoiceService
{
    @Autowired
    private InvoiceRepository repository;
    @Autowired
    private MemberCreationRepository memberCreationRepository;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    // JIRA - P3-3015 start
    private String invoiceNumber = "";
    // End
    @Autowired
    private PricingService pricingService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AccountSummaryRepository accountSummaryRepository;

    @Autowired
    private PaymentMethodAccountRepository paymentMethodAccountRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LocationTaxRateRepository locationTaxRateRepository;

    @Autowired
    private AvaAddressRepository avaAddressRepository;

    @Autowired
    private AvaNexusRepository avaNexusRepository;

    @Autowired
    private RestTemplate restTemplate;
    @Value( "${avalara.uri.createTransaction}" )
    private String createTransactionUri;

    @Autowired
    private AvaAccountRepository avaAccountRepository;

    @Autowired
    private AvaCompanyRepository avaCompanyRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private AvalaraMasterTaxCodeRepository avalaraMasterTaxCodeRepository;

    @Autowired
    private AvaCompanyService avaCompanyService;

    private String invoiceTaxCode;

    @Transactional( readOnly = true )
    public List<InvoiceWithoutSubscriptionVO> findRecentInvoiceByStartDateAndEndDate( UUID memberId, Optional<String> startDate, Optional<String> endDate, Pageable pageable )
    {
        boolean isEndDateValid;
        boolean isStartDateValid;
        LocalDate startDateCreatedInvoice;
        LocalDate endDateCreatedInvoice;

        MemberCreation memberCreation = memberCreationRepository.findMemberById( memberId );
        if( memberCreation == null )
        {
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Invoice.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_MEMBER_ID_NOT_FOUND ) + "" + memberId ) );
        }

        if( !startDate.isPresent() && !endDate.isPresent() )
        {
            isEndDateValid = true;
            isStartDateValid = true;
            log.debug( "If no start/end date is provided then End date is today's date" );
            endDateCreatedInvoice = LocalDate.now( Clock.systemUTC() );

            startDateCreatedInvoice = endDateCreatedInvoice.minusDays( 30 );
        }
        else if( !startDate.isPresent() )
        {
            log.debug( "If end date is provide and no start date then Start date is 30 days prior to end date" );
            isEndDateValid = CommonUtil.isValidDate( endDate.get() );
            isStartDateValid = true;
            endDateCreatedInvoice = CommonUtil.convertToDateTime( endDate.get() );
            startDateCreatedInvoice = endDateCreatedInvoice.minusDays( 30 );

        }
        else if( !endDate.isPresent() )
        {
            log.debug( "If start date is provided and no end date then End date is current date (today's date" );

            isStartDateValid = CommonUtil.isValidDate( startDate.get() );
            isEndDateValid = true;
            endDateCreatedInvoice = LocalDate.now( Clock.systemUTC() );
            startDateCreatedInvoice = CommonUtil.convertToDateTime( startDate.get() );
        }
        else
        {
            log.debug( "when both dates been provided " );
            isStartDateValid = CommonUtil.isValidDate( startDate.get() );
            isEndDateValid = CommonUtil.isValidDate( endDate.get() );
            endDateCreatedInvoice = CommonUtil.convertToDateTime( endDate.get() );
            startDateCreatedInvoice = CommonUtil.convertToDateTime( startDate.get() );
        }

        validateStartDateAndEndDates( isStartDateValid, isEndDateValid, endDateCreatedInvoice, startDateCreatedInvoice );

        List<Invoice> invoices = repository
            .findByMemberIdAndInvoiceDateBetweenOrderByInvoiceDateDesc( memberId, CommonUtil.convertLocDateToLocDateTime( startDateCreatedInvoice ),
                CommonUtil.convertLocDateToLocDateTime( endDateCreatedInvoice.plusDays( 1 ) ), pageable );
        return ModelMapperUtils.mapAll( invoices, InvoiceWithoutSubscriptionVO.class );
    }

    private void validateStartDateAndEndDates( boolean isStartDateValid, boolean isEndDateValid, LocalDate endDateCreatedInvoice, LocalDate startDateCreatedInvoice )
    {

        if( !isStartDateValid || !isEndDateValid )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Invoice.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_INVALID_START_END_DATE ) ) );
        }
        if( !( endDateCreatedInvoice.isBefore( LocalDate.now( Clock.systemUTC() ) ) || ( endDateCreatedInvoice.equals( LocalDate.now( Clock.systemUTC() ) ) ) )
            || !( startDateCreatedInvoice.isBefore( LocalDate.now( Clock.systemUTC() ) ) || ( startDateCreatedInvoice.equals( LocalDate.now( Clock.systemUTC() ) ) ) ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Invoice.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_END_DATE_NO_FUTURE ) ) );
        }
        if( endDateCreatedInvoice.isBefore( startDateCreatedInvoice ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), Invoice.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_END_DATE_BEFORE_START_DATE ) ) );
        }

    }

    public Invoice createInvoice( Subscription subscription, boolean isPameIdAccount ) throws NoHttpResponseException
    {
        TransactionModel transactionModel = null;
        PaymentMethod paymentMethod = null;

        log.trace( "createInvoice {}", subscription );
        log.debug( "Getting Avalara Account with locationId {}", subscription.getLocationId() );
        List<AvaAccount> avaAccounts = avaAccountRepository.findByLocationId( subscription.getLocationId() );
        transactionModel = createTransactionFromAvaAccounts( avaAccounts, subscription );
        PricingDetailsVO pricingDetailsVO = calculateTaxPricing( subscription, subscription.getLocationId() );

        if( pricingDetailsVO.getTotalAmount().compareTo( BigDecimal.ZERO ) < 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), SubscriptionService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_SUBSCRIPTION_AMOUNT ) ) );
        }

        subscription.setTotalTax( pricingDetailsVO.getTotalTax() );
        subscription.setTotalAmount( pricingDetailsVO.getTotalAmount() );
        subscription.setTotalNetPrice( pricingDetailsVO.getTotalNetAmount() );

        // JIRA - P3-3015 Start
        String tempInvoiceNumber = getInvoiceSequenceNo();
        String columnLen = applicationConfiguration.getValue( MessageUtils.COLUMN_LENGTH_INVOICE_NO );
        // JIRA  : 3574
        invoiceNumber = CommonUtil.generateInvoiceNo( Integer.parseInt( columnLen ), tempInvoiceNumber );

        log.trace( "invoiceNumber ::: " + invoiceNumber );
        log.trace( "columnLength from properties file :: " + columnLen );
        // End
        Invoice invoice = ModelMapperUtils.map( subscription, Invoice.class );
        paymentMethod = paymentMethodRepository.findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( subscription.getAccount().getAccountId(), Boolean.TRUE );
        invoice.setTotalDiscountAmount( new BigDecimal( 0 ) );
        invoice.setAccountId( subscription.getAccount().getAccountId() );
        invoice.setSubscription( subscription );
        LocalDateTime invoiceDate = ( Objects.nonNull( subscription.getRenewInvoiceDate() ) && Objects.isNull( subscription.getInvoiceDate() ) ) ?
                                    subscription.getRenewInvoiceDate().atTime( LocalTime.now() ) :
                                    subscription.getInvoiceDate().atTime( java.time.LocalTime.now() );
        invoice.setInvoiceDate( invoiceDate );
        // JIRA P3-3015 Start
        invoice.setInvoiceNumber( invoiceNumber );
        UUID paymentMethodId = ( isPameIdAccount ) ? paymentMethod.getId() : invoice.getPaymentMethodId();
        invoice.setPaymentMethodId( paymentMethodId );
        invoice.setInvoiceType( InvoiceTypeEnum.O );
        // End
        IntStream.range( 0, subscription.getItems().size() )
                 .forEach( i -> {
                     InvoiceItem invoiceItem = invoice.getItems().get( i );
                     ItemVO itemVO = pricingDetailsVO.getItems().get( i );
                     subscription.getItems().get( i ).setPrice( itemVO.getPrice() );
                     invoiceItem.setTaxCode( taxCode( itemVO, subscription.getLocationId() ) );
                     invoiceItem.setDiscountAmount( BigDecimal.ZERO );
                     invoiceItem.setTaxAmount( itemVO.getTaxAmount() );
                     invoiceItem.setAmountRemaining( itemVO.getPrice().add( itemVO.getTaxAmount() ) );
                 } );

        String transactionId = Objects.nonNull( transactionModel ) ? String.valueOf( transactionModel.getId() ) : invoice.getTransactionId();
        invoice.setTransactionId( transactionId );
        return invoice;
    }

    private TransactionModel createTransactionFromAvaAccounts( List<AvaAccount> avaAccounts, Subscription subscription ) throws NoHttpResponseException
    {
        TransactionModel transactionModel = null;
        if( Objects.nonNull( avaAccounts ) && !avaAccounts.isEmpty() )
        {
            AvaAccount avaAccount = avaAccounts.get( 0 );
            AvaCompany avaCompany = avaCompanyRepository.findByAvaAccount( avaAccount );
            if( Objects.nonNull( avaCompany ) )
            {
                log.debug( "Getting AvaNexus  with avaCompany  {} ", avaCompany );
                List<AvaNexus> avaNexusList = avaNexusRepository.findByAvaCompany( avaCompany );
                if( Objects.nonNull( avaNexusList ) && !avaNexusList.isEmpty() )
                {
                    log.debug( "Getting AvaAddress  with avaLocation  {} ", avaNexusList.get( 0 ).getAvaLocation() );
                    AvaAddress avaAddress = avaAddressRepository.findByAvaLocation( avaNexusList.get( 0 ).getAvaLocation() );
                    if( Objects.nonNull( avaAddress ) && avaAddress.getIsValidated() )
                    {
                        String avaAccountId = avaAccount.getAvalaraAccId().toString();
                        String avaLicenceKey = avaCompany.getLicenseKey();
                        transactionModel = createTransaction( subscription, avaAddress, avaAccountId, avaLicenceKey );
                        return transactionModel;
                    }
                }
            }
        }
        return transactionModel;
    }

    public PricingDetailsVO calculateTaxPricing( Subscription subscription, UUID locationId )
    {
        List<BigDecimal> itemsAmount = subscription.getItems().stream().map( SubscriptionItem::getPrice ).collect( Collectors.toList() );
        ItemsVO itemsVO = new ItemsVO();
        itemsVO.setItems( itemsAmount );
        itemsVO.setLocationId( locationId );
        List<UUID> itemCategoryId = subscription.getItems().stream().map( SubscriptionItem::getItemCategoryId ).collect( Collectors.toList() );
        itemsVO.setItemCategoryId( itemCategoryId );
        return pricingService.calculatePricing( itemsVO );
    }
    // End

    // JIRA - P3-3015
    public String getInvoiceSequenceNo()
    {
        invoiceNumber = subscriptionRepository.getInvoiceSequenceNo();
        return invoiceNumber;
    }

    private String taxCode( ItemVO itemVO, UUID locationId )
    {
        LocationTaxRate locationTaxRate = null;
        Optional<LocationTaxRate> optionalLocationTaxRate = null;
        String taxcode = null;
        if( locationId != null && itemVO.getItemCategoryId() == null )
        {
            optionalLocationTaxRate = locationTaxRateRepository.getLocationTaxRateForMaxVersion( locationId );
        }
        else
        {
            optionalLocationTaxRate = locationTaxRateRepository.getLocationTaxRateOfMaxVersionForLocationAndCategoryId( locationId, itemVO.getItemCategoryId() );
        }
        if( optionalLocationTaxRate.isPresent() )
        {
            locationTaxRate = optionalLocationTaxRate.get();

            if( locationTaxRate.getTaxCode() != null )
            {
                Optional<AvalaraMasterTaxCode> optionalAvalaraMasterTaxCode = avalaraMasterTaxCodeRepository.findByTaxCode( locationTaxRate.getTaxCode() );

                if( optionalAvalaraMasterTaxCode.isPresent() )

                {
                    taxcode = optionalAvalaraMasterTaxCode.get().getTaxCode() != null ? locationTaxRate.getTaxCode() : null;
                }
            }
        }
        return taxcode;
    }

    public TransactionModel createTransaction( Subscription subscription, AvaAddress avaAddress, String avaAccountId, String avaLicenceKey ) throws NoHttpResponseException
    {
        UUID locationId = subscription.getLocationId();
        UUID itemCategoryId = null;
        String taxCode = null;
        LocationTaxRate locationTaxRate = null;
        final TaxOverrideModel taxOverrideModel = new TaxOverrideModel();
        List<SubscriptionItem> subscriptionItems = subscription.getItems();
        final String todayDate = LocalDate.now( Clock.systemUTC() ).toString();
        final AddressesModel addressesModel = new AddressesModel();
        InvoiceServiceHelper.setAddressToAddressesModel( addressesModel, avaAddress );
        TransactionModel transactionModel = null;
        List<LineItemModel> lineItemModels = null;
        Optional<LocationTaxRate> locationTaxRateOptional = null;

        SubscriptionItem sItemForLocationIdAndCategoryId =
            subscriptionItems.parallelStream().filter( si -> !Objects.isNull( si.getLocId() ) && !Objects.isNull( si.getItemCategoryId() ) ).findFirst().orElse( null );

        if( Objects.nonNull( sItemForLocationIdAndCategoryId ) )
        {
            itemCategoryId = sItemForLocationIdAndCategoryId.getItemCategoryId();
            log.info( "Getting Location Tax Rate with Max Version for locationId {}, categoryId {} ", subscription.getLocationId(), itemCategoryId );
            locationTaxRateOptional =
                locationTaxRateRepository.getLocationTaxRateOfMaxVersionForLocationAndCategoryId( subscription.getLocationId(), itemCategoryId );
            InvoiceServiceHelper.processOverridenTaxRate( locationTaxRateOptional, taxOverrideModel );
        }
        else
        {
            locationTaxRateOptional = getLocationTaxRateOptionalBySubscription( subscription, subscriptionItems, taxOverrideModel );
        }

        if( Objects.nonNull( locationTaxRateOptional ) && locationTaxRateOptional.isPresent() )
        {
            locationTaxRate = locationTaxRateOptional.get();
            taxCode = locationTaxRate.getTaxCode();
        }

        log.debug( "Checking tax code is existing in avalara master tax code or not for taxCode {} ", taxCode );
        Optional<AvalaraMasterTaxCode> optionalAvalaraMasterTaxCode = avalaraMasterTaxCodeRepository.findByTaxCode( taxCode );

        if( Objects.nonNull( locationTaxRateOptional ) && locationTaxRateOptional.isPresent() )
        {
            taxCode = Objects.isNull( locationTaxRateOptional.get().getDeactivated() ) ? taxCode : null;
        }

        taxCode = optionalAvalaraMasterTaxCode.isPresent() ? taxCode : null;

        log.debug( "Requesting for  List of LineItemModel" );
        lineItemModels = InvoiceServiceHelper.prepareListOfLineItemModel( subscription, taxOverrideModel, taxCode );

        CompanyResponse companyResponse = avaCompanyService.queryCompanies( avaAccountId, avaLicenceKey );
        log.debug( "Requesting transaction object from Transaction.TransactionBuilder." );
        final Transaction transaction = new Transaction.TransactionBuilder( addressesModel, lineItemModels, todayDate, "ABCFinancialAvaTaxIncluded", "SalesInvoice",
            companyResponse.getValue().get( 0 ).getCompanyCode() ).build();

        log.debug( "Requesting TransactionModel from postHitForTransactional." );
        ResponseEntity<TransactionModel> transactionModelResponseEntity =
            postHitForTransactional( transaction, CommonUtil.createAuthorizationHeader( avaAccountId, avaLicenceKey ) );

        transactionModel = transactionModelResponseEntity.getBody();
        if( Objects.nonNull( locationTaxRateOptional ) && locationTaxRateOptional.isPresent() )
        {
            createTransactionForLocationTaxRateOptionalPresent( sItemForLocationIdAndCategoryId, subscription, transactionModel, locationTaxRate, itemCategoryId, locationId );
        }
        else
        {
            createTransactionForLocationTaxRateOptionalNotPresent( subscription, transactionModel );
        }
        return transactionModel;

    }

    private Optional<LocationTaxRate> getLocationTaxRateOptionalBySubscription( Subscription subscription, List<SubscriptionItem> subscriptionItems,
        TaxOverrideModel taxOverrideModel )
    {
        Optional<LocationTaxRate> locationTaxRate = null;
        if( Objects.nonNull( subscriptionItems ) && !subscriptionItems.isEmpty() )
        {
            log.info( "Getting Location Tax Rate with Max Version for locationId {} ", subscription.getLocationId() );
            locationTaxRate = locationTaxRateRepository.getLocationTaxRateForMaxVersion( subscription.getLocationId() );
            InvoiceServiceHelper.processOverridenTaxRate( locationTaxRate, taxOverrideModel );
            return locationTaxRate;
        }
        return locationTaxRate;
    }

    public ResponseEntity<TransactionModel> postHitForTransactional( Transaction transaction, HttpHeaders httpHeaders )
    {
        ResponseEntity<TransactionModel> transactionModel = null;
        try
        {
            HttpEntity<Transaction> requestEntity = new HttpEntity<>( transaction, httpHeaders );
            transactionModel = restTemplate.exchange( createTransactionUri, HttpMethod.POST, requestEntity, TransactionModel.class );
        }
        catch( HttpClientErrorException exception )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                CommonUtil.buildExceptionMessage( exception.getResponseBodyAsString() ) ) );
        }
        catch( HttpServerErrorException exception )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                CommonUtil.buildExceptionMessage( exception.getResponseBodyAsString() ) ) );
        }
        return transactionModel;
    }

    private void createTransactionForLocationTaxRateOptionalPresent( SubscriptionItem sItemForLocationIdAndCategoryId, Subscription subscription, TransactionModel transactionModel,
        LocationTaxRate locationTaxRate, UUID itemCategoryId, UUID locationId )
    {
        BigDecimal transactionTaxRate = transactionModel.getSummary().get( 0 ).getRate();
        LocationTaxVO locationTaxVO = new LocationTaxVO();
        if( Objects.nonNull( sItemForLocationIdAndCategoryId ) )
        {
            locationTaxVO.setItemCategoryId( sItemForLocationIdAndCategoryId.getItemCategoryId() );
        }
        locationTaxVO.setEmpId( subscription.getSalesEmployeeId() );
        if( Objects.nonNull( transactionModel ) )
        {
            locationTaxVO.setTaxRate( transactionTaxRate.setScale( 3, RoundingMode.HALF_UP ) );
            locationTaxVO.setTaxCode( transactionModel.getLines().get( 0 ).getTaxCode() );
        }

        log.debug( "Getting taxCode from Response of postHitForTransactional." );

        log.debug( "Requesting to update the Locaton Tax Rate." );
        if( Objects.isNull( locationTaxRate.getDeactivated() ) )
        {
            LocationTaxRate updateLocationTaxRate = locationService.updateLocationTaxRate( locationTaxVO, locationId, locationTaxRate.getItemCategoryId() );
            for( SubscriptionItem item : subscription.getItems() )
            {
                createLocationTaxRateForDeactivatedNotNull( subscription, transactionModel, item, itemCategoryId );
            }

            log.info( "Location Tax Rate Updated Successfully {} ", updateLocationTaxRate );
        }
        else
        {
            locationTaxRateGetDeactivated( subscription, transactionModel, sItemForLocationIdAndCategoryId );
        }

    }

    private void createTransactionForLocationTaxRateOptionalNotPresent( Subscription subscription, TransactionModel transactionModel )
    {
        BigDecimal transactionTaxRate = transactionModel.getSummary().get( 0 ).getRate();
        for( SubscriptionItem item : subscription.getItems() )
        {
            LocationTaxRate locationTaxRate = new LocationTaxRate();
            locationTaxRate.setLocationId( subscription.getLocationId() );
            if( Objects.nonNull( transactionModel ) )
            {
                locationTaxRate.setTaxRate( transactionTaxRate.setScale( 3, RoundingMode.HALF_UP ) );
                locationTaxRate.setTaxCode( transactionModel.getLines().get( 0 ).getTaxCode() );
            }
            locationTaxRate.setEmpId( subscription.getSalesEmployeeId() );
            Optional<LocationTaxRate> taxRateLocation = locationTaxRateRepository.getLocationTaxRateForMaxVersion( locationTaxRate.getLocationId() );
            if( Objects.isNull( item.getItemCategoryId() ) && !( taxRateLocation.isPresent() ) )
            {
                locationService.createLocationTaxRate( locationTaxRate );
            }
            Optional<LocationTaxRate> taxRateLocationWithItemCategory =
                locationTaxRateRepository.getLocationTaxRateOfMaxVersionForLocationAndCategoryId( locationTaxRate.getLocationId(), item.getItemCategoryId() );
            if( Objects.nonNull( item.getItemCategoryId() ) && !( taxRateLocationWithItemCategory.isPresent() ) )
            {
                locationTaxRate.setItemCategoryId( item.getItemCategoryId() );
                locationService.createLocationTaxRate( locationTaxRate );
            }
        }
    }

    private void createLocationTaxRateForDeactivatedNotNull( Subscription subscription, TransactionModel transactionModel, SubscriptionItem item, UUID itemCategoryId )
    {
        BigDecimal transactionTaxRate = transactionModel.getSummary().get( 0 ).getRate();
        Optional<LocationTaxRate> locationTaxRateOptionallocation = locationTaxRateRepository.getLocationTaxRateForMaxVersion( subscription.getLocationId() );
        Optional<LocationTaxRate> locationTaxRateOptionalLocWithItem =
            locationTaxRateRepository.findByLocationIdAndItemCategoryId( subscription.getLocationId(), item.getItemCategoryId() );
        if( Objects.isNull( item.getItemCategoryId() ) )
        {
            createOrUpdateLocationTaxRateItemCategoryIdNull( locationTaxRateOptionallocation, subscription, transactionModel, item );
        }
        if( ( Objects.nonNull( itemCategoryId ) && !itemCategoryId.equals( item.getItemCategoryId() ) && Objects.nonNull( item.getItemCategoryId() ) ) )
        {
            if( !locationTaxRateOptionalLocWithItem.isPresent() )
            {
                LocationTaxRate locationTaxRate = new LocationTaxRate();
                locationTaxRate.setLocationId( subscription.getLocationId() );
                if( Objects.nonNull( transactionModel ) )
                {
                    locationTaxRate.setTaxRate( transactionTaxRate.setScale( 3, RoundingMode.HALF_UP ) );
                    locationTaxRate.setTaxCode( transactionModel.getLines().get( 0 ).getTaxCode() );
                }
                locationTaxRate.setEmpId( subscription.getSalesEmployeeId() );
                locationTaxRate.setItemCategoryId( item.getItemCategoryId() );
                locationService.createLocationTaxRate( locationTaxRate );
            }
            else
            {
                LocationTaxVO locationTaxVOUpdate = new LocationTaxVO();
                if( Objects.nonNull( transactionModel ) )
                {
                    locationTaxVOUpdate.setTaxRate( transactionTaxRate.setScale( 3, RoundingMode.HALF_UP ) );
                    locationTaxVOUpdate.setTaxCode( transactionModel.getLines().get( 0 ).getTaxCode() );
                }
                locationTaxVOUpdate.setEmpId( subscription.getSalesEmployeeId() );
                locationService.updateLocationTaxRate( locationTaxVOUpdate, subscription.getLocationId(), item.getItemCategoryId() );
            }
        }
    }

    private void locationTaxRateGetDeactivated( Subscription subscription, TransactionModel transactionModel, SubscriptionItem sItemForLocationIdAndCategoryId )
    {
        BigDecimal transactionTaxRate = transactionModel.getSummary().get( 0 ).getRate();
        LocationTaxRate locationTaxRate = new LocationTaxRate();
        locationTaxRate.setLocationId( subscription.getLocationId() );
        if( Objects.nonNull( transactionModel ) )
        {
            locationTaxRate.setTaxRate( transactionTaxRate.setScale( 3, RoundingMode.HALF_UP ) );
            locationTaxRate.setTaxCode( transactionModel.getLines().get( 0 ).getTaxCode() );
        }
        locationTaxRate.setEmpId( subscription.getSalesEmployeeId() );
        if( Objects.nonNull( sItemForLocationIdAndCategoryId ) && Objects.nonNull( sItemForLocationIdAndCategoryId.getItemCategoryId() ) )
        {
            locationTaxRate.setItemCategoryId( sItemForLocationIdAndCategoryId.getItemCategoryId() );
        }
        locationService.createLocationTaxRate( locationTaxRate );
        for( SubscriptionItem item : subscription.getItems() )
        {
            locationTaxRate = new LocationTaxRate();
            locationTaxRate.setLocationId( subscription.getLocationId() );
            if( Objects.nonNull( transactionModel ) )
            {
                locationTaxRate.setTaxRate( transactionTaxRate.setScale( 3, RoundingMode.HALF_UP ) );
                locationTaxRate.setTaxCode( transactionModel.getLines().get( 0 ).getTaxCode() );
            }
            locationTaxRate.setEmpId( subscription.getSalesEmployeeId() );
            Optional<LocationTaxRate> taxRateLocation = locationTaxRateRepository.getLocationTaxRateForMaxVersion( locationTaxRate.getLocationId() );
            if( Objects.isNull( item.getItemCategoryId() ) && !( taxRateLocation.isPresent() ) )
            {
                locationService.createLocationTaxRate( locationTaxRate );
            }
            if( Objects.nonNull( item.getItemCategoryId() ) &&
                ( !item.getItemCategoryId().equals( sItemForLocationIdAndCategoryId.getItemCategoryId() ) && item.getItemCategoryId() != null ) )
            {

                locationTaxRate.setItemCategoryId( item.getItemCategoryId() );
                locationService.createLocationTaxRate( locationTaxRate );
            }
        }
    }

    private void createOrUpdateLocationTaxRateItemCategoryIdNull( Optional<LocationTaxRate> locationTaxRateOptionallocation, Subscription subscription,
        TransactionModel transactionModel, SubscriptionItem item )
    {
        BigDecimal transactionTaxRate = transactionModel.getSummary().get( 0 ).getRate();
        if( !locationTaxRateOptionallocation.isPresent() )
        {
            LocationTaxRate locationTaxRate = new LocationTaxRate();
            locationTaxRate.setLocationId( subscription.getLocationId() );
            if( Objects.nonNull( transactionModel ) )
            {
                locationTaxRate.setTaxRate( transactionTaxRate.setScale( 3, RoundingMode.HALF_UP ) );
                locationTaxRate.setTaxCode( transactionModel.getLines().get( 0 ).getTaxCode() );
            }
            locationTaxRate.setEmpId( subscription.getSalesEmployeeId() );
            locationService.createLocationTaxRate( locationTaxRate );
        }
        else
        {
            LocationTaxVO locationTaxVOUpdate = new LocationTaxVO();
            if( Objects.nonNull( transactionModel ) )
            {
                locationTaxVOUpdate.setTaxRate( transactionTaxRate.setScale( 3, RoundingMode.HALF_UP ) );
                locationTaxVOUpdate.setTaxCode( transactionModel.getLines().get( 0 ).getTaxCode() );
            }
            locationTaxVOUpdate.setEmpId( subscription.getSalesEmployeeId() );
            if( Objects.isNull( locationTaxRateOptionallocation.get().getDeactivated() ) )
            {
                locationService.updateLocationTaxRate( locationTaxVOUpdate, subscription.getLocationId(), item.getItemCategoryId() );
            }
            else
            {
                LocationTaxRate taxRate = new LocationTaxRate();
                taxRate.setTaxRate( transactionTaxRate.setScale( 3, RoundingMode.HALF_UP ) );
                taxRate.setTaxCode( transactionModel.getLines().get( 0 ).getTaxCode() );
                taxRate.setEmpId( subscription.getSalesEmployeeId() );
                taxRate.setLocationId( locationTaxRateOptionallocation.get().getLocationId() );
                locationService.createLocationTaxRate( taxRate );
            }
        }
    }

    @Transactional( propagation = Propagation.REQUIRED )
    public PayorInvoiceResponseVO createPayorInvoice( PayorInvoiceRequestVO payorInvoiceRequestVO )
    {
        log.debug( "Inside createPayorInvoice( PayorInvoiceRequestVO payorInvoiceRequestVO )" );

        validateMandatoryFieldsForPayorInvoiceRequestVO( payorInvoiceRequestVO );
        Optional<Subscription> subscription = Optional.empty();
        PayorInvoiceResponseVO payorInvoiceResponseVO = null;
        Optional<Account> account = accountRepository.findById( payorInvoiceRequestVO.getAccountId() );
        Optional<PaymentMethod> paymentMethodOptional = paymentMethodRepository.findById( payorInvoiceRequestVO.getAccountId() );

        validateAccountAndPaymentMethodOptional( account, paymentMethodOptional, payorInvoiceRequestVO );

        Invoice invoice = ModelMapperUtils.map( payorInvoiceRequestVO, Invoice.class );

        String tempInvoiceNumber = getInvoiceSequenceNo();
        String columnLen = applicationConfiguration.getValue( MessageUtils.COLUMN_LENGTH_INVOICE_NO );
        invoiceNumber = CommonUtil.generateInvoiceNo( Integer.parseInt( columnLen ), tempInvoiceNumber );

        log.trace( "invoiceNumber ::: " + invoiceNumber );
        log.trace( "columnLength from properties file :: " + columnLen );

        if( payorInvoiceRequestVO.getSubscriptionId() != null )
        {

            subscription = subscriptionRepository.findById( payorInvoiceRequestVO.getSubscriptionId() );
        }
        invoice.setTransactionId( payorInvoiceRequestVO.getAvaTransactionId() );
        LocalDateTime invoiceDate = ( Objects.nonNull( payorInvoiceRequestVO.getInvoiceDate() ) ) ? payorInvoiceRequestVO.getInvoiceDate() : LocalDateTime.now( Clock.systemUTC() );
        invoice.setInvoiceDate( invoiceDate );
        invoice.setInvoiceNumber( invoiceNumber );
        if( paymentMethodOptional.isPresent() )
        {
            invoice.setPaymentMethodId( payorInvoiceRequestVO.getAccountId() );
            invoice.setAccountId( paymentMethodOptional.get().getAccountId().getAccountId() );
        }
        invoice.setInvoiceType( InvoiceTypeEnum.O );
        if( subscription.isPresent() )
        {
            invoice.setSubscription( subscription.get() );
        }
        invoice.getItems().parallelStream().forEach( invoiceItem -> {
            invoiceItem.setLocId( payorInvoiceRequestVO.getLocationId() );
            ItemVO itemVO = new ItemVO();
            UUID itemCatId = null;
            itemCatId = Objects.nonNull( invoiceItem.getItemCategoryId() ) ? invoiceItem.getItemCategoryId() : itemVO.getItemCategoryId();
            itemVO.setItemCategoryId( itemCatId );
            invoiceItem.setTaxCode( taxCode( itemVO, payorInvoiceRequestVO.getLocationId() ) );
            if( Objects.nonNull( payorInvoiceRequestVO.getAvaTransactionId() ) && Objects.isNull( invoiceItem.getTaxCode() ) )
            {
                invoiceItem.setTaxCode( "P0000000" );
            }
        } );

        invoice = repository.save( invoice );

        if( account.isPresent() )
        {
            Summary summary = new Summary();
            summary.setAccountId( payorInvoiceRequestVO.getAccountId() );
            summary.setInvoice( invoice );
            summary.setTransactionType( TransactionType.INVOICE );
            if( invoice.getTotalAmount().compareTo( BigDecimal.ZERO ) < 0 )
            {
                summary.setType( Type.Cr );
            }
            else
            {
                summary.setType( Type.Dr );
            }
            summary.setSummaryDate( LocalDateTime.now( Clock.systemUTC() ) );
            summary.setModified( LocalDateTime.now( Clock.systemUTC() ) );
            accountSummaryRepository.save( summary );
        }
        if( paymentMethodOptional.isPresent() )
        {
            PaymentMethodAccount paymentMethodAccount = new PaymentMethodAccount();
            paymentMethodAccount.setAccountId( paymentMethodOptional.get().getAccountId().getAccountId() );
            paymentMethodAccount.setInvoice( invoice );
            if( invoice.getTotalAmount().compareTo( BigDecimal.ZERO ) < 0 )
            {
                paymentMethodAccount.setType( Type.Cr );
            }
            else
            {
                paymentMethodAccount.setType( Type.Dr );
            }
            paymentMethodAccount.setSummaryDate( LocalDateTime.now( Clock.systemUTC() ) );
            paymentMethodAccount.setModified( LocalDateTime.now( Clock.systemUTC() ) );
            paymentMethodAccount.setPaymentMethodId( payorInvoiceRequestVO.getAccountId() );
            paymentMethodAccount.setTransactionType( TransactionType.INVOICE );
            paymentMethodAccountRepository.save( paymentMethodAccount );
        }

        payorInvoiceResponseVO = ModelMapperUtils.map( invoice, PayorInvoiceResponseVO.class );
        if( subscription.isPresent() )
        {
            payorInvoiceResponseVO.setSubscriptionId( payorInvoiceRequestVO.getSubscriptionId() );
        }
        return payorInvoiceResponseVO;
    }

    private void validateMandatoryFieldsForPayorInvoiceRequestVO( PayorInvoiceRequestVO payorInvoiceRequestVO )
    {

        if( null == payorInvoiceRequestVO.getLocationId() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_LOCATION_ID ) ) );
        }
        if( null == payorInvoiceRequestVO.getAccountId() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID ) ) );
        }

        if( payorInvoiceRequestVO.getTotalTax() == null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOTAL_TAX ) ) );
        }
        if( payorInvoiceRequestVO.getTotalTax().compareTo( BigDecimal.ZERO ) < 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOTAL_TAX ) ) );
        }
        if( payorInvoiceRequestVO.getTotalAmount() == null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOTAL_AMOUNT ) ) );
        }
        if( payorInvoiceRequestVO.getTotalAmount().compareTo( BigDecimal.ZERO ) < 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOTAL_AMOUNT ) ) );
        }

        if( payorInvoiceRequestVO.getTotalDiscountAmount() == null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOTAL_DISCOUNT ) ) );
        }

        if( payorInvoiceRequestVO.getTotalDiscountAmount().compareTo( BigDecimal.ZERO ) < 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOTAL_DISCOUNT ) ) );
        }

        if( payorInvoiceRequestVO.getTotalNetPrice() == null )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOTAL_NET_PRICE ) ) );
        }

        if( payorInvoiceRequestVO.getTotalNetPrice().compareTo( BigDecimal.ZERO ) < 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TOTAL_NET_PRICE ) ) );
        }

        List<InvoiceItemRequestVO> items = payorInvoiceRequestVO.getItems();
        if( Objects.isNull( items ) || items.isEmpty() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_COMPLETE_ITEM ) ) );
        }

        for( InvoiceItemRequestVO item : items )
        {
            validateInvoiceItemRequestVOList( item );
        }

    }

    private void validateAccountAndPaymentMethodOptional( Optional<Account> account, Optional<PaymentMethod> paymentMethodOptional, PayorInvoiceRequestVO payorInvoiceRequestVO )
    {

        if( !account.isPresent() && !paymentMethodOptional.isPresent() )
        {
            log.debug( "Account ID is not exist {}", payorInvoiceRequestVO.getAccountId() );
            throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Account.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + payorInvoiceRequestVO.getAccountId() ) );
        }
        if( account.isPresent() )
        {
            Optional<MemberCreation> memberCreation = memberCreationRepository.getDetailsByAccountId( payorInvoiceRequestVO.getAccountId() );
            if( !memberCreation.isPresent() )
            {
                throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Account.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + payorInvoiceRequestVO.getAccountId() ) );
            }
        }
        if( paymentMethodOptional.isPresent() )
        {
            Optional<MemberCreation> memberCreation = memberCreationRepository.getDetailsByAccountId( paymentMethodOptional.get().getAccountId().getAccountId() );
            if( !memberCreation.isPresent() )
            {
                throw new ErrorResponse( new EntityNotFoundResponseError( HttpStatus.NOT_FOUND.value(), Account.class,
                    applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ACCOUNT_ID_NOT_FOUND ) + payorInvoiceRequestVO.getAccountId() ) );
            }
        }
    }

    private void validateInvoiceItemRequestVOList( InvoiceItemRequestVO item )
    {

        if( Strings.isEmpty( item.getItemName() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ITEM_NAME ) ) );
        }
        if( null == item.getItemId() || new UUID( 0L, 0L ).equals( item.getItemId() ) )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ITEM_ID ) ) );
        }
        if( item.getPrice() == null || item.getPrice().compareTo( BigDecimal.ZERO ) < 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_PRICE ) ) );
        }
        if( null == item.getType() )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_TYPE_NOT_NULL ) ) );
        }
        if( item.getTaxAmount() == null || item.getTaxAmount().compareTo( BigDecimal.ZERO ) < 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ITEM_TAX_AMOUNT ) ) );
        }
        if( item.getAmountRemaining() == null || item.getAmountRemaining().compareTo( BigDecimal.ZERO ) < 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ITEM_AMOUNT_REMAINING ) ) );
        }
        if( item.getVersion() == null || item.getVersion() < 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ITEM_VERSION ) ) );
        }
        if( item.getQuantity() <= 0 )
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), InvoiceService.class,
                applicationConfiguration.getValue( MessageUtils.ERROR_MESSAGE_ITEM_QUANTITY ) ) );
        }
    }

}
