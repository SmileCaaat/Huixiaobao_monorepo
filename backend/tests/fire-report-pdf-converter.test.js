/**
 * DocxToPdfConverter contract tests (source assertions + optional LO probe).
 */
const assert = require('assert');
const fs = require('fs');
const os = require('os');
const path = require('path');
const { spawnSync } = require('child_process');

const backend = path.resolve(__dirname, '..');
const converterPath = path.join(backend, 'ruoyi-system/src/main/java/com/ruoyi/system/service/report/DocxToPdfConverter.java');
const servicePath = path.join(backend, 'ruoyi-system/src/main/java/com/ruoyi/system/service/impl/FireReportRecordServiceImpl.java');
const ymlPath = path.join(backend, 'ruoyi-admin/src/main/resources/application.yml');

const converter = fs.readFileSync(converterPath, 'utf8');
const service = fs.readFileSync(servicePath, 'utf8');
const yml = fs.readFileSync(ymlPath, 'utf8');

const MSG_MISSING_LO = '\u672a\u627e\u5230LibreOffice\uff0c\u65e0\u6cd5\u751f\u6210PDF\u9884\u89c8\uff0c\u8bf7\u68c0\u67e5ruoyi.report.libreOfficePath\u914d\u7f6e\u3002';
const MSG_TIMEOUT = '\u8f6c\u6362\u8d85\u65f6';
const MSG_SEE_LOG = '\u8bf7\u67e5\u770b\u670d\u52a1\u5668\u65e5\u5fd7';
const REMARK_PDF_FAIL = 'PDF\u9884\u89c8\u8f6c\u6362\u5931\u8d25';

assert.ok(service.includes('docxToPdfConverter.convert(docxPath, pdfPath)'), 'pdf success path calls convert');
assert.ok(service.includes('DocxToPdfConverter.assertValidPdf(pdfPath)'), 'pdf success validates file');

assert.ok(converter.includes(MSG_MISSING_LO), 'missing lo utf8 msg');
assert.ok(service.includes('pdfFailReason'), 'captures pdf failure reason');
assert.ok(service.includes('storedFileName = docxFileName'), 'fallback store docx');

assert.ok(converter.includes('Files.isDirectory(path)'), 'treat config as install dir');
assert.ok(converter.includes('resolve("soffice.exe")'), 'join soffice.exe');

assert.ok(converter.includes('exitCode != 0'), 'non-zero exit handled');
assert.ok(converter.includes(MSG_SEE_LOG), 'user msg without raw console dump');

assert.ok(converter.includes('waitFor(CONVERT_TIMEOUT_SECONDS'), 'timeout wait');
assert.ok(converter.includes(MSG_TIMEOUT), 'timeout message');

assert.ok(converter.includes('Files.size(produced) <= 0'), 'empty pdf rejected');
assert.ok(converter.includes('assertValidPdf'), 'header validation');

assert.ok(converter.includes('new ProcessBuilder(command)'), 'ProcessBuilder list');
assert.ok(!/Runtime\.getRuntime\(\)\.exec\("/.test(converter), 'no single-string Runtime.exec');
assert.ok(converter.includes('UserInstallation'), 'isolated profile');
assert.ok(converter.includes('toUri()'), 'uri for user install');

assert.ok(service.includes('record.setFilePath(storedFileName)'), 'persist stored name');
assert.ok(service.includes(REMARK_PDF_FAIL), 'remark for pdf fail');

assert.ok(!converter.includes('\uFFFD'), 'no replacement char in converter');
assert.ok(service.includes('name.endsWith(".docx")'), 'docx branch');
assert.ok(service.includes('canPreview'), 'canPreview flag');
assert.ok(service.includes('/fire/report/download/'), 'relative download only');
assert.ok(yml.includes('${LIBREOFFICE_PATH:}'), 'yml env override');

function detectSoffice() {
  const candidates = [];
  if (process.env.LIBREOFFICE_PATH) candidates.push(process.env.LIBREOFFICE_PATH);
  candidates.push('C:\\Program Files\\LibreOffice\\program\\soffice.exe');
  candidates.push('C:\\Program Files (x86)\\LibreOffice\\program\\soffice.exe');
  candidates.push('/usr/bin/soffice');
  candidates.push('/usr/bin/libreoffice');
  for (const c of candidates) {
    try {
      if (c && fs.existsSync(c) && fs.statSync(c).isFile()) return c;
    } catch (_) {}
  }
  try {
    const r = spawnSync(process.platform === 'win32' ? 'where.exe' : 'command',
      process.platform === 'win32' ? ['soffice'] : ['-v', 'soffice'],
      { encoding: 'utf8' });
    if (r.status === 0 && r.stdout && r.stdout.trim()) {
      const line = r.stdout.split(/\r?\n/).map(s => s.trim()).filter(Boolean)[0];
      if (line && fs.existsSync(line)) return line;
    }
  } catch (_) {}
  return null;
}

const found = detectSoffice();
console.log(found ? ('LibreOffice detected: ' + found) : 'LibreOffice: not installed');

const spaceDir = path.join(os.tmpdir(), 'fire report space test');
const cnDir = path.join(os.tmpdir(), '\u7ef4\u4fdd\u62a5\u544a\u6d4b\u8bd5');
fs.mkdirSync(spaceDir, { recursive: true });
fs.mkdirSync(cnDir, { recursive: true });
assert.ok(fs.existsSync(spaceDir), 'space path mkdir');
assert.ok(fs.existsSync(cnDir), 'chinese path mkdir');

console.log('fire-report-pdf-converter tests: all assertions passed');
