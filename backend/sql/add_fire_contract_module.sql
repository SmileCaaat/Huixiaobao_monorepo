-- 维保合同管理模块初始化脚本

-- 1. 维保合同表
CREATE TABLE IF NOT EXISTS `fire_contract` (
  `contract_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `company_id` bigint(20) NOT NULL COMMENT '客户ID',
  `company_name` varchar(200) DEFAULT '' COMMENT '客户名称',
  `project_name` varchar(200) DEFAULT '' COMMENT '项目名称',
  `contract_name` varchar(200) NOT NULL COMMENT '合同名称',
  `contract_no` varchar(100) NOT NULL COMMENT '合同编号',
  `contract_amount` decimal(12,2) DEFAULT 0.00 COMMENT '合同金额',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `entry_unit` varchar(100) DEFAULT '' COMMENT '录入单位',
  `attachment_name` varchar(255) DEFAULT '' COMMENT '附件名称',
  `attachment_path` varchar(500) DEFAULT '' COMMENT '附件路径',
  `terminate_flag` char(1) DEFAULT '0' COMMENT '终止标记(0否 1是)',
  `terminate_time` datetime DEFAULT NULL COMMENT '终止时间',
  `renewed_from_id` bigint(20) DEFAULT NULL COMMENT '续签来源合同ID',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记(0存在 2删除)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`contract_id`),
  KEY `idx_fire_contract_company` (`company_id`),
  KEY `idx_fire_contract_validity` (`start_date`, `end_date`),
  KEY `idx_fire_contract_terminate` (`terminate_flag`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维保合同表';

-- 2. 菜单与权限（可选）
SET @fire_parent_id = IFNULL((SELECT parent_id FROM sys_menu WHERE perms = 'fire:company:view' LIMIT 1), 0);
SET @contract_menu_exists = (SELECT COUNT(1) FROM sys_menu WHERE perms = 'fire:contract:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT '维保合同管理', @fire_parent_id, 6, '/fire/contract', '', 'C', '0', '1', 'fire:contract:view', 'fa fa-file-text-o', 'admin', NOW(), '维保合同管理菜单'
WHERE @contract_menu_exists = 0;

SET @contract_menu_id = IFNULL((SELECT menu_id FROM sys_menu WHERE perms = 'fire:contract:view' LIMIT 1), 0);

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT '维保合同查询', @contract_menu_id, 1, '#', '', 'F', '0', '1', 'fire:contract:list', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:contract:list');

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT '维保合同新增', @contract_menu_id, 2, '#', '', 'F', '0', '1', 'fire:contract:add', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:contract:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT '维保合同编辑', @contract_menu_id, 3, '#', '', 'F', '0', '1', 'fire:contract:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:contract:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT '维保合同续签', @contract_menu_id, 4, '#', '', 'F', '0', '1', 'fire:contract:renew', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:contract:renew');

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT '维保合同终止', @contract_menu_id, 5, '#', '', 'F', '0', '1', 'fire:contract:terminate', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:contract:terminate');

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT '维保合同删除', @contract_menu_id, 6, '#', '', 'F', '0', '1', 'fire:contract:remove', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:contract:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT '维保合同导出', @contract_menu_id, 7, '#', '', 'F', '0', '1', 'fire:contract:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:contract:export');

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE perms IN (
  'fire:contract:view',
  'fire:contract:list',
  'fire:contract:add',
  'fire:contract:edit',
  'fire:contract:renew',
  'fire:contract:terminate',
  'fire:contract:remove',
  'fire:contract:export'
);
