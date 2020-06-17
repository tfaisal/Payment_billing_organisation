package com.abcfinancial.api.billing.generalledger.payment.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.generalledger.payment.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID>
{
    PaymentMethod findPaymentMethodByAccountIdAndActive( Account accountId, Boolean active );

    @Query( value = "select * from payment_method where accn_id = :accnId and pame_is_active = :active", nativeQuery = true )
    PaymentMethod findByAccountIdAndActive( @Param( "accnId" ) UUID accountId, @Param( "active" ) Boolean active );

    Optional<PaymentMethod> findPaymentMethodByIdAndActive( UUID paymentId, Boolean active );

    @Query( value = "select * from payment_method where pame_id = :paymentId ", nativeQuery = true )
    PaymentMethod findPaymentMethodDetailsByPaymentId( @Param( "paymentId" ) UUID paymentId );

    @Query( value = "select * from payment_method where accn_id = :accn_id ", nativeQuery = true )
    PaymentMethod findPaymentMethodDetailsByAccountId( @Param( "accn_id" ) UUID paymentId );

    List<PaymentMethod> findByAccountIdAccountId( UUID accountId );

    PaymentMethod findByIdAndActive( UUID paymentId, Boolean active );

    PaymentMethod findFirstPaymentMethodByAccountIdAccountIdAndActiveOrderByCreatedAsc( UUID accountId, Boolean active );

}
