-- Rollback menu split (encoding-safe). Restores C-under-C structure.
SET NAMES utf8mb4;

UPDATE sys_menu u
SET
  u.menu_name = CONVERT(UNHEX('E794A8E688B7E7AEA1E79086') USING utf8mb4),
  u.parent_id = 1,
  u.url = '/system/user',
  u.menu_type = 'C',
  u.perms = 'system:user:view',
  u.icon = 'fa fa-user-o'
WHERE u.perms = 'system:user:view'
  AND u.menu_type = 'C'
  AND u.url = '/system/user';

UPDATE sys_menu r
INNER JOIN sys_menu u ON u.perms = 'system:user:view' AND u.menu_type = 'C' AND u.url = '/system/user'
SET
  r.parent_id = u.menu_id,
  r.order_num = 12,
  r.url = '/system/user/registerRecord',
  r.menu_type = 'C',
  r.perms = 'system:user:registerRecord'
WHERE r.perms = 'system:user:registerRecord';

UPDATE sys_menu a
INNER JOIN sys_menu u ON u.perms = 'system:user:view' AND u.menu_type = 'C' AND u.url = '/system/user'
SET a.parent_id = u.menu_id,
    a.order_num = 11
WHERE a.perms = 'system:user:audit' AND a.menu_type = 'F';

DELETE rm FROM sys_role_menu rm
INNER JOIN sys_menu d ON d.menu_id = rm.menu_id
WHERE d.remark = 'user_mgmt_directory' AND d.menu_type = 'M';

DELETE FROM sys_menu
WHERE remark = 'user_mgmt_directory' AND menu_type = 'M';

SELECT menu_id, menu_name, parent_id, url, menu_type, perms
FROM sys_menu
WHERE perms IN ('system:user:view', 'system:user:registerRecord', 'system:user:audit')
ORDER BY parent_id, order_num, menu_id;
