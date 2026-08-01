/**
 * Check-in data scope contract tests (source assertions).
 */
const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (rel) => fs.readFileSync(path.join(backend, rel), 'utf8');

const permIface = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/IFireDataPermissionService.java');
const permImpl = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireDataPermissionServiceImpl.java');
const mapperXml = read('ruoyi-system/src/main/resources/mapper/fire/FireCheckInMapper.xml');
const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireCheckInController.java');
const mini = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java');
const page = read('ruoyi-admin/src/main/resources/templates/fire/checkIn/checkIn.html');
const taskMapper = read('ruoyi-system/src/main/java/com/ruoyi/fire/mapper/FireMaintenanceTaskMapper.java');

assert.ok(permIface.includes('applyCheckInListScope'), 'iface applyCheckInListScope');
assert.ok(permIface.includes('canAccessCheckIn'), 'iface canAccessCheckIn');
assert.ok(permIface.includes('listLeadCompanyIds'), 'iface listLeadCompanyIds');
assert.ok(permImpl.includes('selectManagedCompanyIdsByUserId'), 'lead via task manager');
assert.ok(permImpl.includes('isCompanyLead'), 'lead via membership role');
assert.ok(permImpl.includes('scopeMode'), 'inject scopeMode');
assert.ok(permImpl.includes('remove("scopeMode")'), 'strip client forged scope');
assert.ok(!/13413462481/.test(permImpl), 'no hardcoded phone in permission');

assert.ok(mapperXml.includes("scopeMode == 'self'"), 'self scope SQL');
assert.ok(mapperXml.includes("scopeMode == 'leadOrSelf'"), 'lead scope SQL');
assert.ok(mapperXml.includes('fire_user_company'), 'member exists check');
assert.ok(mapperXml.includes('find_in_set(c.user_id, mt.operator_ids)'), 'task assignee check');

assert.ok(controller.includes('applyCheckInListScope'), 'list/export scope');
assert.ok(controller.includes('assertCanAccessCheckIn'), 'detail assert');
assert.ok(controller.includes('/filterCompanies'), 'scoped company filter API');
assert.ok(controller.includes('/filterUsers'), 'scoped user filter API');
assert.ok(controller.includes('export') && controller.includes('applyCheckInListScope'), 'export scoped');

assert.ok(mini.includes('applyCheckInListScope'), 'mini list scoped');
assert.ok(mini.includes('assertCanAccessCheckIn'), 'mini detail assert');

assert.ok(page.includes('filterCompanies'), 'page uses scoped companies');
assert.ok(page.includes('filterUsers'), 'page uses scoped users');
assert.ok(!page.includes("fire/company/all"), 'page no longer loads all companies');

assert.ok(taskMapper.includes('selectManagedCompanyIdsByUserId'), 'mapper method');

console.log('fire-checkin-data-scope tests: all assertions passed');
