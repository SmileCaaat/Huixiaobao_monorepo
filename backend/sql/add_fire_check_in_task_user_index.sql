-- ά��ǩ��������+��Ա���/������ѯ����
-- ִ��ǰ���ȱ��������⣻���ű�Ĭ�ϲ��Զ�ִ�С�
--
-- Ԥ���Ƿ��Ѵ���ͬ��������
-- SHOW INDEX FROM fire_check_in WHERE Key_name = 'idx_fire_check_in_task_user_time';

ALTER TABLE fire_check_in
    ADD INDEX idx_fire_check_in_task_user_time (task_id, user_id, check_in_time, del_flag);
