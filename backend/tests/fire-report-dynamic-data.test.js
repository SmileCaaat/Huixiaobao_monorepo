const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..', '..');
const read = (file) => fs.readFileSync(path.join(root, file), 'utf8');

test('维保报告三张汇总表按巡查、测试、保养标签读取当前任务数据', () => {
  const service = read('backend/ruoyi-system/src/main/java/com/ruoyi/system/service/impl/FireReportRecordServiceImpl.java');
  assert.match(service, /patrolLevel1Query\.setRecordType\("0"\)/);
  assert.match(service, /testQuery\.setRecordType\("1"\)/);
  assert.match(service, /"2"\.equals\(item\.getRecordType\(\)\)/);
  assert.match(service, /createMaintenanceTaskRecordTable/);
});

test('设备实测记录包含任务链记录及同单位任务周期内独立测试记录', () => {
  const mapper = read('backend/ruoyi-system/src/main/resources/mapper/fire/FireInspectionMapper.xml');
  const service = read('backend/ruoyi-system/src/main/java/com/ruoyi/system/service/impl/FireReportRecordServiceImpl.java');
  assert.match(mapper, /task_id = #\{taskId\}/);
  assert.match(mapper, /task_id is null/);
  assert.match(mapper, /company_id = #\{companyId\}/);
  assert.match(mapper, /inspection_time &gt;= #\{startTime\}/);
  assert.match(mapper, /inspection_time &lt;= #\{endTime\}/);
  assert.match(mapper, /inspection_type = '0'/);
  assert.match(service, /for \(int i = 0; i < inspections\.size\(\); i\+\+\)/);
  assert.match(service, /inspection\.getImages\(\)/);
});

test('故障维修报告仅按当前维保任务链关联', () => {
  const service = read('backend/ruoyi-system/src/main/java/com/ruoyi/system/service/impl/FireReportRecordServiceImpl.java');
  const mapper = read('backend/ruoyi-system/src/main/resources/mapper/fire/FireFaultRepairMapper.xml');
  const pc = read('backend/ruoyi-admin/src/main/resources/templates/fire/repair/add.html');
  const mini = read('miniprogram/pages/repair/form.js');
  assert.match(service, /repairQuery\.setTaskId\(taskId\)/);
  assert.match(mapper, /AND f\.task_id = #\{taskId\}/);
  assert.match(pc, /name="taskId"/);
  assert.match(mini, /taskId: this\.data\.taskId/);
});
