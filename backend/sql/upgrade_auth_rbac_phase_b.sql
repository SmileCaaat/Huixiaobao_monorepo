-- ============================================================
-- Ȩ����ϵ���� B���û��˿��� / ΢�Ű� / ��Ŀ��Ա��չ / ��ɫ����
-- �ļ���backend/sql/upgrade_auth_rbac_phase_b.sql
-- ˵�����������ظ�ִ�У�ִ��ǰ�뱸�� sys_user��fire_user_company��sys_role
-- ���룺mysql --default-character-set=utf8mb4 < ���ļ�
-- ============================================================

SET NAMES utf8mb4;

-- ---------- 1. sys_user ��չ ----------
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'allow_admin_login'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE sys_user
     ADD COLUMN allow_admin_login char(1) DEFAULT ''1'' COMMENT ''�Ƿ�������̨��¼(0��1��)'' AFTER status,
     ADD COLUMN allow_mini_login char(1) DEFAULT ''1'' COMMENT ''�Ƿ�����С�����¼(0��1��)'' AFTER allow_admin_login,
     ADD COLUMN audit_status char(1) DEFAULT ''0'' COMMENT ''���״̬(0��ͨ��1�����2�Ѿܾ�)'' AFTER allow_mini_login,
     ADD COLUMN openid varchar(64) DEFAULT NULL COMMENT ''΢��openid'' AFTER audit_status,
     ADD COLUMN union_id varchar(64) DEFAULT NULL COMMENT ''΢��unionid'' AFTER openid,
     ADD COLUMN wx_bind_time datetime DEFAULT NULL COMMENT ''΢�Ű�ʱ��'' AFTER union_id',
  'SELECT ''sys_user auth columns already exist''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE sys_user
   SET allow_admin_login = IFNULL(allow_admin_login, '1'),
       allow_mini_login = IFNULL(allow_mini_login, '1'),
       audit_status = IFNULL(audit_status, '0')
 WHERE del_flag = '0';

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND INDEX_NAME = 'uk_sys_user_openid'
);
SET @sql := IF(@idx_exists = 0,
  'ALTER TABLE sys_user ADD UNIQUE INDEX uk_sys_user_openid (openid)',
  'SELECT ''uk_sys_user_openid exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 2. fire_user_company ��չ ----------
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fire_user_company' AND COLUMN_NAME = 'biz_line'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE fire_user_company
     ADD COLUMN biz_line varchar(16) DEFAULT ''all'' COMMENT ''ҵ���� inner/outer/all'' AFTER role_type,
     ADD COLUMN status char(1) DEFAULT ''0'' COMMENT ''��Ա״̬(0��Ч1�˳�)'' AFTER biz_line,
     ADD COLUMN join_time datetime DEFAULT NULL COMMENT ''����ʱ��'' AFTER status,
     ADD COLUMN leave_time datetime DEFAULT NULL COMMENT ''�˳�ʱ��'' AFTER join_time',
  'SELECT ''fire_user_company ext columns already exist''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE fire_user_company
   SET biz_line = IFNULL(biz_line, 'all'),
       status = IFNULL(status, '0'),
       join_time = IFNULL(join_time, create_time)
 WHERE 1 = 1;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fire_user_company' AND INDEX_NAME = 'uk_fuc_user_company'
);
SET @sql := IF(@idx_exists = 0,
  'ALTER TABLE fire_user_company ADD UNIQUE INDEX uk_fuc_user_company (user_id, company_id)',
  'SELECT ''uk_fuc_user_company exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 3. ���ţ�ά���� / �ڳ�ά���� / �ⳡά���� ----------
INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time)
SELECT 100, '0,100', 'ά����', 10, NULL, NULL, NULL, '0', '0', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dept WHERE dept_name = 'ά����' AND del_flag = '0');

SET @wb_dept_id := (SELECT dept_id FROM sys_dept WHERE dept_name = 'ά����' AND del_flag = '0' LIMIT 1);
SET @wb_ancestors := (SELECT CONCAT(ancestors, ',', dept_id) FROM sys_dept WHERE dept_id = @wb_dept_id);

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, status, del_flag, create_by, create_time)
SELECT @wb_dept_id, @wb_ancestors, '�ڳ�ά����', 1, '0', '0', 'admin', NOW()
WHERE @wb_dept_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_dept WHERE dept_name = '�ڳ�ά����' AND del_flag = '0');

INSERT INTO sys_dept (parent_id, ancestors, dept_name, order_num, status, del_flag, create_by, create_time)
SELECT @wb_dept_id, @wb_ancestors, '�ⳡά����', 2, '0', '0', 'admin', NOW()
WHERE @wb_dept_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_dept WHERE dept_name = '�ⳡά����' AND del_flag = '0');

-- ---------- 4. ��λ���ӣ�������֯�ṹͼ�� ----------
INSERT INTO sys_post (post_code, post_name, post_sort, status, create_by, create_time, remark)
SELECT * FROM (
  SELECT 'wb_director' AS post_code, 'ά����������' AS post_name, 10 AS post_sort, '0' AS status, 'admin' AS create_by, NOW() AS create_time, '����B��λ' AS remark
  UNION ALL SELECT 'pm_inner', '�ڳ���Ŀ������', 11, '0', 'admin', NOW(), '����B��λ'
  UNION ALL SELECT 'pm_outer', '�ⳡ��Ŀ������', 12, '0', 'admin', NOW(), '����B��λ'
  UNION ALL SELECT 'tl_inner', '�ڳ�ά���鳤', 13, '0', 'admin', NOW(), '����B��λ'
  UNION ALL SELECT 'tl_outer', '�ⳡά���鳤', 14, '0', 'admin', NOW(), '����B��λ'
  UNION ALL SELECT 'wb_member', 'ά����Ա', 15, '0', 'admin', NOW(), '����B��λ'
  UNION ALL SELECT 'staff', '��ͨԱ��', 16, '0', 'admin', NOW(), '����B��λ'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_post p WHERE p.post_code = t.post_code);

-- ---------- 5. ҵ���ɫ���� ----------
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark)
SELECT * FROM (
  SELECT 'ά��������Ա' AS role_name, 'fire_dept_admin' AS role_key, 10 AS role_sort, '1' AS data_scope, '0' AS status, '0' AS del_flag, 'admin' AS create_by, NOW() AS create_time, '����B��ɫ' AS remark
  UNION ALL SELECT '��Ŀ������', 'project_manager', 11, '5', '0', '0', 'admin', NOW(), '����B��ɫ-��Ŀ�ɼ����Գ�Ա��Ϊ׼'
  UNION ALL SELECT 'ά���鳤', 'team_leader', 12, '5', '0', '0', 'admin', NOW(), '����B��ɫ'
  UNION ALL SELECT 'ά��ִ����Ա', 'fire_operator', 13, '5', '0', '0', 'admin', NOW(), '����B��ɫ'
  UNION ALL SELECT '��ͨ����Ա��', 'dept_staff', 14, '5', '0', '0', 'admin', NOW(), '����B��ɫ'
  UNION ALL SELECT 'ֻ�����', 'auditor', 15, '2', '0', '0', 'admin', NOW(), '����B��ɫ'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_role r WHERE r.role_key = t.role_key AND r.del_flag = '0');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'fire_dept_admin' AND r.del_flag = '0'
  AND m.perms LIKE 'fire:%'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'project_manager' AND r.del_flag = '0'
  AND (
    m.perms LIKE 'fire:task:%'
    OR m.perms LIKE 'fire:repair:%'
    OR m.perms LIKE 'fire:report:%'
    OR m.perms LIKE 'fire:company:%'
    OR m.perms LIKE 'fire:checkIn:%'
    OR m.perms LIKE 'fire:inspection:%'
    OR (m.menu_type = 'M' AND m.menu_name LIKE '%����%')
  )
  AND IFNULL(m.perms, '') NOT LIKE 'system:%'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'team_leader' AND r.del_flag = '0'
  AND (
    m.perms IN ('fire:task:view','fire:task:list','fire:task:query','fire:task:edit','fire:task:export')
    OR m.perms LIKE 'fire:task:%'
    OR m.perms LIKE 'fire:repair:view%'
    OR m.perms LIKE 'fire:repair:list%'
    OR m.perms LIKE 'fire:checkIn:%'
    OR m.perms LIKE 'fire:report:view%'
    OR m.perms LIKE 'fire:report:list%'
  )
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key IN ('fire_operator', 'dept_staff') AND r.del_flag = '0'
  AND m.perms IN (
    'fire:task:view','fire:task:list','fire:task:query',
    'fire:report:view','fire:report:list',
    'fire:repair:view','fire:repair:list',
    'fire:checkIn:view','fire:checkIn:list'
  )
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

-- Attach root directory menu_id=2000 so getChildPerms can find parentId=0 nodes
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, 2000
FROM sys_role r
WHERE r.del_flag = '0'
  AND r.role_key IN ('fire_dept_admin', 'project_manager', 'team_leader', 'fire_operator', 'dept_staff', 'auditor')
  AND EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_id = 2000)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = 2000);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'auditor' AND r.del_flag = '0'
  AND (
    m.perms LIKE 'fire:%:view'
    OR m.perms LIKE 'fire:%:list'
    OR m.perms LIKE 'fire:%:query'
    OR m.perms LIKE 'fire:%:export'
    OR m.perms LIKE 'fire:report:%'
  )
  AND m.perms NOT LIKE '%:add%'
  AND m.perms NOT LIKE '%:edit%'
  AND m.perms NOT LIKE '%:remove%'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

SET @company_menu_id := (SELECT menu_id FROM sys_menu WHERE perms = 'fire:company:view' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT '�ͻ���Ա����', @company_menu_id, 20, '#', '', 'F', '0', '1', 'fire:company:assign', '#', 'admin', NOW(), '����B'
WHERE @company_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fire:company:assign');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key IN ('admin', 'fire_dept_admin', 'project_manager') AND r.del_flag = '0'
  AND m.perms = 'fire:company:assign'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

INSERT INTO sys_menu (menu_name, parent_id, order_num, url, target, menu_type, visible, is_refresh, perms, icon, create_by, create_time, remark)
SELECT '�û����', 100, 11, '#', '', 'F', '0', '1', 'system:user:audit', '#', 'admin', NOW(), '����B'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:user:audit');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key IN ('admin', 'fire_dept_admin') AND r.del_flag = '0'
  AND m.perms = 'system:user:audit'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

UPDATE sys_user
   SET allow_admin_login = '1', allow_mini_login = '1', audit_status = '0', status = '0'
 WHERE user_id = 1;

-- �����������������룬����ִ�� fix_auth_rbac_chinese_names.sql
