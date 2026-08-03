-- Rename sidebar menu only: 巡检测试 -> 巡查测试
-- Target: sys_menu where perms = 'fire:inspection:view' AND menu_type = 'C'
-- Does not change button permissions (list/add/edit/remove/export) or page titles.

UPDATE sys_menu
SET menu_name = CONVERT(UNHEX('E5B7A1E69FA5E6B58BE8AF95') USING utf8mb4),
    update_by = 'admin',
    update_time = NOW()
WHERE perms = 'fire:inspection:view'
  AND menu_type = 'C';

-- Verify:
-- SELECT menu_id, menu_name, perms, menu_type FROM sys_menu WHERE perms = 'fire:inspection:view';
