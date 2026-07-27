-- Register fire:task:completeAll in sys_menu only.
-- Do NOT grant to maintenance_member / project_manager / team_leader / other normal roles.
-- System super admin (SysUser.isAdmin => userId=1) has *:*:* and passes Shiro;
-- Controller additionally enforces user.isAdmin().
SET NAMES utf8mb4;

-- menu_name = UTF-8 "ά������һ�����"
INSERT INTO sys_menu (
    menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh,
    perms, icon, create_by, create_time, remark
)
SELECT
    CONVERT(UNHEX('E7BBB4E4BF9DE4BBBBE58AA1E4B880E994AEE5AE8CE68890') USING utf8mb4),
    p.menu_id,
    21,
    '#',
    '',
    'F',
    '0',
    '1',
    'fire:task:completeAll',
    '#',
    'admin',
    NOW(),
    'completeAll restricted to system super admin (userId=1)'
FROM sys_menu p
WHERE p.perms = 'fire:task:view'
  AND p.menu_type = 'C'
  AND IFNULL(p.visible, '0') = '0'
  AND NOT EXISTS (
        SELECT 1 FROM sys_menu x
        WHERE x.parent_id = p.menu_id AND x.perms = 'fire:task:completeAll'
  );
