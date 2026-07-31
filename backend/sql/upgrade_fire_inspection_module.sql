-- ============================================================
-- Fire inspection module upgrade (idempotent)
-- 1) fire_inspection columns
-- 2) menu between task and repair
-- 3) sync common role permissions
-- ============================================================
SET NAMES utf8mb4;

-- 1. columns
SET @col_system_type_id := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fire_inspection' AND COLUMN_NAME = 'system_type_id'
);
SET @sql_system_type_id := IF(
  @col_system_type_id = 0,
  'ALTER TABLE fire_inspection ADD COLUMN system_type_id bigint DEFAULT NULL COMMENT ''system type id'' AFTER system_name',
  'SELECT ''skip system_type_id'' AS info'
);
PREPARE stmt FROM @sql_system_type_id; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_equipment_type_id := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fire_inspection' AND COLUMN_NAME = 'equipment_type_id'
);
SET @sql_equipment_type_id := IF(
  @col_equipment_type_id = 0,
  'ALTER TABLE fire_inspection ADD COLUMN equipment_type_id bigint DEFAULT NULL COMMENT ''equipment type id'' AFTER system_type_id',
  'SELECT ''skip equipment_type_id'' AS info'
);
PREPARE stmt FROM @sql_equipment_type_id; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_maintenance_standard := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fire_inspection' AND COLUMN_NAME = 'maintenance_standard'
);
SET @sql_maintenance_standard := IF(
  @col_maintenance_standard = 0,
  'ALTER TABLE fire_inspection ADD COLUMN maintenance_standard varchar(255) DEFAULT NULL COMMENT ''maintenance standard snapshot'' AFTER equipment_type_id',
  'SELECT ''skip maintenance_standard'' AS info'
);
PREPARE stmt FROM @sql_maintenance_standard; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. menu placement: task < inspection < repair (visible menus only)
SET @fire_parent_id := IFNULL((
  SELECT parent_id FROM sys_menu
  WHERE perms = 'fire:task:view' AND menu_type = 'C' AND IFNULL(visible, '0') = '0'
  ORDER BY menu_id DESC LIMIT 1
), IFNULL((
  SELECT parent_id FROM sys_menu WHERE perms = 'fire:company:view' AND menu_type = 'C' LIMIT 1
), 0));

SET @task_order := IFNULL((
  SELECT order_num FROM sys_menu
  WHERE perms = 'fire:task:view' AND menu_type = 'C' AND parent_id = @fire_parent_id
    AND IFNULL(visible, '0') = '0'
  ORDER BY menu_id DESC LIMIT 1
), 3);

SET @inspection_order := @task_order + 1;

-- Move visible siblings at/after target slot (except inspection itself)
UPDATE sys_menu
SET order_num = order_num + 1
WHERE parent_id = @fire_parent_id
  AND menu_type = 'C'
  AND IFNULL(visible, '0') = '0'
  AND perms <> 'fire:inspection:view'
  AND order_num >= @inspection_order;

UPDATE sys_menu
SET menu_name = CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95') USING utf8mb4),
    parent_id = @fire_parent_id,
    order_num = @inspection_order,
    url = '/fire/inspection',
    menu_type = 'C',
    visible = '0',
    is_refresh = '1',
    icon = 'fa fa-search',
    update_by = 'admin',
    update_time = NOW()
WHERE perms = 'fire:inspection:view' AND menu_type = 'C';

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh,
  perms, icon, create_by, create_time, remark
)
SELECT
  CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95') USING utf8mb4),
  @fire_parent_id,
  @inspection_order,
  '/fire/inspection',
  '',
  'C',
  '0',
  '1',
  'fire:inspection:view',
  'fa fa-search',
  'admin',
  NOW(),
  'inspection test menu'
FROM DUAL
WHERE @fire_parent_id > 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'fire:inspection:view' AND menu_type = 'C'
  );

SET @inspection_menu_id := IFNULL((
  SELECT menu_id FROM sys_menu WHERE perms = 'fire:inspection:view' AND menu_type = 'C' LIMIT 1
), 0);

UPDATE sys_menu SET menu_name = CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95E69FA5E8AFA2') USING utf8mb4),
  parent_id = @inspection_menu_id, order_num = 1, update_by = 'admin', update_time = NOW()
WHERE perms = 'fire:inspection:list' AND @inspection_menu_id > 0;
INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95E69FA5E8AFA2') USING utf8mb4), @inspection_menu_id, 1, '#', '', 'F', '0', '1', 'fire:inspection:list', '#', 'admin', NOW(), ''
FROM DUAL WHERE @inspection_menu_id > 0 AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:inspection:list');

UPDATE sys_menu SET menu_name = CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95E696B0E5A29E') USING utf8mb4),
  parent_id = @inspection_menu_id, order_num = 2, update_by = 'admin', update_time = NOW()
WHERE perms = 'fire:inspection:add' AND @inspection_menu_id > 0;
INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95E696B0E5A29E') USING utf8mb4), @inspection_menu_id, 2, '#', '', 'F', '0', '1', 'fire:inspection:add', '#', 'admin', NOW(), ''
FROM DUAL WHERE @inspection_menu_id > 0 AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:inspection:add');

UPDATE sys_menu SET menu_name = CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95E4BFAEE694B9') USING utf8mb4),
  parent_id = @inspection_menu_id, order_num = 3, update_by = 'admin', update_time = NOW()
WHERE perms = 'fire:inspection:edit' AND @inspection_menu_id > 0;
INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95E4BFAEE694B9') USING utf8mb4), @inspection_menu_id, 3, '#', '', 'F', '0', '1', 'fire:inspection:edit', '#', 'admin', NOW(), ''
FROM DUAL WHERE @inspection_menu_id > 0 AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:inspection:edit');

UPDATE sys_menu SET menu_name = CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95E588A0E999A4') USING utf8mb4),
  parent_id = @inspection_menu_id, order_num = 4, update_by = 'admin', update_time = NOW()
WHERE perms = 'fire:inspection:remove' AND @inspection_menu_id > 0;
INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95E588A0E999A4') USING utf8mb4), @inspection_menu_id, 4, '#', '', 'F', '0', '1', 'fire:inspection:remove', '#', 'admin', NOW(), ''
FROM DUAL WHERE @inspection_menu_id > 0 AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:inspection:remove');

UPDATE sys_menu SET menu_name = CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95E5AFBCE587BA') USING utf8mb4),
  parent_id = @inspection_menu_id, order_num = 5, update_by = 'admin', update_time = NOW()
WHERE perms = 'fire:inspection:export' AND @inspection_menu_id > 0;
INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95E5AFBCE587BA') USING utf8mb4), @inspection_menu_id, 5, '#', '', 'F', '0', '1', 'fire:inspection:export', '#', 'admin', NOW(), ''
FROM DUAL WHERE @inspection_menu_id > 0 AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:inspection:export');

-- 3. role sync
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.del_flag = '0'
  AND r.role_key IN ('admin', 'project_manager', 'team_leader', 'maintenance_member', 'business_admin')
  AND m.perms IN (
    'fire:inspection:view',
    'fire:inspection:list',
    'fire:inspection:add',
    'fire:inspection:edit',
    'fire:inspection:remove',
    'fire:inspection:export'
  )
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

SELECT 'menu_order' AS check_item, menu_id, menu_name, order_num, url, perms, visible
FROM sys_menu
WHERE parent_id = @fire_parent_id AND menu_type = 'C' AND IFNULL(visible, '0') = '0'
ORDER BY order_num, menu_id;
