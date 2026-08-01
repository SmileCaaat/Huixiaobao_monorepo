/**
 * completeAll restricted to fire:task:completeAll + SysUser.isAdmin (source assertions).
 */
const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (rel) => fs.readFileSync(path.join(backend, rel), 'utf8');

const page = read('ruoyi-admin/src/main/resources/templates/fire/task/task.html');
const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireMaintenanceTaskController.java');
const sql = read('sql/upgrade_task_complete_all_permission.sql');

assert.ok(page.includes("hasPermi('fire:task:completeAll')"), 'completeAllFlag defined');
assert.ok(page.includes("' + completeAllFlag + '"), 'button gated by completeAllFlag');
assert.ok(page.includes("row.taskStatus == '1'"), 'status still required');
assert.ok(page.includes('briefingFlag'), 'briefing untouched');

assert.ok(controller.includes('@RequiresPermissions("fire:task:completeAll")'), 'shiro perm');
assert.ok(controller.includes('!user.isAdmin()'), 'admin check');
assert.ok(/completeAll[\s\S]*?getTaskStatus\(\)[\s\S]*?"1"/.test(controller)
  || controller.includes('"1".equals(existing.getTaskStatus())'), 'status check');

// saveBriefing must stay on briefing perm; completeAll must not use edit
const saveBriefingIdx = controller.indexOf('saveBriefing');
const completeIdx = controller.indexOf('completeAll(@PathVariable');
assert.ok(completeIdx > 0, 'completeAll method exists');
const beforeComplete = controller.slice(Math.max(0, completeIdx - 400), completeIdx);
assert.ok(beforeComplete.includes('fire:task:completeAll'), 'annotation near completeAll');
assert.ok(!beforeComplete.includes('@RequiresPermissions("fire:task:edit")'), 'completeAll not edit');

assert.ok(sql.includes('fire:task:completeAll'), 'sql registers perm');
assert.ok(!/INSERT INTO sys_role_menu/.test(sql), 'no role_menu grants');
assert.ok(!/role_key\s*=\s*'maintenance_member'/.test(sql), 'not granted to member by role_key');
assert.ok(!/role_key\s*=\s*'project_manager'/.test(sql), 'not granted to PM by role_key');
assert.ok(!/role_key\s*=\s*'team_leader'/.test(sql), 'not granted to team leader by role_key');

console.log('fire-task-complete-all-permission tests: all assertions passed');
