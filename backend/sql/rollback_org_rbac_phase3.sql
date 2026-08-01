-- ============================================================
-- Phase3 ROLLBACK from bak_p3_* tables
-- MUST: mysql --default-character-set=utf8mb4 < this file
-- Restores dept/role/post/user links; does NOT touch fire_* biz tables
-- ============================================================
SET NAMES utf8mb4;
SET @OLD_FK := @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- Prerequisite: bak_p3_* tables from upgrade_org_rbac_phase3.sql

DELETE FROM sys_user_role;
INSERT INTO sys_user_role SELECT * FROM bak_p3_sys_user_role;

DELETE FROM sys_user_post;
INSERT INTO sys_user_post SELECT * FROM bak_p3_sys_user_post;

DELETE FROM sys_role_menu;
INSERT INTO sys_role_menu SELECT * FROM bak_p3_sys_role_menu;

DELETE FROM sys_role_dept;
INSERT INTO sys_role_dept SELECT * FROM bak_p3_sys_role_dept;

-- restore core rows by primary key upsert pattern
DELETE FROM sys_user;
INSERT INTO sys_user SELECT * FROM bak_p3_sys_user;

DELETE FROM sys_role;
INSERT INTO sys_role SELECT * FROM bak_p3_sys_role;

DELETE FROM sys_post;
INSERT INTO sys_post SELECT * FROM bak_p3_sys_post;

DELETE FROM sys_dept;
INSERT INTO sys_dept SELECT * FROM bak_p3_sys_dept;

COMMIT;
SET FOREIGN_KEY_CHECKS = @OLD_FK;

SELECT 'rollback_done' AS s, COUNT(*) users FROM sys_user;
SELECT dept_id, dept_name, del_flag FROM sys_dept ORDER BY dept_id;
SELECT role_id, role_key, del_flag FROM sys_role ORDER BY role_id;
