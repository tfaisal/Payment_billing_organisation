-- SEQUENCE EMULATION && UUID OPTIMIZATION/REORDERING FUNCTIONS
CREATE TABLE sequence_data
(
  sequence_name      varchar(100) NOT NULL,
  sequence_increment int unsigned NOT NULL DEFAULT 1,
  sequence_min_value bigint unsigned NOT NULL DEFAULT 1,
  sequence_max_value bigint unsigned NOT NULL DEFAULT 18446744073709551615,
  sequence_cur_value bigint unsigned DEFAULT 1,
  sequence_cycle     boolean      NOT NULL DEFAULT FALSE,
  PRIMARY KEY (sequence_name)
) ENGINE=InnoDB|

CREATE FUNCTION nextval (seq_name VARCHAR(100))
RETURNS BIGINT UNSIGNED NOT DETERMINISTIC
BEGIN
    DECLARE cur_val BIGINT UNSIGNED;

    SELECT
        sequence_cur_value INTO cur_val
    FROM
        sequence_data
    WHERE
        sequence_name = seq_name
    FOR UPDATE;

    IF cur_val IS NOT NULL THEN
        UPDATE
            sequence_data
        SET
            sequence_cur_value = IF (
                (sequence_cur_value + sequence_increment) > sequence_max_value,
                IF (
                    sequence_cycle = TRUE,
                    sequence_min_value,
                    NULL
                ),
                sequence_cur_value + sequence_increment
            )
        WHERE
            sequence_name = seq_name
        ;
    END IF;

    RETURN cur_val;
END
|

CREATE FUNCTION uuid_swap_bin(_uuid VARBINARY(36))
        RETURNS VARBINARY(16)
        LANGUAGE SQL  DETERMINISTIC  CONTAINS SQL  SQL SECURITY INVOKER
    RETURN
        UNHEX(CONCAT(
            SUBSTR(_uuid, 15, 4),
            SUBSTR(_uuid, 10, 4),
            SUBSTR(_uuid,  1, 8),
            SUBSTR(_uuid, 20, 4),
            SUBSTR(_uuid, 25) ));
|

CREATE FUNCTION bin_swap_uuid(_bin VARBINARY(16))
        RETURNS VARBINARY(36)
        LANGUAGE SQL  DETERMINISTIC  CONTAINS SQL  SQL SECURITY INVOKER
    RETURN
        LCASE(CONCAT_WS('-',
            HEX(SUBSTR(_bin,  5, 4)),
            HEX(SUBSTR(_bin,  3, 2)),
            HEX(SUBSTR(_bin,  1, 2)),
            HEX(SUBSTR(_bin,  9, 2)),
            HEX(SUBSTR(_bin, 11))
                 ));

|

CREATE FUNCTION uuid_bin(_uuid VARBINARY(36))
  RETURNS VARBINARY(16)
  LANGUAGE SQL  DETERMINISTIC  CONTAINS SQL  SQL SECURITY INVOKER
RETURN
  UNHEX(REPLACE(_uuid, '-', ''));
|

CREATE FUNCTION bin_uuid(_bin VARBINARY(16))
  RETURNS VARBINARY(36)
  LANGUAGE SQL  DETERMINISTIC  CONTAINS SQL  SQL SECURITY INVOKER
RETURN
  LOWER(CONCAT(
      SUBSTR(HEX(_bin), 1, 8), '-',
      SUBSTR(HEX(_bin), 9, 4), '-',
      SUBSTR(HEX(_bin), 13, 4), '-',
      SUBSTR(HEX(_bin), 17, 4), '-',
      SUBSTR(HEX(_bin), 21)
    ));

|