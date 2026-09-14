-- LJT RouteFlow - Cleanup after an interrupted/failed installation
-- Safe to run when no RouteFlow objects exist.
-- Run as LJT_ROUTE_FLOW in XEPDB1.

SET ECHO ON
SET SQLBLANKLINES ON
SET DEFINE OFF
SET FEEDBACK ON
SET VERIFY OFF
SET SERVEROUTPUT ON

DECLARE
    PROCEDURE drop_table_if_exists(
        p_table_name VARCHAR2
    ) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*)
        INTO v_count
        FROM user_tables
        WHERE table_name = UPPER(p_table_name);

        IF v_count > 0 THEN
            EXECUTE IMMEDIATE
                'DROP TABLE ' || DBMS_ASSERT.SIMPLE_SQL_NAME(p_table_name)
                || ' CASCADE CONSTRAINTS PURGE';

            DBMS_OUTPUT.PUT_LINE(
                'Dropped table: ' || UPPER(p_table_name)
            );
        ELSE
            DBMS_OUTPUT.PUT_LINE(
                'Table not present: ' || UPPER(p_table_name)
            );
        END IF;
    END;
BEGIN
    drop_table_if_exists('DMS_ROLE_CHANGE_REQUEST');
    drop_table_if_exists('DMS_NOTIFICATION');
    drop_table_if_exists('DMS_DOCUMENT_HISTORY');
    drop_table_if_exists('DMS_APPROVAL');
    drop_table_if_exists('DMS_DOCUMENT_FILE');
    drop_table_if_exists('DMS_DOCUMENT');
    drop_table_if_exists('DMS_USER_ROLE');
    drop_table_if_exists('DMS_USER');
    drop_table_if_exists('DMS_ROLE');
    drop_table_if_exists('DMS_DEPARTMENT');
END;
/

BEGIN
    FOR trigger_record IN (
        SELECT trigger_name
        FROM user_triggers
        WHERE trigger_name IN (
            'TRG_DMS_USER_UPDATED_AT',
            'TRG_DMS_DOCUMENT_UPDATED_AT'
        )
    ) LOOP
        EXECUTE IMMEDIATE
            'DROP TRIGGER '
            || DBMS_ASSERT.SIMPLE_SQL_NAME(
                trigger_record.trigger_name
            );

        DBMS_OUTPUT.PUT_LINE(
            'Dropped trigger: ' || trigger_record.trigger_name
        );
    END LOOP;
END;
/

PURGE RECYCLEBIN;

PROMPT Cleanup completed. The schema is ready for a fresh installation.
