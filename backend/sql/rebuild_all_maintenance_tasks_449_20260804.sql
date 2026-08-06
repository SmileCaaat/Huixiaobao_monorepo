-- Rebuild every historical maintenance task from the verified 449-item task (task_id=54).
-- This intentionally discards existing task answers/progress. Full backups are created first.

CREATE TABLE IF NOT EXISTS fire_maintenance_task_bak_rebuild449_20260804
LIKE fire_maintenance_task;

INSERT IGNORE INTO fire_maintenance_task_bak_rebuild449_20260804
SELECT * FROM fire_maintenance_task;

CREATE TABLE IF NOT EXISTS fire_maintenance_record_bak_rebuild449_20260804
LIKE fire_maintenance_record;

INSERT IGNORE INTO fire_maintenance_record_bak_rebuild449_20260804
SELECT * FROM fire_maintenance_record;

START TRANSACTION;

DELETE FROM fire_maintenance_record
WHERE task_id <> 54;

-- Level 1 records.
INSERT INTO fire_maintenance_record
    (task_id, template_id, level, parent_record_id, item_name, item_code,
     check_result, sort_order, create_time, record_type)
SELECT t.task_id, src.template_id, src.level, NULL, src.item_name, src.item_code,
       '0', src.sort_order, NOW(), src.record_type
FROM fire_maintenance_task t
JOIN fire_maintenance_record src
  ON src.task_id = 54 AND src.level = 1
WHERE t.task_id <> 54;

-- Level 2 records, remapping their parent record IDs to each target task.
INSERT INTO fire_maintenance_record
    (task_id, template_id, level, parent_record_id, item_name, item_code,
     check_result, sort_order, create_time, record_type)
SELECT t.task_id, src.template_id, src.level, target_parent.record_id,
       src.item_name, src.item_code, '0', src.sort_order, NOW(), src.record_type
FROM fire_maintenance_task t
JOIN fire_maintenance_record src
  ON src.task_id = 54 AND src.level = 2
JOIN fire_maintenance_record source_parent
  ON source_parent.record_id = src.parent_record_id
JOIN fire_maintenance_record target_parent
  ON target_parent.task_id = t.task_id
 AND target_parent.level = 1
 AND target_parent.template_id = source_parent.template_id
WHERE t.task_id <> 54;

-- Level 3 records, remapping their parent record IDs to each target task.
INSERT INTO fire_maintenance_record
    (task_id, template_id, level, parent_record_id, item_name, item_code,
     check_result, sort_order, create_time, record_type)
SELECT t.task_id, src.template_id, src.level, target_parent.record_id,
       src.item_name, src.item_code, '0', src.sort_order, NOW(), src.record_type
FROM fire_maintenance_task t
JOIN fire_maintenance_record src
  ON src.task_id = 54 AND src.level = 3
JOIN fire_maintenance_record source_parent
  ON source_parent.record_id = src.parent_record_id
JOIN fire_maintenance_record target_parent
  ON target_parent.task_id = t.task_id
 AND target_parent.level = 2
 AND target_parent.template_id = source_parent.template_id
 AND target_parent.record_type = source_parent.record_type
WHERE t.task_id <> 54;

UPDATE fire_maintenance_task
SET task_status = '1',
    total_items = 449,
    completed_items = 0,
    normal_items = 0,
    fault_items = 0,
    no_device_items = 0,
    actual_end_time = NULL,
    update_time = NOW();

COMMIT;

SELECT task_id, task_name, task_status, total_items, completed_items
FROM fire_maintenance_task
ORDER BY task_id DESC;

SELECT task_id,
       COUNT(CASE WHEN level = 3 THEN 1 END) AS total_items,
       COUNT(CASE WHEN level = 3 AND record_type = '0' THEN 1 END) AS patrol_items,
       COUNT(CASE WHEN level = 3 AND record_type = '1' THEN 1 END) AS test_items,
       COUNT(CASE WHEN level = 3 AND record_type = '2' THEN 1 END) AS upkeep_items
FROM fire_maintenance_record
GROUP BY task_id
ORDER BY task_id DESC;
