-- LJT RouteFlow - Schema verification

SET ECHO ON
SET SQLBLANKLINES ON
SET DEFINE OFF
SET FEEDBACK ON
SET VERIFY OFF
SET SERVEROUTPUT ON
SET LINESIZE 220
SET PAGESIZE 100
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

PROMPT === Connection ===

SELECT
    SYS_CONTEXT('USERENV', 'CURRENT_USER') AS current_user,
    SYS_CONTEXT('USERENV', 'CON_NAME') AS container_name,
    SYS_CONTEXT('USERENV', 'SERVICE_NAME') AS service_name
FROM dual;

PROMPT === Row counts ===

SELECT 'Departments' item, COUNT(*) total FROM dms_department
UNION ALL SELECT 'Roles', COUNT(*) FROM dms_role
UNION ALL SELECT 'Users', COUNT(*) FROM dms_user
UNION ALL SELECT 'User Roles', COUNT(*) FROM dms_user_role
UNION ALL SELECT 'Documents', COUNT(*) FROM dms_document
UNION ALL SELECT 'Files', COUNT(*) FROM dms_document_file
UNION ALL SELECT 'Approvals', COUNT(*) FROM dms_approval
UNION ALL SELECT 'History', COUNT(*) FROM dms_document_history
UNION ALL SELECT 'Notifications', COUNT(*) FROM dms_notification
UNION ALL SELECT 'Role Requests', COUNT(*) FROM dms_role_change_request;

PROMPT === Users and roles ===

SELECT
    u.username,
    u.full_name,
    d.department_name,
    LISTAGG(r.role_code, ', ')
        WITHIN GROUP (ORDER BY r.role_code) AS roles
FROM dms_user u
LEFT JOIN dms_department d
    ON d.department_id = u.department_id
JOIN dms_user_role ur
    ON ur.user_id = u.user_id
JOIN dms_role r
    ON r.role_id = ur.role_id
GROUP BY
    u.username,
    u.full_name,
    d.department_name
ORDER BY u.username;

PROMPT === Invalid foreign keys or disabled constraints ===

SELECT
    constraint_name,
    table_name,
    constraint_type,
    status,
    validated
FROM user_constraints
WHERE table_name LIKE 'DMS_%'
  AND (status <> 'ENABLED' OR validated <> 'VALIDATED')
ORDER BY table_name, constraint_name;

PROMPT === Required objects ===

SELECT object_type, object_name, status
FROM user_objects
WHERE object_name LIKE 'DMS_%'
   OR object_name LIKE 'TRG_DMS_%'
ORDER BY object_type, object_name;

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM dms_department;
    IF v_count < 3 THEN
        RAISE_APPLICATION_ERROR(-20101, 'Expected at least 3 departments.');
    END IF;

    SELECT COUNT(*) INTO v_count FROM dms_role;
    IF v_count < 4 THEN
        RAISE_APPLICATION_ERROR(-20102, 'Expected at least 4 roles.');
    END IF;

    SELECT COUNT(*) INTO v_count FROM dms_user;
    IF v_count < 5 THEN
        RAISE_APPLICATION_ERROR(-20103, 'Expected at least 5 users.');
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM dms_user u
    JOIN dms_user_role ur ON ur.user_id = u.user_id
    JOIN dms_role r ON r.role_id = ur.role_id
    WHERE u.username = 'clerk'
      AND r.role_code IN ('CLERK', 'SYSTEM_ADMIN');

    IF v_count <> 2 THEN
        RAISE_APPLICATION_ERROR(
            -20104,
            'The clerk account must have CLERK and SYSTEM_ADMIN roles.'
        );
    END IF;

    DBMS_OUTPUT.PUT_LINE('Verification checks passed.');
END;
/

PROMPT Schema verification completed successfully.
