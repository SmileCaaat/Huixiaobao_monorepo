const http = require('http');
const fs = require('fs');

function req(method, path, body, headers) {
  return new Promise((resolve, reject) => {
    const data = body
      ? (typeof body === 'string' ? body : new URLSearchParams(body).toString())
      : null;
    const opts = {
      hostname: '127.0.0.1',
      port: 83,
      path,
      method,
      headers: {
        'User-Agent':
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        Accept: 'text/html,application/json,*/*',
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
  console.log('LOGIN', r.body);

  // /index д�� session csrf_token��ҵ��ҳ include header �Ż���� meta
  r = await req('GET', '/index', null, { Cookie: cookie });
  cookie = mergeCookies(r.headers, cookie);
  console.log('index status', r.status, 'len', r.body.length);
  r = await req('GET', '/fire/report', null, { Cookie: cookie });
  cookie = mergeCookies(r.headers, cookie);
  let token = '';
  const m1 = r.body.match(/name="csrf-token"\s+content="([^"]+)"/);
  const m2 = r.body.match(/name='csrf-token'\s+content='([^']+)'/);
  const m3 = r.body.match(/content="([^"]+)"\s+name="csrf-token"/);
  token = (m1 && m1[1]) || (m2 && m2[1]) || (m3 && m3[1]) || '';
  console.log('CSRF', token || '(empty)');
  if (!token) {
    const idx = r.body.toLowerCase().indexOf('csrf');
    console.log('csrf snippet', r.body.slice(Math.max(0, idx - 40), idx + 120));
    console.log('index len', r.body.length, 'status', r.status);
    require('fs').writeFileSync(
      'C:/Users/Yincen/Desktop/Huixiaobao_monorepo/local_sever/logs/index.html',
      r.body,
      'utf8'
    );
  }

  r = await req(
    'POST',
    '/fire/report/generate',
    { companyId: '25', taskId: '54' },
    {
      Cookie: cookie,
      'X-CSRF-Token': token,
      'X-Requested-With': 'XMLHttpRequest'
    }
  );
  console.log('GEN_STATUS', r.status);
  console.log('GEN', r.body.slice(0, 800));
  fs.writeFileSync(
    'C:/Users/Yincen/Desktop/Huixiaobao_monorepo/local_sever/logs/gen-no-lo.json',
    r.body,
    'utf8'
  );
})();
