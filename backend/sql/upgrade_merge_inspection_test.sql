-- ============================================================
-- Ѳ����Ժϲ�������ִ�м�¼����������ͳ��
-- ǰ�ã��ѳɹ�ִ�� backup_inspection_test_records.sql
-- ע�⣺���ű������ɶ༶ parent ��������������ɳ��ܵ���
--       POST /fire/task/rebuildInspectionTestRecords ����ؽ���
-- ============================================================
SET NAMES utf8mb4;

SET @backup_suffix = DATE_FORMAT(NOW(), '%Y%m%d');
SET @backup_table = CONCAT('fire_maintenance_record_bak_', @backup_suffix);
SET @task_bak_table = CONCAT('fire_maintenance_task_stats_bak_', @backup_suffix);

-- ȷ�ϱ��ݱ�����������һ��
SET @bak_exists := (
  SELECT COUNT(*) FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @backup_table
);
SET @task_bak_exists := (
  SELECT COUNT(*) FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @task_bak_table
);

SET @src_rows := (SELECT COUNT(*) FROM fire_maintenance_record);
SET @bak_rows := 0;
SET @q = CONCAT('SELECT COUNT(*) INTO @bak_rows FROM `', @backup_table, '`');
PREPARE stmt FROM @q;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT @backup_table AS backup_table, @bak_exists AS bak_exists,
       @src_rows AS source_count, @bak_rows AS backup_count,
       @task_bak_table AS task_bak_table, @task_bak_exists AS task_bak_exists;

SET @guard = IF(
  @bak_exists = 1 AND @task_bak_exists = 1 AND @src_rows = @bak_rows,
  'SELECT ''backup verified, proceed cleanup'' AS result',
  'SIGNAL SQLSTATE ''45000'' SET MESSAGE_TEXT = ''Backup incomplete or count mismatch; abort upgrade'''
);
PREPARE stmt FROM @guard;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

START TRANSACTION;

-- ��ռ���¼���������ִ�н����ģ��/����/ǩ��/���汣����
DELETE FROM fire_maintenance_record;

-- ��ȡ�������ô�ִ�в�������ͳ�ƣ�ȡ��(3)���ֲ���
UPDATE fire_maintenance_task
SET task_status = '0',
    total_items = 0,
    completed_items = 0,
    normal_items = 0,
    fault_items = 0,
    no_device_items = 0,
    update_by = 'upgrade_merge_inspection_test',
    update_time = NOW()
WHERE task_status <> '3';

COMMIT;

SELECT COUNT(*) AS records_after_delete FROM fire_maintenance_record;
SELECT task_status, COUNT(*) AS cnt FROM fire_maintenance_task GROUP BY task_status;

SELECT 'NEXT: call admin POST /fire/task/rebuildInspectionTestRecords (super admin only) to rebuild trees' AS next_step;
