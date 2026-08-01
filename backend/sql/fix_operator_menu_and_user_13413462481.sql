-- ============================================================
-- Phase2: operator menu parent + user 13413462481 identity fix
-- MUST: mysql --default-character-set=utf8mb4 < this file
-- ============================================================
SET NAMES utf8mb4;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, 2000
FROM sys_role r
WHERE r.del_flag = '0'
  AND r.role_key IN ('fire_dept_admin', 'project_manager', 'team_leader', 'fire_operator', 'dept_staff', 'auditor')
  AND EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_id = 2000)
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = 2000
  );

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'fire_operator' AND r.del_flag = '0'
  AND m.perms IN (
    'fire:task:view','fire:task:list','fire:task:query','fire:task:detail',
    'fire:report:view','fire:report:list',
    'fire:repair:view','fire:repair:list',
    'fire:checkIn:view','fire:checkIn:list'
  )
  AND IFNULL(m.visible,'0') = '0'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

-- keep menu_id=2134 title shared; personal scope is enforced in backend

UPDATE sys_user
   SET user_name = login_name,
       remark = 'wx_nick restored; display uses login_name',
       allow_admin_login = '1',
       allow_mini_login = '1',
       audit_status = '0',
       status = '0',
       del_flag = '0'
WHERE user_id = 16
  AND (login_name = '13413462481' OR phonenumber = '13413462481');

-- dept_id=112 = outer maintenance team (seeded by phase-b org)
UPDATE sys_user SET dept_id = 112
WHERE user_id = 16
  AND EXISTS (SELECT 1 FROM sys_dept WHERE dept_id = 112 AND del_flag = '0');

DELETE ur FROM sys_user_role ur
WHERE ur.user_id = 16;

INSERT INTO sys_user_role (user_id, role_id)
SELECT 16, r.role_id
FROM sys_role r
WHERE r.role_key = 'fire_operator' AND r.del_flag = '0'
LIMIT 1;

SET @wb_post := (SELECT post_id FROM sys_post WHERE post_code = 'wb_member' LIMIT 1);
DELETE FROM sys_user_post WHERE user_id = 16;
INSERT INTO sys_user_post (user_id, post_id)
SELECT 16, @wb_post FROM DUAL WHERE @wb_post IS NOT NULL;

UPDATE sys_user SET del_flag = '2'
WHERE user_id = 15 AND login_name = '13413462481';
