-- ============================================================
-- Ѳ����Ժϲ��ع���ɾ���¼�¼���Ե��ձ��ݻָ���¼������ͳ��
-- ǰ�ã����� fire_maintenance_record_bak_YYYYMMDD
--       �� fire_maintenance_task_stats_bak_YYYYMMDD
-- ============================================================
SET NAMES utf8mb4;

SET @backup_suffix = DATE_FORMAT(NOW(), '%Y%m%d');
SET @backup_table = CONCAT('fire_maintenance_record_bak_', @backup_suffix);
SET @task_bak_table = CONCAT('fire_maintenance_task_stats_bak_', @backup_suffix);

SET @bak_exists := (
  SELECT COUNT(*) FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @backup_table
);
SET @task_bak_exists := (
  SELECT COUNT(*) FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @task_bak_table
);

SET @guard = IF(
  @bak_exists = 1 AND @task_bak_exists = 1,
  'SELECT ''backup tables found, proceed rollback'' AS result',
  'SIGNAL SQLSTATE ''45000'' SET MESSAGE_TEXT = ''Backup tables missing for today; abort rollback'''
);
PREPARE stmt FROM @guard;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

START TRANSACTION;

DELETE FROM fire_maintenance_record;

SET @sql_restore = CONCAT(
  'INSERT INTO fire_maintenance_record SELECT * FROM `', @backup_table, '`'
);
PREPARE stmt FROM @sql_restore;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_restore_task = CONCAT(
  'UPDATE fire_maintenance_task t ',
  'INNER JOIN `', @task_bak_table, '` b ON t.task_id = b.task_id ',
  'SET t.task_status = b.task_status, ',
  '    t.total_items = b.total_items, ',
  '    t.completed_items = b.completed_items, ',
  '    t.normal_items = b.normal_items, ',
  '    t.fault_items = b.fault_items, ',
  '    t.no_device_items = b.no_device_items, ',
  '    t.update_by = ''rollback_merge_inspection_test'', ',
  '    t.update_time = NOW()'
);
PREPARE stmt FROM @sql_restore_task;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

COMMIT;

SET @src_rows := (SELECT COUNT(*) FROM fire_maintenance_record);
SET @bak_rows := 0;
SET @q = CONCAT('SELECT COUNT(*) INTO @bak_rows FROM `', @backup_table, '`');
PREPARE stmt FROM @q;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT @src_rows AS restored_count, @bak_rows AS backup_count;

SET @assert = IF(
  @src_rows = @bak_rows,
  'SELECT ''rollback_merge_inspection_test OK'' AS result',
  'SIGNAL SQLSTATE ''45000'' SET MESSAGE_TEXT = ''Rollback count mismatch'''
);
PREPARE stmt FROM @assert;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
