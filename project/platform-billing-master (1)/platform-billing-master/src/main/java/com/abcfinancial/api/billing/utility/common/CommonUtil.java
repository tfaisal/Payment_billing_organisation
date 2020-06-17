package com.abcfinancial.api.billing.utility.common;

import com.abcfinancial.api.billing.subscriptionmanagement.account.member.domain.MemberCreation;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AvalaraAccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.OnboardingAccountResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.CustomAvalara;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.NexusResponseModel;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAddress;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaLocation;
import com.abcfinancial.api.common.domain.ErrorResponse;
import com.abcfinancial.api.billing.utility.exception.DataIntegrityViolationResponse;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static org.springframework.util.StringUtils.hasLength;

@Slf4j

public final class CommonUtil
{

    private CommonUtil()
    {
    }

    public static String[] getNullPropertyNames( Object source )
    {
        final BeanWrapper src = new BeanWrapperImpl( source );
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet();
        for( java.beans.PropertyDescriptor pd : pds )
        {
            Object srcValue = src.getPropertyValue( pd.getName() );
            if( srcValue == null )
            {
                emptyNames.add( pd.getName() );
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray( result );
    }

    public static Phonenumber.PhoneNumber toGoogleLibphone( String phoneNo ) throws NumberParseException
    {
        Phonenumber.PhoneNumber validPhonenumber = null;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phonenumber = phoneUtil.parse( phoneNo, "US" );
        if( phoneUtil.isValidNumber( phonenumber ) )
        {
            validPhonenumber = phonenumber;
        }
        else
        {
            throw new ErrorResponse( new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), CommonUtil.class, " Please enter a valid US phone number" ) );
        }

        return validPhonenumber;
    }

    public static String getLastnCharacters( String inputString, int subStringLength )
    {
        int length = inputString.length();
        if( length <= subStringLength )
        {
            return inputString;
        }
        int startIndex = length - subStringLength;
        return inputString.substring( startIndex );
    }

    public static boolean isValidEmailAddress( String email )
    {
        if( email == null )
        {
            return false;
        }

        boolean isValid = false;
        EmailValidator validator = EmailValidator.getInstance();

        if( validator.isValid( email ) )
        {
            isValid = true;
        }
        return isValid;
    }

    public static LocalDateTime convertLocDateToLocDateTime( LocalDate localDate )
    {
        LocalTime localTime = LocalTime.of( 0, 0 );
        return LocalDateTime.of( localDate, localTime );
    }

    public static LocalDate convertToDateTime( String date )
    {
        LocalDate formattedDate;
        try
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "MM-dd-yyyy" );
            formattedDate = LocalDate.parse( date, formatter );
        }
        catch( DateTimeParseException exception )
        {
            throw new ErrorResponse(
                new DataIntegrityViolationResponse( HttpStatus.BAD_REQUEST.value(), MemberCreation.class,
                    "Incorrect date format. Expected format is  MM-DD-YYYY" ) );
        }

        return formattedDate;
    }

    public static boolean isValidDate( String stringDate )
    {

        try
        {
            SimpleDateFormat format = new SimpleDateFormat( "MM-dd-yyyy" );
            format.setLenient( false );
            format.parse( stringDate );
        }
        catch( ParseException | IllegalArgumentException exception )
        {
            return false;
        }
        return true;
    }

    public static String getComponentBuilderUrl( String url, Map<String, String> uriParams )
    {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString( url );
        url = builder.buildAndExpand( uriParams ).toUri().toString();
        log.warn( "Calling Dimebox URL: {}", url );
        return url;
    }

    // JIRA : P3-3015 Start
    public static String generateInvoiceNo( final int length, final String invNo )
    {

        StringBuilder buffer = new StringBuilder( StringUtils.repeat( "0", ( length - invNo.length() ) ) );
        return buffer.append( invNo ).toString();
    }

    public static BigDecimal convertCenttoDollar( Integer amount )
    {
        return BigDecimal.valueOf( amount ).divide( BigDecimal.valueOf( 100 ) ).setScale( 2, RoundingMode.HALF_UP );
    }

    public static Integer convertDollartoCent( BigDecimal amount )
    {
        return amount.setScale( 2, RoundingMode.HALF_UP ).multiply( new BigDecimal( 100 ) ).intValueExact();
    }

    public static boolean containsWhitespace( String str )
    {
        if( !hasLength( str ) )
        {
            return false;
        }
        int strLen = str.length();
        for( int i = 0; i < strLen; i++ )
        {
            if( Character.isWhitespace( str.charAt( i ) ) )
            {
                return true;
            }
        }
        return false;
    }

    public static HttpHeaders createAuthorizationHeader( String username, String password )
    {
        String authHeader = null;
        String auth = username + ":" + password;
        try
        {
            authHeader = "Basic " + Base64.getEncoder().encodeToString( auth.getBytes( "utf-8" ) );
        }
        catch( UnsupportedEncodingException exception )
        {
            log.error( exception.getMessage() );
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add( "Authorization", authHeader );
        headers.setContentType( MediaType.APPLICATION_JSON );

        return headers;
    }

    public static String buildExceptionMessage( String errorMessage )
    {
        Gson g = new Gson();
        CustomAvalara customException = g.fromJson( errorMessage, CustomAvalara.class );
        return customException.toString();
    }

    public static OnboardingAccountResponse buildTestResponseObject( AvalaraAccountVO avalaraAccountVO )
    {
        OnboardingAccountResponse onboardingAccountResponse = new OnboardingAccountResponse();
        String line = avalaraAccountVO.getCompanyAddress().getLine();
        String city = avalaraAccountVO.getCompanyAddress().getCity();
        String region = avalaraAccountVO.getCompanyAddress().getRegion();
        String country = avalaraAccountVO.getCompanyAddress().getCountry();
        String postalCode = avalaraAccountVO.getCompanyAddress().getPostalCode();
        onboardingAccountResponse.setAccountId( "2000328529" );
        onboardingAccountResponse.setLicenseKey( "9959E096E9345B8A" );
        onboardingAccountResponse.setCompanyId( "842855" );
        AvaLocation avaLocation = new AvaLocation();
        avaLocation.setLocationId( 354319L );
        AvaAddress avaAddress = new AvaAddress();
        avaAddress.setLine( line );
        avaAddress.setCity( city );
        avaAddress.setRegion( region );
        avaAddress.setCountry( country );
        avaAddress.setPostalCode( postalCode );
        avaAddress.setLatitude( 33.684689 );
        avaAddress.setLatitude( -117.851495 );
        avaAddress.setAddressId( UUID.fromString( "d1d298f9-f286-4055-ba5e-41abb2484fb8" ) );
        avaAddress.setIsValidated( true );
        avaLocation.setAvaAddress( avaAddress );
        onboardingAccountResponse.setAvaLocation( avaLocation );
        NexusResponseModel nexusResponseModel = new NexusResponseModel();
        nexusResponseModel.setNexusid( 1000L );
        nexusResponseModel.setCompanyId( "10001" );
        nexusResponseModel.setCountry( country );
        nexusResponseModel.setRegion( region );
        nexusResponseModel.setJurisTypeId( "STA" );
        nexusResponseModel.setJurisCode( "06" );
        nexusResponseModel.setJurisName( "UNITED STATES" );
        nexusResponseModel.setShortName( "US" );
        nexusResponseModel.setNexusTypeId( "SalesOrSellersUseTax" );
        nexusResponseModel.setModifiedUserId( "327279" );
        NexusResponseModel nexusResponseModel2 = new NexusResponseModel();
        nexusResponseModel2.setNexusid( 1001L );
        nexusResponseModel2.setCompanyId( "10001" );
        nexusResponseModel2.setCountry( country );
        nexusResponseModel2.setRegion( region );
        nexusResponseModel2.setJurisTypeId( "CNT" );
        nexusResponseModel2.setJurisCode( "07" );
        nexusResponseModel2.setJurisName( "UNITED STATES" );
        nexusResponseModel2.setShortName( "US" );
        nexusResponseModel2.setNexusTypeId( "SalesOrSellersUseTax" );
        nexusResponseModel2.setModifiedUserId( "327279" );
        onboardingAccountResponse.setNexusResponseModels( Arrays.asList( nexusResponseModel, nexusResponseModel2 ) );
        return onboardingAccountResponse;
    }

    public static LocalTime convertTimeStringToLocalTime( String time )
    {
        String[] timeSeperated = time.split( "\\." );
        return LocalTime
            .of( Integer.parseInt( timeSeperated[0] ), Integer.parseInt( timeSeperated[1] ), Integer.parseInt( timeSeperated[2] ), Integer.parseInt( timeSeperated[3] ) );
    }
}
