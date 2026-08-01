const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (relative) => fs.readFileSync(path.join(backend, relative), 'utf8');

const phaseB = read('sql/upgrade_auth_rbac_phase_b.sql');
const fixSql = read('sql/fix_operator_menu_and_user_13413462481.sql');
const homeCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireHomeController.java');
const indexCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysIndexController.java');
const apiLogin = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/ApiLoginController.java');
const fireMain = read('ruoyi-admin/src/main/resources/templates/fire_main.html');
const indexHtml = read('ruoyi-admin/src/main/resources/templates/index.html');

// Root menu 2000 must be attached for getChildPerms(parentId=0)
assert.ok(phaseB.includes('menu_id = 2000') || phaseB.includes(', 2000'), 'phase_b attaches menu 2000');
assert.ok(phaseB.includes('fire_operator'), 'phase_b seeds fire_operator');
assert.ok(
  /role_key IN \([\s\S]*'fire_operator'[\s\S]*\)[\s\S]*menu_id = 2000/.test(phaseB)
    || /SELECT r\.role_id, 2000[\s\S]*fire_operator/.test(phaseB),
  'phase_b operator roles get menu 2000'
);

assert.ok(fixSql.includes('menu_id'), 'fix sql exists');
assert.ok(fixSql.includes('13413462481'), 'fix sql targets phone account');
assert.ok(fixSql.includes("role_key = 'fire_operator'"), 'fix sql assigns fire_operator');
assert.ok(fixSql.includes('user_id = 16'), 'fix sql targets user 16');
assert.ok(fixSql.includes('user_name = login_name'), 'fix sql restores display name');
assert.ok(fixSql.includes('user_id = 15') && fixSql.includes("del_flag = '2'"), 'fix keeps soft-deleted dup');

// Home: personal vs global
assert.ok(homeCtrl.includes('buildPersonalTaskStats'), 'home personal stats');
assert.ok(homeCtrl.includes('buildGlobalStats'), 'home global stats');
assert.ok(homeCtrl.includes('isGlobalHomeUser'), 'home scope gate');
assert.ok(homeCtrl.includes('applyTaskListScope'), 'personal stats uses task scope');
assert.ok(homeCtrl.includes('public static String resolveDisplayName'), 'displayName helper public');
assert.ok(fireMain.includes("homeScope == 'personal'"), 'fire_main personal block');
assert.ok(fireMain.includes("homeScope == 'global'"), 'fire_main global block');
assert.ok(fireMain.includes('res.scope === \'personal\''), 'fire_main js personal branch');
assert.ok(fireMain.includes('displayName'), 'fire_main welcome displayName');

// Index / API
assert.ok(indexCtrl.includes('resolveDisplayName'), 'index uses displayName');
assert.ok(indexHtml.includes('displayName'), 'index.html shows displayName');
assert.ok(
  apiLogin.includes('StringUtils.isNotEmpty(user.getUserName())'),
  'api profile update skips empty userName'
);

console.log('auth-rbac-phase2-operator-menu tests: all assertions passed');
