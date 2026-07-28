-- Employee register invite QR + user audit fields (idempotent)
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 1) Department register invite table
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_dept_register_invite (
  invite_id        BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT 'invite id',
  company_dept_id  BIGINT(20)   NOT NULL COMMENT 'company dept_id (sys_dept root under company)',
  dept_id          BIGINT(20)   NOT NULL COMMENT 'target department dept_id',
  token_hash       VARCHAR(64)  NOT NULL COMMENT 'SHA-256 of raw token',
  invite_code      VARCHAR(16)  NOT NULL COMMENT 'short invite code',
  register_mode    CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0 auto pass / 1 manual audit',
  status           CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0 enabled / 1 disabled',
  expire_time      DATETIME     NOT NULL COMMENT 'expire time (create + 7 days)',
  use_count        INT(11)      NOT NULL DEFAULT 0 COMMENT 'used count',
  use_limit        INT(11)      NOT NULL DEFAULT 0 COMMENT '0=unlimited',
  create_by        VARCHAR(64)  DEFAULT '' COMMENT 'creator',
  create_time      DATETIME     DEFAULT NULL COMMENT 'create time',
  last_use_time    DATETIME     DEFAULT NULL COMMENT 'last use time',
  remark           VARCHAR(500) DEFAULT NULL COMMENT 'remark',
  PRIMARY KEY (invite_id),
  UNIQUE KEY uk_token_hash (token_hash),
  UNIQUE KEY uk_invite_code (invite_code),
  KEY idx_dept_id (dept_id),
  KEY idx_expire_status (expire_time, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='department register invite / QR';

-- ---------------------------------------------------------------------------
-- 2) sys_user incremental columns
-- ---------------------------------------------------------------------------
SET @sql := (
  SELECT IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'register_source'),
    'SELECT 1',
    'ALTER TABLE sys_user ADD COLUMN register_source VARCHAR(20) DEFAULT NULL COMMENT ''qr/invite_code/direct/admin'' AFTER audit_status'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'register_invite_id'),
    'SELECT 1',
    'ALTER TABLE sys_user ADD COLUMN register_invite_id BIGINT(20) DEFAULT NULL COMMENT ''invite id'' AFTER register_source'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'audit_by'),
    'SELECT 1',
    'ALTER TABLE sys_user ADD COLUMN audit_by VARCHAR(64) DEFAULT NULL COMMENT ''auditor login name'' AFTER register_invite_id'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'audit_time'),
    'SELECT 1',
    'ALTER TABLE sys_user ADD COLUMN audit_time DATETIME DEFAULT NULL COMMENT ''audit time'' AFTER audit_by'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'audit_remark'),
    'SELECT 1',
    'ALTER TABLE sys_user ADD COLUMN audit_remark VARCHAR(500) DEFAULT NULL COMMENT ''audit remark / reject reason'' AFTER audit_time'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'dispatchable'),
    'SELECT 1',
    'ALTER TABLE sys_user ADD COLUMN dispatchable CHAR(1) NOT NULL DEFAULT ''1'' COMMENT ''0 cannot dispatch / 1 can dispatch'' AFTER audit_remark'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Historical accounts: approved + dispatchable
UPDATE sys_user
SET audit_status = IFNULL(NULLIF(audit_status, ''), '0'),
    dispatchable = '1'
WHERE del_flag = '0'
  AND (dispatchable IS NULL OR dispatchable = '' OR audit_status IS NULL OR audit_status = '');

UPDATE sys_user
SET dispatchable = '0'
WHERE del_flag = '0'
  AND audit_status = '1'
  AND (dispatchable IS NULL OR dispatchable = '' OR dispatchable = '1');

-- Duplicate phone report (do NOT auto-add unique index if duplicates exist)
SELECT phonenumber, COUNT(*) AS cnt
FROM sys_user
WHERE del_flag = '0'
  AND phonenumber IS NOT NULL
  AND phonenumber <> ''
GROUP BY phonenumber
HAVING COUNT(*) > 1;

-- Soft unique index when no duplicates (skip if exists or duplicates remain)
SET @dup_cnt := (
  SELECT COUNT(*) FROM (
    SELECT phonenumber
    FROM sys_user
    WHERE del_flag = '0' AND phonenumber IS NOT NULL AND phonenumber <> ''
    GROUP BY phonenumber
    HAVING COUNT(*) > 1
  ) t
);
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND INDEX_NAME = 'uk_sys_user_phonenumber'
);
SET @sql := IF(@dup_cnt = 0 AND @idx_exists = 0,
  'ALTER TABLE sys_user ADD UNIQUE INDEX uk_sys_user_phonenumber (phonenumber)',
  'SELECT ''skip phonenumber unique index'' AS info');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- 3) Menus: register QR perms + register records page
-- ---------------------------------------------------------------------------
-- Buttons under system dept menu (parent by perms system:dept:view C)
INSERT INTO sys_menu (
    menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh,
    perms, icon, create_by, create_time, remark
)
SELECT
    CONVERT(UNHEX('E6B3A8E5868CE4BA8CE7BBB4E7A081E69FA5E79C8B') USING utf8mb4),
    p.menu_id, 20, '#', '', 'F', '0', '1',
    'system:dept:registerQrcode:view', '#', 'admin', NOW(), 'dept register qrcode view'
FROM sys_menu p
WHERE p.perms = 'system:dept:view' AND p.menu_type = 'C'
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'system:dept:registerQrcode:view');

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh,
    perms, icon, create_by, create_time, remark
)
SELECT
    CONVERT(UNHEX('E6B3A8E5868CE4BA8CE7BBB4E7A081E4B88BE8BDBD') USING utf8mb4),
    p.menu_id, 21, '#', '', 'F', '0', '1',
    'system:dept:registerQrcode:download', '#', 'admin', NOW(), 'dept register qrcode download'
FROM sys_menu p
WHERE p.perms = 'system:dept:view' AND p.menu_type = 'C'
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'system:dept:registerQrcode:download');

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh,
    perms, icon, create_by, create_time, remark
)
SELECT
    CONVERT(UNHEX('E6B3A8E5868CE4BA8CE7BBB4E7A081E7AEA1E79086') USING utf8mb4),
    p.menu_id, 22, '#', '', 'F', '0', '1',
    'system:dept:registerQrcode:manage', '#', 'admin', NOW(), 'dept register qrcode manage'
FROM sys_menu p
WHERE p.perms = 'system:dept:view' AND p.menu_type = 'C'
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'system:dept:registerQrcode:manage');

-- Register records menu under system user parent
INSERT INTO sys_menu (
    menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh,
    perms, icon, create_by, create_time, remark
)
SELECT
    CONVERT(UNHEX('E6B3A8E5868CE8AEB0E5BD95') USING utf8mb4),
    p.menu_id, 12, '/system/user/registerRecord', 'menuItem', 'C', '0', '1',
    'system:user:registerRecord', 'fa fa-list-alt', 'admin', NOW(), 'employee register records'
FROM sys_menu p
WHERE p.perms = 'system:user:view' AND p.menu_type = 'C'
  AND NOT EXISTS (SELECT 1 FROM sys_menu x WHERE x.perms = 'system:user:registerRecord');

-- Grant QR + register record to admin / project_manager / team_leader
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.del_flag = '0'
  AND r.role_key IN ('admin', 'project_manager', 'team_leader')
  AND m.perms IN (
    'system:dept:registerQrcode:view',
    'system:dept:registerQrcode:download',
    'system:dept:registerQrcode:manage',
    'system:user:registerRecord',
    'system:user:audit'
  )
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );
