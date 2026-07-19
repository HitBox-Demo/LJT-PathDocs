SELECT 'Departments' item, COUNT(*) total FROM dms_department
UNION ALL SELECT 'Roles',COUNT(*) FROM dms_role
UNION ALL SELECT 'Users',COUNT(*) FROM dms_user
UNION ALL SELECT 'User Roles',COUNT(*) FROM dms_user_role
UNION ALL SELECT 'Documents',COUNT(*) FROM dms_document;

SELECT u.username,u.full_name,d.department_name,LISTAGG(r.role_code,', ') WITHIN GROUP(ORDER BY r.role_code) roles
FROM dms_user u LEFT JOIN dms_department d ON d.department_id=u.department_id
JOIN dms_user_role ur ON ur.user_id=u.user_id JOIN dms_role r ON r.role_id=ur.role_id
GROUP BY u.username,u.full_name,d.department_name ORDER BY u.username;
