-- =============================================================================
-- Upgrade: project_manager 仅可访问所属公司及其下级部门（幂等）
-- IMPORTANT: 必须先部署识别 data_scope=6 的后端并重启，再执行本脚本。
-- 旧后端运行期间禁止先改为 6（旧代码不识别会导致过滤失效）。
-- 适用于已执行过 project_manager 业务管理员权限升级的环境。
-- 本脚本只调整数据范围，不修改菜单、用户、部门或消防业务权限。
-- 执行后项目负责人需要重新登录，以刷新 Shiro 授权缓存。
-- =============================================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS bak_pm_company_scope_role_20260730 LIKE sys_role;

INSERT IGNORE INTO bak_pm_company_scope_role_20260730
SELECT *
FROM sys_role
WHERE role_key = 'project_manager'
  AND del_flag = '0';

START TRANSACTION;

UPDATE sys_role
SET data_scope = '6',
    update_by = 'admin',
    update_time = NOW()
WHERE role_key = 'project_manager'
  AND del_flag = '0';

COMMIT;

SELECT role_id, role_name, role_key, data_scope, status, del_flag
FROM sys_role
WHERE role_key = 'project_manager'
  AND del_flag = '0';

