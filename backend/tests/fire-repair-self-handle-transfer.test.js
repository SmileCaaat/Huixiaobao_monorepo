/**
 * Repair self-handle / transfer / log contract tests.
 */
const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const root = path.resolve(__dirname, '../..');
const read = (rel) => fs.readFileSync(path.join(backend, rel), 'utf8');
const readRoot = (rel) => fs.readFileSync(path.join(root, rel), 'utf8');

const svc = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireFaultRepairServiceImpl.java');
const iface = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/IFireFaultRepairService.java');
const pcCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireFaultRepairController.java');
const miniCtrl = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java');
const listPage = read('ruoyi-admin/src/main/resources/templates/fire/repair/repair.html');
const handlePage = read('ruoyi-admin/src/main/resources/templates/fire/repair/handle.html');
const sql = read('sql/upgrade_repair_self_handle_transfer_log.sql');
const detailJs = readRoot('miniprogram/pages/repair/detail.js');
const detailWxml = readRoot('miniprogram/pages/repair/detail.wxml');
const transferJs = readRoot('miniprogram/pages/repair/transfer.js');
const api = readRoot('miniprogram/api/index.js');
const appJson = readRoot('miniprogram/app.json');

assert.ok(svc.includes('assignReporterAsHandler'), 'auto assign reporter');
assert.ok(svc.includes('transferRepair'), 'transfer service');
assert.ok(svc.includes('selectTransferUsers'), 'transfer users');
assert.ok(svc.includes('appendLog'), 'repair log write');
assert.ok(iface.includes('transferRepair'), 'iface transfer');
assert.ok(iface.includes('selectRepairLogs'), 'iface logs');

assert.ok(pcCtrl.includes('/handle/{repairId}'), 'pc handle page');
assert.ok(pcCtrl.includes('/transfer'), 'pc transfer api');
assert.ok(pcCtrl.includes('/logs/{repairId}'), 'pc logs api');
assert.ok(listPage.includes('handleRepair'), 'pc list handle button');
assert.ok(handlePage.includes('openTransfer'), 'pc handle transfer');
assert.ok(handlePage.includes('tab-logs') || handlePage.includes("switchTab('logs'"), 'pc handle logs tab');

assert.ok(miniCtrl.includes('/repair/transfer'), 'mini transfer');
assert.ok(miniCtrl.includes('/repair/logs/'), 'mini logs');
assert.ok(miniCtrl.includes('/repair/transferUsers/'), 'mini transfer users');

assert.ok(sql.includes('fire_fault_repair_log'), 'sql log table');
assert.ok(detailWxml.includes("activeTab==='info'") && detailWxml.includes("activeTab==='logs'"), 'mp tabs');
assert.ok(detailJs.includes('canTransfer'), 'mp transfer flag');
assert.ok(transferJs.includes('transferRepair'), 'mp transfer page');
assert.ok(api.includes('getRepairLogs'), 'api logs');
assert.ok(api.includes('transferRepair'), 'api transfer');
assert.ok(appJson.includes('pages/repair/transfer'), 'app.json transfer page');

console.log('fire-repair-self-handle-transfer.test.js OK');
