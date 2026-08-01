# Phase3 ��ԱǨ���嵥�� ID ӳ��

> �����Ա���/Զ�� `dev_manager` ʵ�顣ִ�� `upgrade_org_rbac_phase3.sql` ǰ���ٺ˶ԡ�

## ���� ID ӳ��

| �� | ���� | �´��� |
|---|---|---|
| 100 | �����ܹ�˾ | **���� ID**������Ϊ���л��������㶫�������Ƽ����޹�˾�� |
| 110 | ά���� | **���� ID**��parent=100 |
| �½� | ������˾ | parent=0�����Զ��¼� |
| 101 | �ɶ��ֹ�˾ | �߼�ɾ�� del_flag=2 |
| 103 | �л����������ڳɶ��µ��ظ��ڵ㣩 | �߼�ɾ������ԱǨ�� |
| 111/112 | ��/�ⳡά���� | �߼�ɾ������ԱǨ�� 110 |
| 102,104-109,113 | ��ʾ/ͣ�ò��� | �߼�ɾ�� |

## ��ɫ ID ӳ��

| �� role_key | role_id | �� |
|---|---|---|
| admin | 1 | ������sort=1 |
| project_manager | 4 | ������data_scope **1**��sort=2 |
| team_leader | 5 | ������data_scope 5��sort=3 |
| fire_operator | 6 | **role_key �� maintenance_member**������ά����Ա��sort=4 |
| common / fire_dept_admin / dept_staff / auditor | 2/3/7/8 | del_flag=2 ͣ�� |

## ��λ����ӳ��

| �� post_code | �� |
|---|---|
| ceo/se/hr/user/staff/wb_director/pm_*/tl_*/wb_member | status=1 ͣ�� |
| ���½���PROJECT_MANAGER / MAINTENANCE_LEADER / MAINTENANCE_MEMBER | ���� |

## �û�Ǩ��

| user_id | �˺� | ���� | ����ժҪ | ���� |
|---|---|---|---|---|
| 1 | admin | ˫�� | ���� | dept��**100**����ɫ admin�����λ |
| 5 | mouchao | Զ�̻�Ծ/������ɾ | fuc��2����¼27������19������0 | **ͣ��**�����ɫ��λ����ɾ�� |
| 7 | ����/18782959011 | ˫�� | ����2��fuc���������� PM | dept��**110**����ɫ project_manager����λ PROJECT_MANAGER |
| 14 | ��ǭ��/18318246781 | Զ�� | ���� manager��3����3���� fuc | dept��**110**��**���ɫ**���˹�ȷ�ϣ����Զ� PM�� |
| 16 | 13413462481 | ˫�� | ������� | dept��**110**��maintenance_member��MAINTENANCE_MEMBER |
| 17 | YY / 19076055157 | ˫��ͬ ID/�ֻ� | ���� TL��Զ���޽�ɫ��������� | **ͬһ��Ȼ��**��ͳһ TL + 110��������ϲ� user_id |
| 6,8,9,11 | �ֻ��� | ˫�� | �� fuc����/������ | �� common����ֹ��̨��**�˹�ȷ��**������ά������ |
| 10,12,13 | �ֻ��� | ˫�� | �� fuc/���� | **ͣ��**�˺ţ��˹�ȷ�� |

## YY ˵��

�����С�Զ���û�ID 103��Ϊ **dept_id=103** �����Զ�� YY Ϊ **user_id=17**���뱾����ͬ��¼��/�ֻ��ţ���ͬһ�˴��������ϲ�������ͬ user_id��

## ���Ķ���ҵ���

`fire_company` / `fire_maintenance_task` / `fire_user_company` / `fire_check_in` / `sys_oper_log` / `sys_logininfor` / openid �ֶ� �� Ǩ�ƽű���ɾ��������дҵ����������� sys_user ��֯��ɫ����
