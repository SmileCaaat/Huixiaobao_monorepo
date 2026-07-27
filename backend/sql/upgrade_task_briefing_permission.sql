-- Split fire:task:briefing from fire:task:edit (idempotent, role_key based, no hardcoded role_id)
SET NAMES utf8mb4;

-- menu_name = UTF-8 "ά������д" via UNHEX to avoid client encoding issues
INSERT INTO sys_menu (
    menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh,
    perms, icon, create_by, create_time, remark
)
SELECT
    CONVERT(UNHEX('E7BBB4E4BF9DE7AE80E68AA5E5A1ABE58699') USING utf8mb4),
    p.menu_id,
    20,
    '#',
    '',
    'F',
    '0',
    '1',
    'fire:task:briefing',
    '#',
    'admin',
    NOW(),
    'briefing permission split from fire:task:edit'
FROM sys_menu p
WHERE p.perms = 'fire:task:view'
  AND p.menu_type = 'C'
  AND IFNULL(p.visible, '0') = '0'
  AND NOT EXISTS (
        SELECT 1 FROM sys_menu x
        WHERE x.parent_id = p.menu_id AND x.perms = 'fire:task:briefing'
  );

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.del_flag = '0'
  AND r.role_key = 'maintenance_member'
  AND m.perms = 'fire:task:briefing'
  AND NOT EXISTS (
        SELECT 1 FROM sys_role_menu rm
        WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.del_flag = '0'
  AND r.role_key IN ('project_manager', 'team_leader')
  AND m.perms = 'fire:task:briefing'
  AND NOT EXISTS (
        SELECT 1 FROM sys_role_menu rm
        WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.del_flag = '0'
  AND r.role_key = 'admin'
  AND m.perms = 'fire:task:briefing'
  AND NOT EXISTS (
        SELECT 1 FROM sys_role_menu rm
        WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

-- Notes:
-- 1) userId=1 super-admin usually bypasses menu checks in RuoYi.
-- 2) Does not revoke existing fire:task:edit / fire:task:remove grants.
-- 3) For other employee role_keys, grant fire:task:briefing the same way (do not guess role_id).
