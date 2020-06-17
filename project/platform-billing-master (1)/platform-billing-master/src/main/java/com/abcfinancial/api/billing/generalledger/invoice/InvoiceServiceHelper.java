package com.abcfinancial.api.billing.generalledger.invoice;

import com.abcfinancial.api.billing.subscriptionmanagement.account.location.domain.LocationTaxRate;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.AddressesModel;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.domain.TaxOverrideModel;
import com.abcfinancial.api.billing.subscriptionmanagement.avalara.model.AvaAddress;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Subscription;
import com.abcfinancial.api.billing.generalledger.invoice.domain.LineItemModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.abcfinancial.api.billing.utility.common.MessageUtils.TRUE;

public final class InvoiceServiceHelper
{
    private InvoiceServiceHelper()
    {
    }

    public static final void setAddressToAddressesModel( AddressesModel addressesModel, AvaAddress avaAddress )
    {
        AvaAddress avaAddressTransaction = new AvaAddress( );
        avaAddressTransaction.setLine1( avaAddress.getLine( ) );
        avaAddressTransaction.setLine( avaAddress.getLine() );
        avaAddressTransaction.setCity( avaAddress.getCity( ) );
        avaAddressTransaction.setRegion( avaAddress.getRegion( ) );
        avaAddressTransaction.setCountry( avaAddress.getCountry( ) );
        avaAddressTransaction.setPostalCode( avaAddress.getPostalCode( ) );
        addressesModel.setSingleLocation( avaAddressTransaction );
    }

    public static void processOverridenTaxRate( Optional<LocationTaxRate> optionalLocationTaxRate, TaxOverrideModel taxOverrideModel )
    {
        LocationTaxRate locationTaxRate = null;
        if( optionalLocationTaxRate.isPresent( ) )
        {
            locationTaxRate = optionalLocationTaxRate.get( );
            if( Objects.nonNull( locationTaxRate ) && Objects.nonNull( locationTaxRate.getIsOverriden( ) ) && locationTaxRate.getIsOverriden( ) == TRUE &&
                Objects.nonNull( taxOverrideModel ) )
            {
                double taxAmount = Objects.isNull( locationTaxRate.getSuggestedTaxRate( ) ) ? 0 : locationTaxRate.getSuggestedTaxRate( ).doubleValue( );
                taxOverrideModel.setTaxAmount( taxAmount );
            }
        }
    }

    public static List<LineItemModel> prepareListOfLineItemModel( Subscription subscription, TaxOverrideModel taxOverrideModel, final String taxCode ) {
        List<LineItemModel> lineItemModels = new ArrayList<>( );
        IntStream.range( 0, subscription.getItems( ).size( ) )
                 .forEach( i -> {
                     LineItemModel lines = new LineItemModel( );
                     lines.setTaxOverride( taxOverrideModel );
                     lines.setTaxCode( taxCode );
                     lines.setNumber( String.valueOf( i ) );
                     lines.setQuantity( subscription.getItems( ).get( i ).getQuantity( ) );
                     lines.setAmount( Double.parseDouble( subscription.getItems( ).get( i ).getPrice( ).toString( ) ) );
                     lineItemModels.add( lines );
                 } );
        return lineItemModels;
    }
}
