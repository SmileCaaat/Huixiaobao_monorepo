const assert = require('assert');
const fs = require('fs');
const path = require('path');

const backend = path.resolve(__dirname, '..');
const read = (relative) => fs.readFileSync(path.join(backend, relative), 'utf8');

const listPage = read('ruoyi-admin/src/main/resources/templates/fire/report/report.html');
const previewPage = read('ruoyi-admin/src/main/resources/templates/fire/report/preview.html');
const controller = read('ruoyi-admin/src/main/java/com/ruoyi/web/controller/fire/FireReportController.java');
const service = read('ruoyi-system/src/main/java/com/ruoyi/system/service/impl/FireReportRecordServiceImpl.java');
const converter = read('ruoyi-system/src/main/java/com/ruoyi/system/service/report/DocxToPdfConverter.java');
const yml = read('ruoyi-admin/src/main/resources/application.yml');

assert.ok(listPage.includes('prefix + "/view/"'), 'relative view route');
assert.ok(!/127\.0\.0\.1/.test(listPage), 'no hardcoded 127.0.0.1');
assert.ok(!/:83\b/.test(listPage), 'no hardcoded port 83');
assert.ok(previewPage.includes('MSG_LOADING'), 'loading message constant');
assert.ok(previewPage.includes('\\u6b63\\u5728\\u52a0\\u8f7d\\u62a5\\u544a'), 'loading unicode');
assert.ok(previewPage.includes('/check/'), 'precheck before iframe');
assert.ok(previewPage.includes('/preview/'), 'iframe uses relative preview');

assert.match(controller, /@GetMapping\("\/view\/\{reportId\}"\)/);
assert.match(controller, /@GetMapping\("\/check\/\{reportId\}"\)/);
assert.ok(controller.includes('application/pdf'), 'pdf content type');
assert.ok(controller.includes('inline'), 'inline disposition for preview');
assert.ok(controller.includes('@RequiresPermissions("fire:report:list")'), 'permission on preview/download');

assert.ok(service.includes('docxToPdfConverter.convert'), 'convert to pdf');
assert.ok(service.includes('.pdf"'), 'pdf filename');
assert.ok(service.includes('resolveReportFile'), 'unified resolve');
assert.ok(converter.includes('soffice'), 'libreoffice soffice');
assert.ok(converter.includes('assertValidPdf'), 'validate pdf header');
assert.ok(yml.includes('libreOfficePath'), 'config key');

console.log('fire-report-pdf tests: all assertions passed');
