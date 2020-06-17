package com.abcfinancial.api.billing.utility.email;
//
//import com.abcfinancial.api.common.annotation.PreferHeader;
//import com.abcfinancial.api.common.email.service.EmailService;
//import com.abcfinancial.api.common.http.HeaderPrefer;
//import com.abcfinancial.api.billing.utility.email.valueobject.PurchaseEmailVO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.ui.ModelMap;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.validation.Valid;
//import java.util.Optional;
//import java.util.function.Supplier;
//
//@Slf4j
//@Validated
//@RestController
//
//@RequiredArgsConstructor
//@RequestMapping( "/email" )
//
//public class EmailController
//{
//    private final EmailService emailService;
//    private final PreferHeaderProcessor preferHeaderProcessor;
//    private final EmailTemplateDataFiller emailTemplateDataFiller;
//    /**
//     * Purchase Email tenant required.
//     */
//
//    @PostMapping( "sendPurchaseEmail" )
//    @PreAuthorize( "#oauth2.hasScope( 'subscription:write' )" )
//    public ResponseEntity<ModelMap> sendPurchaseEmail( @PreferHeader Optional<HeaderPrefer> headerPrefer,
//        @Valid @RequestBody PurchaseEmailVO emailVO )
//    {
//        emailTemplateDataFiller.fillPurchaseEmailData( emailVO );
//        Supplier<String> action = ( ) -> emailService.sendEmail( emailVO );
//        return preferHeaderProcessor.processPreferHeader( headerPrefer,
//            action,
//            ( ) -> ResponseEntity.ok( ).body( new ModelMap( ).addAttribute( "messageId", action.get( ) ) ),
//            null,
//            null,
//            HttpStatus.OK );
//    }
//}
