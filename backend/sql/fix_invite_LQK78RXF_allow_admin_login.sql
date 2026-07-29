-- Fix: invite-code registered staff should be able to login PC admin (not admin privilege).
-- Scope: invite_code = LQK78RXF; active, not deleted, not rejected.
-- Safe / idempotent.

UPDATE sys_user u
INNER JOIN sys_dept_register_invite i ON u.register_invite_id = i.invite_id
SET u.allow_admin_login = '1',
    u.allow_mini_login = IFNULL(NULLIF(u.allow_mini_login, ''), '1'),
    u.update_by = 'admin',
    u.update_time = NOW()
WHERE i.invite_code = 'LQK78RXF'
  AND u.del_flag = '0'
  AND u.status = '0'
  AND (u.audit_status IS NULL OR u.audit_status IN ('0', '1'));

-- Ensure maintenance_member only (do not grant admin)
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u
INNER JOIN sys_dept_register_invite i ON u.register_invite_id = i.invite_id
INNER JOIN sys_role r ON r.role_key = 'maintenance_member' AND r.status = '0'
WHERE i.invite_code = 'LQK78RXF'
  AND u.del_flag = '0'
  AND u.status = '0'
  AND (u.audit_status IS NULL OR u.audit_status IN ('0', '1'))
  AND NOT EXISTS (
      SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id
  );
