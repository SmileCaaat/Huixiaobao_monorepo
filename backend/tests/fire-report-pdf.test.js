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

// --- list / preview routes ---
assert.ok(listPage.includes('prefix + "/view/"'), 'relative view route');
assert.ok(!/127\.0\.0\.1/.test(listPage), 'no hardcoded 127.0.0.1');
assert.ok(!/:83\b/.test(listPage), 'no hardcoded port 83');
assert.ok(listPage.includes('downloadReport'), 'download action');
assert.ok(previewPage.includes('MSG_LOADING'), 'loading message constant');
assert.ok(previewPage.includes('\\u6b63\\u5728\\u52a0\\u8f7d\\u62a5\\u544a'), 'loading unicode');
assert.ok(previewPage.includes('/check/'), 'precheck before iframe');
assert.ok(previewPage.includes('/preview/'), 'iframe uses relative preview');
assert.ok(previewPage.includes('canPreview'), 'docx fallback by canPreview');
assert.ok(previewPage.includes('downloadWord'), 'docx download helper');
assert.ok(previewPage.includes('res.msg'), 'show backend message not whole response');

assert.match(controller, /@GetMapping\("\/view\/\{reportId\}"\)/);
assert.match(controller, /@GetMapping\("\/check\/\{reportId\}"\)/);
assert.ok(controller.includes('application/pdf'), 'pdf content type');
assert.ok(controller.includes('inline'), 'inline disposition for preview');
assert.ok(controller.includes('@RequiresPermissions("fire:report:list")'), 'permission on preview/download');
assert.ok(controller.includes('报告已生成，但PDF预览转换失败'), 'docx success warning message');
assert.ok(controller.includes('describeReportFile'), 'check uses describeReportFile');
assert.ok(!controller.includes('报告生成失败: " + e.getMessage()'), 'do not leak raw exception to user on generate');

// --- DOCX / PDF decoupling ---
assert.ok(service.includes('docxToPdfConverter.convert'), 'convert to pdf');
assert.ok(service.includes('.docx"'), 'docx filename');
assert.ok(service.includes('.pdf"'), 'pdf filename');
assert.ok(service.includes('resolveReportFile'), 'unified resolve');
assert.ok(service.includes('PDF 成功时保留同名 DOCX') || service.includes('供“下载 Word”') || service.includes('filePath 存 PDF'), 'keep docx when pdf ok');
assert.ok(controller.includes('siblingWithExtension') || controller.includes('siblingDocx'), 'download prefers sibling docx');
assert.ok(service.includes('describeReportFile'), 'describe report capability');
assert.ok(!/throw new ServiceException\("历史报告为 Word/.test(service), 'docx no longer hard-fail assert');

// --- converter quality ---
assert.ok(converter.includes('soffice'), 'libreoffice soffice');
assert.ok(converter.includes('assertValidPdf'), 'validate pdf header');
assert.ok(converter.includes('ProcessBuilder'), 'ProcessBuilder');
assert.ok(converter.includes('--convert-to'), 'convert-to flag');
assert.ok(converter.includes('UserInstallation'), 'isolated user profile');
assert.ok(converter.includes('toUri()'), 'URI for UserInstallation');
assert.ok(converter.includes('redirectErrorStream(true)'), 'merge streams');
assert.ok(converter.includes('CONVERT_TIMEOUT_SECONDS'), 'timeout');
assert.ok(converter.includes('destroyForcibly'), 'kill on timeout');
assert.ok(converter.includes("\u672a\u627e\u5230LibreOffice\uff0c\u65e0\u6cd5\u751f\u6210PDF\u9884\u89c8\uff0c\u8bf7\u68c0\u67e5ruoyi.report.libreOfficePath\u914d\u7f6e\u3002"), 'utf8 missing lo message');
assert.ok(!converter.includes('\uFFFD\uFFFD'), 'no replacement-char mojibake');
assert.ok(!/new String\([^)]*ISO-8859-1/.test(converter), 'no ISO-8859-1 re-encode');
assert.ok(!/getBytes\("GBK"\)/.test(converter), 'no GBK bytes');
assert.ok(converter.includes('LIBREOFFICE_PATH') || yml.includes('LIBREOFFICE_PATH'), 'env override');

// --- config ---
assert.ok(yml.includes('libreOfficePath'), 'config key');
assert.ok(yml.includes('${LIBREOFFICE_PATH:}'), 'env placeholder');

// --- no local disk path returned to browser in describe ---
assert.ok(service.includes('downloadPath'), 'relative download path key');
assert.ok(!/info\.put\("absolutePath"/.test(service), 'no absolute path in describe');

console.log('fire-report-pdf tests: all assertions passed');
