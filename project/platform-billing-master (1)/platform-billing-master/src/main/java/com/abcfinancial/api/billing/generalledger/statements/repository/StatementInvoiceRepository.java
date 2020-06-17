package com.abcfinancial.api.billing.generalledger.statements.repository;

import com.abcfinancial.api.billing.generalledger.statements.domain.StatementInvoice;
import com.abcfinancial.api.billing.generalledger.statements.domain.StatementInvoiceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StatementInvoiceRepository extends JpaRepository<StatementInvoice, StatementInvoiceId>
{
    
    @Query( value = "select * from statement_invoice WHERE st_id  = :statementId", nativeQuery = true )
    public List<StatementInvoice> findStatementInvoiceByStatementId( @Param( "statementId" ) UUID statementId );
}
