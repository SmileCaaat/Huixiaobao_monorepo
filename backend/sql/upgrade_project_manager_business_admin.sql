-- =============================================================================
-- Upgrade: project_manager business admin menus (idempotent)
-- Locate menus by role_key / perms / remark / parent-child (no fixed menu_id)
-- Does NOT grant fire:task:completeAll (one-click complete stays system admin only)
-- Does NOT remove existing grants; does NOT grant role/menu/config/dict/post/monitor/tool
-- =============================================================================
-- On failure: ROLLBACK. On success: COMMIT. Users must re-login (or clear Shiro auth cache).
-- =============================================================================

SET NAMES utf8mb4;
SET @OLD_FK := @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ---------------------------------------------------------------------------
-- 0) Role flags + account allow_admin_login
-- ---------------------------------------------------------------------------
UPDATE sys_role
SET status = '0',
    del_flag = '0',
    update_by = 'admin',
    update_time = NOW(),
    remark = CASE
        WHEN IFNULL(remark, '') LIKE '%pm_biz_admin%' THEN remark
        ELSE CONCAT(IFNULL(remark, ''), ' | pm_biz_admin')
    END
WHERE role_key = 'project_manager'
  AND del_flag = '0';
-- NOTE: data_scope is owned by upgrade_project_manager_company_scope.sql (value 6).
-- Do NOT set data_scope here, to avoid applying scope-6 before the backend recognizes it.

INSERT INTO sys_role (
    role_name, role_key, role_sort, data_scope, status, del_flag,
    create_by, create_time, remark
)
SELECT
    CONVERT(UNHEX('E9A1B9E79BAEE8B49FE8B4A3E4BABA') USING utf8mb4),
    'project_manager',
    2,
    '6',
    '0',
    '0',
    'admin',
    NOW(),
    'pm_biz_admin'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE role_key = 'project_manager' AND del_flag = '0'
);

UPDATE sys_user u
INNER JOIN sys_user_role ur ON ur.user_id = u.user_id
INNER JOIN sys_role r ON r.role_id = ur.role_id
                     AND r.role_key = 'project_manager'
                     AND r.del_flag = '0'
SET u.allow_admin_login = '1',
    u.update_by = 'admin',
    u.update_time = NOW()
WHERE IFNULL(u.del_flag, '0') = '0';

SET @pm_role_id := (
    SELECT role_id FROM sys_role
    WHERE role_key = 'project_manager' AND del_flag = '0'
    ORDER BY role_id
    LIMIT 1
);

-- ---------------------------------------------------------------------------
-- 1) Backup current project_manager sys_role_menu
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bak_pm_biz_admin_role_menu_20260730 (
    bak_id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    bak_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (bak_id),
    UNIQUE KEY uk_bak_pm_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PM role_menu backup before biz-admin upgrade';

INSERT INTO bak_pm_biz_admin_role_menu_20260730 (role_id, menu_id)
SELECT rm.role_id, rm.menu_id
FROM sys_role_menu rm
WHERE rm.role_id = @pm_role_id
  AND NOT EXISTS (
      SELECT 1 FROM bak_pm_biz_admin_role_menu_20260730 b
      WHERE b.role_id = rm.role_id AND b.menu_id = rm.menu_id
  );

-- ---------------------------------------------------------------------------
-- 2) Collect target menus into seed temp table (no self read/write)
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_pm_biz_seed;
CREATE TEMPORARY TABLE tmp_pm_biz_seed (
    menu_id BIGINT NOT NULL PRIMARY KEY
) ENGINE=Memory;

-- 2.1 Customer+contract tree (top M ancestor of fire:company:view)
INSERT IGNORE INTO tmp_pm_biz_seed (menu_id)
WITH RECURSIVE up_cc AS (
    SELECT m.menu_id, m.parent_id, m.menu_type
    FROM sys_menu m
    WHERE m.perms = 'fire:company:view' AND m.menu_type = 'C'
    UNION ALL
    SELECT p.menu_id, p.parent_id, p.menu_type
    FROM sys_menu p
    INNER JOIN up_cc c ON p.menu_id = c.parent_id
),
root_cc AS (
    SELECT menu_id
    FROM (
        SELECT menu_id
        FROM up_cc
        WHERE menu_type = 'M' AND IFNULL(parent_id, 0) = 0
        ORDER BY menu_id
        LIMIT 1
    ) x
),
tree_cc AS (
    SELECT m.menu_id
    FROM sys_menu m
    INNER JOIN root_cc r ON m.menu_id = r.menu_id
    UNION ALL
    SELECT c.menu_id
    FROM sys_menu c
    INNER JOIN tree_cc t ON c.parent_id = t.menu_id
)
SELECT menu_id FROM tree_cc;

INSERT IGNORE INTO tmp_pm_biz_seed (menu_id)
SELECT p.menu_id
FROM sys_menu c
INNER JOIN sys_menu p ON p.menu_id = c.parent_id AND p.menu_type = 'M'
WHERE c.perms = 'fire:company:view' AND c.menu_type = 'C';

INSERT IGNORE INTO tmp_pm_biz_seed (menu_id)
SELECT m.menu_id
FROM sys_menu m
WHERE m.perms IN (
    'fire:company:view', 'fire:company:list', 'fire:company:add', 'fire:company:edit',
    'fire:company:remove', 'fire:company:export', 'fire:company:assign',
    'fire:contract:view', 'fire:contract:list', 'fire:contract:add', 'fire:contract:edit',
    'fire:contract:remove', 'fire:contract:export', 'fire:contract:renew', 'fire:contract:terminate'
);

-- 2.2 Fire management tree (top M ancestor of fire:task:view), exclude completeAll
INSERT IGNORE INTO tmp_pm_biz_seed (menu_id)
WITH RECURSIVE up_fire AS (
    SELECT m.menu_id, m.parent_id, m.menu_type
    FROM sys_menu m
    WHERE m.perms = 'fire:task:view' AND m.menu_type = 'C'
    UNION ALL
    SELECT p.menu_id, p.parent_id, p.menu_type
    FROM sys_menu p
    INNER JOIN up_fire c ON p.menu_id = c.parent_id
),
root_fire AS (
    SELECT menu_id
    FROM (
        SELECT menu_id
        FROM up_fire
        WHERE menu_type = 'M' AND IFNULL(parent_id, 0) = 0
        ORDER BY menu_id
        LIMIT 1
    ) x
),
tree_fire AS (
    SELECT m.menu_id, m.perms
    FROM sys_menu m
    INNER JOIN root_fire r ON m.menu_id = r.menu_id
    UNION ALL
    SELECT c.menu_id, c.perms
    FROM sys_menu c
    INNER JOIN tree_fire t ON c.parent_id = t.menu_id
)
SELECT menu_id
FROM tree_fire
WHERE IFNULL(perms, '') <> 'fire:task:completeAll';

INSERT IGNORE INTO tmp_pm_biz_seed (menu_id)
SELECT m.menu_id
FROM sys_menu m
WHERE (
        m.perms LIKE 'fire:inspection:%'
     OR m.perms LIKE 'fire:repair:%'
     OR m.perms LIKE 'fire:task:%'
     OR m.perms LIKE 'fire:report:%'
     OR m.perms LIKE 'fire:checkIn:%'
     OR m.perms LIKE 'fire:building:%'
     OR m.perms LIKE 'fire:equipment:%'
     OR m.perms LIKE 'fire:systemType:%'
  )
  AND IFNULL(m.perms, '') <> 'fire:task:completeAll';

-- 2.2b Info maintain directory (ancestor of fire:building:view)
INSERT IGNORE INTO tmp_pm_biz_seed (menu_id)
WITH RECURSIVE up_info AS (
    SELECT m.menu_id, m.parent_id, m.menu_type
    FROM sys_menu m
    WHERE m.perms = 'fire:building:view' AND m.menu_type = 'C'
    UNION ALL
    SELECT p.menu_id, p.parent_id, p.menu_type
    FROM sys_menu p
    INNER JOIN up_info c ON p.menu_id = c.parent_id
),
root_info AS (
    SELECT menu_id
    FROM (
        SELECT menu_id
        FROM up_info
        WHERE menu_type = 'M' AND IFNULL(parent_id, 0) = 0
        ORDER BY menu_id
        LIMIT 1
    ) x
),
tree_info AS (
    SELECT m.menu_id
    FROM sys_menu m
    INNER JOIN root_info r ON m.menu_id = r.menu_id
    UNION ALL
    SELECT c.menu_id
    FROM sys_menu c
    INNER JOIN tree_info t ON c.parent_id = t.menu_id
)
SELECT menu_id FROM tree_info;

-- 2.3 System: org + register only
INSERT IGNORE INTO tmp_pm_biz_seed (menu_id)
SELECT m.menu_id
FROM sys_menu m
WHERE m.perms IN (
    'system:dept:view', 'system:dept:list', 'system:dept:add', 'system:dept:edit', 'system:dept:remove',
    'system:dept:registerQrcode:view', 'system:dept:registerQrcode:download', 'system:dept:registerQrcode:manage',
    'system:user:registerRecord', 'system:user:audit'
);

INSERT IGNORE INTO tmp_pm_biz_seed (menu_id)
SELECT m.menu_id
FROM sys_menu m
WHERE m.remark = 'user_mgmt_directory' AND m.menu_type = 'M';

INSERT IGNORE INTO tmp_pm_biz_seed (menu_id)
WITH RECURSIVE up_sys AS (
    SELECT m.menu_id, m.parent_id, m.menu_type
    FROM sys_menu m
    WHERE m.perms = 'system:dept:view' AND m.menu_type = 'C'
    UNION ALL
    SELECT p.menu_id, p.parent_id, p.menu_type
    FROM sys_menu p
    INNER JOIN up_sys c ON p.menu_id = c.parent_id
)
SELECT menu_id
FROM up_sys
WHERE menu_type = 'M' AND IFNULL(parent_id, 0) = 0;

-- ---------------------------------------------------------------------------
-- 3) Expand ancestors into final temp (seed is read-only here)
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_pm_biz_menus;
CREATE TEMPORARY TABLE tmp_pm_biz_menus (
    menu_id BIGINT NOT NULL PRIMARY KEY
) ENGINE=Memory;

INSERT IGNORE INTO tmp_pm_biz_menus (menu_id)
WITH RECURSIVE anc AS (
    SELECT m.menu_id, m.parent_id
    FROM sys_menu m
    INNER JOIN tmp_pm_biz_seed s ON s.menu_id = m.menu_id
    UNION ALL
    SELECT p.menu_id, p.parent_id
    FROM sys_menu p
    INNER JOIN anc a ON p.menu_id = a.parent_id
    WHERE IFNULL(a.parent_id, 0) <> 0
)
SELECT menu_id FROM anc;

INSERT IGNORE INTO tmp_pm_biz_menus (menu_id)
SELECT menu_id FROM tmp_pm_biz_seed;

-- ---------------------------------------------------------------------------
-- 4) Strip forbidden menus
-- ---------------------------------------------------------------------------
DELETE t
FROM tmp_pm_biz_menus t
INNER JOIN sys_menu m ON m.menu_id = t.menu_id
WHERE m.perms = 'fire:task:completeAll'
   OR IFNULL(m.perms, '') LIKE 'system:role%'
   OR IFNULL(m.perms, '') LIKE 'system:menu%'
   OR IFNULL(m.perms, '') LIKE 'system:config%'
   OR IFNULL(m.perms, '') LIKE 'system:dict%'
   OR IFNULL(m.perms, '') LIKE 'system:post%'
   OR IFNULL(m.perms, '') LIKE 'monitor:%'
   OR IFNULL(m.perms, '') LIKE 'tool:%'
   OR (
        IFNULL(m.perms, '') LIKE 'system:user:%'
        AND m.perms NOT IN ('system:user:registerRecord', 'system:user:audit')
   )
   OR IFNULL(m.url, '') IN (
        '/system/role', '/system/menu', '/system/dict', '/system/config',
        '/system/post', '/system/notice', '/system/user'
   );

-- ---------------------------------------------------------------------------
-- 5) Grant (keep existing)
-- ---------------------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @pm_role_id, t.menu_id
FROM tmp_pm_biz_menus t
WHERE @pm_role_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = @pm_role_id AND rm.menu_id = t.menu_id
  );


UPDATE sys_role
SET status = '0', del_flag = '0'
WHERE role_key = 'project_manager' AND del_flag = '0';
-- data_scope intentionally unchanged here (see upgrade_project_manager_company_scope.sql)

DROP TEMPORARY TABLE IF EXISTS tmp_pm_biz_seed;
DROP TEMPORARY TABLE IF EXISTS tmp_pm_biz_menus;

SET FOREIGN_KEY_CHECKS = @OLD_FK;

SELECT r.role_key, r.data_scope, r.status, r.del_flag, COUNT(rm.menu_id) AS menu_cnt
FROM sys_role r
LEFT JOIN sys_role_menu rm ON rm.role_id = r.role_id
WHERE r.role_key = 'project_manager' AND r.del_flag = '0'
GROUP BY r.role_id, r.role_key, r.data_scope, r.status, r.del_flag;

SELECT m.menu_type, m.perms, m.url, m.remark, m.menu_name
FROM sys_role_menu rm
INNER JOIN sys_role r ON r.role_id = rm.role_id AND r.role_key = 'project_manager' AND r.del_flag = '0'
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.perms IN (
    'fire:company:view', 'fire:contract:view', 'fire:task:view', 'fire:repair:view',
    'fire:checkIn:view', 'fire:report:view', 'fire:inspection:view',
    'system:dept:view', 'system:user:registerRecord', 'system:user:audit',
    'system:dept:registerQrcode:view', 'fire:task:completeAll',
    'fire:building:view', 'fire:equipment:view', 'fire:systemType:view'
)
   OR m.remark = 'user_mgmt_directory'
   OR (m.menu_type = 'M' AND IFNULL(m.parent_id, 0) = 0)
ORDER BY m.parent_id, m.order_num, m.menu_id;

-- Preview queries above ran inside the transaction.
-- Success: run COMMIT;
-- Failure: run ROLLBACK;
-- COMMIT;
