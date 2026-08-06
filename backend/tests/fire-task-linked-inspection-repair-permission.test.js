/**
 * Task-linked inspection/repair: task stakeholders may access company context
 * without fire_user_company membership.
 */
const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (rel) => fs.readFileSync(path.join(backend, rel), 'utf8');

const permIface = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/IFireDataPermissionService.java');
const permImpl = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireDataPermissionServiceImpl.java');
const adminCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireInspectionController.java');
const miniCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java');
const repairCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireFaultRepairController.java');
const addPage = read('ruoyi-admin/src/main/resources/templates/fire/inspection/add_new.html');
const sql = read('sql/upgrade_member_task_linked_inspection_repair.sql');

assert.ok(permIface.includes('canAccessCompanyContext'), 'iface canAccessCompanyContext');
assert.ok(permIface.includes('assertCanAccessCompanyContext'), 'iface assertCanAccessCompanyContext');
assert.ok(permImpl.includes('canAccessCompanyContext'), 'impl canAccessCompanyContext');
assert.ok(permImpl.includes('selectCompanyListByTaskUserId'), 'impl merges task companies');
assert.ok(permImpl.includes('getInspectorId'), 'impl allows inspector access');
assert.ok(permImpl.includes('canAccessCompanyViaTaskStakeholder'), 'impl task stakeholder company');
assert.ok(permImpl.includes('listTaskStakeholderCompanyIds'), 'impl list task companies');

assert.ok(adminCtrl.includes('assertCanAccessCompanyContext'), 'admin add uses company context');
assert.ok(adminCtrl.includes('taskId'), 'admin buildings accepts taskId');
assert.ok(addPage.includes('?taskId='), 'add page passes taskId to buildings');

assert.ok(miniCtrl.includes('assertCanAccessCompanyContext'), 'mini inspection/repair use context');
assert.ok(repairCtrl.includes('assertCanAccessTask'), 'pc repair linked uses task access');
assert.ok(repairCtrl.includes('assertCanAccessCompanyContext'), 'pc repair linked uses company context');
assert.ok(!/RequiresPermissions\(\"fire:repair:add\"\)/.test(repairCtrl), 'linked repair add not blocked by shiro annotation');
assert.ok(repairCtrl.includes('isPermitted(\"fire:repair:add\")'), 'independent repair still checks add perm');
assert.ok(adminCtrl.includes('isPermitted(\"fire:inspection:add\")'), 'independent inspection still checks add perm');
assert.ok(!/RequiresPermissions\(\"fire:inspection:add\"\)[\s\S]{0,80}@PostMapping\(\"\/add\"\)/.test(adminCtrl)
    && !/@RequiresPermissions\(\"fire:inspection:add\"\)\s*\n\s*@Log[\s\S]*?@PostMapping\(\"\/add\"\)/.test(adminCtrl),
    'linked inspection add not blocked by shiro annotation on POST');

assert.ok(sql.includes('fire:inspection:add'), 'sql grants inspection add');
assert.ok(sql.includes('fire:repair:add'), 'sql grants repair add');
assert.ok(sql.includes('maintenance_member'), 'sql targets maintenance_member');

console.log('fire-task-linked-inspection-repair-permission.test.js OK');
