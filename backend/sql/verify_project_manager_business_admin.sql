-- =============================================================================
-- ??????????????????????????
-- =============================================================================
SET NAMES utf8mb4;

SET @pm_role_id := (
    SELECT role_id FROM sys_role
    WHERE role_key = 'project_manager' AND del_flag = '0'
    ORDER BY role_id
    LIMIT 1
);

-- 1) ???????
SELECT role_id, role_key, role_name, status, del_flag, data_scope, remark
FROM sys_role
WHERE role_key = 'project_manager' AND del_flag = '0';
-- ??????status=0, del_flag=0, data_scope=1

-- 2) ??????????????��?????
SELECT required.perm AS required_perm,
       CASE WHEN m.menu_id IS NULL THEN 'MISSING' ELSE 'OK' END AS grant_status
FROM (
    SELECT 'fire:company:view' AS perm UNION ALL SELECT 'fire:company:list' UNION ALL
    SELECT 'fire:company:add' UNION ALL SELECT 'fire:company:edit' UNION ALL
    SELECT 'fire:company:remove' UNION ALL SELECT 'fire:company:export' UNION ALL
    SELECT 'fire:company:assign' UNION ALL
    SELECT 'fire:contract:view' UNION ALL SELECT 'fire:contract:list' UNION ALL
    SELECT 'fire:contract:add' UNION ALL SELECT 'fire:contract:edit' UNION ALL
    SELECT 'fire:contract:remove' UNION ALL SELECT 'fire:contract:export' UNION ALL
    SELECT 'fire:contract:renew' UNION ALL SELECT 'fire:contract:terminate' UNION ALL
    SELECT 'fire:inspection:view' UNION ALL SELECT 'fire:repair:view' UNION ALL
    SELECT 'fire:task:view' UNION ALL SELECT 'fire:report:view' UNION ALL
    SELECT 'fire:checkIn:view' UNION ALL
    SELECT 'fire:building:view' UNION ALL SELECT 'fire:equipment:view' UNION ALL SELECT 'fire:systemType:view' UNION ALL
    SELECT 'system:dept:view' UNION ALL SELECT 'system:dept:list' UNION ALL
    SELECT 'system:dept:add' UNION ALL SELECT 'system:dept:edit' UNION ALL
    SELECT 'system:dept:remove' UNION ALL
    SELECT 'system:dept:registerQrcode:view' UNION ALL
    SELECT 'system:dept:registerQrcode:download' UNION ALL
    SELECT 'system:dept:registerQrcode:manage' UNION ALL
    SELECT 'system:user:registerRecord' UNION ALL SELECT 'system:user:audit'
) required
LEFT JOIN sys_menu m ON m.perms = required.perm
LEFT JOIN sys_role_menu rm ON rm.menu_id = m.menu_id AND rm.role_id = @pm_role_id
ORDER BY required.perm;

SELECT required.perm
FROM (
    SELECT 'fire:company:view' AS perm UNION ALL SELECT 'fire:company:list' UNION ALL
    SELECT 'fire:company:add' UNION ALL SELECT 'fire:company:edit' UNION ALL
    SELECT 'fire:company:remove' UNION ALL SELECT 'fire:company:export' UNION ALL
    SELECT 'fire:company:assign' UNION ALL
    SELECT 'fire:contract:view' UNION ALL SELECT 'fire:contract:list' UNION ALL
    SELECT 'fire:contract:add' UNION ALL SELECT 'fire:contract:edit' UNION ALL
    SELECT 'fire:contract:remove' UNION ALL SELECT 'fire:contract:export' UNION ALL
    SELECT 'fire:contract:renew' UNION ALL SELECT 'fire:contract:terminate' UNION ALL
    SELECT 'fire:inspection:view' UNION ALL SELECT 'fire:repair:view' UNION ALL
    SELECT 'fire:task:view' UNION ALL SELECT 'fire:report:view' UNION ALL
    SELECT 'fire:checkIn:view' UNION ALL
    SELECT 'fire:building:view' UNION ALL SELECT 'fire:equipment:view' UNION ALL SELECT 'fire:systemType:view' UNION ALL
    SELECT 'system:dept:view' UNION ALL SELECT 'system:dept:list' UNION ALL
    SELECT 'system:dept:add' UNION ALL SELECT 'system:dept:edit' UNION ALL
    SELECT 'system:dept:remove' UNION ALL
    SELECT 'system:dept:registerQrcode:view' UNION ALL
    SELECT 'system:dept:registerQrcode:download' UNION ALL
    SELECT 'system:dept:registerQrcode:manage' UNION ALL
    SELECT 'system:user:registerRecord' UNION ALL SELECT 'system:user:audit'
) required
LEFT JOIN sys_menu m ON m.perms = required.perm
LEFT JOIN sys_role_menu rm ON rm.menu_id = m.menu_id AND rm.role_id = @pm_role_id
WHERE rm.menu_id IS NULL;
-- ????????????

-- 3) ?????????????????????
SELECT 'customer_contract_root' AS item,
       CASE WHEN EXISTS (
           SELECT 1
           FROM sys_role_menu rm
           INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
           WHERE rm.role_id = @pm_role_id
             AND m.menu_type = 'M' AND IFNULL(m.parent_id, 0) = 0
             AND m.menu_id IN (
                 SELECT p.menu_id
                 FROM sys_menu c
                 INNER JOIN sys_menu p ON p.menu_id = c.parent_id
                 WHERE c.perms = 'fire:company:view' AND c.menu_type = 'C'
             )
       ) THEN 'OK' ELSE 'MISSING' END AS status
UNION ALL
SELECT 'fire_mgmt_root',
       CASE WHEN EXISTS (
           SELECT 1
           FROM sys_role_menu rm
           INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
           WHERE rm.role_id = @pm_role_id AND m.menu_id IN (
               WITH RECURSIVE up_fire AS (
                   SELECT menu_id, parent_id, menu_type
                   FROM sys_menu
                   WHERE perms = 'fire:task:view' AND menu_type = 'C'
                   UNION ALL
                   SELECT p.menu_id, p.parent_id, p.menu_type
                   FROM sys_menu p INNER JOIN up_fire c ON p.menu_id = c.parent_id
               )
               SELECT menu_id FROM up_fire WHERE menu_type = 'M' AND IFNULL(parent_id, 0) = 0
           )
       ) THEN 'OK' ELSE 'MISSING' END
UNION ALL
SELECT 'system_root',
       CASE WHEN EXISTS (
           SELECT 1
           FROM sys_role_menu rm
           INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
           WHERE rm.role_id = @pm_role_id
             AND m.menu_type = 'M' AND IFNULL(m.parent_id, 0) = 0
             AND m.menu_id IN (
                 WITH RECURSIVE up_sys AS (
                     SELECT menu_id, parent_id, menu_type
                     FROM sys_menu
                     WHERE perms = 'system:dept:view' AND menu_type = 'C'
                     UNION ALL
                     SELECT p.menu_id, p.parent_id, p.menu_type
                     FROM sys_menu p INNER JOIN up_sys c ON p.menu_id = c.parent_id
                 )
                 SELECT menu_id FROM up_sys WHERE menu_type = 'M' AND IFNULL(parent_id, 0) = 0
             )
       ) THEN 'OK' ELSE 'MISSING' END
UNION ALL
SELECT 'user_mgmt_directory',
       CASE WHEN EXISTS (
           SELECT 1
           FROM sys_role_menu rm
           INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
           WHERE rm.role_id = @pm_role_id
             AND m.remark = 'user_mgmt_directory' AND m.menu_type = 'M'
       ) THEN 'OK' ELSE 'MISSING' END;

-- 4) ????????��???
SELECT m.perms, m.url, m.menu_name
FROM sys_role_menu rm
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE rm.role_id = @pm_role_id
  AND (
        m.perms = 'fire:task:completeAll'
     OR IFNULL(m.perms, '') LIKE 'system:role%'
     OR IFNULL(m.perms, '') LIKE 'system:menu%'
     OR IFNULL(m.perms, '') LIKE 'system:config%'
     OR IFNULL(m.perms, '') LIKE 'system:dict%'
     OR IFNULL(m.perms, '') LIKE 'system:post%'
     OR IFNULL(m.perms, '') LIKE 'monitor:%'
     OR IFNULL(m.perms, '') LIKE 'tool:%'
     OR m.perms = 'system:user:view'
     OR IFNULL(m.url, '') IN ('/system/role', '/system/menu', '/system/dict', '/system/config', '/system/post', '/system/user')
  );
-- expected empty

-- 5) ????? role_menu
SELECT role_id, menu_id, COUNT(*) AS cnt
FROM sys_role_menu
WHERE role_id = @pm_role_id
GROUP BY role_id, menu_id
HAVING COUNT(*) > 1;
-- ????????????

-- 6) ???????��????????????�???????????��????????
SELECT r.role_key, m.perms
FROM sys_role r
INNER JOIN sys_role_menu rm ON rm.role_id = r.role_id
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE r.role_key = 'maintenance_member'
  AND r.del_flag = '0'
  AND m.perms IN ('fire:contract:add', 'system:dept:add', 'system:role:view');
-- ?????????????????????????????????????

-- 7) ??? allow_admin_login
SELECT u.user_id, u.login_name, u.allow_admin_login, u.status, u.del_flag
FROM sys_user u
INNER JOIN sys_user_role ur ON ur.user_id = u.user_id
INNER JOIN sys_role r ON r.role_id = ur.role_id AND r.role_key = 'project_manager' AND r.del_flag = '0'
WHERE IFNULL(u.del_flag, '0') = '0';
-- ??????allow_admin_login=1
