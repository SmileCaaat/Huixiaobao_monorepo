const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (relative) => fs.readFileSync(path.join(backend, relative), 'utf8');

const sql = read('sql/upgrade_auth_rbac_phase_b.sql');
const sysUser = read('ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/SysUser.java');
const userCompany = read('ruoyi-system/src/main/java/com/ruoyi/fire/domain/FireUserCompany.java');
const permIface = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/IFireDataPermissionService.java');
const permImpl = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireDataPermissionServiceImpl.java');
const taskCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireMaintenanceTaskController.java');
const repairCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireFaultRepairController.java');
const reportCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireReportController.java');
const miniCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java');
const loginSvc = read('ruoyi-framework/src/main/java/com/ruoyi/framework/shiro/service/SysLoginService.java');
const apiLogin = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/ApiLoginController.java');
const apiToken = read('ruoyi-framework/src/main/java/com/ruoyi/framework/shiro/web/filter/ApiTokenFilter.java');
const apiWx = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/ApiWxController.java');
const registerCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysRegisterController.java');
const userCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysUserController.java');
const companyCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireCompanyController.java');
const companySvc = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireCompanyServiceImpl.java');
const assignHtml = read('ruoyi-admin/src/main/resources/templates/fire/company/assignUsers.html');
const userHtml = read('ruoyi-admin/src/main/resources/templates/system/user/user.html');
const yml = read('ruoyi-admin/src/main/resources/application.yml');
const companyMapper = read('ruoyi-system/src/main/resources/mapper/fire/FireCompanyMapper.xml');
const repairMapper = read('ruoyi-system/src/main/resources/mapper/fire/FireFaultRepairMapper.xml');
const reportMapper = read('ruoyi-system/src/main/resources/mapper/system/FireReportRecordMapper.xml');

// SQL migration
assert.ok(sql.includes('allow_admin_login'), 'sql allow_admin_login');
assert.ok(sql.includes('allow_mini_login'), 'sql allow_mini_login');
assert.ok(sql.includes('audit_status'), 'sql audit_status');
assert.ok(sql.includes('openid'), 'sql openid');
assert.ok(sql.includes('biz_line'), 'sql biz_line');
assert.ok(sql.includes('uk_fuc_user_company'), 'sql unique membership');
assert.ok(sql.includes('fire_dept_admin'), 'sql role fire_dept_admin');
assert.ok(sql.includes('project_manager'), 'sql role project_manager');
assert.ok(sql.includes('fire:company:assign'), 'sql assign perm');
assert.ok(sql.includes('system:user:audit'), 'sql audit perm');
assert.ok(sql.includes('2000') && (/fire_operator/.test(sql) || /maintenance_member/.test(sql)), 'sql attaches fire root menu 2000 for operators');

// Entities
assert.ok(sysUser.includes('allowAdminLogin'), 'SysUser allowAdminLogin');
assert.ok(sysUser.includes('allowMiniLogin'), 'SysUser allowMiniLogin');
assert.ok(sysUser.includes('auditStatus'), 'SysUser auditStatus');
assert.ok(sysUser.includes('openid'), 'SysUser openid');
assert.ok(userCompany.includes('ROLE_TEAM_LEADER'), 'FireUserCompany ROLE_TEAM_LEADER');
assert.ok(userCompany.includes('bizLine'), 'FireUserCompany bizLine');

// Data permission service + wiring
assert.ok(permIface.includes('applyTaskListScope'), 'perm iface task scope');
assert.ok(permIface.includes('applyRepairListScope'), 'perm iface repair scope');
assert.ok(permIface.includes('applyReportListScope'), 'perm iface report scope');
assert.ok(permImpl.includes('hasGlobalBizDataScope') || permImpl.includes('isFireDeptAdmin'), 'perm impl global biz scope');
assert.ok(permImpl.includes('canAccessTask'), 'perm impl canAccessTask');
assert.ok(taskCtrl.includes('fireDataPermissionService.applyTaskListScope'), 'task list scope');
assert.ok(taskCtrl.includes('assertCanDispatchTask'), 'task dispatch assert');
assert.ok(repairCtrl.includes('applyRepairListScope'), 'repair list scope');
assert.ok(repairCtrl.includes('assertCanAccessRepair'), 'repair assert');
assert.ok(reportCtrl.includes('applyReportListScope'), 'report list scope');
assert.ok(reportCtrl.includes('assertCanAccessReport'), 'report assert');
assert.ok(miniCtrl.includes('fireDataPermissionService.canAccessCompany'), 'mini company access');
assert.ok(miniCtrl.includes('applyTaskListScope'), 'mini task list scope');
assert.ok(!/private boolean hasCompanyAccess\(Long companyId, Long userId\) \{\s*return companyId != null;\s*\}/.test(miniCtrl),
  'mini hasCompanyAccess must not always true');

// Login / register / wx
assert.ok(loginSvc.includes('validateAuditStatus'), 'login audit');
assert.ok(loginSvc.includes('validateAdminLoginAllowed'), 'login admin flag');
assert.ok(loginSvc.includes('validateMiniLoginAllowed'), 'login mini flag');
assert.ok(apiLogin.includes('validateMiniLoginAllowed') || apiLogin.includes('allowMiniLogin') || apiLogin.includes('AllowMini'),
  'api login mini gate');
assert.ok(apiToken.includes('validateAuditStatus') || apiToken.includes('auditStatus') || apiToken.includes('AllowMini'),
  'api token recheck');
assert.match(apiWx, /@RequestMapping\("\/api\/wx"\)/);
assert.match(apiWx, /@PostMapping\("\/bind"\)/);
assert.match(apiWx, /@PostMapping\("\/login"\)/);
assert.ok(registerCtrl.includes('auditStatus') || registerCtrl.includes('setAuditStatus'), 'register audit');
assert.ok(userCtrl.includes('unbindWx') || userCtrl.includes('unbind'), 'user unbind wx');
assert.ok(userCtrl.includes('audit') || userCtrl.includes('system:user:audit'), 'user audit api');
assert.ok(userHtml.includes('allowAdminLogin') || userHtml.includes('allow_admin') || userHtml.includes('\u5ba1\u6838'),
  'user list/edit phase-b fields');

// Member assign
assert.ok(companyCtrl.includes('fire:company:assign'), 'assign permission');
assert.ok(companyCtrl.includes('canManageCompanyMembers'), 'assign manage check');
assert.ok(companyCtrl.includes('membersJson'), 'assign membersJson');
assert.ok(companySvc.includes('upsertUserCompany') || companyMapper.includes('upsertUserCompany'), 'upsert membership');
assert.ok(assignHtml.includes('roleType') || assignHtml.includes('role_type') || assignHtml.includes('\u804c\u8d23'),
  'assign UI roles');

// Config + mapper scopes
assert.ok(yml.includes('wx:') && yml.includes('miniapp:'), 'wx.miniapp config');
assert.ok(companyMapper.includes('selectActiveMembershipsByUserId'), 'active memberships query');
assert.ok(repairMapper.includes('leadOrRelated') || repairMapper.includes('leadCompanyIds'), 'repair scope SQL');
assert.ok(reportMapper.includes('scopeUserId'), 'report scope SQL');

console.log('auth-rbac-phase-b tests: all assertions passed');
