-- 将 fire_check_in.user_name 快照与 sys_user.user_name 对齐
-- 执行前请先备份生产库；本脚本默认不自动执行。
--
-- 预检：差异数量
-- SELECT COUNT(*) AS mismatch_count
-- FROM fire_check_in c
-- LEFT JOIN sys_user u ON u.user_id = c.user_id
-- WHERE c.del_flag = '0'
--   AND COALESCE(c.user_name, '') <> COALESCE(u.user_name, '');
--
-- 预检：差异明细
-- SELECT
--     c.check_in_id,
--     c.user_id,
--     c.user_name AS stored_name,
--     u.user_name AS account_name
-- FROM fire_check_in c
-- LEFT JOIN sys_user u ON u.user_id = c.user_id
-- WHERE c.del_flag = '0'
--   AND COALESCE(c.user_name, '') <> COALESCE(u.user_name, '');

UPDATE fire_check_in c
INNER JOIN sys_user u ON u.user_id = c.user_id
SET c.user_name = u.user_name
WHERE c.del_flag = '0'
  AND u.del_flag = '0'
  AND COALESCE(c.user_name, '') <> COALESCE(u.user_name, '');
