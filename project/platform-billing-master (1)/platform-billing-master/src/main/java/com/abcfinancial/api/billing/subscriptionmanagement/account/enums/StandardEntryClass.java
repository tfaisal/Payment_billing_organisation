package com.abcfinancial.api.billing.subscriptionmanagement.account.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum StandardEntryClass
{
    ARC( "Accounts receivable conversion", 
        "A consumer check converted to a one-time ACH debit. The difference between ARC and POP is that ARC can result from a check mailed in whereas POP is in-person." ), 
    BOC( "Back office conversion", 
        "A single entry debit initiated at the point of purchase or at a manned bill payment location to transfer funds through conversion to an ACH debit entry during back " +
        "office processing. Unlike ARC entries, BOC conversions require that the customer be present, and that the vendor post a notice that checks may be converted to BOC ACH " +
        "entries." ), 
    CBR( "Corporate cross-border payment", 
        "Used for international business transactions, replaced by SEC Code IAT.[7]" ), 
    CCD( "Corporate Credit or Debit Entry", 
        "Used to consolidate and sweep cash funds within an entity's controlled accounts, or make/collect payments to/from other corporate entities." ), 
    CIE( "Customer Initiated Entries", 
        "Use limited to credit applications where the consumer initiates the transfer of funds to a company for payment of funds owed to that company, typically through some " +
        "type of home banking product or bill payment service provider.[8]" ), 
    CTX( "Corporate trade exchange", 
        "Transactions that include ASC X12 or EDIFACT information.[5]" ), 
    DNE( "Death notification entry", 
        "Issued by the federal government." ), 
    IAT( "International ACH transaction", 
        "This is a SEC code for cross-border payment traffic to replace the PBR and CBR codes. The code has been implemented since September 18, 2009.[7]" ), 
    PBR( "Consumer cross-border payment", 
        "Used for international household transactions, replaced by SEC Code IAT.[7]" ), 
    POP( "Point-of-purchase", 
        "A check presented in-person to a merchant for purchase is presented as an ACH entry instead of a physical check." ), 
    POS( "Point-of-sale", 
        "A debit at an electronic terminal initiated by use of a plastic card. An example is using your debit card to purchase gas." ), 
    PPD( "Prearranged payment and deposits", 
        "Used to credit or debit a consumer account. Popularly used for payroll direct deposits and preauthorized bill payments." ), 
    RCK( "Represented check entries", 
        "A physical check that was presented but returned because of insufficient funds may be represented as an ACH entry." ), 
    TEL( "Telephone-initiated entry", 
        "Oral authorization by telephone to issue an ACH entry such as checks by phone. ( TEL code allowed for inbound telephone orders only. NACHA disallows the use of this code" +
        " for outbound telephone solicitations unless a prior business arrangement with the customer has been established. )" ), 
    WEB( "Web-initiated entry", 
        "Electronic authorization through the Internet to create an ACH entry." ), 
    XCK( "Destroyed check entry", 
        "A physical check that was destroyed because of a disaster can be presented as an ACH entry." );
    @Getter
    private final String name;
    @Getter
    private final String description;
}
