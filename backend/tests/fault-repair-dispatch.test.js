const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (relative) => fs.readFileSync(path.join(backend, relative), 'utf8');

const dispatchPage = read('ruoyi-admin/src/main/resources/templates/fire/repair/dispatch.html');
const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireFaultRepairController.java');
const miniController = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java');
const repairService = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireFaultRepairServiceImpl.java');
const companyMapper = read('ruoyi-system/src/main/resources/mapper/fire/FireCompanyMapper.xml');
const repairMapper = read('ruoyi-system/src/main/resources/mapper/fire/FireFaultRepairMapper.xml');

assert.ok(dispatchPage.includes('name="repairUserId"'), '派发表单必须提交处理人用户ID');
assert.ok(dispatchPage.includes("prefix + '/dispatchUsers/' + repairId"), '必须异步请求处理人列表接口');
assert.ok(dispatchPage.includes('user.userId'), '下拉 value 必须绑定 userId');
assert.ok(dispatchPage.includes('user.userName'), '显示文本必须使用 userName');
assert.ok(dispatchPage.includes('请选择报修处理人'), '未选择处理人时必须有明确提示');
assert.ok(dispatchPage.includes('当前单位暂无可派发的员工账号'), '空人员列表必须有明确提示');
assert.ok(dispatchPage.includes('报修处理人加载失败，请稍后重试'), '加载失败必须有明确提示');
assert.ok(
    dispatchPage.includes('确认后，该故障报修任务将派发给所选报修处理人。'),
    '派发提示文案不正确'
);
assert.ok(!dispatchPage.includes('确认后将由当前登录账号处理'), '不能继续使用当前账号默认处理文案');
assert.ok(!dispatchPage.includes('handlerUserId'), '不得使用错误字段名 handlerUserId');
assert.match(controller, /@GetMapping\("\/dispatchUsers\/\{repairId\}"\)/);
assert.match(controller, /dispatchSave\(Long repairId, Long repairUserId\)/);
assert.match(controller, /dispatchRepair\(\s*repairId, repairUserId, ShiroUtils\.getLoginName\(\)\)/);
assert.match(repairService, /@Transactional\(rollbackFor = Exception\.class\)[\s\S]*dispatchRepair\(/);
assert.match(repairService, /selectActiveUserListByCompanyId\(repair\.getCompanyId\(\)\)/);
assert.ok(!repairService.includes('dispatchRepairToCurrentUser'), '不得再默认派发给当前登录账号');
assert.match(repairService, /validateDispatchAuthority\(repair\)/);
assert.match(companyMapper, /inner join sys_user u on uc\.user_id = u\.user_id/);
assert.match(companyMapper, /u\.del_flag = '0'/);
assert.match(companyMapper, /u\.status = '0'/);
assert.match(repairMapper, /repair_user_id = #\{repairUserId\}/);
assert.match(repairMapper, /dispatch_by = #\{dispatchBy\}/);
assert.match(repairMapper, /dispatch_time = #\{dispatchTime\}/);
assert.match(miniController, /myAssignedRepairList[\s\S]*repair\.setRepairUserId\(userId\)/);

console.log('fault-repair-dispatch tests: all assertions passed');
