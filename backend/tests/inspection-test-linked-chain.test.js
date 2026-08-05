const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..', '..');
const read = (file) => fs.readFileSync(path.join(root, file), 'utf8');

test('测试设备有无判断与无设备批量接口同时覆盖电脑端和小程序端', () => {
  const pc = read('backend/ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_system.html');
  const mini = read('miniprogram/pages/task/system.js');
  const mapper = read('backend/ruoyi-system/src/main/resources/mapper/fire/FireMaintenanceRecordMapper.xml');
  assert.match(pc, /是否有该设备？/);
  assert.match(pc, /markEquipmentAllNoDevice/);
  assert.match(pc, /fire\/inspection\/add\?linked=true&taskId=/);
  assert.match(mini, /itemList: \[(?:"有"|"\\u6709"), (?:"无"|"\\u65e0")\]/);
  assert.match(mini, /markInspectionTestNoDevice/);
  assert.match(mapper, /check_result = '3'/);
});

test('关联巡检异常可进入锁定的故障报修链路', () => {
  const inspectionPc = read('backend/ruoyi-admin/src/main/resources/templates/fire/inspection/add_new.html');
  const inspectionMini = read('miniprogram/pages/inspection/form.js');
  const repairMini = read('miniprogram/pages/repair/form.js');
  assert.match(inspectionPc, /linkedInspection/);
  assert.match(inspectionPc, /是否上报故障？/);
  assert.match(inspectionPc, /fire\/repair\/add\?linked=true/);
  assert.match(inspectionMini, /form\.equipmentStatus": "0"/);
  assert.match(inspectionMini, /是否上报故障？/);
  assert.match(repairMini, /isReported: this\.data\.linked \? "1"/);
  assert.match(repairMini, /customerAddress: this\.data\.customerAddress/);
});

test('消防维护层级读取避免N+1查询并阻止重复点击', () => {
  const service = read('backend/ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireMaintenanceTaskServiceImpl.java');
  const detail = read('backend/ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_detail.html');
  const system = read('backend/ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_system.html');
  const equipment = read('backend/ruoyi-admin/src/main/resources/templates/fire/task/inspection_test_equipment.html');
  assert.doesNotMatch(service, /calculateSystemStats\(system, taskId\)/);
  assert.match(service, /calculateSystemStats\(system, allRecords, recordsById\)/);
  assert.match(service, /selectFireMaintenanceTaskBaseByTaskId/);
  assert.match(detail, /navigationBusy/);
  assert.match(system, /navigationBusy/);
  assert.match(equipment, /navigationBusy/);
  assert.match(detail, /layer\.load\(2/);
  assert.match(equipment, /pointer-events/);
});
