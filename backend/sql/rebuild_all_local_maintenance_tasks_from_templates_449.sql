-- Rebuild every task directly from the current local 449-item template catalog.
-- Existing task answers are intentionally discarded; callers must back up first.

START TRANSACTION;

DELETE FROM fire_maintenance_record;

INSERT INTO fire_maintenance_record
    (task_id, template_id, level, parent_record_id, item_name, item_code,
     check_result, sort_order, create_time, record_type)
SELECT task.task_id, tpl.id, 1, NULL, tpl.item_name,
       COALESCE(NULLIF(tpl.item_code, ''), CONCAT('TPL_', tpl.id)),
       '0', tpl.sort_order, NOW(), tpl.template_type
FROM fire_maintenance_task task
JOIN fire_maintenance_template tpl
  ON tpl.level = 1 AND tpl.template_type IN ('0', '1', '2');

INSERT INTO fire_maintenance_record
    (task_id, template_id, level, parent_record_id, item_name, item_code,
     check_result, sort_order, create_time, record_type)
SELECT task.task_id, tpl.id, 2, parent_record.record_id, tpl.item_name,
       COALESCE(NULLIF(tpl.item_code, ''), CONCAT('TPL_', tpl.id)),
       '0', tpl.sort_order, NOW(), tpl.template_type
FROM fire_maintenance_task task
JOIN fire_maintenance_template tpl
  ON tpl.level = 2 AND tpl.template_type IN ('0', '1', '2')
JOIN fire_maintenance_record parent_record
  ON parent_record.task_id = task.task_id
 AND parent_record.level = 1
 AND parent_record.template_id = tpl.parent_id
 AND parent_record.record_type = tpl.template_type;

INSERT INTO fire_maintenance_record
    (task_id, template_id, level, parent_record_id, item_name, item_code,
     check_result, sort_order, create_time, record_type)
SELECT task.task_id, tpl.id, 3, parent_record.record_id, tpl.item_name,
       COALESCE(NULLIF(tpl.item_code, ''), CONCAT('TPL_', tpl.id)),
       '0', tpl.sort_order, NOW(), tpl.template_type
FROM fire_maintenance_task task
JOIN fire_maintenance_template tpl
  ON tpl.level = 3 AND tpl.template_type IN ('0', '1', '2')
JOIN fire_maintenance_record parent_record
  ON parent_record.task_id = task.task_id
 AND parent_record.level = 2
 AND parent_record.template_id = tpl.parent_id
 AND parent_record.record_type = tpl.template_type;

UPDATE fire_maintenance_task
SET selected_system_ids = NULL,
    selected_fire_test_ids = NULL,
    task_status = '1',
    total_items = 449,
    completed_items = 0,
    normal_items = 0,
    fault_items = 0,
    no_device_items = 0,
    actual_end_time = NULL,
    update_time = NOW();

COMMIT;

SELECT task.task_id, task.task_name, task.total_items, task.completed_items,
       COUNT(CASE WHEN record.level = 3 THEN 1 END) AS actual_total,
       COUNT(CASE WHEN record.level = 3 AND record.record_type = '0' THEN 1 END) AS patrol_items,
       COUNT(CASE WHEN record.level = 3 AND record.record_type = '1' THEN 1 END) AS test_items,
       COUNT(CASE WHEN record.level = 3 AND record.record_type = '2' THEN 1 END) AS upkeep_items
FROM fire_maintenance_task task
LEFT JOIN fire_maintenance_record record ON record.task_id = task.task_id
GROUP BY task.task_id, task.task_name, task.total_items, task.completed_items
ORDER BY task.task_id DESC;
