/**
 * Task briefing permission split + field-safe save (source assertions).
 */
const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (rel) => fs.readFileSync(path.join(backend, rel), 'utf8');

const page = read('ruoyi-admin/src/main/resources/templates/fire/task/task.html');
const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireMaintenanceTaskController.java');
const service = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireMaintenanceTaskServiceImpl.java');
const mapperXml = read('ruoyi-system/src/main/resources/mapper/fire/FireMaintenanceTaskMapper.xml');
const sql = read('sql/upgrade_task_briefing_permission.sql');

assert.ok(page.includes("hasPermi('fire:task:briefing')"), 'briefingFlag defined');
assert.ok(page.includes('briefingFlag'), 'briefingFlag used');
assert.ok(page.includes("' + editFlag + '"), 'edit uses editFlag');
assert.ok(page.includes("' + removeFlag + '"), 'remove uses removeFlag');
assert.ok(page.includes("' + briefingFlag + '"), 'briefing uses briefingFlag');

assert.ok(controller.includes('@RequiresPermissions("fire:task:briefing")'), 'briefing perm on controller');
assert.ok(controller.includes('saveBriefing'), 'saveBriefing exists');
assert.ok(!/@RequiresPermissions\("fire:task:edit"\)\s*\n\s*@GetMapping\("\/briefing/.test(controller), 'briefing page not on edit');
assert.ok(controller.includes('assertCanAccessTask'), 'task access assert');
assert.ok(controller.includes('updateTaskBriefing'), 'uses dedicated briefing update');
assert.ok(controller.includes('"1".equals(status)') && controller.includes('"2".equals(status)'), 'status gate 1/2');

assert.ok(service.includes('updateTaskBriefing'), 'service method');
assert.ok(mapperXml.includes('id="updateTaskBriefing"'), 'mapper briefing update');
const briefingBlock = (mapperXml.match(/<update id="updateTaskBriefing">[\s\S]*?<\/update>/) || [])[0] || '';
assert.ok(briefingBlock.includes('maintenance_summary') && briefingBlock.includes('maintenance_time'), 'only briefing cols');
assert.ok(!briefingBlock.includes('manager_id') && !briefingBlock.includes('task_status') && !briefingBlock.includes('company_id'), 'no non-briefing cols');

assert.ok(sql.includes('fire:task:briefing'), 'sql perm');
assert.ok(sql.includes("role_key = 'maintenance_member'"), 'grant by role_key');
assert.ok(!/role_id\s*=\s*\d+/.test(sql), 'no hardcoded role_id');

console.log('fire-task-briefing-permission tests: all assertions passed');
