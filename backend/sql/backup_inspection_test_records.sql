-- ============================================================
-- Ѳ����Ժϲ������� fire_maintenance_record���ݵȿɺ˶ԣ�
-- �÷�����Ŀ���ִ�б��ű������ݱ��������ں�׺��
-- �����ݺ� COUNT ��һ���� SIGNAL ��ֹ��
-- ============================================================

SET @backup_suffix = DATE_FORMAT(NOW(), '%Y%m%d');
SET @backup_table = CONCAT('fire_maintenance_record_bak_', @backup_suffix);
SET @task_bak_table = CONCAT('fire_maintenance_task_stats_bak_', @backup_suffix);

-- 1) Դ��ͳ��
SELECT COUNT(*) AS src_total FROM fire_maintenance_record;
SELECT task_id, record_type, `level`, COUNT(*) AS cnt
FROM fire_maintenance_record
GROUP BY task_id, record_type, `level`
ORDER BY task_id, record_type, `level`;

-- 2) ��������Ƿ����ָ�� record_id ���������Ϣ�˶ԣ����Զ���ֹ��
SELECT TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND REFERENCED_TABLE_NAME = 'fire_maintenance_record'
  AND REFERENCED_COLUMN_NAME = 'record_id';

-- 3) �������ݱ����������Ѵ��������������������º˶ԣ�
SET @sql_create = CONCAT(
  'CREATE TABLE IF NOT EXISTS `', @backup_table, '` LIKE fire_maintenance_record'
);
PREPARE stmt FROM @sql_create;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @bak_cnt := (
  SELECT COUNT(*) FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @backup_table
);
-- �ձ�ʱ�������ݣ��ǿ�����Ϊ�����ѱ��ݹ�����У������
SET @bak_rows := 0;
SET @q = CONCAT('SELECT COUNT(*) INTO @bak_rows FROM `', @backup_table, '`');
PREPARE stmt FROM @q;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @src_rows := (SELECT COUNT(*) FROM fire_maintenance_record);

SET @sql_insert = IF(
  @bak_rows = 0,
  CONCAT('INSERT INTO `', @backup_table, '` SELECT * FROM fire_maintenance_record'),
  'SELECT ''backup already filled, skip insert'' AS info'
);
PREPARE stmt FROM @sql_insert;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @q2 = CONCAT('SELECT COUNT(*) INTO @bak_rows FROM `', @backup_table, '`');
PREPARE stmt FROM @q2;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT @src_rows AS source_count, @bak_rows AS backup_count, @backup_table AS backup_table;

-- COUNT ��һ������ֹ
SET @fail_msg = CONCAT('Backup count mismatch: source=', @src_rows, ' backup=', @bak_rows);
SET @assert_sql = IF(
  @src_rows = @bak_rows,
  'SELECT ''backup_inspection_test_records OK'' AS result',
  CONCAT('SIGNAL SQLSTATE ''45000'' SET MESSAGE_TEXT = ''', REPLACE(@fail_msg, '''', ''), '''')
);
PREPARE stmt FROM @assert_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) ͬ����������ͳ��/״̬�У��� upgrade/rollback ʹ�ã�
SET @sql_task_bak = CONCAT(
  'CREATE TABLE IF NOT EXISTS `', @task_bak_table, '` AS ',
  'SELECT task_id, task_status, total_items, completed_items, normal_items, fault_items, no_device_items ',
  'FROM fire_maintenance_task WHERE 1=0'
);
PREPARE stmt FROM @sql_task_bak;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @task_bak_rows := 0;
SET @q3 = CONCAT('SELECT COUNT(*) INTO @task_bak_rows FROM `', @task_bak_table, '`');
PREPARE stmt FROM @q3;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_task_fill = IF(
  @task_bak_rows = 0,
  CONCAT(
    'INSERT INTO `', @task_bak_table, '` ',
    '(task_id, task_status, total_items, completed_items, normal_items, fault_items, no_device_items) ',
    'SELECT task_id, task_status, total_items, completed_items, normal_items, fault_items, no_device_items ',
    'FROM fire_maintenance_task'
  ),
  'SELECT ''task stats backup already filled'' AS info'
);
PREPARE stmt FROM @sql_task_fill;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT @task_bak_table AS task_stats_backup_table,
       (SELECT COUNT(*) FROM fire_maintenance_task) AS task_source_count;
SET @q4 = CONCAT('SELECT COUNT(*) AS task_backup_count FROM `', @task_bak_table, '`');
PREPARE stmt FROM @q4;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
