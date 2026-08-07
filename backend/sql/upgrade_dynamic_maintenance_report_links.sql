-- 动态维保报告数据链升级
-- 1. 故障报修记录可精确关联来源维保任务
-- 2. 巡查测试报告按任务、单位和时间范围查询时使用组合索引

SET @repair_task_column_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fire_fault_repair'
      AND COLUMN_NAME = 'task_id'
);
SET @repair_task_column_sql := IF(
    @repair_task_column_exists = 0,
    'ALTER TABLE fire_fault_repair ADD COLUMN task_id BIGINT DEFAULT NULL COMMENT ''来源维保任务ID'' AFTER company_id',
    'SELECT ''fire_fault_repair.task_id exists'''
);
PREPARE stmt FROM @repair_task_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @repair_task_index_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fire_fault_repair'
      AND INDEX_NAME = 'idx_fire_fault_repair_task'
);
SET @repair_task_index_sql := IF(
    @repair_task_index_exists = 0,
    'ALTER TABLE fire_fault_repair ADD INDEX idx_fire_fault_repair_task (task_id)',
    'SELECT ''idx_fire_fault_repair_task exists'''
);
PREPARE stmt FROM @repair_task_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @inspection_report_index_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fire_inspection'
      AND INDEX_NAME = 'idx_fire_inspection_report'
);
SET @inspection_report_index_sql := IF(
    @inspection_report_index_exists = 0,
    'ALTER TABLE fire_inspection ADD INDEX idx_fire_inspection_report (task_id, company_id, inspection_type, inspection_time, del_flag)',
    'SELECT ''idx_fire_inspection_report exists'''
);
PREPARE stmt FROM @inspection_report_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
