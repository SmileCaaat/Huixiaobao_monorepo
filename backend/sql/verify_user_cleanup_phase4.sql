-- Phase4 verify
SET NAMES utf8mb4;
SELECT user_id, login_name, user_name, dept_id, status, del_flag FROM sys_user ORDER BY user_id;
SELECT COUNT(*) AS user_count FROM sys_user;
SELECT COUNT(*) AS bad_active_managers FROM fire_maintenance_task
 WHERE IFNULL(task_status,'0') NOT IN ('2','3') AND manager_id IS NOT NULL AND manager_id NOT IN (1,7,16);
SELECT task_id, task_status, manager_id, operator_ids FROM fire_maintenance_task WHERE task_id IN (40,42,44,49,51,52);
SELECT COUNT(*) AS tasks FROM fire_maintenance_task;
SELECT COUNT(*) AS fuc FROM fire_user_company WHERE user_id NOT IN (1,7,16);
