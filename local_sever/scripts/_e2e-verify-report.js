const http = require('http');
const fs = require('fs');
const path = require('path');

const REPORT_ID = process.env.REPORT_ID || '193';
const OUT = 'C:/Users/Yincen/Desktop/Huixiaobao_monorepo/local_sever/logs';

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

  r = await req('GET', `/fire/report/check/${REPORT_ID}`, null, {
    Cookie: cookie,
    'X-CSRF-Token': token,
    'X-Requested-With': 'XMLHttpRequest',
    Accept: 'application/json'
  });
  console.log('CHECK', r.body);
  fs.writeFileSync(path.join(OUT, 'check-no-lo.json'), r.body, 'utf8');
  if (r.body.includes('\uFFFD\uFFFD')) throw new Error('mojibake in check');
  const check = JSON.parse(r.body);
  if (check.code !== 0) throw new Error('check failed: ' + check.msg);
  if (!(check.data && check.data.canPreview === false && check.data.format === 'docx')) {
    throw new Error('expected docx canPreview=false, got ' + JSON.stringify(check.data));
  }

  r = await req('GET', `/fire/report/download/${REPORT_ID}`, null, {
    Cookie: cookie,
    'X-CSRF-Token': token
  });
  console.log(
    'DOWNLOAD status',
    r.status,
    'ctype',
    r.headers['content-type'],
    'len',
    r.bodyBuf.length
  );
  const dlPath = path.join(OUT, `report-${REPORT_ID}.docx`);
  fs.writeFileSync(dlPath, r.bodyBuf);
  if (r.bodyBuf[0] !== 0x50 || r.bodyBuf[1] !== 0x4b) {
    throw new Error('downloaded file is not a docx/zip');
  }

  r = await req('GET', `/fire/report/view/${REPORT_ID}`, null, { Cookie: cookie });
  fs.writeFileSync(path.join(OUT, `preview-page-${REPORT_ID}.html`), r.body, 'utf8');
  console.log('VIEW status', r.status);

  console.log('verify-no-lo OK');
})();
