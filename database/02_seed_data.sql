-- LJT RouteFlow - Core seed data
-- Safe to rerun. Run as LJT_ROUTE_FLOW in XEPDB1.

SET ECHO ON
SET SQLBLANKLINES ON
SET DEFINE OFF
SET FEEDBACK ON
SET VERIFY OFF
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

MERGE INTO dms_role target
USING (
    SELECT 'SYSTEM_ADMIN' role_code, 'System Administrator' role_name FROM dual
    UNION ALL SELECT 'CLERK', 'Clerk' FROM dual
    UNION ALL SELECT 'BOSS', 'Boss / Approver' FROM dual
    UNION ALL SELECT 'DEPARTMENT_USER', 'Department User' FROM dual
) source
ON (target.role_code = source.role_code)
WHEN MATCHED THEN
    UPDATE SET target.role_name = source.role_name
WHEN NOT MATCHED THEN
    INSERT (role_code, role_name)
    VALUES (source.role_code, source.role_name);

MERGE INTO dms_department target
USING (
    SELECT 'IT' department_code, 'Information Technology' department_name FROM dual
    UNION ALL SELECT 'FINANCE', 'Finance' FROM dual
    UNION ALL SELECT 'MANAGEMENT', 'Management' FROM dual
) source
ON (target.department_code = source.department_code)
WHEN MATCHED THEN
    UPDATE SET
        target.department_name = source.department_name,
        target.status = 'ACTIVE'
WHEN NOT MATCHED THEN
    INSERT (department_code, department_name, status)
    VALUES (source.department_code, source.department_name, 'ACTIVE');

-- PBKDF2-HMAC-SHA256 hashes used by the current PasswordUtil.
-- Demo credentials:
-- clerk / Clerk@123
-- boss / Boss@123
-- it.user, finance.user, management.user / Dept@123

MERGE INTO dms_user target
USING (
    SELECT
        'clerk' username,
        'pbkdf2_sha256$210000$Y2xlcmstc2VlZC1zYWx0MQ==$FS86yqtytEpR6EpYelLrAIiKmcWu0P21Set68J5Zbms=' password_hash,
        'Ahmad Fadzli' full_name,
        'ahmad.fadzli@agency.gov.my' email,
        CAST(NULL AS NUMBER) department_id
    FROM dual
) source
ON (LOWER(target.username) = LOWER(source.username))
WHEN MATCHED THEN
    UPDATE SET
        target.password_hash = source.password_hash,
        target.full_name = source.full_name,
        target.email = source.email,
        target.status = 'ACTIVE'
WHEN NOT MATCHED THEN
    INSERT (
        username, password_hash, full_name, email,
        department_id, status
    )
    VALUES (
        source.username, source.password_hash, source.full_name, source.email,
        source.department_id, 'ACTIVE'
    );

MERGE INTO dms_user target
USING (
    SELECT
        'boss' username,
        'pbkdf2_sha256$210000$Ym9zcy1zZWVkLXNhbHQxMg==$zpsygi16dNhuyA5fT3u+tODyLHRqLRNb2UCVerBdTz8=' password_hash,
        'En. Razali Osman' full_name,
        'razali@agency.gov.my' email,
        CAST(NULL AS NUMBER) department_id
    FROM dual
) source
ON (LOWER(target.username) = LOWER(source.username))
WHEN MATCHED THEN
    UPDATE SET
        target.password_hash = source.password_hash,
        target.full_name = source.full_name,
        target.email = source.email,
        target.status = 'ACTIVE'
WHEN NOT MATCHED THEN
    INSERT (
        username, password_hash, full_name, email,
        department_id, status
    )
    VALUES (
        source.username, source.password_hash, source.full_name, source.email,
        source.department_id, 'ACTIVE'
    );

MERGE INTO dms_user target
USING (
    SELECT
        'it.user' username,
        'pbkdf2_sha256$210000$ZGVwdC1zZWVkLXNhbHQxMg==$5E1PzK3vN4X35u5JvdtO5RiJlrTY0LX3sdwnNHNguqw=' password_hash,
        'Nur Izzati' full_name,
        'izzati.it@agency.gov.my' email,
        department_id
    FROM dms_department
    WHERE department_code = 'IT'
) source
ON (LOWER(target.username) = LOWER(source.username))
WHEN MATCHED THEN
    UPDATE SET
        target.password_hash = source.password_hash,
        target.full_name = source.full_name,
        target.email = source.email,
        target.department_id = source.department_id,
        target.status = 'ACTIVE'
WHEN NOT MATCHED THEN
    INSERT (
        username, password_hash, full_name, email,
        department_id, status
    )
    VALUES (
        source.username, source.password_hash, source.full_name, source.email,
        source.department_id, 'ACTIVE'
    );

MERGE INTO dms_user target
USING (
    SELECT
        'finance.user' username,
        'pbkdf2_sha256$210000$ZGVwdC1zZWVkLXNhbHQxMg==$5E1PzK3vN4X35u5JvdtO5RiJlrTY0LX3sdwnNHNguqw=' password_hash,
        'Siti Rahimah' full_name,
        'siti.finance@agency.gov.my' email,
        department_id
    FROM dms_department
    WHERE department_code = 'FINANCE'
) source
ON (LOWER(target.username) = LOWER(source.username))
WHEN MATCHED THEN
    UPDATE SET
        target.password_hash = source.password_hash,
        target.full_name = source.full_name,
        target.email = source.email,
        target.department_id = source.department_id,
        target.status = 'ACTIVE'
WHEN NOT MATCHED THEN
    INSERT (
        username, password_hash, full_name, email,
        department_id, status
    )
    VALUES (
        source.username, source.password_hash, source.full_name, source.email,
        source.department_id, 'ACTIVE'
    );

MERGE INTO dms_user target
USING (
    SELECT
        'management.user' username,
        'pbkdf2_sha256$210000$ZGVwdC1zZWVkLXNhbHQxMg==$5E1PzK3vN4X35u5JvdtO5RiJlrTY0LX3sdwnNHNguqw=' password_hash,
        'Pn. Norhaslinda' full_name,
        'norhaslinda@agency.gov.my' email,
        department_id
    FROM dms_department
    WHERE department_code = 'MANAGEMENT'
) source
ON (LOWER(target.username) = LOWER(source.username))
WHEN MATCHED THEN
    UPDATE SET
        target.password_hash = source.password_hash,
        target.full_name = source.full_name,
        target.email = source.email,
        target.department_id = source.department_id,
        target.status = 'ACTIVE'
WHEN NOT MATCHED THEN
    INSERT (
        username, password_hash, full_name, email,
        department_id, status
    )
    VALUES (
        source.username, source.password_hash, source.full_name, source.email,
        source.department_id, 'ACTIVE'
    );

MERGE INTO dms_user_role target
USING (
    SELECT u.user_id, r.role_id
    FROM dms_user u
    CROSS JOIN dms_role r
    WHERE (
        (u.username = 'clerk' AND r.role_code IN ('CLERK', 'SYSTEM_ADMIN'))
        OR (u.username = 'boss' AND r.role_code = 'BOSS')
        OR (
            u.username IN ('it.user', 'finance.user', 'management.user')
            AND r.role_code = 'DEPARTMENT_USER'
        )
        OR (u.username = 'management.user' AND r.role_code = 'BOSS')
    )
) source
ON (
    target.user_id = source.user_id
    AND target.role_id = source.role_id
)
WHEN NOT MATCHED THEN
    INSERT (user_id, role_id)
    VALUES (source.user_id, source.role_id);

COMMIT;

PROMPT Core seed data completed successfully.
