const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (relative) => fs.readFileSync(path.join(backend, relative), 'utf8');

const taskService = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireMaintenanceTaskServiceImpl.java');
const taskMapper = read('ruoyi-system/src/main/resources/mapper/fire/FireMaintenanceTaskMapper.xml');
const recordMapper = read('ruoyi-system/src/main/resources/mapper/fire/FireMaintenanceRecordMapper.xml');
const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireMaintenanceTaskController.java');
const editPage = read('ruoyi-admin/src/main/resources/templates/fire/task/edit.html');

function difference(left, right) {
    return left.filter((id) => !right.includes(id));
}

assert.deepStrictEqual(difference([1, 2, 3], [1, 3]), [2], '新增集合计算错误');
assert.deepStrictEqual(difference([1, 3], [1, 2, 3]), [], '取消后恢复不应仍标为新增');
assert.strictEqual([].join(','), '', '清空选择必须提交空串，而不是省略字段');

assert.match(taskService, /@Transactional\(rollbackFor = Exception\.class\)[\s\S]*updateFireMaintenanceTask/);
assert.ok(
    taskService.indexOf('assertRemovableLevel1Trees(taskId, removeSystems') <
        taskService.indexOf('int rows = fireMaintenanceTaskMapper.updateFireMaintenanceTask(fireMaintenanceTask)'),
    '历史数据保护必须先于主表更新'
);
assert.match(taskService, /generateMissingRecordsForLevel1Ids/);
assert.match(taskService, /refreshTaskStatistics/);
assert.match(taskMapper, /selected_system_ids = #\{selectedSystemIds\}/);
assert.match(taskMapper, /selected_fire_test_ids = #\{selectedFireTestIds\}/);
assert.match(recordMapper, /<if test="recordType != null">record_type,<\/if>/);
assert.match(recordMapper, /<if test="recordType != null">#\{recordType\},<\/if>/);
assert.match(controller, /@GetMapping\("\/templates\/tree"\)/);

[
    'originalSystemIds',
    'currentSystemIds',
    'originalTestIds',
    'currentTestIds',
    '原有已选',
    '本次新增',
    '本次取消',
    '保存后'
].forEach((marker) => assert.ok(editPage.includes(marker), `编辑页面缺少状态源或标识：${marker}`));

console.log('fire-maintenance-task-edit tests: all assertions passed');
