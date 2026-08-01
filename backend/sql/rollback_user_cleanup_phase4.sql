-- ============================================================
-- Phase4 ROLLBACK: restore deleted users + task rows from bak_p4_*
-- ============================================================
SET NAMES utf8mb4;
SET @OLD_FK := @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

-- restore task fields from backup
UPDATE fire_maintenance_task t
  INNER JOIN bak_p4_task_before b ON b.task_id = t.task_id
   SET t.manager_id = b.manager_id,
       t.executor_id = b.executor_id,
       t.operator_ids = b.operator_ids,
       t.update_by = b.update_by,
       t.update_time = b.update_time;

-- restore users
INSERT INTO sys_user
SELECT * FROM bak_p4_sys_user b
WHERE NOT EXISTS (SELECT 1 FROM sys_user u WHERE u.user_id = b.user_id);

INSERT INTO sys_user_role
SELECT * FROM bak_p4_sys_user_role b
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role x WHERE x.user_id=b.user_id AND x.role_id=b.role_id);

INSERT INTO sys_user_post
SELECT * FROM bak_p4_sys_user_post b
WHERE NOT EXISTS (SELECT 1 FROM sys_user_post x WHERE x.user_id=b.user_id AND x.post_id=b.post_id);

INSERT INTO fire_user_company
SELECT * FROM bak_p4_fire_user_company b
WHERE NOT EXISTS (SELECT 1 FROM fire_user_company x WHERE x.id = b.id);

COMMIT;
SET FOREIGN_KEY_CHECKS = @OLD_FK;
SELECT 'users_after_rollback' AS s, user_id, login_name, del_flag FROM sys_user ORDER BY user_id;
