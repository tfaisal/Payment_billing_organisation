package com.abcfinancial.api.billing.generalledger.statements.repository;

import com.abcfinancial.api.billing.subscriptionmanagement.account.domain.Account;
import com.abcfinancial.api.billing.generalledger.statements.domain.Statement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StatementRepository extends JpaRepository<Statement, UUID>
{
    List<Statement> findStatementsByAccountId( Account account, Pageable pageable );

    Statement findStatementByAccountIdAccountIdAndDeactivatedAndPaymentMethodNull( UUID account, LocalDateTime deactivaDate );

    Optional<Statement> findByStatementIdAndAccountId( UUID statementId, Account account );

    Statement findStatementByPaymentMethodIdAndDeactivated( UUID paymentMethodId, LocalDateTime deactivaDate );

    Optional<Statement> findByStatementId( UUID statementId );

}
