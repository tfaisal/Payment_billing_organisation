UPDATE payment_method_account SET pma_trans_type='STATEMENT' WHERE inv_id is not null and st_id is not null and pay_id is null and stlm_id is null and adj_id is null;
UPDATE payment_method_account SET pma_trans_type='STATEMENT' WHERE st_id is not null AND inv_id is null and pay_id is null and stlm_id is null and adj_id is null;
UPDATE payment_method_account SET pma_trans_type='PAYMENT' WHERE st_id is not null AND inv_id is null and pay_id is not null and stlm_id is null and adj_id is null;
UPDATE account_summary SET acsu_trans_type='PAYMENT' WHERE st_id is not null AND inv_id is null and pay_id is not null and stlm_id is not null and adj_id is null;
UPDATE account_summary SET acsu_trans_type='ADJUSTMENT' WHERE st_id is not null AND inv_id is null and pay_id is not null and stlm_id is not null and adj_id is not null;
UPDATE account_summary SET acsu_trans_type='SETTLEMENT' WHERE st_id is null AND inv_id is null and pay_id is null and stlm_id is not null and adj_id is null;
UPDATE payment_method_account SET pma_trans_type='PAYMENT' WHERE st_id is null AND inv_id is not null and pay_id is not null and stlm_id is null and adj_id is null;
UPDATE account_summary SET acsu_trans_type='INVOICE' WHERE st_id is  null AND inv_id is not null and pay_id is  null and stlm_id is  null and adj_id is  null;
UPDATE account_summary SET acsu_trans_type='PAYMENT' WHERE st_id is not null AND inv_id is  null and pay_id is not null and stlm_id is null and adj_id is null;
UPDATE account_summary SET acsu_trans_type='STATEMENT' WHERE st_id is not null AND inv_id is  null and pay_id is null and stlm_id is null and adj_id is null;
UPDATE account_summary SET acsu_trans_type='INVOICE' WHERE st_id is not null AND inv_id is not null and pay_id is  null and stlm_id is  null and adj_id is  null;
UPDATE account_summary SET acsu_trans_type='ADJUSTMENT' WHERE st_id is not null AND inv_id is null and pay_id is not null and stlm_id is  null and adj_id is not null;
UPDATE account_summary SET acsu_trans_type='PAYMENT' WHERE st_id is  null AND inv_id is  not null and pay_id is not null and stlm_id is null and adj_id is null;
UPDATE payment_method_account SET pma_trans_type='INVOICE' WHERE inv_id is not null and st_id is  null and pay_id is null and stlm_id is null and adj_id is null;
UPDATE account_summary SET acsu_trans_type='PAYMENT' WHERE st_id is not null AND inv_id is not null and pay_id is not null and stlm_id is null and adj_id is null;
UPDATE account_summary SET acsu_trans_type='PAYMENT' WHERE st_id is  null AND inv_id is  null and pay_id is not null and stlm_id is null and adj_id is null;
UPDATE account_summary SET acsu_trans_type='DEPOSIT' WHERE st_id is null AND inv_id is  null and pay_id is not null and stlm_id is not null and adj_id is null;