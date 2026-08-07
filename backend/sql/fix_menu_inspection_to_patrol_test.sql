-- Rename sidebar + button menus: 巡检测试 -> 巡查测试
-- Target: sys_menu where perms like 'fire:inspection%'

UPDATE sys_menu
SET menu_name = CONVERT(UNHEX('E5B7A1E69FA5E6B58BE8AF95') USING utf8mb4),
    update_by = 'admin',
    update_time = NOW()
WHERE perms = 'fire:inspection:view'
  AND menu_type = 'C';

UPDATE sys_menu
SET menu_name = REPLACE(
        menu_name,
        CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95') USING utf8mb4),
        CONVERT(UNHEX('E5B7A1E69FA5E6B58BE8AF95') USING utf8mb4)
    ),
    update_by = 'admin',
    update_time = NOW()
WHERE perms LIKE 'fire:inspection%'
  AND menu_name LIKE CONCAT('%', CONVERT(UNHEX('E5B7A1E6A380E6B58BE8AF95') USING utf8mb4), '%');

-- Verify:
-- SELECT menu_id, menu_name, perms, menu_type FROM sys_menu WHERE perms LIKE 'fire:inspection%';
