-- ============================================================
-- 巡检测试模块回滚（幂等，不删历史业务数据）
-- 仅回滚本升级新增的列与菜单/角色授权；不物理删除 fire_inspection 记录
-- ============================================================
SET NAMES utf8mb4;

-- 1. 回滚角色菜单授权
DELETE rm FROM sys_role_menu rm
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.perms IN (
  'fire:inspection:view',
  'fire:inspection:list',
  'fire:inspection:add',
  'fire:inspection:edit',
  'fire:inspection:remove',
  'fire:inspection:export'
);

-- 2. 回滚按钮菜单
DELETE FROM sys_menu
WHERE perms IN (
  'fire:inspection:list',
  'fire:inspection:add',
  'fire:inspection:edit',
  'fire:inspection:remove',
  'fire:inspection:export'
);

-- 3. 回滚主菜单（可选：若需保留菜单入口可注释本段）
DELETE FROM sys_menu
WHERE perms = 'fire:inspection:view' AND menu_type = 'C';

-- 4. 回滚新增列（保留历史行数据；仅去掉本升级新增的快照列）
SET @col_maintenance_standard := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_inspection'
    AND COLUMN_NAME = 'maintenance_standard'
);
SET @sql_drop_ms := IF(
  @col_maintenance_standard > 0,
  'ALTER TABLE fire_inspection DROP COLUMN maintenance_standard',
  'SELECT ''skip drop maintenance_standard'' AS info'
);
PREPARE stmt FROM @sql_drop_ms; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_equipment_type_id := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_inspection'
    AND COLUMN_NAME = 'equipment_type_id'
);
SET @sql_drop_et := IF(
  @col_equipment_type_id > 0,
  'ALTER TABLE fire_inspection DROP COLUMN equipment_type_id',
  'SELECT ''skip drop equipment_type_id'' AS info'
);
PREPARE stmt FROM @sql_drop_et; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_system_type_id := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_inspection'
    AND COLUMN_NAME = 'system_type_id'
);
SET @sql_drop_st := IF(
  @col_system_type_id > 0,
  'ALTER TABLE fire_inspection DROP COLUMN system_type_id',
  'SELECT ''skip drop system_type_id'' AS info'
);
PREPARE stmt FROM @sql_drop_st; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'rollback_fire_inspection_module done' AS info;
