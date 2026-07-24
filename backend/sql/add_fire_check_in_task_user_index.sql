-- 维保签到按任务+人员配对/开流查询索引
-- 执行前请先备份生产库；本脚本默认不自动执行。
--
-- 预检是否已存在同名索引：
-- SHOW INDEX FROM fire_check_in WHERE Key_name = 'idx_fire_check_in_task_user_time';

-- 幂等添加：已存在则跳过，避免重复执行报错
SET @idx_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'fire_check_in'
      AND index_name = 'idx_fire_check_in_task_user_time'
);
SET @ddl := IF(
    @idx_exists = 0,
    'ALTER TABLE fire_check_in ADD INDEX idx_fire_check_in_task_user_time (task_id, user_id, check_in_time, del_flag)',
    'SELECT ''idx_fire_check_in_task_user_time already exists'' AS info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
