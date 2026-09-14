-- DANGER: removes all LJT RouteFlow database objects and data.
-- Run only when intentionally resetting the development schema.

SET ECHO ON
SET SQLBLANKLINES ON
SET DEFINE OFF
SET FEEDBACK ON
SET VERIFY OFF
SET SERVEROUTPUT ON

BEGIN
    FOR item IN (
        SELECT 'DROP TABLE ' || table_name || ' CASCADE CONSTRAINTS PURGE' ddl
        FROM user_tables
        WHERE table_name IN (
            'DMS_ROLE_CHANGE_REQUEST',
            'DMS_NOTIFICATION',
            'DMS_DOCUMENT_HISTORY',
            'DMS_APPROVAL',
            'DMS_DOCUMENT_FILE',
            'DMS_DOCUMENT',
            'DMS_USER_ROLE',
            'DMS_USER',
            'DMS_ROLE',
            'DMS_DEPARTMENT'
        )
        ORDER BY CASE table_name
            WHEN 'DMS_ROLE_CHANGE_REQUEST' THEN 1
            WHEN 'DMS_NOTIFICATION' THEN 2
            WHEN 'DMS_DOCUMENT_HISTORY' THEN 3
            WHEN 'DMS_APPROVAL' THEN 4
            WHEN 'DMS_DOCUMENT_FILE' THEN 5
            WHEN 'DMS_DOCUMENT' THEN 6
            WHEN 'DMS_USER_ROLE' THEN 7
            WHEN 'DMS_USER' THEN 8
            WHEN 'DMS_ROLE' THEN 9
            WHEN 'DMS_DEPARTMENT' THEN 10
        END
    ) LOOP
        EXECUTE IMMEDIATE item.ddl;
        DBMS_OUTPUT.PUT_LINE(item.ddl);
    END LOOP;
END;
/

PURGE RECYCLEBIN;

PROMPT LJT RouteFlow schema objects removed.
