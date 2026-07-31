-- Add category_key / equipment_key for fire_inspection (idempotent)
SET NAMES utf8mb4;

SET @col_category_key := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fire_inspection' AND COLUMN_NAME = 'category_key'
);
SET @sql_category_key := IF(
  @col_category_key = 0,
  'ALTER TABLE fire_inspection ADD COLUMN category_key varchar(128) DEFAULT NULL COMMENT ''template category key'' AFTER system_name',
  'SELECT ''skip category_key'' AS info'
);
PREPARE stmt FROM @sql_category_key; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_equipment_key := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fire_inspection' AND COLUMN_NAME = 'equipment_key'
);
SET @sql_equipment_key := IF(
  @col_equipment_key = 0,
  'ALTER TABLE fire_inspection ADD COLUMN equipment_key varchar(128) DEFAULT NULL COMMENT ''template equipment key'' AFTER category_key',
  'SELECT ''skip equipment_key'' AS info'
);
PREPARE stmt FROM @sql_equipment_key; EXECUTE stmt; DEALLOCATE PREPARE stmt;
