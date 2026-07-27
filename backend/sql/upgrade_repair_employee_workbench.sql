-- 故障报修员工工作台：权限收敛 + 紧急程度历史检查（不自动改数）
-- 执行前请备份：sys_role_menu / fire_fault_repair

-- 1) 紧急程度分布（只读检查）
-- SELECT urgency_level, COUNT(*) cnt FROM fire_fault_repair WHERE IFNULL(del_flag,'0')='0' GROUP BY urgency_level;

-- 2) 非法紧急程度明细备份查询
-- SELECT repair_id, repair_no, urgency_level, create_by, create_time, reporter_id
-- FROM fire_fault_repair
-- WHERE IFNULL(del_flag,'0')='0' AND urgency_level NOT IN ('0','1','2');

-- 3) 备份非法数据（可选）
-- CREATE TABLE IF NOT EXISTS bak_fire_fault_repair_urgency_20260727 AS
-- SELECT * FROM fire_fault_repair WHERE IFNULL(del_flag,'0')='0' AND urgency_level NOT IN ('0','1','2');

-- 注意：当前存在 urgency_level='3' 共 3 条，来源可能是旧前端/手工写入。
-- 项目最终枚举为 0一般/1紧急/2特急。在未确认“3”历史语义前，不要批量 UPDATE。

-- 4) 员工角色权限：保留 view/list/start/complete；移除 accept/edit/add/export/remove
DELETE rm
FROM sys_role_menu rm
INNER JOIN sys_role r ON r.role_id = rm.role_id
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE r.role_key = 'maintenance_member'
  AND r.del_flag = '0'
  AND m.perms IN (
    'fire:repair:accept',
    'fire:repair:edit',
    'fire:repair:add',
    'fire:repair:export',
    'fire:repair:remove'
  );

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'maintenance_member'
  AND r.del_flag = '0'
  AND m.perms IN ('fire:repair:view', 'fire:repair:list', 'fire:repair:start', 'fire:repair:complete')
  AND IFNULL(m.visible, '0') = '0'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );
