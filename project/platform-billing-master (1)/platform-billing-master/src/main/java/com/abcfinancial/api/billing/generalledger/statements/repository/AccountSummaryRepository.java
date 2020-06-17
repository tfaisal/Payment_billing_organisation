package com.abcfinancial.api.billing.generalledger.statements.repository;

import com.abcfinancial.api.billing.generalledger.enums.TransactionType;
import com.abcfinancial.api.billing.generalledger.settlement.domain.Settlement;
import com.abcfinancial.api.billing.generalledger.statements.domain.Statement;
import com.abcfinancial.api.billing.generalledger.statements.domain.Summary;
import com.abcfinancial.api.billing.subscriptionmanagement.subscription.domain.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountSummaryRepository extends JpaRepository<Summary, UUID>
{
    List<Summary> findByAccountIdAndStatementAndSettlementOrderBySummaryDateDesc( UUID accountId, Statement statement, Settlement settlement );

    List<Summary> findByLocationIdAndSettlementOrderBySummaryDateDesc( UUID locationId, Settlement settlement );

    Summary findByAccountId( UUID accountId );

    Optional<Summary> findByPayment( Payment payment );

    Optional<Summary> findByInvoiceIdAndPaymentNullAndStatementStatementIdNotNull( UUID invoiceId );

    Optional<Summary> findByInvoiceIdAndPaymentNotNullAndStatementStatementIdNotNull( UUID invoiceId );

    List<Summary> findAllByAccountIdAndLocationIdIsNull( UUID accountId, Pageable pageable );

    List<Summary> findByAccountIdAndLocationIdIsNullAndSummaryDateBetween( UUID accountId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable );

    List<Summary> findByAccountIdAndTransactionTypeAndLocationIdIsNull( UUID accountId, TransactionType transactionType, Pageable pageable );

    List<Summary> findByAccountIdAndTransactionTypeAndLocationIdIsNullAndSummaryDateBetween( UUID accountId, TransactionType transactionType, LocalDateTime fromDate,
        LocalDateTime toDate,
        Pageable pageable );

    List<Summary> findAllByLocationId( UUID locId, Pageable pageable );

    List<Summary> findByLocationIdAndSummaryDateBetween( UUID locId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable );

    List<Summary> findByLocationIdAndTransactionType( UUID locId, TransactionType transactionType, Pageable pageable );

    List<Summary> findByLocationIdAndTransactionTypeAndSummaryDateBetween( UUID locId, TransactionType transactionType, LocalDateTime fromDate, LocalDateTime toDate,
        Pageable pageable );

}
