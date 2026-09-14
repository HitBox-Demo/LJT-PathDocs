-- LJT RouteFlow - Transaction smoke test
-- Inserts a temporary document and dependent records, then rolls everything back.

SET ECHO ON
SET SQLBLANKLINES ON
SET DEFINE OFF
SET FEEDBACK ON
SET VERIFY OFF
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

DECLARE
    v_department_id dms_department.department_id%TYPE;
    v_boss_id       dms_user.user_id%TYPE;
    v_clerk_id      dms_user.user_id%TYPE;
    v_document_id   dms_document.document_id%TYPE;
    v_code          VARCHAR2(60) := 'SMOKE-' || TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF3');
BEGIN
    SELECT department_id
    INTO v_department_id
    FROM dms_department
    WHERE department_code = 'IT'
      AND status = 'ACTIVE';

    SELECT user_id
    INTO v_boss_id
    FROM dms_user
    WHERE username = 'boss'
      AND status = 'ACTIVE';

    SELECT user_id
    INTO v_clerk_id
    FROM dms_user
    WHERE username = 'clerk'
      AND status = 'ACTIVE';

    INSERT INTO dms_document (
        document_code,
        submission_key,
        title,
        reference_no,
        sender,
        date_received,
        category,
        priority,
        confidential_flag,
        description,
        destination_department_id,
        boss_id,
        status,
        created_by,
        submitted_at,
        due_date
    )
    VALUES (
        v_code,
        'SMOKE-' || RAWTOHEX(SYS_GUID()),
        'Oracle transaction smoke test',
        'SMOKE/REF/001',
        'LJT RouteFlow Test',
        TRUNC(SYSDATE),
        'Test',
        'NORMAL',
        'N',
        'This row must be rolled back.',
        v_department_id,
        v_boss_id,
        'PENDING_APPROVAL',
        v_clerk_id,
        SYSTIMESTAMP,
        TRUNC(SYSDATE) + 5
    )
    RETURNING document_id INTO v_document_id;

    INSERT INTO dms_document_history (
        document_id,
        user_id,
        action,
        old_value,
        new_value,
        remarks
    )
    VALUES (
        v_document_id,
        v_clerk_id,
        'SMOKE_TEST',
        NULL,
        'PENDING_APPROVAL',
        'Rollback transaction test'
    );

    INSERT INTO dms_notification (
        recipient_id,
        type,
        message,
        document_id,
        read_flag
    )
    VALUES (
        v_boss_id,
        'SMOKE_TEST',
        v_code || ' transaction test',
        v_document_id,
        'N'
    );

    DBMS_OUTPUT.PUT_LINE(
        'Temporary document created inside transaction: ID=' || v_document_id
    );

    ROLLBACK;

    SELECT COUNT(*)
    INTO v_document_id
    FROM dms_document
    WHERE document_code = v_code;

    IF v_document_id <> 0 THEN
        RAISE_APPLICATION_ERROR(
            -20201,
            'Rollback failed: temporary document still exists.'
        );
    END IF;

    DBMS_OUTPUT.PUT_LINE('Transaction rollback test passed.');
END;
/

PROMPT Transaction smoke test completed successfully.
