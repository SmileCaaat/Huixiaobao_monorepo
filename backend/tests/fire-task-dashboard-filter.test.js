/**
 * Employee home -> maintenance task dashboard filter conventions
 */
const fs = require('fs');
const path = require('path');
const assert = require('assert');

const mapperXml = fs.readFileSync(
  path.join(__dirname, '../ruoyi-system/src/main/resources/mapper/fire/FireMaintenanceTaskMapper.xml'),
  'utf8'
);
const taskHtml = fs.readFileSync(
  path.join(__dirname, '../ruoyi-admin/src/main/resources/templates/fire/task/task.html'),
  'utf8'
);
const homeHtml = fs.readFileSync(
  path.join(__dirname, '../ruoyi-admin/src/main/resources/templates/fire_main.html'),
  'utf8'
);
const controllerJava = fs.readFileSync(
  path.join(__dirname, '../ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireMaintenanceTaskController.java'),
  'utf8'
);
const indexHtml = fs.readFileSync(
  path.join(__dirname, '../ruoyi-admin/src/main/resources/templates/index.html'),
  'utf8'
);

const ALLOWED = ['pending', 'doing', 'today', 'completed', 'overdue', 'mine'];

assert.ok(mapperXml.includes('<sql id="effectiveTaskStatusExpr">'), 'effective status fragment');
assert.ok(
  mapperXml.includes('(<include refid="effectiveTaskStatusExpr"/>) = #{taskStatus}'),
  'taskStatus filter uses effective status'
);
assert.ok(mapperXml.includes("params.dashboardFilter == 'today'"), 'today dashboard filter');
assert.ok(mapperXml.includes("params.dashboardFilter == 'overdue'"), 'overdue dashboard filter');

assert.ok(taskHtml.includes('name="params[dashboardFilter]"'), 'hidden dashboardFilter field');
assert.ok(taskHtml.includes('function resetTaskSearch'), 'resetTaskSearch exists');
assert.ok(taskHtml.includes('fireTaskDashboardFilter'), 'task page reads storage key');
assert.ok(taskHtml.includes('applyExternalDashboardFilter'), 'unified external filter entry');
assert.ok(taskHtml.includes("addEventListener('message'"), 'listens for postMessage');
assert.ok(taskHtml.includes('__fireTaskDashboardFilterListenerBound'), 'avoids duplicate listeners');
assert.ok(taskHtml.includes('consumeStoredDashboardFilter'), 'consumes sessionStorage');

assert.ok(!homeHtml.includes('activateTaskTabAndSetUrl'), 'legacy tab CSS hack removed');
assert.ok(homeHtml.includes('sessionStorage.setItem'), 'writes sessionStorage');
assert.ok(homeHtml.includes('postMessage'), 'posts filter message');
assert.ok(homeHtml.includes('openMenu(TASK_MENU_BASE'), 'uses openMenu for tab switch');
assert.ok(homeHtml.includes('function openTaskDashboard'), 'openTaskDashboard exists');
ALLOWED.forEach(function (f) {
  assert.ok(homeHtml.includes("openTaskDashboard('" + f + "')"), 'home card maps ' + f);
});

assert.ok(controllerJava.includes('sanitizeDashboardFilter'), 'controller sanitizes dashboardFilter');

const welcomeIdx = indexHtml.indexOf('welcome-message');
assert.ok(welcomeIdx >= 0, 'navbar welcome-message exists');
const navbarSlice = indexHtml.slice(welcomeIdx, welcomeIdx + 450);
assert.ok(navbarSlice.includes('@{/system/main}'), 'top navbar fire home uses /system/main');
assert.ok(!navbarSlice.includes('@{/fire/home}'), 'top navbar fire home no longer uses /fire/home');
assert.ok(navbarSlice.includes('fa-fire'), 'top navbar fire home icon retained');

function normalizeFilter(filter) {
  return ALLOWED.indexOf(filter) >= 0 ? filter : 'mine';
}
assert.strictEqual(normalizeFilter('doing'), 'doing');
assert.strictEqual(normalizeFilter('hack'), 'mine');

console.log('fire-task-dashboard-filter.test.js passed');
