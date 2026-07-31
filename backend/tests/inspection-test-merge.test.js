/**
 * Merge maintenance/fire-test into inspection-test (source assertions).
 */
const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const root = path.resolve(backend, '..');
const read = (abs) => fs.readFileSync(abs, 'utf8');
const exists = (abs) => fs.existsSync(abs);

const LABEL = '\u6d88\u9632\u7ef4\u62a4'; // 消防维护
const TIP = '\u8bf7\u5148\u4e0b\u53d1\u4efb\u52a1'; // 请先下发任务

const keys = read(path.join(backend, 'ruoyi-system/src/main/java/com/ruoyi/fire/service/support/FireInspectionTestKeys.java'));
const service = read(path.join(backend, 'ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireMaintenanceTaskServiceImpl.java'));
const controller = read(path.join(backend, 'ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireMaintenanceTaskController.java'));
const mini = read(path.join(backend, 'ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java'));
const taskHtml = read(path.join(backend, 'ruoyi-admin/src/main/resources/templates/fire/task/task.html'));
const detailHtml = read(path.join(backend, 'ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_detail.html'));
const systemHtml = read(path.join(backend, 'ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_system.html'));
const equipHtml = read(path.join(backend, 'ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_equipment.html'));
const apiJs = read(path.join(root, 'miniprogram/api/index.js'));
const indexJs = read(path.join(root, 'miniprogram/pages/task/index.js'));
const detailJs = read(path.join(root, 'miniprogram/pages/task/detail.js'));
const appJson = read(path.join(root, 'miniprogram/app.json'));

assert.ok(keys.includes('c:') || keys.includes('"c:"'), 'code prefix in keys');
assert.ok(keys.includes('n:') || keys.includes('"n:"'), 'name prefix in keys');
assert.ok(keys.includes('normalizeText') || keys.includes('replaceAll') || keys.includes('replace'), 'normalize whitespace');

assert.ok(service.includes('buildInspectionTestDetail'), 'build detail');
assert.ok(service.includes('buildInspectionTestSystem'), 'build system');
assert.ok(service.includes('buildInspectionTestEquipment'), 'build equipment');
assert.ok(service.includes('rebuildInspectionTestRecords'), 'rebuild one');
assert.ok(service.includes('rebuildAllInspectionTestRecords'), 'rebuild all');

assert.ok(controller.includes('/inspectionTestDetail/{taskId}'), 'admin detail route');
assert.ok(controller.includes('/inspectionTestSystem/{taskId}/{categoryKey}'), 'admin system route');
assert.ok(controller.includes('/inspectionTestEquipment/{taskId}/{categoryKey}/{equipmentKey}'), 'admin equipment route');
assert.ok(controller.includes('return inspectionTestDetail'), 'legacy detail forward');
assert.ok(controller.includes('rebuildInspectionTestRecords'), 'rebuild endpoint');
assert.ok(controller.includes('user.isAdmin()'), 'rebuild admin only');

assert.ok(taskHtml.includes(LABEL), 'task button label');
assert.ok(taskHtml.includes('openInspectionTestDetail'), 'task open fn');
assert.ok(!taskHtml.includes('openMaintenanceDetail'), 'old maintenance opener removed');
assert.ok(!taskHtml.includes('openFireTestDetail'), 'old firetest opener removed');
assert.ok(taskHtml.includes(TIP), 'disabled tip kept');

assert.ok(detailHtml.includes(LABEL), 'detail title');
assert.ok(systemHtml.includes('inspectionTestDetail'), 'system back to detail');
assert.ok(equipHtml.includes('inspectionTestSystem'), 'equip back to system');

assert.ok(mini.includes('/task/inspectionTest/{taskId}'), 'mini detail api');
assert.ok(mini.includes('/task/inspectionTest/system/'), 'mini system api');
assert.ok(mini.includes('/task/inspectionTest/equipment/'), 'mini equipment api');
assert.ok(mini.includes('updateMaintenance'), 'mini updateMaintenance');
assert.ok(mini.includes('assertWritableRecord'), 'record-task guard');

assert.ok(apiJs.includes('getInspectionTestDetail'), 'api detail');
assert.ok(apiJs.includes('getInspectionTestSystem'), 'api system');
assert.ok(apiJs.includes('getInspectionTestEquipment'), 'api equipment');
assert.ok(indexJs.includes('/pages/task/detail?id='), 'index skips action');
assert.ok(!indexJs.includes('/pages/task/action'), 'index no action');
assert.ok(detailJs.includes('getInspectionTestDetail'), 'detail uses unified api');
assert.ok(detailJs.includes(LABEL), 'detail title text');
assert.ok(!appJson.includes('pages/task/action'), 'action removed from app.json');
assert.ok(!exists(path.join(root, 'miniprogram/pages/task/action.js')), 'action.js deleted');

assert.ok(exists(path.join(backend, 'sql/backup_inspection_test_records.sql')), 'backup sql');
assert.ok(exists(path.join(backend, 'sql/upgrade_merge_inspection_test.sql')), 'upgrade sql');
assert.ok(exists(path.join(backend, 'sql/rollback_merge_inspection_test.sql')), 'rollback sql');

console.log('inspection-test-merge tests: all assertions passed');