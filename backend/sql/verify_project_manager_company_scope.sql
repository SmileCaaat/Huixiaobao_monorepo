-- =============================================================================
-- Verify: project_manager 所属公司数据范围
-- data_scope 应为 6；最后一项查询应返回 0 行。
-- =============================================================================

SET NAMES utf8mb4;

-- 1) 角色数据范围
SELECT role_id, role_name, role_key, data_scope, status, del_flag
FROM sys_role
WHERE role_key = 'project_manager'
  AND del_flag = '0';

-- 2) 项目负责人的所属部门及计算出的公司根部门
SELECT u.user_id,
       u.login_name,
       u.user_name,
       d.dept_id,
       d.dept_name,
       d.parent_id,
       d.ancestors,
       CASE
           WHEN d.parent_id = 0 THEN d.dept_id
           ELSE CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(d.ancestors, ',', 2), ',', -1) AS UNSIGNED)
       END AS company_dept_id
FROM sys_user u
INNER JOIN sys_user_role ur ON ur.user_id = u.user_id
INNER JOIN sys_role r ON r.role_id = ur.role_id
LEFT JOIN sys_dept d ON d.dept_id = u.dept_id
WHERE r.role_key = 'project_manager'
  AND r.status = '0'
  AND r.del_flag = '0'
  AND u.status = '0'
  AND u.del_flag = '0';

-- 3) 可见部门预览：只应包含每位项目负责人所属公司的根部门及其全部下级部门
SELECT pm.user_id,
       pm.login_name,
       pm.company_dept_id,
       visible_dept.dept_id AS visible_dept_id,
       visible_dept.dept_name AS visible_dept_name,
       visible_dept.ancestors
FROM (
    SELECT DISTINCT
           u.user_id,
           u.login_name,
           CASE
               WHEN d.parent_id = 0 THEN d.dept_id
               ELSE CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(d.ancestors, ',', 2), ',', -1) AS UNSIGNED)
           END AS company_dept_id
    FROM sys_user u
    INNER JOIN sys_user_role ur ON ur.user_id = u.user_id
    INNER JOIN sys_role r ON r.role_id = ur.role_id
    INNER JOIN sys_dept d ON d.dept_id = u.dept_id
    WHERE r.role_key = 'project_manager'
      AND r.status = '0'
      AND r.del_flag = '0'
      AND u.status = '0'
      AND u.del_flag = '0'
) pm
INNER JOIN sys_dept visible_dept
        ON visible_dept.dept_id = pm.company_dept_id
        OR FIND_IN_SET(pm.company_dept_id, visible_dept.ancestors)
WHERE visible_dept.del_flag = '0'
ORDER BY pm.user_id, visible_dept.parent_id, visible_dept.order_num, visible_dept.dept_id;

-- 4) 多角色越权检查：若项目负责人还拥有 data_scope=1 的其他角色，会合并成全部数据权限
SELECT DISTINCT
       u.user_id,
       u.login_name,
       all_scope_role.role_id,
       all_scope_role.role_name,
       all_scope_role.role_key
FROM sys_user u
INNER JOIN sys_user_role pm_ur ON pm_ur.user_id = u.user_id
INNER JOIN sys_role pm_role
        ON pm_role.role_id = pm_ur.role_id
       AND pm_role.role_key = 'project_manager'
       AND pm_role.status = '0'
       AND pm_role.del_flag = '0'
INNER JOIN sys_user_role all_ur ON all_ur.user_id = u.user_id
INNER JOIN sys_role all_scope_role
        ON all_scope_role.role_id = all_ur.role_id
       AND all_scope_role.data_scope = '1'
       AND all_scope_role.status = '0'
       AND all_scope_role.del_flag = '0'
WHERE all_scope_role.role_id <> pm_role.role_id
  AND u.status = '0'
  AND u.del_flag = '0';

