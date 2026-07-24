const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (relative) => fs.readFileSync(path.join(backend, relative), 'utf8');

const page = read('ruoyi-admin/src/main/resources/templates/fire/report/addJob.html');
const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireReportController.java');
const serviceIface = read('ruoyi-system/src/main/java/com/ruoyi/system/service/IFireReportRecordService.java');
const serviceImpl = read('ruoyi-system/src/main/java/com/ruoyi/system/service/impl/FireReportRecordServiceImpl.java');

assert.ok(page.includes('id="companyId"'), 'company select exists');
assert.ok(page.includes('请选择客户'), 'company placeholder');
assert.ok(page.includes('请先选择客户'), 'task placeholder before company');
assert.ok(page.includes('manualTaskId'), 'manual task select');
assert.ok(page.includes('prop("disabled"'), 'task disabled until company chosen');
assert.ok(page.includes('onCompanyChange'), 'company change handler');
assert.ok(page.includes('formatTaskLabel'), 'task label with date/id');
assert.ok(page.includes('该客户暂无可生成报告的维保任务'), 'empty tip');
assert.ok(page.includes('companyId: companyId, taskId: taskId'), 'submit both ids');
assert.ok(page.includes("prefix + \"/companies\""), 'load companies api');
assert.ok(page.includes("prefix + \"/tasks\""), 'load tasks api');

assert.match(controller, /@GetMapping\("\/companies"\)/);
assert.match(controller, /@GetMapping\("\/tasks"\)/);
assert.match(controller, /generate\(Long companyId, Long taskId\)/);
assert.ok(controller.includes('query.setCompanyId(companyId)'), 'tasks filtered by companyId');
assert.ok(serviceIface.includes('generateReportForTask(Long companyId, Long taskId)'), 'service overload');
assert.ok(serviceImpl.includes('维保任务不属于所选客户'), 'ownership check');
assert.ok(serviceImpl.includes('doGenerateReportForTask'), 'shared generate core');
assert.ok(serviceImpl.includes('generateReportForTask(Long taskId)'), 'quartz path kept');

console.log('fire-report-generate-company tests: all assertions passed');
