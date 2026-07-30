-- =============================================================================
-- Upgrade: project_manager 授权「信息维护」目录及子功能（幂等）
-- 包含：信息维护 / 建筑信息 / 设备信息 / 项目类别 及其按钮权限
-- 执行后项目负责人需重新登录以刷新 Shiro 授权与菜单缓存。
-- =============================================================================

SET NAMES utf8mb4;

SET @pm_role_id := (
    SELECT role_id FROM sys_role
    WHERE role_key = 'project_manager' AND del_flag = '0'
    ORDER BY role_id
    LIMIT 1
);

CREATE TABLE IF NOT EXISTS bak_pm_info_maintain_role_menu_20260730 (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

DROP TEMPORARY TABLE IF EXISTS tmp_pm_info_seed;
DROP TEMPORARY TABLE IF EXISTS tmp_pm_info_menus;
CREATE TEMPORARY TABLE tmp_pm_info_seed (
    menu_id BIGINT NOT NULL PRIMARY KEY
) ENGINE=Memory;
CREATE TEMPORARY TABLE tmp_pm_info_menus (
    menu_id BIGINT NOT NULL PRIMARY KEY
) ENGINE=Memory;

-- 目录「信息维护」及其子菜单/按钮（按 fire:building:view 的父链定位）
INSERT IGNORE INTO tmp_pm_info_seed (menu_id)
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

-- 兜底：按权限字符补全
INSERT IGNORE INTO tmp_pm_info_seed (menu_id)
SELECT m.menu_id
FROM sys_menu m
WHERE IFNULL(m.perms, '') LIKE 'fire:building:%'
   OR IFNULL(m.perms, '') LIKE 'fire:equipment:%'
   OR IFNULL(m.perms, '') LIKE 'fire:systemType:%';

-- 祖先目录写入另一张临时表（避免 reopen 同一临时表）
INSERT IGNORE INTO tmp_pm_info_menus (menu_id)
WITH RECURSIVE anc AS (
    SELECT m.menu_id, m.parent_id
    FROM sys_menu m
    INNER JOIN tmp_pm_info_seed s ON s.menu_id = m.menu_id
    UNION ALL
    SELECT p.menu_id, p.parent_id
    FROM sys_menu p
    INNER JOIN anc a ON p.menu_id = a.parent_id
    WHERE IFNULL(a.parent_id, 0) <> 0
)
SELECT menu_id FROM anc;

INSERT IGNORE INTO tmp_pm_info_menus (menu_id)
SELECT menu_id FROM tmp_pm_info_seed;

INSERT IGNORE INTO bak_pm_info_maintain_role_menu_20260730 (role_id, menu_id)
SELECT @pm_role_id, t.menu_id
FROM tmp_pm_info_menus t
WHERE @pm_role_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = @pm_role_id AND rm.menu_id = t.menu_id
  );

START TRANSACTION;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @pm_role_id, t.menu_id
FROM tmp_pm_info_menus t
WHERE @pm_role_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = @pm_role_id AND rm.menu_id = t.menu_id
  );

COMMIT;

DROP TEMPORARY TABLE IF EXISTS tmp_pm_info_seed;
DROP TEMPORARY TABLE IF EXISTS tmp_pm_info_menus;

SELECT m.menu_id, m.parent_id, m.menu_name, m.menu_type, m.perms, m.url
FROM sys_role_menu rm
INNER JOIN sys_role r ON r.role_id = rm.role_id AND r.role_key = 'project_manager' AND r.del_flag = '0'
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.menu_name IN ('信息维护', '建筑信息', '设备信息', '项目类别')
   OR IFNULL(m.perms, '') LIKE 'fire:building:%'
   OR IFNULL(m.perms, '') LIKE 'fire:equipment:%'
   OR IFNULL(m.perms, '') LIKE 'fire:systemType:%'
ORDER BY m.parent_id, m.order_num, m.menu_id;
