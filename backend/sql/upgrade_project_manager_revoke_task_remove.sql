-- =============================================================================
-- DEPRECATED: 请勿再执行。
-- 原先整权收回 fire:task:remove 会导致顶部工具栏「删除」一并消失。
-- 现改为仅在 task.html 对项目负责人隐藏操作列行内删除；顶部批量删除保留。
-- 若已执行过本脚本，请运行 rollback_project_manager_revoke_task_remove.sql 恢复授权。
-- =============================================================================

SELECT 'DEPRECATED: use UI rowRemoveFlag for project_manager; restore fire:task:remove via rollback if needed' AS notice;
