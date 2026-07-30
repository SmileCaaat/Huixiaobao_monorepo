-- =============================================================================
-- 回滚：项目负责人业务超级管理员权限升级
-- 将 project_manager 的 sys_role_menu 恢复为升级前备份表内容
-- =============================================================================
SET NAMES utf8mb4;
SET @OLD_FK := @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

SET @pm_role_id := (
    SELECT role_id FROM sys_role
    WHERE role_key = 'project_manager' AND del_flag = '0'
    ORDER BY role_id
    LIMIT 1
);

-- 若无备份表则中止（避免误清空）
SET @bak_exists := (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'bak_pm_biz_admin_role_menu_20260730'
);

-- 仅当备份存在时回滚权限
DELETE rm
FROM sys_role_menu rm
WHERE rm.role_id = @pm_role_id
  AND @bak_exists > 0
  AND @pm_role_id IS NOT NULL;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT b.role_id, b.menu_id
FROM bak_pm_biz_admin_role_menu_20260730 b
WHERE @bak_exists > 0
  AND b.role_id = @pm_role_id
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = b.role_id AND rm.menu_id = b.menu_id
  );

-- 清理升级脚本在 remark 追加的标记（可选，不影响权限）
UPDATE sys_role
SET remark = TRIM(BOTH ' ' FROM REPLACE(REPLACE(IFNULL(remark, ''), ' | pm_biz_admin', ''), 'pm_biz_admin', '')),
    update_by = 'admin',
    update_time = NOW()
WHERE role_key = 'project_manager' AND del_flag = '0';

-- 保持角色仍为有效业务角色（回滚只还原菜单，不改 data_scope 以外的身份）
UPDATE sys_role
SET status = '0', del_flag = '0', data_scope = '1'
WHERE role_key = 'project_manager' AND del_flag = '0';

SELECT
    @bak_exists AS bak_table_exists,
    @pm_role_id AS project_manager_role_id,
    (SELECT COUNT(*) FROM sys_role_menu WHERE role_id = @pm_role_id) AS restored_menu_cnt;

SET FOREIGN_KEY_CHECKS = @OLD_FK;

-- 成功请执行：COMMIT;
-- 失败请执行：ROLLBACK;
-- 若 @bak_exists=0，请勿 COMMIT（本次未安全回滚）。
-- COMMIT;
