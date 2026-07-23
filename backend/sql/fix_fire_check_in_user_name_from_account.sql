-- �� fire_check_in.user_name ������ sys_user.user_name ����
-- ִ��ǰ���ȱ��������⣻���ű�Ĭ�ϲ��Զ�ִ�С�
--
-- Ԥ�죺��������
-- SELECT COUNT(*) AS mismatch_count
-- FROM fire_check_in c
-- LEFT JOIN sys_user u ON u.user_id = c.user_id
-- WHERE c.del_flag = '0'
--   AND COALESCE(c.user_name, '') <> COALESCE(u.user_name, '');
--
-- Ԥ�죺������ϸ
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
