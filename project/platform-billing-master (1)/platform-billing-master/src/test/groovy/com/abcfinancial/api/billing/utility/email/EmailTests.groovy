//package com.abcfinancial.api.billing.utility.email
//
//import com.abcfinancial.api.billing.common.BaseTest
//import com.abcfinancial.api.billing.subscriptionmanagement.account.repository.AccountRepository
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.http.ResponseEntity
//import org.testng.annotations.BeforeMethod
//import org.testng.annotations.Test
//
//import static groovy.json.JsonOutput.toJson
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//import static com.abcfinancial.api.billing.utility.constant.Constant.*
//
//class EmailTests extends BaseTest
//{
//    // def locationId = "20b3ac26-9fb6-44f7-a4f6-320468e0b829"
//    static def accountId
//    def subscriptionMethod
//    static def memberId = UUID.randomUUID()
//    static def locationId = UUID.randomUUID()
//    static def payorId = UUID.randomUUID()
//    static def createdMember
//    static def createProcessor
//    static def ExpireDate = new Date().plus(1).format('MM-dd-yyyy');
//
//    //@Autowired
//    //SubscriptionRepository subscriptionRepository
//
//    @Autowired
//    AccountRepository accountRepository
//
//    @BeforeMethod
//    void setToken()
//    {
//        bearerToken = getBearerToken()
//    }
//
//    /* UUID addSubscription() {
//         Account account = new Account()
//         account.setName("TestAccount")
//         account.setSevaluation("Annually")
//         account.setEmail("test@test.com")
//         account.setLocation(UUID.fromString(locationId))
//         accountRepository.save(account)
//         Subscription subscription = new Subscription()
//         subscription.setLocationId(UUID.fromString(locationId))
//         subscription.setPlanId(UUID.fromString("f926fea0-3d2d-41f4-9cf5-97d528a94eec"))
//         subscription.setAccount(account)
//         subscription.setStart(LocalDate.now())
//         subscription.setName("Test Subscription")
//         subscription.setFrequency(Frequency.MONTHLY)
//         subscription.setDuration(3)
//         SubscriptionItem subscriptionItem = new SubscriptionItem()
//         subscriptionItem.setLocId(UUID.fromString(locationId))
//         subscriptionItem.setItemName("Test Subscription Item")
//         subscriptionItem.setType(ItemType.AMENITY)
//         subscriptionItem.setItemId(UUID.fromString("eb0ef920-57e3-4055-854b-684886c6948d"))
//         subscriptionItem.setPrice(BigDecimal.valueOf(10))
//         subscription.setItems(Collections.singletonList(subscriptionItem))
//         subscriptionRepository.save(subscription)
//         return subscription.subId
//     }*/
//
//    /*UUID addSubscription_priceLessThanZero() {
//        Account account = new Account()
//        account.setName("TestAccount")
//        account.setSevaluation("Annually")
//        account.setEmail("test@test.com")
//        account.setLocation(UUID.fromString(locationId))
//        accountRepository.save(account)
//        Subscription subscription = new Subscription()
//        subscription.setLocationId(UUID.fromString(locationId))
//        subscription.setPlanId(UUID.fromString("f926fea0-3d2d-41f4-9cf5-97d528a94eec"))
//        subscription.setAccount(account)
//        subscription.setStart(LocalDate.now())
//        subscription.setName("Test Subscription")
//        subscription.setFrequency(Frequency.MONTHLY)
//        subscription.setDuration(3)
//        SubscriptionItem subscriptionItem = new SubscriptionItem()
//        subscriptionItem.setLocId(UUID.fromString(locationId))
//        subscriptionItem.setItemName("Test Subscription Item")
//        subscriptionItem.setType(ItemType.AMENITY)
//        subscriptionItem.setItemId(UUID.fromString("eb0ef920-57e3-4055-854b-684886c6948d"))
//        subscriptionItem.setPrice(BigDecimal.valueOf(-5))
//        subscription.setItems(Collections.singletonList(subscriptionItem))
//        subscriptionRepository.save(subscription)
//        return subscription.subId
//    }*/
//
//    /* UUID addSubscription_invalidLocationId() {
//         Account account = new Account()
//         account.setName("TestAccount")
//         account.setSevaluation("Annually")
//         account.setEmail("test@test.com")
//         account.setLocation(UUID.randomUUID())
//         accountRepository.save(account)
//         Subscription subscription = new Subscription()
//         subscription.setLocationId(UUID.fromString(locationId))
//         subscription.setPlanId(UUID.fromString("f926fea0-3d2d-41f4-9cf5-97d528a94eec"))
//         subscription.setAccount(account)
//         subscription.setStart(LocalDate.now())
//         subscription.setName("Test Subscription")
//         subscription.setFrequency(Frequency.MONTHLY)
//         subscription.setDuration(3)
//         SubscriptionItem subscriptionItem = new SubscriptionItem()
//         subscriptionItem.setLocId(UUID.fromString(locationId))
//         subscriptionItem.setItemName("Test Subscription Item")
//         subscriptionItem.setType(ItemType.AMENITY)
//         subscriptionItem.setItemId(UUID.fromString("eb0ef920-57e3-4055-854b-684886c6948d"))
//         subscriptionItem.setPrice(BigDecimal.valueOf(10))
//         subscription.setItems(Collections.singletonList(subscriptionItem))
//         subscriptionRepository.save(subscription)
//         return subscription.subId
//     }*/
//
//    @Test(priority = 0)
//    void createMember()
//    {
//        createdMember = parseJson(mvc().perform(post('/account/payor').header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(createMemberAccountRequest())))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andReturn())
//
//    }
//
//    @Test(priority = 1, enabled = true)
//    void subscriptionTest()
//    {
//        def request = createSubscriptionRequest()
//
//        request.collect { requests ->
//            subscriptionMethod = parseJson(mvc().perform(post('/subscription/').header("Authorization", "Bearer ${bearerToken}")
//                    .content(toJson(requests)))
//                    .andDo(print())
//                    .andExpect(status().isCreated())
//                    .andExpect(jsonPath("\$.subId").value(isUuid()))
//                    .andExpect(jsonPath("\$.locationId").value(isUuid()))
//                    .andExpect(jsonPath("\$.accountId").value(isUuid()))
//                    .andReturn())
//        }
//
//    }
//
//    @Test(priority = 2, enabled = true)
//    void membershipReceiptEmailTest()
//    {
//        def subscriptionId = subscriptionMethod.subId
//
//        def sendEmailRequest = getMembershipReceiptEmailRequest(subscriptionId)
//
//        mvc().perform(post('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andDo(document('email/sendPurchaseEmail'))
//    }
//
//    @Test(priority = 2, enabled = true)
//    void membershipReceiptEmailAsyncTest()
//    {
//        def subscriptionId = subscriptionMethod.subId
//
//        def request = getMembershipReceiptEmailRequest(subscriptionId)
//
//        mvc().perform(post('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .header("Prefer", "respond-async")
//                .content(toJson(request)))
//                .andDo(print())
//                .andExpect(status().isAccepted())
//    }
//
//    @Test(priority = 3, enabled = true)
//    void membershipReceiptEmailTest_badRequest1()
//    {
//        def subscriptionId = subscriptionMethod.subId
//
//        def sendEmailRequest = null
//
//        mvc().perform(post('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('400_Bad Request' ))
//    }
//
//    @Test(priority = 4, enabled = true)
//    void membershipReceiptEmailTest_badRequest2()
//    {
//        def subscriptionId = subscriptionMethod.subId
//
//        def sendEmailRequest = getMembershipReceiptEmailRequest_badRequest(subscriptionId)
//
//        mvc().perform(post('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('400_Bad Request' ))
//    }
//
//    @Test(priority = 5, enabled = true)
//    void membershipReceiptEmailTest_notFound()
//    {
//        def subscriptionId = subscriptionMethod.subId
//
//        def sendEmailRequest = getMembershipReceiptEmailRequest(subscriptionId)
//
//        mvc().perform(post('/email/sendPurchaseEmail/eee')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andDo(document('404_Not Found' ))
//    }
//
//// Test case is correct. Need to fix the bug of negative value of subscription pricing
//    /*@Test(priority = 7, enabled = true)
//     void membershipReceiptEmailTest_priceLessThanZero() {
//         def subscriptionId = addSubscription_priceLessThanZero()
//
//         def sendEmailRequest = getMembershipReceiptEmailRequest(subscriptionId)
//
//         mvc().perform(post('/email/sendPurchaseEmail')
//                 .header("Authorization", "Bearer ${bearerToken}")
//                 .content(toJson(sendEmailRequest)))
//                 .andDo(print())
//                 .andExpect(status().isBadRequest())
//                 .andDo(document('email/sendPurchaseEmail', responseBodyAsType(ResponseEntity)))
//    }*/
//
//    // Test Case is correct but need to fix the invalid phone bug as its give 200 on invalid phone number
//    /*@Test(priority = 8, enabled = true)
//     void membershipReceiptEmailTest_invalidPhoneNo() {
//         def subscriptionId = addSubscription()
//
//         def sendEmailRequest = getMembershipReceiptEmailRequest_invalidPhoneNo(subscriptionId)
//
//         mvc().perform(post('/email/sendPurchaseEmail')
//                 .header("Authorization", "Bearer ${bearerToken}")
//                 .content(toJson(sendEmailRequest)))
//                 .andDo(print())
//                 .andExpect(status().isBadRequest())
//                 .andDo(document('Invalid Phone No', responseBodyAsType(ResponseEntity)))
//     }*/
//
//    //Test is correct but need to fix the bug for invalid name
//    /*@Test(priority = 9, enabled = true)
//    void membershipReceiptEmailTest_invalidName() {
//        def subscriptionId = addSubscription()
//
//        def sendEmailRequest = getMembershipReceiptEmailRequest_invalidName(subscriptionId)
//
//        mvc().perform(post('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('Invalid Name', responseBodyAsType(ResponseEntity)))
//    }*/
//    //Test is correct but need to fix the bug for diff location subId
//    /*@Test(priority = 10, enabled = true)
//    void membershipReceiptEmailTest_invalidLocationId() {
//        def subscriptionId = addSubscription_invalidLocationId()
//
//        def sendEmailRequest = getMembershipReceiptEmailRequest(subscriptionId)
//
//        mvc().perform(post('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andDo(document('Invalid Location Id', responseBodyAsType(ResponseEntity)))
//    }*/
//
//    //Test is correct but need to fix the bug for invalid logo url
//    /*@Test(priority = 11, enabled = true)
//    void membershipReceiptEmailTest_invalidLogoURL() {
//        def subscriptionId = addSubscription()
//
//        def sendEmailRequest = getMembershipReceiptEmailRequest_invalidLogoURL(subscriptionId)
//
//        mvc().perform(post('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('Invalid Logo URL', responseBodyAsType(ResponseEntity)))
//    }
//*/
//
//    @Test(priority = 12, enabled = true)
//    void membershipReceiptEmailTest_invalidSubscriptionId()
//    {
//        def subscriptionId = subscriptionMethod.subId
//
//        def sendEmailRequest = getMembershipReceiptEmailRequest("123456788")
//
//        mvc().perform(get('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//    }
//
//    @Test(priority = 13, enabled = true)
//    void membershipReceiptEmailTest_withoutSubscriptionId()
//    {
//        def sendEmailRequest = getMembershipReceiptEmailRequest(null)
//
//        mvc().perform(get('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//    }
//
//// In below test cases Expected bad request(400) but it is returning internal server error(500)
//
//    /* @Test(priority = 14, enabled = true)
//     void membershipReceiptEmailTest_withoutBusinessLogo() {
//         def subscriptionId = addSubscription()
//         def sendEmailRequest = getMembershipReceiptEmailRequest_withoutBusinessLogo(subscriptionId)
//
//         mvc().perform(get('/email/sendPurchaseEmail')
//                 .header("Authorization", "Bearer ${bearerToken}")
//                 .content(toJson(sendEmailRequest)))
//                 .andDo(print())
//                 .andExpect(status().isBadRequest())
//                 .andDo(document('Without Business Logo', responseBodyAsType(ResponseEntity)))
//     }*/
//
///*    @Test(priority = 15, enabled = true)
//    void membershipReceiptEmailTest_withoutFirstName() {
//        def subscriptionId = addSubscription()
//        def sendEmailRequest = getMembershipReceiptEmailRequest_withoutFirstName(subscriptionId)
//
//        mvc().perform(get('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('Without First Name', responseBodyAsType(ResponseEntity)))
//    }*/
//
//    /*@Test(priority = 16, enabled = true)
//    void membershipReceiptEmailTest_withoutClassDate() {
//        def subscriptionId = addSubscription()
//        def sendEmailRequest = getMembershipReceiptEmailRequest_withoutClassDate(subscriptionId)
//
//        mvc().perform(get('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('Without Class Date', responseBodyAsType(ResponseEntity)))
//    }*/
//
//    /*@Test(priority = 17, enabled = true)
//    void membershipReceiptEmailTest_withoutSubTotalPrice() {
//        def subscriptionId = addSubscription()
//        def sendEmailRequest = getMembershipReceiptEmailRequest_withoutSubtotalPrice(subscriptionId)
//
//        mvc().perform(get('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('Without Sub Total Price', responseBodyAsType(ResponseEntity)))
//    }*/
//
//    /*@Test(priority = 18, enabled = true)
//    void membershipReceiptEmailTest_withoutTaxesValue() {
//        def subscriptionId = addSubscription()
//        def sendEmailRequest = getMembershipReceiptEmailRequest_withoutTaxesValue(subscriptionId)
//
//        mvc().perform(get('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('Without Tax Value', responseBodyAsType(ResponseEntity)))
//    }
//*/
//    /*@Test(priority = 19, enabled = true)
//    void membershipReceiptEmailTest_withoutTotalValue() {
//        def subscriptionId = addSubscription()
//        def sendEmailRequest = getMembershipReceiptEmailRequest_withoutTotalValue(subscriptionId)
//
//        mvc().perform(get('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('Without Total Value', responseBodyAsType(ResponseEntity)))
//    }*/
//
//    /*@Test(priority = 20, enabled = true)
//    void membershipReceiptEmailTest_withoutBusinessName() {
//        def subscriptionId = addSubscription()
//        def sendEmailRequest = getMembershipReceiptEmailRequest_withoutBusinessName(subscriptionId)
//
//        mvc().perform(get('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('Without Business Name', responseBodyAsType(ResponseEntity)))
//    }*/
//
//    /* @Test(priority = 21, enabled = true)
//     void membershipReceiptEmailTest_withoutBusinessAddress1() {
//         def subscriptionId = addSubscription()
//         def sendEmailRequest = getMembershipReceiptEmailRequest_withoutBusinessAddress1(subscriptionId)
//
//         mvc().perform(get('/email/sendPurchaseEmail')
//                 .header("Authorization", "Bearer ${bearerToken}")
//                 .content(toJson(sendEmailRequest)))
//                 .andDo(print())
//                 .andExpect(status().isBadRequest())
//                 .andDo(document('Without Business Address1', responseBodyAsType(ResponseEntity)))
//     }*/
///*
//    @Test(priority = 22, enabled = true)
//    void membershipReceiptEmailTest_withoutBusinessCity() {
//        def subscriptionId = addSubscription()
//        def sendEmailRequest = getMembershipReceiptEmailRequest_withoutBusinessCity(subscriptionId)
//
//        mvc().perform(get('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('Without Business City', responseBodyAsType(ResponseEntity)))
//    }*/
//
//    /* @Test(priority = 23, enabled = true)
//     void membershipReceiptEmailTest_withoutBusinessState() {
//         def subscriptionId = addSubscription()
//         def sendEmailRequest = getMembershipReceiptEmailRequest_withoutBusinessState(subscriptionId)
//
//         mvc().perform(get('/email/sendPurchaseEmail')
//                 .header("Authorization", "Bearer ${bearerToken}")
//                 .content(toJson(sendEmailRequest)))
//                 .andDo(print())
//                 .andExpect(status().isBadRequest())
//                 .andDo(document('Without Business State', responseBodyAsType(ResponseEntity)))
//     }
// */
//    /* @Test(priority = 24, enabled = true)
//     void membershipReceiptEmailTest_withoutBusinessZIP() {
//         def subscriptionId = addSubscription()
//         def sendEmailRequest = getMembershipReceiptEmailRequest_withoutBusinessZIP(subscriptionId)
//
//         mvc().perform(get('/email/sendPurchaseEmail')
//                 .header("Authorization", "Bearer ${bearerToken}")
//                 .content(toJson(sendEmailRequest)))
//                 .andDo(print())
//                 .andExpect(status().isBadRequest())
//                 .andDo(document('Without Business ZIP', responseBodyAsType(ResponseEntity)))
//     }*/
//
//    /* @Test(priority = 25, enabled = true)
//     void membershipReceiptEmailTest_withoutBusinessPhoneNumber() {
//         def subscriptionId = addSubscription()
//         def sendEmailRequest = getMembershipReceiptEmailRequest_withoutBusinessPhoneNumber(subscriptionId)
//
//         mvc().perform(get('/email/sendPurchaseEmail')
//                 .header("Authorization", "Bearer ${bearerToken}")
//                 .content(toJson(sendEmailRequest)))
//                 .andDo(print())
//                 .andExpect(status().isBadRequest())
//                 .andDo(document('Without Business Phone Number', responseBodyAsType(ResponseEntity)))
//     }*/
//
//    /*@Test(priority = 26, enabled = true)
//    void membershipReceiptEmailTest_withoutBusinessWebsiteAddress() {
//        def subscriptionId = addSubscription()
//        def sendEmailRequest = getMembershipReceiptEmailRequest_withoutBusinessWebsiteAddress(subscriptionId)
//
//        mvc().perform(get('/email/sendPurchaseEmail')
//                .header("Authorization", "Bearer ${bearerToken}")
//                .content(toJson(sendEmailRequest)))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andDo(document('Without Business Website Address', responseBodyAsType(ResponseEntity)))
//    }*/
//
//    static def getMembershipReceiptEmailRequest(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_badRequest(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "testqa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_invalidPhoneNo(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+1234567890000888",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_invalidName(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : 12345,
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+1234567890000",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_invalidLogoURL(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "htt",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutBusinessLogo(subscriptionId)
//    {
//        return [
//
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutFirstName(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutClassDate(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutSubtotalPrice(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutTaxesValue(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutTotalValue(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutBusinessName(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutBusinessAddress1(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutBusinessCity(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutBusinessState(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutBusinessZIP(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutBusinessPhoneNumber(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessAdministrativeEmail": "test@qa4life.com",
//                "businessWebsiteAddress"     : "qa4life.com"
//        ]
//    }
//
//    static def getMembershipReceiptEmailRequest_withoutBusinessWebsiteAddress(subscriptionId)
//    {
//        return [
//                "businessLogo"               : "http://localhost",
//                "subscriptionId"             : subscriptionId,
//                "subtotalPrice"              : 10,
//                "taxesValue"                 : 2,
//                "totalValue"                 : 12,
//                "firstName"                  : "firstName",
//                "classDate"                  : "classDate",
//                "businessName"               : "businessName",
//                "businessAddress1"           : "businessAddress1",
//                "businessAddress2"           : "businessAddress2",
//                "businessCity"               : "businessCity",
//                "businessState"              : "businessState",
//                "businessZIP"                : "businessZIP",
//                "businessPhoneNumber"        : "+123456789",
//                "businessAdministrativeEmail": "test@qa4life.com",
//        ]
//    }
//
//    static def createMemberAccountRequest()
//    {
//        return [
//                "locationId": createdClientLocationId,
//                "memberId"  : memberId,
//                "payorId"   : payorId,
//                account     : [
//                        "name"       : "SubscriptionService",
//                        "email"      : "SubscriptionService@QA4LIFE.COM",
//                        "phone"      : "19075526943",
//                        "sevaluation": sEvaluation,
//                        "billingDate": currentDate,
//                        paymentMethod: [
//                                "type": "CASH"
//                        ]
//                ]
//        ]
//    }
//
//    static def createProcessorRequest()
//    {
//        return [
//                "accountId"     : "5b47534bf58fa7398df15d38",
//                "organizationId": "5b115891b6cdfd755de3d4bd",
//                "locationId"    : createdMember.locationId
//        ]
//    }
//
//
//    def createSubscriptionRequest()
//    {
//        return [
//                [
//                        "locationId"     : createdMember.locationId,
//                        "salesEmployeeId": UUID.randomUUID(),
//                        "accountId"      : createdMember.account.accountId,
//                        "memberIdList"   : [createdMember.memberId],
//                        "start"          : currentDate,
//                        "invoiceDate"    : currentDate,
//                        "expirationDate" : ExpireDate,
//                        "frequency"      : "DAILY",
//                        "duration"       : "4",
//                        "items"          : [[
//                                                    "itemName"       : "adcds",
//                                                    "itemId"         : UUID.randomUUID(),
//                                                    "version"        : 1,
//                                                    "price"          : 2117,
//                                                    "quantity"       : "1",
//                                                    "expirationStart": "PURCHASE",
//                                                    "type"           : "PRODUCT",
//                                                    "unlimited"      : "false"
//                                            ]]
//                ]
//        ]
//
//    }
//
//}
