package com.abcfinancial.api.billing.subscriptionmanagement.avalara.helper;

import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.AvalaraAccountVO;
import com.abcfinancial.api.billing.subscriptionmanagement.account.valueobject.OnboardingAccountResponse;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.Address;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.LocationModel;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.NexusModel;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.NexusResponseModel;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAccount;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaCompany;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaLocation;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaNexus;

import java.sql.Timestamp;

public class OnboadingServiceHelper
{

    public static AvaAccount prepare( OnboardingAccountResponse onboardingAccountResponse, AvalaraAccountVO avalaraAccountVO )
    {
        AvaAccount avaAccount = new AvaAccount();
        avaAccount.setAvalaraAccId( Long.parseLong( onboardingAccountResponse.getAccountId() ) );
        Timestamp createOrModifyDateTime = new Timestamp( System.currentTimeMillis() );
        avaAccount.setCreatedDate( createOrModifyDateTime );
        avaAccount.setModifiedDate( createOrModifyDateTime );
        avaAccount.setOrganizationId( avalaraAccountVO.getOrganizationId() );
        avaAccount.setLocationId( avalaraAccountVO.getLocationId() );
        return avaAccount;
    }

    public static AvaCompany prepareAvaCompany( String companyId, AvaAccount avaAccount, String licenseKey )
    {
        Timestamp createOrModifiedDateTime = new Timestamp( System.currentTimeMillis() );
        AvaCompany avaCompany = new AvaCompany();
        avaCompany.setCompanyId( Long.valueOf( companyId ) );
        avaCompany.setCreatedDate( createOrModifiedDateTime );
        avaCompany.setModifiedDate( createOrModifiedDateTime );
        avaCompany.setAvaAccount( avaAccount );
        avaCompany.setLicenseKey( licenseKey );
        return avaCompany;
    }

    public static Address prepareAddress( LocationModel locationModel )
    {
        Address address = new Address();
        String city = locationModel.getCity();
        String postalCode = locationModel.getPostalCode();
        String line = locationModel.getLine1();
        String region = locationModel.getRegion();
        String country = locationModel.getCountry();
        address.setLine1( line );
        address.setLine( line );
        address.setCity( city );
        address.setCountry( country );
        address.setPostalCode( postalCode );
        address.setRegion( region );
        return address;
    }

    public static NexusModel prepareNexusModel( LocationModel locationModel )
    {
        NexusModel nexusModel = new NexusModel();
        nexusModel.setCountry( locationModel.getCountry() );
        nexusModel.setRegion( locationModel.getRegion() );
        nexusModel.setJurisTypeId( "STA" );
        nexusModel.setJurisdictionTypeId( "State" );
        return nexusModel;
    }

    public static AvaNexus prepareAvaNexus( NexusResponseModel nexusResponseModel, AvaLocation avaLocation, AvaCompany avaCompany )
    {
        AvaNexus avaNexus = new AvaNexus();
        avaNexus.setAvaLocation( avaLocation );
        avaNexus.setAvaCompany( avaCompany );
        avaNexus.setNexusid( nexusResponseModel.getNexusid() );
        avaNexus.setCreatedDate( avaLocation.getCreatedDateTime() );
        avaNexus.setModifiedDate( avaLocation.getCreatedDateTime() );
        return avaNexus;
    }
}
