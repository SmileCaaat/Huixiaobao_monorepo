const http = require('http');
const fs = require('fs');
const path = require('path');

const OUT = 'C:/Users/Yincen/Desktop/Huixiaobao_monorepo/local_sever/logs';
const REPORT_DIR =
  'C:/Users/Yincen/Desktop/Huixiaobao_monorepo/backend/ruoyi-admin/uploadPath/report';

function req(method, urlPath, body, headers) {
  return new Promise((resolve, reject) => {
    const data = body
      ? (typeof body === 'string' ? body : new URLSearchParams(body).toString())
      : null;
    const opts = {
      hostname: '127.0.0.1',
      port: 83,
      path: urlPath,
      method,
      headers: {
        'User-Agent':
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        Accept: '*/*',
        ...(headers || {}),
        ...(data
          ? {
              'Content-Type': 'application/x-www-form-urlencoded',
              'Content-Length': Buffer.byteLength(data)
            }
          : {})
      }
    };
    const r = http.request(opts, (res) => {
      const chunks = [];
      res.on('data', (d) => chunks.push(d));
      res.on('end', () =>
        resolve({
          status: res.statusCode,
          headers: res.headers,
          bodyBuf: Buffer.concat(chunks),
          body: Buffer.concat(chunks).toString('utf8')
        })
      );
    });
    r.on('error', reject);
    if (data) r.write(data);
    r.end();
  });
}

function mergeCookies(headers, prev) {
  const map = Object.fromEntries(
    (prev || '')
      .split('; ')
      .filter(Boolean)
      .map((x) => {
        const i = x.indexOf('=');
        return [x.slice(0, i), x.slice(i + 1)];
      })
  );
  for (const c of headers['set-cookie'] || []) {
    const kv = c.split(';')[0];
    const i = kv.indexOf('=');
    map[kv.slice(0, i)] = kv.slice(i + 1);
  }
  return Object.entries(map)
    .map(([k, v]) => k + '=' + v)
    .join('; ');
}

function extractCsrf(html) {
  const m =
    html.match(/name="csrf-token"\s+content="([^"]+)"/) ||
    html.match(/content="([^"]+)"\s+name="csrf-token"/);
  return (m && m[1]) || '';
}

(async () => {
  let cookie = '';
  let r = await req('GET', '/login');
  cookie = mergeCookies(r.headers, cookie);
  r = await req(
    'POST',
    '/login',
    { username: 'admin', password: 'admin123', rememberMe: 'false' },
    { Cookie: cookie }
  );
  cookie = mergeCookies(r.headers, cookie);
  r = await req('GET', '/index', null, { Cookie: cookie });
  cookie = mergeCookies(r.headers, cookie);
  r = await req('GET', '/fire/report', null, { Cookie: cookie });
  cookie = mergeCookies(r.headers, cookie);
  const token = extractCsrf(r.body);

  r = await req(
    'POST',
    '/fire/report/generate',
    { companyId: '25', taskId: '53' },
    {
      Cookie: cookie,
      'X-CSRF-Token': token,
      'X-Requested-With': 'XMLHttpRequest'
    }
  );
  console.log('GEN', r.body);
  fs.writeFileSync(path.join(OUT, 'gen-with-mock-lo.json'), r.body, 'utf8');
  const gen = JSON.parse(r.body);
  if (gen.code !== 0) throw new Error('generate failed');
  const data = gen.data;
  if (!String(data.filePath).toLowerCase().endsWith('.pdf')) {
    throw new Error('expected pdf filePath, got ' + data.filePath);
  }
  const reportId = data.reportId;
  const base = path.basename(data.filePath, '.pdf');
  const pdfDisk = path.join(REPORT_DIR, data.filePath);
  const docxDisk = path.join(REPORT_DIR, base + '.docx');
  console.log('pdfDisk', pdfDisk, fs.existsSync(pdfDisk) && fs.statSync(pdfDisk).size);
  console.log('docxDisk', docxDisk, fs.existsSync(docxDisk) && fs.statSync(docxDisk).size);
  if (!fs.existsSync(pdfDisk) || fs.statSync(pdfDisk).size <= 0) throw new Error('pdf missing');
  if (!fs.existsSync(docxDisk) || fs.statSync(docxDisk).size <= 0) throw new Error('docx missing');

  r = await req('GET', `/fire/report/check/${reportId}`, null, {
    Cookie: cookie,
    'X-CSRF-Token': token,
    'X-Requested-With': 'XMLHttpRequest',
    Accept: 'application/json'
  });
  console.log('CHECK', r.body);
  const check = JSON.parse(r.body);
  if (!(check.data && check.data.canPreview === true && check.data.format === 'pdf')) {
    throw new Error('expected pdf preview');
  }

  r = await req('GET', `/fire/report/preview/${reportId}`, null, { Cookie: cookie });
  console.log('PREVIEW status', r.status, 'ctype', r.headers['content-type'], 'len', r.bodyBuf.length);
  fs.writeFileSync(path.join(OUT, `preview-${reportId}.pdf`), r.bodyBuf);
  if (r.bodyBuf.slice(0, 4).toString() !== '%PDF') throw new Error('preview not pdf');

  r = await req('GET', `/fire/report/download/${reportId}`, null, { Cookie: cookie });
  console.log('DOWNLOAD status', r.status, 'ctype', r.headers['content-type'], 'len', r.bodyBuf.length);
  fs.writeFileSync(path.join(OUT, `download-${reportId}.bin`), r.bodyBuf);
  if (r.bodyBuf[0] !== 0x50 || r.bodyBuf[1] !== 0x4b) {
    throw new Error('download should be docx when sibling exists');
  }

  // missing file case: fake id
  r = await req('GET', `/fire/report/check/999999`, null, {
    Cookie: cookie,
    'X-Requested-With': 'XMLHttpRequest',
    Accept: 'application/json'
  });
  console.log('MISSING', r.body);

  console.log('verify-with-mock-lo OK reportId=' + reportId);
})();
