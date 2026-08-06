-- Optional: sync inspection/repair button perms for maintenance_member / team_leader.
-- Linked task flow (�����豸 -> Ѳ����� -> ���ϱ���) no longer requires fire:repair:add
-- in code when taskId is present; task stakeholder access is enough.
-- This script still grants the buttons for independent module use where menus exist.
-- Idempotent.

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.del_flag = '0'
  AND r.role_key IN ('maintenance_member', 'team_leader')
  AND m.perms IN (
    'fire:inspection:view',
    'fire:inspection:list',
    'fire:inspection:add',
    'fire:inspection:edit',
    'fire:repair:view',
    'fire:repair:list',
    'fire:repair:add'
  )
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

SELECT r.role_key, m.perms, m.menu_name
FROM sys_role r
INNER JOIN sys_role_menu rm ON rm.role_id = r.role_id
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE r.role_key IN ('maintenance_member', 'team_leader')
  AND r.del_flag = '0'
  AND m.perms IN (
    'fire:inspection:add',
    'fire:repair:add',
    'fire:inspection:view',
    'fire:repair:view'
  )
ORDER BY r.role_key, m.perms;
