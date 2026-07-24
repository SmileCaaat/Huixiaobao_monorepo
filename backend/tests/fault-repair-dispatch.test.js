const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (relative) => fs.readFileSync(path.join(backend, relative), 'utf8');

const dispatchPage = read('ruoyi-admin/src/main/resources/templates/fire/repair/dispatch.html');
const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireFaultRepairController.java');
const miniController = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java');
const repairService = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireFaultRepairServiceImpl.java');
const userService = read('ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysUserServiceImpl.java');
const repairMapper = read('ruoyi-system/src/main/resources/mapper/fire/FireFaultRepairMapper.xml');

assert.ok(dispatchPage.includes('name="repairUserId"'), 'form must submit repairUserId');
assert.ok(dispatchPage.includes("prefix + '/dispatchUsers/' + repairId"), 'must ajax load users');
assert.ok(dispatchPage.includes('user.userId'), 'value binds userId');
assert.ok(dispatchPage.includes('user.userName'), 'label uses userName');
assert.ok(dispatchPage.includes('user.phonenumber') || dispatchPage.includes('phonenumber'), 'label uses phonenumber');
assert.ok(dispatchPage.includes("userName + '（' + phone + '）'") || /userName\s*\?\s*\(userName\s*\+\s*'/.test(dispatchPage), 'display name+phone');
assert.ok(!/loginName/.test(dispatchPage.split('renderDispatchUsers')[1] || ''), 'should not show loginName in render');
assert.ok(dispatchPage.includes('暂无可派发的系统用户'), 'empty tip');
assert.ok(repairService.includes('selectActiveRegisteredUserList'), 'use all registered users');
assert.ok(repairService.includes('// 全部已注册、状态正常、未删除的系统用户') || /selectDispatchUsers[\s\S]{0,400}selectActiveRegisteredUserList/.test(repairService), 'dispatch users from all registered');
assert.ok(userService.includes('selectActiveRegisteredUserList'), 'user service method exists');
assert.match(controller, /@GetMapping\("\/dispatchUsers\/\{repairId\}"\)/);
assert.match(repairService, /@Transactional\(rollbackFor = Exception\.class\)[\s\S]*dispatchRepair\(/);
assert.ok(!repairService.includes('dispatchRepairToCurrentUser'), 'no default current user');
assert.match(repairMapper, /repair_user_id = #\{repairUserId\}/);
assert.match(miniController, /myAssignedRepairList[\s\S]*repair\.setRepairUserId\(userId\)/);
assert.match(miniController, /selectActiveRegisteredUserList/);

console.log('fault-repair-dispatch tests: all assertions passed');
