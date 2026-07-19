-- Roles
INSERT INTO dms_role(role_code,role_name) VALUES('SYSTEM_ADMIN','System Administrator');
INSERT INTO dms_role(role_code,role_name) VALUES('CLERK','Clerk');
INSERT INTO dms_role(role_code,role_name) VALUES('BOSS','Boss / Approver');
INSERT INTO dms_role(role_code,role_name) VALUES('DEPARTMENT_USER','Department User');

-- Departments
INSERT INTO dms_department(department_code,department_name) VALUES('IT','Information Technology');
INSERT INTO dms_department(department_code,department_name) VALUES('FINANCE','Finance');
INSERT INTO dms_department(department_code,department_name) VALUES('MANAGEMENT','Management');

-- Password hashes are PBKDF2-HMAC-SHA256, 210000 iterations.
INSERT INTO dms_user(username,password_hash,full_name,email,status)
VALUES('clerk','pbkdf2_sha256$210000$Y2xlcmstc2VlZC1zYWx0MQ==$FS86yqtytEpR6EpYelLrAIiKmcWu0P21Set68J5Zbms=','Ahmad Fadzli','ahmad.fadzli@agency.gov.my','ACTIVE');
INSERT INTO dms_user(username,password_hash,full_name,email,status)
VALUES('boss','pbkdf2_sha256$210000$Ym9zcy1zZWVkLXNhbHQxMg==$zpsygi16dNhuyA5fT3u+tODyLHRqLRNb2UCVerBdTz8=','En. Razali Osman','razali@agency.gov.my','ACTIVE');
INSERT INTO dms_user(username,password_hash,full_name,email,department_id,status)
SELECT 'it.user','pbkdf2_sha256$210000$ZGVwdC1zZWVkLXNhbHQxMg==$5E1PzK3vN4X35u5JvdtO5RiJlrTY0LX3sdwnNHNguqw=','Nur Izzati','izzati.it@agency.gov.my',department_id,'ACTIVE' FROM dms_department WHERE department_code='IT';
INSERT INTO dms_user(username,password_hash,full_name,email,department_id,status)
SELECT 'finance.user','pbkdf2_sha256$210000$ZGVwdC1zZWVkLXNhbHQxMg==$5E1PzK3vN4X35u5JvdtO5RiJlrTY0LX3sdwnNHNguqw=','Siti Rahimah','siti.finance@agency.gov.my',department_id,'ACTIVE' FROM dms_department WHERE department_code='FINANCE';
INSERT INTO dms_user(username,password_hash,full_name,email,department_id,status)
SELECT 'management.user','pbkdf2_sha256$210000$ZGVwdC1zZWVkLXNhbHQxMg==$5E1PzK3vN4X35u5JvdtO5RiJlrTY0LX3sdwnNHNguqw=','Pn. Norhaslinda','norhaslinda@agency.gov.my',department_id,'ACTIVE' FROM dms_department WHERE department_code='MANAGEMENT';

INSERT INTO dms_user_role SELECT u.user_id,r.role_id,SYSTIMESTAMP FROM dms_user u,dms_role r WHERE u.username='clerk' AND r.role_code IN('CLERK','SYSTEM_ADMIN');
INSERT INTO dms_user_role SELECT u.user_id,r.role_id,SYSTIMESTAMP FROM dms_user u,dms_role r WHERE u.username='boss' AND r.role_code='BOSS';
INSERT INTO dms_user_role SELECT u.user_id,r.role_id,SYSTIMESTAMP FROM dms_user u,dms_role r WHERE u.username IN('it.user','finance.user','management.user') AND r.role_code='DEPARTMENT_USER';
INSERT INTO dms_user_role SELECT u.user_id,r.role_id,SYSTIMESTAMP FROM dms_user u,dms_role r WHERE u.username='management.user' AND r.role_code='BOSS';
INSERT INTO dms_user_role SELECT u.user_id,r.role_id,SYSTIMESTAMP FROM dms_user u,dms_role r WHERE u.username IN('finance.user','management.user') ;

-- Sample metadata records. File paths are intentionally omitted; create/upload real files through the application.
INSERT INTO dms_document(document_code,title,reference_no,sender,date_received,category,priority,description,destination_department_id,boss_id,status,created_by,submitted_at,due_date)
SELECT 'DOC-2026-0001','Procurement Request - Server Upgrade','IT/PR/2026/001','Jabatan IT',DATE '2026-07-10','Procurement','URGENT','N','Sample migrated record.',d.department_id,b.user_id,'PENDING_APPROVAL',c.user_id,TIMESTAMP '2026-07-10 10:00:00',DATE '2026-07-12'
FROM dms_department d,dms_user b,dms_user c WHERE d.department_code='IT' AND b.username='boss' AND c.username='clerk';

INSERT INTO dms_document(document_code,title,reference_no,sender,date_received,category,priority,description,destination_department_id,boss_id,status,created_by,submitted_at,due_date,rejection_reason)
SELECT 'DOC-2026-0002','Office Renovation Quotation','FAC/QU/2026/002','Syarikat Bina Maju',DATE '2026-07-11','Facilities','NORMAL','N','Sample returned record.',d.department_id,b.user_id,'RETURNED_FOR_CORRECTION',c.user_id,TIMESTAMP '2026-07-11 09:00:00',DATE '2026-07-16','Missing page 3 of quotation.'
FROM dms_department d,dms_user b,dms_user c WHERE d.department_code='MANAGEMENT' AND b.username='management.user' AND c.username='clerk';

COMMIT;
