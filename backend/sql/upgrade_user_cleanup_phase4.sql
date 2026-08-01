-- ============================================================
-- Phase4: keep only user_id IN (1,7,16); backup + migrate + delete
-- MUST: mysql --default-character-set=utf8mb4 < this file
-- ============================================================
SET NAMES utf8mb4;
SET @OLD_FK := @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- 0) guard: keep targets must exist
SET @keep_admin := (SELECT COUNT(*) FROM sys_user WHERE user_id=1 AND login_name='admin' AND del_flag='0');
SET @keep_liu := (SELECT COUNT(*) FROM sys_user WHERE user_id=7 AND login_name='18782959011' AND del_flag='0');
SET @keep_glc := (SELECT COUNT(*) FROM sys_user WHERE user_id=16 AND login_name='13413462481' AND del_flag='0');
-- keep targets pre-checked: user_id 1,7,16

-- 1) backups (refresh)
DROP TABLE IF EXISTS bak_p4_sys_user;
CREATE TABLE bak_p4_sys_user AS SELECT * FROM sys_user WHERE user_id NOT IN (1,7,16);
DROP TABLE IF EXISTS bak_p4_sys_user_role;
CREATE TABLE bak_p4_sys_user_role AS SELECT * FROM sys_user_role WHERE user_id NOT IN (1,7,16);
DROP TABLE IF EXISTS bak_p4_sys_user_post;
CREATE TABLE bak_p4_sys_user_post AS SELECT * FROM sys_user_post WHERE user_id NOT IN (1,7,16);
DROP TABLE IF EXISTS bak_p4_fire_user_company;
CREATE TABLE bak_p4_fire_user_company AS SELECT * FROM fire_user_company WHERE user_id NOT IN (1,7,16);
DROP TABLE IF EXISTS bak_p4_task_before;
CREATE TABLE bak_p4_task_before AS
SELECT * FROM fire_maintenance_task
WHERE manager_id NOT IN (1,7,16)
   OR executor_id NOT IN (1,7,16)
   OR FIND_IN_SET('14', IFNULL(operator_ids,''))
   OR FIND_IN_SET('17', IFNULL(operator_ids,''))
   OR FIND_IN_SET('5', IFNULL(operator_ids,''))
   OR task_id = 49;

-- 2) migrate ACTIVE tasks only (status not done/cancelled)
-- 2a) Lin Qianbo (14) active managers -> Liu Zhen (7)
UPDATE fire_maintenance_task
   SET manager_id = 7,
       update_by = 'admin',
       update_time = NOW()
 WHERE manager_id = 14
   AND IFNULL(task_status,'0') NOT IN ('2','3');

-- 2b) task 49 / YY(17) active manager -> Guolicheng (16)
UPDATE fire_maintenance_task
   SET manager_id = 16,
       update_by = 'admin',
       update_time = NOW()
 WHERE manager_id = 17
   AND IFNULL(task_status,'0') NOT IN ('2','3');

-- 2c) active tasks: executor_id pointing to deleted users
UPDATE fire_maintenance_task
   SET executor_id = 16,
       update_by = 'admin',
       update_time = NOW()
 WHERE executor_id = 17
   AND IFNULL(task_status,'0') NOT IN ('2','3');

UPDATE fire_maintenance_task
   SET executor_id = 7,
       update_by = 'admin',
       update_time = NOW()
 WHERE executor_id = 14
   AND IFNULL(task_status,'0') NOT IN ('2','3');

-- 2d) active tasks: remove deleted user ids from operator_ids (keep history on done/cancelled)
UPDATE fire_maintenance_task
   SET operator_ids = TRIM(BOTH ',' FROM REPLACE(REPLACE(REPLACE(CONCAT(',', IFNULL(operator_ids,''), ','), ',17,', ','), ',14,', ','), ',,', ',')),
       update_by = 'admin',
       update_time = NOW()
 WHERE IFNULL(task_status,'0') NOT IN ('2','3')
   AND (
     FIND_IN_SET('17', IFNULL(operator_ids,''))
     OR FIND_IN_SET('14', IFNULL(operator_ids,''))
   );

-- ensure task 49 active has 16 in operators
UPDATE fire_maintenance_task
   SET operator_ids = CASE
         WHEN FIND_IN_SET('16', IFNULL(operator_ids,'')) THEN operator_ids
         WHEN IFNULL(operator_ids,'') = '' THEN '16'
         ELSE CONCAT(operator_ids, ',16')
       END,
       update_by = 'admin',
       update_time = NOW()
 WHERE task_id = 49
   AND IFNULL(task_status,'0') NOT IN ('2','3');

-- 3) Guolicheng display: keep login as name; note nick in remark
UPDATE sys_user
   SET user_name = login_name,
       remark = 'phase4 keep: nick was guolicheng; display=login_name',
       dept_id = 110,
       status = '0',
       del_flag = '0',
       allow_admin_login = '1',
       allow_mini_login = '1',
       audit_status = '0',
       update_by = 'admin',
       update_time = NOW()
 WHERE user_id = 16;

UPDATE sys_user
   SET dept_id = 110,
       status = '0',
       del_flag = '0',
       allow_admin_login = '1',
       allow_mini_login = '1',
       audit_status = '0',
       remark = 'phase4 keep: project_manager',
       update_by = 'admin',
       update_time = NOW()
 WHERE user_id = 7;

UPDATE sys_user
   SET dept_id = 100,
       status = '0',
       del_flag = '0',
       allow_admin_login = '1',
       remark = 'phase4 keep: admin',
       update_by = 'admin',
       update_time = NOW()
 WHERE user_id = 1;

-- 4) clear membership / role / post for users to delete
DELETE FROM fire_user_company WHERE user_id NOT IN (1,7,16);
DELETE FROM sys_user_role WHERE user_id NOT IN (1,7,16);
DELETE FROM sys_user_post WHERE user_id NOT IN (1,7,16);

-- optional online session rows
DELETE FROM sys_user_online WHERE login_name NOT IN (
  SELECT login_name FROM (SELECT login_name FROM sys_user WHERE user_id IN (1,7,16)) t
);

-- 5) physical delete other users (by user_id only)
DELETE FROM sys_user WHERE user_id NOT IN (1,7,16);

COMMIT;
SET FOREIGN_KEY_CHECKS = @OLD_FK;

-- 6) verify
SELECT 'kept_users' AS s, user_id, login_name, user_name, dept_id, status, del_flag FROM sys_user ORDER BY user_id;
SELECT 'user_count' AS s, COUNT(*) AS c FROM sys_user;
SELECT 'active_bad_manager' AS s, COUNT(*) AS c FROM fire_maintenance_task
 WHERE IFNULL(task_status,'0') NOT IN ('2','3')
   AND manager_id IS NOT NULL
   AND manager_id NOT IN (1,7,16);
SELECT 'task49' AS s, task_id, task_status, manager_id, operator_ids FROM fire_maintenance_task WHERE task_id=49;
SELECT 'lin_done_tasks' AS s, task_id, task_status, manager_id, operator_ids FROM fire_maintenance_task WHERE task_id IN (40,42,44);
SELECT 'task_cnt' AS s, COUNT(*) AS c FROM fire_maintenance_task;
SELECT 'fuc_cnt' AS s, COUNT(*) AS c FROM fire_user_company;
SELECT 'oper_log_cnt' AS s, COUNT(*) AS c FROM sys_oper_log;
