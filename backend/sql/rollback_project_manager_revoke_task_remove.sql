-- =============================================================================
-- Rollback: 恢复 project_manager 的 fire:task:remove 授权
-- 前提：已执行 upgrade_project_manager_revoke_task_remove.sql 并保留备份表。
-- =============================================================================

SET NAMES utf8mb4;

SET @pm_role_id := (
    SELECT role_id FROM sys_role
    WHERE role_key = 'project_manager' AND del_flag = '0'
    ORDER BY role_id
    LIMIT 1
);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @pm_role_id, b.menu_id
FROM bak_pm_task_remove_role_menu_20260730 b
WHERE @pm_role_id IS NOT NULL;

SELECT r.role_key, m.menu_id, m.menu_name, m.perms
FROM sys_role_menu rm
INNER JOIN sys_role r ON r.role_id = rm.role_id
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE r.role_key = 'project_manager'
  AND r.del_flag = '0'
  AND m.perms = 'fire:task:remove';
