-- fix auth rbac chinese names + org structure
SET NAMES utf8mb4;

-- 1) departments
UPDATE sys_dept
   SET dept_name = '维保部', ancestors = '0,100', parent_id = 100, order_num = 10, status = '0', del_flag = '0'
 WHERE del_flag = '0' AND parent_id = 100 AND order_num = 10
   AND (dept_name LIKE '?%' OR dept_name = '维保部');

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, status, del_flag, create_by, create_time)
SELECT 100, '0,100', '维保部', 10, '0', '0', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dept WHERE dept_name = '维保部' AND del_flag = '0');

SET @wb_dept_id := (SELECT dept_id FROM sys_dept WHERE dept_name = '维保部' AND del_flag = '0' ORDER BY dept_id LIMIT 1);
SET @wb_ancestors := (SELECT CONCAT('0,100,', dept_id) FROM sys_dept WHERE dept_id = @wb_dept_id);

UPDATE sys_dept SET ancestors = '0,100', parent_id = 100 WHERE dept_id = @wb_dept_id;

UPDATE sys_dept
   SET dept_name = '内场维保组', parent_id = @wb_dept_id, order_num = 1, status = '0', del_flag = '0'
 WHERE del_flag = '0' AND parent_id = @wb_dept_id AND order_num = 1
   AND (dept_name LIKE '?%' OR dept_name = '内场维保组');

UPDATE sys_dept
   SET dept_name = '外场维保组', parent_id = @wb_dept_id, order_num = 2, status = '0', del_flag = '0'
 WHERE del_flag = '0' AND parent_id = @wb_dept_id AND order_num = 2
   AND (dept_name LIKE '?%' OR dept_name = '外场维保组');

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, status, del_flag, create_by, create_time)
SELECT @wb_dept_id, @wb_ancestors, '内场维保组', 1, '0', '0', 'admin', NOW()
WHERE @wb_dept_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_dept WHERE dept_name = '内场维保组' AND del_flag = '0');

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, status, del_flag, create_by, create_time)
SELECT @wb_dept_id, @wb_ancestors, '外场维保组', 2, '0', '0', 'admin', NOW()
WHERE @wb_dept_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_dept WHERE dept_name = '外场维保组' AND del_flag = '0');

UPDATE sys_dept d
   SET parent_id = @wb_dept_id,
       ancestors = CONCAT(@wb_ancestors, ',', d.dept_id)
 WHERE d.dept_name IN ('内场维保组', '外场维保组') AND d.del_flag = '0';

-- 2) posts
UPDATE sys_post SET post_name = '维保部负责人', post_sort = 10, remark = '方案B岗位' WHERE post_code = 'wb_director';
UPDATE sys_post SET post_name = '内场项目负责人', post_sort = 11, remark = '方案B岗位' WHERE post_code = 'pm_inner';
UPDATE sys_post SET post_name = '外场项目负责人', post_sort = 12, remark = '方案B岗位' WHERE post_code = 'pm_outer';
UPDATE sys_post SET post_name = '内场维保组长', post_sort = 13, remark = '方案B岗位' WHERE post_code = 'tl_inner';
UPDATE sys_post SET post_name = '外场维保组长', post_sort = 14, remark = '方案B岗位' WHERE post_code = 'tl_outer';
UPDATE sys_post SET post_name = '维保组员', post_sort = 15, remark = '方案B岗位' WHERE post_code = 'wb_member';
UPDATE sys_post SET post_name = '普通员工', post_sort = 16, remark = '方案B岗位' WHERE post_code = 'staff';

INSERT INTO sys_post (post_code, post_name, post_sort, status, create_by, create_time, remark)
SELECT t.post_code, t.post_name, t.post_sort, t.status, t.create_by, t.create_time, t.remark FROM (
  SELECT 'wb_director' AS post_code, '维保部负责人' AS post_name, 10 AS post_sort, '0' AS status, 'admin' AS create_by, NOW() AS create_time, '方案B岗位' AS remark
  UNION ALL SELECT 'pm_inner', '内场项目负责人', 11, '0', 'admin', NOW(), '方案B岗位'
  UNION ALL SELECT 'pm_outer', '外场项目负责人', 12, '0', 'admin', NOW(), '方案B岗位'
  UNION ALL SELECT 'tl_inner', '内场维保组长', 13, '0', 'admin', NOW(), '方案B岗位'
  UNION ALL SELECT 'tl_outer', '外场维保组长', 14, '0', 'admin', NOW(), '方案B岗位'
  UNION ALL SELECT 'wb_member', '维保组员', 15, '0', 'admin', NOW(), '方案B岗位'
  UNION ALL SELECT 'staff', '普通员工', 16, '0', 'admin', NOW(), '方案B岗位'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_post p WHERE p.post_code = t.post_code);

-- 3) roles
UPDATE sys_role SET role_name = '维保部管理员', remark = '方案B角色' WHERE role_key = 'fire_dept_admin' AND del_flag = '0';
UPDATE sys_role SET role_name = '项目负责人', remark = '方案B角色-项目可见性以成员表为准' WHERE role_key = 'project_manager' AND del_flag = '0';
UPDATE sys_role SET role_name = '维保组长', remark = '方案B角色' WHERE role_key = 'team_leader' AND del_flag = '0';
UPDATE sys_role SET role_name = '维保执行人员', remark = '方案B角色' WHERE role_key = 'fire_operator' AND del_flag = '0';
UPDATE sys_role SET role_name = '普通部门员工', remark = '方案B角色' WHERE role_key = 'dept_staff' AND del_flag = '0';
UPDATE sys_role SET role_name = '只读审计', remark = '方案B角色' WHERE role_key = 'auditor' AND del_flag = '0';

-- 4) menus
UPDATE sys_menu SET menu_name = '客户成员分配', remark = '方案B' WHERE perms = 'fire:company:assign';
UPDATE sys_menu SET menu_name = '用户审核', remark = '方案B' WHERE perms = 'system:user:audit';

-- 5) admin flags
UPDATE sys_user
   SET allow_admin_login = '1', allow_mini_login = '1', audit_status = '0', status = '0'
 WHERE user_id = 1;
