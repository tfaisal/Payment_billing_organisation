package com.abcfinancial.api.billing.utility.constant

import org.apache.commons.lang.RandomStringUtils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Constant
{
    public static def billingDate = LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("MM-dd-yyyy")).toString();
    public static def afterBillingDate = LocalDate.now().plusYears(1).plusDays(2).format(DateTimeFormatter.ofPattern("MM-dd-yyyy")).toString();
    public static def beforeBillingDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("MM-dd-yyyy")).toString();
    public static def currentDate = new Date().format('MM-dd-yyyy');
    public static def currentDatePlus = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM-dd-yyyy")).toString()
    public static def emailId = "bhavna@QA4LIFE.COM";
    public static def name = "american subscriptionmanagement";
    public static def testName = "abc testtwo";
    public static def phone = "1234567890";
    public static def bankType = "BANK_ACCOUNT";
    public static def bankAccountType = "SAVING";
    public static def creditCardType = "CREDIT_CARD";
    public static def routingNumber = "101000967";
    public static def accountNumber = "117895633211";
    public static def sEvaluation = "ANNUALLY";
    public static def uuidNeverCreated = "00000000-0000-0000-0000-000000000001";
    public static def getRandomString = RandomStringUtils.randomAlphabetic(10);
    public static def getRandomAccountCreationString = RandomStringUtils.randomAlphabetic(10);
    public static def getRandomAccountUpdationString = RandomStringUtils.randomAlphabetic(10);
    public static def createdClientLocationId = "fd1ebc2e-0fab-440f-879f-9e930cc97298";
    public static def secCreatedClientLocationId = "fd1ebc3e-0fab-440f-879f-9e930cc98298";
    public static def thirdCreatedClientLocationId = "fd1ebc3e-0fab-440f-879f-9e930cc98299";
    public static def fourthCreatedClientLocationId = "fd1ebc3e-0fab-440f-879f-9e930cc98300";
    public static def clientAccountId = null
    public static def fifthcreatedClientLocationId = "fd1ebc2e-0fab-440f-879f-9e930cc97198";
    public static def sixthcreatedClientLocationId = "fd1ebc2e-0fab-440f-879f-9e930cc98301";
    public static def transactionType = "invoice"
    public static def invalidDate = "22121212"
    public static def invalidTransType = "invoic"
    public static def today = LocalDate.now();
    public static def accnId = UUID.randomUUID();
}
