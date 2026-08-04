/**
 * Source assertions for fire maintenance conclusion feature.
 */
const fs = require('fs');
const path = require('path');
const assert = require('assert');

function read(rel) {
  return fs.readFileSync(path.join(__dirname, '..', rel), 'utf8');
}

const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireMaintenanceTaskController.java');
const mini = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java');
const mapperXml = read('ruoyi-system/src/main/resources/mapper/fire/FireMaintenanceTaskMapper.xml');
const service = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireMaintenanceTaskServiceImpl.java');
const detailPage = read('ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_detail.html');
const conclusionPage = read('ruoyi-admin/src/main/resources/templates/fire/task/conclusion.html');
const mpDetail = read('../miniprogram/pages/task/detail.wxml');
const mpApi = read('../miniprogram/api/index.js');
const mpApp = read('../miniprogram/app.json');
const sql = read('sql/upgrade_task_conclusion_fields.sql');

assert.ok(controller.includes('/conclusion/{taskId}'), 'pc conclusion page route');
assert.ok(controller.includes('/saveConclusion'), 'pc save conclusion');
assert.ok(controller.includes('/conclusion/previous/{taskId}'), 'pc previous quote');
assert.ok(controller.includes('updateTaskConclusion'), 'pc uses dedicated update');

assert.ok(mini.includes('/task/conclusion/{taskId}'), 'mini get conclusion');
assert.ok(mini.includes('/task/saveConclusion'), 'mini save conclusion');
assert.ok(mini.includes('/task/conclusion/previous/{taskId}'), 'mini previous quote');

assert.ok(mapperXml.includes('id="updateTaskConclusion"'), 'mapper updateTaskConclusion');
assert.ok(mapperXml.includes('id="selectPreviousTaskForConclusion"'), 'mapper previous select');
assert.ok(mapperXml.includes('patrol_summary_remark'), 'mapper patrol remark');
assert.ok(mapperXml.includes('alarm_host_voucher'), 'mapper voucher');

assert.ok(service.includes('updateTaskConclusion'), 'service updateTaskConclusion');
assert.ok(service.includes('selectPreviousTaskForConclusion'), 'service previous');

assert.ok(detailPage.includes('openConclusion'), 'detail has conclusion entry');
assert.ok(detailPage.includes(String.fromCharCode(0x7ed3, 0x8bba)), 'detail shows conclusion button');
assert.ok(conclusionPage.includes(String.fromCharCode(0x60c5,0x51b5,0x7b80,0x8ff0)), 'conclusion page title');
assert.ok(conclusionPage.includes(String.fromCharCode(0x5f15,0x7528,0x4e0a,0x6708,0x5de1,0x67e5,0x5185,0x5bb9)), 'quote patrol');
assert.ok(conclusionPage.includes(String.fromCharCode(0x5f15,0x7528,0x4e0a,0x6708,0x6d4b,0x8bd5,0x5185,0x5bb9)), 'quote test');
assert.ok(conclusionPage.includes(String.fromCharCode(0x6d88,0x9632,0x62a5,0x8b66,0x4e3b,0x673a)), 'voucher field');

assert.ok(mpDetail.includes('goConclusion'), 'mp detail entry');
assert.ok(mpApi.includes('getTaskConclusion'), 'mp api get');
assert.ok(mpApi.includes('saveTaskConclusion'), 'mp api save');
assert.ok(mpApp.includes('pages/task/conclusion'), 'mp app.json registers page');
assert.ok(sql.includes('patrol_summary_remark'), 'sql has columns');

console.log('fire-task-conclusion tests: all assertions passed');
