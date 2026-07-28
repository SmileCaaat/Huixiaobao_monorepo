-- =============================================================================
-- Split user list / register record menus (idempotent, encoding-safe)
-- Root cause: C-type "registerRecord" under C-type "user" makes sidebar render
-- "用户管理" as javascript:; expander, so /system/user (org tree) is unreachable.
-- Target:
--   系统管理
--   └─ 用户管理 (M) remark=user_mgmt_directory
--      ├─ 用户列表 (C) /system/user system:user:view
--      └─ 注册记录 (C) /system/user/registerRecord system:user:registerRecord
-- =============================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_menu_bak_user_layout_20260728 AS
SELECT * FROM sys_menu WHERE 1 = 0;

INSERT INTO sys_menu_bak_user_layout_20260728
SELECT m.*
FROM sys_menu m
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu_bak_user_layout_20260728 b WHERE b.menu_id = m.menu_id
);

CREATE TABLE IF NOT EXISTS sys_role_menu_bak_user_layout_20260728 AS
SELECT * FROM sys_role_menu WHERE 1 = 0;

INSERT INTO sys_role_menu_bak_user_layout_20260728
SELECT rm.*
FROM sys_role_menu rm
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu_bak_user_layout_20260728 b
  WHERE b.role_id = rm.role_id AND b.menu_id = rm.menu_id
);

-- M directory: menu_name = 用户管理
INSERT INTO sys_menu (
  menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh,
  perms, icon, create_by, create_time, remark
)
SELECT
  CONVERT(UNHEX('E794A8E688B7E7AEA1E79086') USING utf8mb4),
  1,
  COALESCE((
    SELECT u.order_num FROM sys_menu u
    WHERE u.perms = 'system:user:view' AND u.menu_type = 'C' AND u.url = '/system/user'
    LIMIT 1
  ), 1),
  '#',
  '',
  'M',
  '0',
  '1',
  '',
  'fa fa-user-o',
  'admin',
  NOW(),
  'user_mgmt_directory'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu x WHERE x.remark = 'user_mgmt_directory' AND x.menu_type = 'M'
);

-- Rename original C page to 用户列表 and move under M
UPDATE sys_menu u
INNER JOIN sys_menu d ON d.remark = 'user_mgmt_directory' AND d.menu_type = 'M'
SET
  u.menu_name = CONVERT(UNHEX('E794A8E688B7E58897E8A1A8') USING utf8mb4),
  u.parent_id = d.menu_id,
  u.order_num = 1,
  u.url = '/system/user',
  u.menu_type = 'C',
  u.perms = 'system:user:view',
  u.icon = 'fa fa-user'
WHERE u.perms = 'system:user:view'
  AND u.menu_type = 'C'
  AND u.url = '/system/user';

-- Ensure register record exists under M (name=注册记录)
INSERT INTO sys_menu (
  menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh,
  perms, icon, create_by, create_time, remark
)
SELECT
  CONVERT(UNHEX('E6B3A8E5868CE8AEB0E5BD95') USING utf8mb4),
  d.menu_id,
  2,
  '/system/user/registerRecord',
  'menuItem',
  'C',
  '0',
  '1',
  'system:user:registerRecord',
  'fa fa-list-alt',
  'admin',
  NOW(),
  'employee register records'
FROM sys_menu d
WHERE d.remark = 'user_mgmt_directory' AND d.menu_type = 'M'
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'system:user:registerRecord');

UPDATE sys_menu r
INNER JOIN sys_menu d ON d.remark = 'user_mgmt_directory' AND d.menu_type = 'M'
SET
  r.menu_name = CONVERT(UNHEX('E6B3A8E5868CE8AEB0E5BD95') USING utf8mb4),
  r.parent_id = d.menu_id,
  r.order_num = 2,
  r.url = '/system/user/registerRecord',
  r.menu_type = 'C',
  r.target = 'menuItem',
  r.perms = 'system:user:registerRecord',
  r.icon = 'fa fa-list-alt'
WHERE r.perms = 'system:user:registerRecord';

-- Move audit button under register record page
UPDATE sys_menu a
INNER JOIN sys_menu r ON r.perms = 'system:user:registerRecord' AND r.menu_type = 'C'
SET a.parent_id = r.menu_id,
    a.order_num = 1
WHERE a.perms = 'system:user:audit' AND a.menu_type = 'F';

-- Grant parent M to roles that already have user/register menus
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, d.menu_id
FROM sys_role_menu rm
INNER JOIN sys_menu child ON child.menu_id = rm.menu_id
INNER JOIN sys_menu d ON d.remark = 'user_mgmt_directory' AND d.menu_type = 'M'
WHERE child.perms IN ('system:user:view', 'system:user:registerRecord', 'system:user:list', 'system:user:audit')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = d.menu_id
  );

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, d.menu_id
FROM sys_role r
CROSS JOIN sys_menu d
WHERE r.del_flag = '0'
  AND r.role_key IN ('admin', 'project_manager', 'team_leader')
  AND d.remark = 'user_mgmt_directory' AND d.menu_type = 'M'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu x WHERE x.role_id = r.role_id AND x.menu_id = d.menu_id
  );

SELECT menu_id, menu_name, parent_id, order_num, url, menu_type, perms, remark
FROM sys_menu
WHERE remark = 'user_mgmt_directory'
   OR perms IN ('system:user:view', 'system:user:registerRecord', 'system:user:audit')
   OR parent_id = (SELECT menu_id FROM sys_menu WHERE remark = 'user_mgmt_directory' LIMIT 1)
ORDER BY parent_id, order_num, menu_id;
