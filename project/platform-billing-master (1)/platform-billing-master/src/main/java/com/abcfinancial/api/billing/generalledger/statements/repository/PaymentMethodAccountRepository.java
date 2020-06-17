package com.abcfinancial.api.billing.generalledger.statements.repository;

import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.generalledger.settlement.domain.Settlement;
import com.abcfinancial.api.billing.generalledger.statements.domain.PaymentMethodAccount;
import com.abcfinancial.api.billing.generalledger.statements.domain.Statement;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodAccountRepository extends JpaRepository<PaymentMethodAccount, UUID>
{
    public static String LAST_STATEMENT_TRANSACTION =
        "SELECT * FROM payment_method_account WHERE pame_id = :pame_id AND pma_created > (SELECT pma_created created FROM payment_method_account WHERE pame_id = :pame_id" +
        " AND pay_id IS NULL AND inv_id IS NULL AND st_id IS NOT NULL AND stlm_id IS NULL AND adj_id IS NULL ORDER BY pma_created DESC LIMIT 1) ORDER BY pma_created ASC";

    List<PaymentMethodAccount> findByAccountIdAndStatementAndSettlementOrderBySummaryDateDesc( UUID accountId, Statement statement, Settlement settlement );

    List<PaymentMethodAccount> findByPaymentMethodIdAndStatementAndSettlementOrderBySummaryDateDesc( UUID paymentMethodId, Statement statement, Settlement settlement );

    PaymentMethodAccount findByAccountId( UUID accountId );

    Optional<PaymentMethodAccount> findByPayment( Payment payment );

    List<PaymentMethodAccount> findPaymentMethodAccountByAccountId( UUID accountId, Pageable pageable );

    List<PaymentMethodAccount> findPaymentMethodAccountByPaymentMethodId( UUID paymentMethodId, Pageable pageable );

    List<PaymentMethodAccount> findByAccountIdAndSummaryDateBetween( UUID accountId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable );

    List<PaymentMethodAccount> findByPaymentMethodIdAndSummaryDateBetween( UUID paymentMethodId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable );

    List<PaymentMethodAccount> findByAccountIdAndTransactionType( UUID accountId, TransactionType transactionType, Pageable pageable );

    List<PaymentMethodAccount> findByPaymentMethodIdAndTransactionType( UUID paymentMethodId, TransactionType transactionType, Pageable pageable );

    List<PaymentMethodAccount> findByAccountIdAndTransactionTypeAndSummaryDateBetween( UUID accountId, TransactionType transactionType, LocalDateTime fromDate,
        LocalDateTime toDate, Pageable pageable );

    List<PaymentMethodAccount> findByPaymentMethodIdAndTransactionTypeAndSummaryDateBetween( UUID paymentMethodId, TransactionType transactionType, LocalDateTime fromDate,
        LocalDateTime toDate, Pageable pageable );

    @Query( value = LAST_STATEMENT_TRANSACTION, nativeQuery = true )
    List<PaymentMethodAccount> findBySinceLastStatement( @Param( "pame_id" ) UUID paymentMethodId, Pageable pageable );
}
