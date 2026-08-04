/**
 * Category-level all-normal (exclude test + already saved).
 */
const fs = require('fs');
const path = require('path');
const assert = require('assert');

function read(rel) {
  return fs.readFileSync(path.join(__dirname, '..', rel), 'utf8');
}

const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireMaintenanceTaskController.java');
const mini = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java');
const systemPage = read('ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_system.html');
const mpSystem = read('../miniprogram/pages/task/system.js');
const mpWxml = read('../miniprogram/pages/task/system.wxml');
const mpApi = read('../miniprogram/api/index.js');

assert.ok(controller.includes('/markCategoryAllNormal/{taskId}/{categoryKey}'), 'pc route');
assert.ok(controller.includes('"1".equals(equipment.getRecordType())'), 'pc skips test equipment');
assert.ok(controller.includes('markUncheckedNormalByRecordIds'), 'pc marks unchecked only');
assert.ok(mini.includes('/task/inspectionTest/markCategoryAllNormal/'), 'mini route');
assert.ok(systemPage.includes('btnMarkCategoryAllNormal'), 'pc button');
assert.ok(systemPage.includes(String.fromCharCode(0x662f, 0x5426, 0x5168, 0x90e8, 0x6b63, 0x5e38)), 'pc dialog title');
assert.ok(mpSystem.includes('onMarkAllNormal'), 'mp handler');
assert.ok(mpWxml.includes('all-normal-btn'), 'mp button');
assert.ok(mpApi.includes('markCategoryAllNormal'), 'mp api');

console.log('fire-task-category-all-normal tests: all assertions passed');
