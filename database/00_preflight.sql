-- LJT RouteFlow - Oracle XE 21c preflight
-- Connect as LJT_ROUTE_FLOW@localhost:1521/XEPDB1 before running.

SET ECHO ON
SET SQLBLANKLINES ON
SET DEFINE OFF
SET FEEDBACK ON
SET VERIFY OFF
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

DECLARE
    v_user      VARCHAR2(128) := SYS_CONTEXT('USERENV', 'CURRENT_USER');
    v_container VARCHAR2(128) := SYS_CONTEXT('USERENV', 'CON_NAME');
BEGIN
    IF UPPER(v_user) <> 'LJT_ROUTE_FLOW' THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'Wrong schema. Connect as LJT_ROUTE_FLOW, current user is ' || v_user
        );
    END IF;

    IF UPPER(v_container) <> 'XEPDB1' THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'Wrong container. Connect to XEPDB1, current container is ' || v_container
        );
    END IF;

    DBMS_OUTPUT.PUT_LINE('Preflight OK');
    DBMS_OUTPUT.PUT_LINE('Schema    : ' || v_user);
    DBMS_OUTPUT.PUT_LINE('Container : ' || v_container);
END;
/

SELECT
    SYS_CONTEXT('USERENV', 'DB_NAME')      AS database_name,
    SYS_CONTEXT('USERENV', 'SERVICE_NAME') AS service_name,
    SYS_CONTEXT('USERENV', 'CURRENT_USER') AS current_user,
    SYS_CONTEXT('USERENV', 'CON_NAME')     AS container_name
FROM dual;

PROMPT Preflight completed successfully.
