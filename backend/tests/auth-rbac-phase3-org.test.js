const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (relative) => fs.readFileSync(path.join(backend, relative), 'utf8');

const mig = read('sql/upgrade_org_rbac_phase3.sql');
const rb = read('sql/rollback_org_rbac_phase3.sql');
const permIface = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/IFireDataPermissionService.java');
const permImpl = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireDataPermissionServiceImpl.java');
const register = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysRegisterController.java');
const home = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireHomeController.java');

assert.ok(mig.includes('WHERE dept_id=100') || mig.includes('WHERE dept_id = 100'), 'keep dept_id 100');
assert.ok(mig.includes('dept_id=110') || mig.includes('dept_id = 110'), 'weibao dept 110');
assert.ok(mig.includes('maintenance_member'), 'member role');
assert.ok(mig.includes('project_manager') && mig.includes("data_scope='1'"), 'PM all data scope');
assert.ok(mig.includes('PROJECT_MANAGER') && mig.includes('MAINTENANCE_LEADER') && mig.includes('MAINTENANCE_MEMBER'), '3 posts');
assert.ok(mig.includes('bak_p3_sys_user'), 'backup tables');
assert.ok(mig.includes('START TRANSACTION'), 'transaction');
assert.ok(mig.includes('101,102,103') || mig.includes('101, 102, 103'), 'soft disable chengdu tree');
assert.ok(mig.includes('fire_dept_admin'), 'disable fire_dept_admin');
assert.ok(mig.includes('fire_operator'), 'migrate fire_operator key');
assert.ok(!/TRUNCATE\s+sys_/i.test(mig), 'no truncate');

assert.ok(rb.includes('bak_p3_sys_user'), 'rollback uses backup');
assert.ok(rb.includes('START TRANSACTION'), 'rollback tx');

assert.ok(permIface.includes('hasGlobalBizDataScope'), 'iface global biz');
assert.ok(permImpl.includes('project_manager'), 'impl uses project_manager');
assert.ok(!permImpl.includes('fire_dept_admin'), 'impl no fire_dept_admin');
assert.ok(register.includes('maintenance_member'), 'register assigns maintenance_member');
assert.ok(!register.includes('fire_operator'), 'register no fire_operator');
assert.ok(home.includes('hasGlobalBizDataScope'), 'home uses global biz scope');

console.log('auth-rbac-phase3-org tests: all assertions passed');
