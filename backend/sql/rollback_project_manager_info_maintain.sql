-- =============================================================================
-- Rollback: 撤销本次为 project_manager 新增的「信息维护」菜单授权
-- 前提：已执行 upgrade_project_manager_info_maintain.sql 并保留备份表。
-- =============================================================================

SET NAMES utf8mb4;

SET @pm_role_id := (
    SELECT role_id FROM sys_role
    WHERE role_key = 'project_manager' AND del_flag = '0'
    ORDER BY role_id
    LIMIT 1
);

DELETE rm
FROM sys_role_menu rm
INNER JOIN bak_pm_info_maintain_role_menu_20260730 b
        ON b.role_id = rm.role_id AND b.menu_id = rm.menu_id
WHERE rm.role_id = @pm_role_id;

SELECT COUNT(*) AS remaining_info_menu_grants
FROM sys_role_menu rm
INNER JOIN sys_role r ON r.role_id = rm.role_id AND r.role_key = 'project_manager' AND r.del_flag = '0'
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.menu_name IN ('信息维护', '建筑信息', '设备信息', '项目类别')
   OR IFNULL(m.perms, '') LIKE 'fire:building:%'
   OR IFNULL(m.perms, '') LIKE 'fire:equipment:%'
   OR IFNULL(m.perms, '') LIKE 'fire:systemType:%';
