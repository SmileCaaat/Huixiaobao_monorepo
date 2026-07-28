-- Rollback employee register invite feature (best-effort)
SET NAMES utf8mb4;

DELETE rm FROM sys_role_menu rm
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.perms IN (
  'system:dept:registerQrcode:view',
  'system:dept:registerQrcode:download',
  'system:dept:registerQrcode:manage',
  'system:user:registerRecord'
);

DELETE FROM sys_menu
WHERE perms IN (
  'system:dept:registerQrcode:view',
  'system:dept:registerQrcode:download',
  'system:dept:registerQrcode:manage',
  'system:user:registerRecord'
);

DROP TABLE IF EXISTS sys_dept_register_invite;

-- Optional: drop unique phone index if created by upgrade
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND INDEX_NAME = 'uk_sys_user_phonenumber'
);
SET @sql := IF(@idx_exists > 0,
  'ALTER TABLE sys_user DROP INDEX uk_sys_user_phonenumber',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Columns kept by default (safer). Uncomment to hard-drop:
-- ALTER TABLE sys_user
--   DROP COLUMN register_source,
--   DROP COLUMN register_invite_id,
--   DROP COLUMN audit_by,
--   DROP COLUMN audit_time,
--   DROP COLUMN audit_remark,
--   DROP COLUMN dispatchable;
