package com.abcfinancial.api.billing.subscriptionmanagement.account.repository;

import com.abcfinancial.api.billing.generalledger.statements.domain.Statement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository

public interface AccountStatementRepository extends JpaRepository<Statement, UUID>
{

    @Query( value = "select * from statement where st_id = :st_id", nativeQuery = true )
    Statement getStatementDataByStatementId( @Param( "st_id" ) UUID statementId );

    @Query( value = "select * from statement where accn_id = :accn_id", nativeQuery = true )
    List<Statement> getStatementByAccountId( @Param( "accn_id" ) UUID accountId );

    List<Statement> findByAccountIdAccountIdAndStmtDateBetween( UUID accountId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable );

    List<Statement> findByAccountId_AccountId( UUID accountId, Pageable pageable );
}
