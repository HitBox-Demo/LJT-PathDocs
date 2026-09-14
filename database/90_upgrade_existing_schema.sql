-- LJT RouteFlow - Upgrade an older schema to the final constraints
-- Use only if the old 1.2.0 schema has already been created.

SET ECHO ON
SET SQLBLANKLINES ON
SET DEFINE OFF
SET FEEDBACK ON
SET VERIFY OFF
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM user_tab_columns
    WHERE table_name = 'DMS_DOCUMENT'
      AND column_name = 'SUBMISSION_KEY';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE dms_document ADD submission_key VARCHAR2(64)';
        EXECUTE IMMEDIATE
            'ALTER TABLE dms_document ADD CONSTRAINT uq_dms_document_submission UNIQUE (submission_key)';
        DBMS_OUTPUT.PUT_LINE('Added DMS_DOCUMENT.SUBMISSION_KEY.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('DMS_DOCUMENT.SUBMISSION_KEY already exists.');
    END IF;
END;
/

DECLARE
    PROCEDURE create_index_if_missing(
        p_index_name VARCHAR2,
        p_sql        VARCHAR2
    ) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*)
        INTO v_count
        FROM user_indexes
        WHERE index_name = UPPER(p_index_name);

        IF v_count = 0 THEN
            EXECUTE IMMEDIATE p_sql;
            DBMS_OUTPUT.PUT_LINE('Created index ' || p_index_name);
        ELSE
            DBMS_OUTPUT.PUT_LINE('Index already exists: ' || p_index_name);
        END IF;
    END;
BEGIN
    create_index_if_missing(
        'UQ_DMS_USER_USERNAME_CI',
        'CREATE UNIQUE INDEX uq_dms_user_username_ci ON dms_user (LOWER(username))'
    );

    create_index_if_missing(
        'UQ_DMS_USER_EMAIL_CI',
        'CREATE UNIQUE INDEX uq_dms_user_email_ci ON dms_user (LOWER(email))'
    );

    create_index_if_missing(
        'UQ_DMS_FILE_ONE_PRIMARY',
        q'[CREATE UNIQUE INDEX uq_dms_file_one_primary
           ON dms_document_file (
               CASE WHEN primary_flag = 'Y' THEN document_id END
           )]'
    );

    create_index_if_missing(
        'UQ_DMS_PENDING_ROLE_REQUEST',
        q'[CREATE UNIQUE INDEX uq_dms_pending_role_request
           ON dms_role_change_request (
               CASE WHEN status = 'PENDING' THEN user_id END,
               CASE WHEN status = 'PENDING' THEN requested_role_code END
           )]'
    );
END;
/

CREATE OR REPLACE TRIGGER trg_dms_user_updated_at
BEFORE UPDATE ON dms_user
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER trg_dms_document_updated_at
BEFORE UPDATE ON dms_document
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

COMMIT;

PROMPT Older schema upgraded successfully.
