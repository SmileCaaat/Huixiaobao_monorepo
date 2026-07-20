-- 故障报修派发能力升级
-- 作用：
-- 1. 支持记录派发对象、派发人、派发时间
-- 2. 为报修人 / 处理人 / 公司状态查询增加索引
-- 说明：
-- 1. 脚本按当前数据库动态判断字段和索引是否存在，可重复执行
-- 2. reporter_id 若历史库中不存在，也会自动补齐

SET @reporter_id_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fire_fault_repair'
      AND COLUMN_NAME = 'reporter_id'
);
SET @reporter_id_sql := IF(
    @reporter_id_exists = 0,
    'ALTER TABLE `fire_fault_repair` ADD COLUMN `reporter_id` BIGINT(20) DEFAULT NULL COMMENT ''报修人用户ID'' AFTER `reporter_name`',
    'SELECT ''reporter_id exists'''
);
PREPARE stmt FROM @reporter_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @repair_user_id_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fire_fault_repair'
      AND COLUMN_NAME = 'repair_user_id'
);
SET @repair_user_id_sql := IF(
    @repair_user_id_exists = 0,
    'ALTER TABLE `fire_fault_repair` ADD COLUMN `repair_user_id` BIGINT(20) DEFAULT NULL COMMENT ''处理人ID'' AFTER `repair_status`',
    'SELECT ''repair_user_id exists'''
);
PREPARE stmt FROM @repair_user_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @dispatch_by_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fire_fault_repair'
      AND COLUMN_NAME = 'dispatch_by'
);
SET @dispatch_by_sql := IF(
    @dispatch_by_exists = 0,
    'ALTER TABLE `fire_fault_repair` ADD COLUMN `dispatch_by` VARCHAR(64) DEFAULT NULL COMMENT ''派发人'' AFTER `repair_phone`',
    'SELECT ''dispatch_by exists'''
);
PREPARE stmt FROM @dispatch_by_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @dispatch_time_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fire_fault_repair'
      AND COLUMN_NAME = 'dispatch_time'
);
SET @dispatch_time_sql := IF(
    @dispatch_time_exists = 0,
    'ALTER TABLE `fire_fault_repair` ADD COLUMN `dispatch_time` DATETIME DEFAULT NULL COMMENT ''派发时间'' AFTER `dispatch_by`',
    'SELECT ''dispatch_time exists'''
);
PREPARE stmt FROM @dispatch_time_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @reporter_idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fire_fault_repair'
      AND INDEX_NAME = 'idx_fire_fault_repair_reporter_id'
);
SET @reporter_idx_sql := IF(
    @reporter_idx_exists = 0,
    'ALTER TABLE `fire_fault_repair` ADD INDEX `idx_fire_fault_repair_reporter_id` (`reporter_id`)',
    'SELECT ''idx_fire_fault_repair_reporter_id exists'''
);
PREPARE stmt FROM @reporter_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @repair_user_idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fire_fault_repair'
      AND INDEX_NAME = 'idx_fire_fault_repair_repair_user_id'
);
SET @repair_user_idx_sql := IF(
    @repair_user_idx_exists = 0,
    'ALTER TABLE `fire_fault_repair` ADD INDEX `idx_fire_fault_repair_repair_user_id` (`repair_user_id`)',
    'SELECT ''idx_fire_fault_repair_repair_user_id exists'''
);
PREPARE stmt FROM @repair_user_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @company_status_idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fire_fault_repair'
      AND INDEX_NAME = 'idx_fire_fault_repair_company_status'
);
SET @company_status_idx_sql := IF(
    @company_status_idx_exists = 0,
    'ALTER TABLE `fire_fault_repair` ADD INDEX `idx_fire_fault_repair_company_status` (`company_id`, `repair_status`)',
    'SELECT ''idx_fire_fault_repair_company_status exists'''
);
PREPARE stmt FROM @company_status_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
