-- =============================================================================
-- Verify: project_manager has Info Maintain menus (building/equipment/systemType)
-- =============================================================================

SET NAMES utf8mb4;

SET @pm_role_id := (
    SELECT role_id FROM sys_role
    WHERE role_key = 'project_manager' AND del_flag = '0'
    ORDER BY role_id
    LIMIT 1
);

SET @info_root_id := (
    SELECT p.menu_id
    FROM sys_menu c
    INNER JOIN sys_menu p ON p.menu_id = c.parent_id
    WHERE c.perms = 'fire:building:view'
      AND c.menu_type = 'C'
      AND p.menu_type = 'M'
    ORDER BY p.menu_id
    LIMIT 1
);

-- 1) Root directory + three page menus
SELECT required.item,
       CASE WHEN EXISTS (
           SELECT 1
           FROM sys_role_menu rm
           INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
           WHERE rm.role_id = @pm_role_id
             AND (
                   (required.item = 'info_root' AND m.menu_id = @info_root_id)
                OR (required.item = 'building' AND m.perms = 'fire:building:view' AND m.menu_type = 'C')
                OR (required.item = 'equipment' AND m.perms = 'fire:equipment:view' AND m.menu_type = 'C')
                OR (required.item = 'systemType' AND m.perms = 'fire:systemType:view' AND m.menu_type = 'C')
             )
       ) THEN 'OK' ELSE 'MISSING' END AS grant_status
FROM (
    SELECT 'info_root' AS item UNION ALL
    SELECT 'building' UNION ALL
    SELECT 'equipment' UNION ALL
    SELECT 'systemType'
) required;

-- 2) Button permissions
SELECT required.perm,
       CASE WHEN rm.menu_id IS NULL THEN 'MISSING' ELSE 'OK' END AS grant_status
FROM (
    SELECT 'fire:building:view' AS perm UNION ALL SELECT 'fire:building:list' UNION ALL
    SELECT 'fire:building:add' UNION ALL SELECT 'fire:building:edit' UNION ALL
    SELECT 'fire:building:remove' UNION ALL SELECT 'fire:building:export' UNION ALL
    SELECT 'fire:equipment:view' UNION ALL SELECT 'fire:equipment:list' UNION ALL
    SELECT 'fire:equipment:add' UNION ALL SELECT 'fire:equipment:edit' UNION ALL
    SELECT 'fire:equipment:remove' UNION ALL SELECT 'fire:equipment:export' UNION ALL
    SELECT 'fire:systemType:view' UNION ALL SELECT 'fire:systemType:list' UNION ALL
    SELECT 'fire:systemType:add' UNION ALL SELECT 'fire:systemType:edit' UNION ALL
    SELECT 'fire:systemType:remove'
) required
LEFT JOIN sys_menu m ON m.perms = required.perm
LEFT JOIN sys_role_menu rm ON rm.menu_id = m.menu_id AND rm.role_id = @pm_role_id
ORDER BY required.perm;

-- 3) Missing summary (expect empty)
SELECT required.perm AS missing_perm
FROM (
    SELECT 'fire:building:view' AS perm UNION ALL SELECT 'fire:building:list' UNION ALL
    SELECT 'fire:building:add' UNION ALL SELECT 'fire:building:edit' UNION ALL
    SELECT 'fire:building:remove' UNION ALL SELECT 'fire:building:export' UNION ALL
    SELECT 'fire:equipment:view' UNION ALL SELECT 'fire:equipment:list' UNION ALL
    SELECT 'fire:equipment:add' UNION ALL SELECT 'fire:equipment:edit' UNION ALL
    SELECT 'fire:equipment:remove' UNION ALL SELECT 'fire:equipment:export' UNION ALL
    SELECT 'fire:systemType:view' UNION ALL SELECT 'fire:systemType:list' UNION ALL
    SELECT 'fire:systemType:add' UNION ALL SELECT 'fire:systemType:edit' UNION ALL
    SELECT 'fire:systemType:remove'
) required
LEFT JOIN sys_menu m ON m.perms = required.perm
LEFT JOIN sys_role_menu rm ON rm.menu_id = m.menu_id AND rm.role_id = @pm_role_id
WHERE rm.menu_id IS NULL;
