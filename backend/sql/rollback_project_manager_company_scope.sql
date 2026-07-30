-- =============================================================================
-- Rollback: 恢复 project_manager 调整前的数据范围
-- 前提：已执行 upgrade_project_manager_company_scope.sql 并保留备份表。
-- =============================================================================

SET NAMES utf8mb4;

START TRANSACTION;

UPDATE sys_role current_role
INNER JOIN bak_pm_company_scope_role_20260730 backup_role
        ON backup_role.role_id = current_role.role_id
SET current_role.data_scope = backup_role.data_scope,
    current_role.update_by = 'admin',
    current_role.update_time = NOW()
WHERE current_role.role_key = 'project_manager';

COMMIT;

SELECT role_id, role_name, role_key, data_scope, status, del_flag
FROM sys_role
WHERE role_key = 'project_manager'
  AND del_flag = '0';

