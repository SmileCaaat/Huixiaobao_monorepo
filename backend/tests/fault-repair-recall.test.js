const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (relative) => fs.readFileSync(path.join(backend, relative), 'utf8');

const listPage = read('ruoyi-admin/src/main/resources/templates/fire/repair/repair.html');
const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireFaultRepairController.java');
const miniController = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/api/FireMiniAppController.java');
const repairService = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/impl/FireFaultRepairServiceImpl.java');
const repairIface = read('ruoyi-system/src/main/java/com/ruoyi/fire/service/IFireFaultRepairService.java');
const repairMapperJava = read('ruoyi-system/src/main/java/com/ruoyi/fire/mapper/FireFaultRepairMapper.java');
const repairMapperXml = read('ruoyi-system/src/main/resources/mapper/fire/FireFaultRepairMapper.xml');

assert.ok(listPage.includes('recallRepair('), 'list must render recall action');
assert.ok(listPage.includes('canRecallRepair'), 'list must gate recall visibility');
assert.ok(listPage.includes("btn: ['\u53d6\u6d88', '\u786e\u5b9a\u64a4\u56de']"), 'confirm buttons order');
assert.ok(listPage.includes('\u64a4\u56de\u4e2d'), 'disable duplicate submit');
assert.ok(listPage.includes("prefix + '/recall'"), 'post recall endpoint');
assert.ok(listPage.includes('btn-warning'), 'warning style for recall');
assert.match(controller, /@PostMapping\("\/recall"\)/);
assert.match(controller, /@RequiresPermissions\("fire:repair:accept"\)[\s\S]*recall\(/);
assert.ok(controller.includes('\u64a4\u56de\u6210\u529f'), 'success message');
assert.ok(repairIface.includes('recallDispatch'), 'service interface');
assert.match(repairService, /@Transactional\(rollbackFor = Exception\.class\)[\s\S]*recallDispatch\(/);
assert.ok(repairService.includes('\u8be5\u5de5\u5355\u5df2\u5f00\u59cb\u5904\u7406\uff0c\u4e0d\u80fd\u64a4\u56de\u3002'), 'started reject message');
assert.ok(repairService.includes('\u8be5\u5de5\u5355\u5df2\u5b8c\u6210\uff0c\u4e0d\u80fd\u64a4\u56de\u3002'), 'completed reject message');
assert.ok(repairService.includes('\u5de5\u5355\u72b6\u6001\u5df2\u53d1\u751f\u53d8\u5316\uff0c\u8bf7\u5237\u65b0\u540e\u91cd\u8bd5\u3002'), 'stale state message');
assert.ok(repairMapperJava.includes('recallFireFaultRepair'), 'mapper method');
assert.match(repairMapperXml, /id="recallFireFaultRepair"/);
assert.ok(repairMapperXml.includes("repair_status = '0'"), 'restore pending');
assert.ok(repairMapperXml.includes('repair_user_id = NULL'), 'clear assignee');
assert.ok(repairMapperXml.includes('dispatch_time = NULL'), 'clear dispatch time');
assert.ok(repairMapperXml.includes('start_time IS NULL'), 'guard not started');
assert.match(miniController, /myAssignedRepairList[\s\S]*repair\.setRepairUserId\(userId\)/);
assert.ok(miniController.includes('\u5de5\u5355\u72b6\u6001\u5df2\u53d1\u751f\u53d8\u5316\uff0c\u8bf7\u5237\u65b0\u540e\u91cd\u8bd5\u3002'), 'mini complete rejects recalled');

console.log('fault-repair-recall tests: all assertions passed');
