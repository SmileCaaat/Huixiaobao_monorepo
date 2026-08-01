const http = require('http');
const https = require('https');
const { URL } = require('url');

function request(method, urlStr, { headers = {}, body, cookies = [] } = {}) {
  return new Promise((resolve, reject) => {
    const url = new URL(urlStr);
    const lib = url.protocol === 'https:' ? https : http;
    const data = body == null ? null : (typeof body === 'string' ? body : new URLSearchParams(body).toString());
    const req = lib.request({
      hostname: url.hostname,
      port: url.port,
      path: url.pathname + url.search,
      method,
      headers: {
        ...headers,
        Cookie: cookies.join('; '),
        ...(data ? { 'Content-Type': 'application/x-www-form-urlencoded', 'Content-Length': Buffer.byteLength(data) } : {})
      }
    }, (res) => {
      const chunks = [];
      res.on('data', (c) => chunks.push(c));
      res.on('end', () => {
        const setCookie = res.headers['set-cookie'] || [];
        const nextCookies = cookies.slice();
        for (const sc of setCookie) {
          const part = sc.split(';')[0];
          const name = part.split('=')[0];
          const idx = nextCookies.findIndex((c) => c.startsWith(name + '='));
          if (idx >= 0) nextCookies[idx] = part; else nextCookies.push(part);
        }
        resolve({ status: res.statusCode, headers: res.headers, body: Buffer.concat(chunks).toString('utf8'), cookies: nextCookies, location: res.headers.location });
      });
    });
    req.on('error', reject);
    if (data) req.write(data);
    req.end();
  });
}

async function login(username, password) {
  let r = await request('GET', 'http://127.0.0.1:83/login');
  let cookies = r.cookies;
  let csrf = null;
  const m1 = r.body.match(/name=["']csrfToken["']\s+value=["']([^"']+)["']/);
  const m2 = r.body.match(/csrfToken["']?\s*[:=]\s*["']([^"']+)["']/);
  csrf = (m1 && m1[1]) || (m2 && m2[1]) || null;
  const csrfCookie = cookies.find((c) => /csrf|xsrf/i.test(c));
  const headers = {};
  if (csrfCookie) headers['X-CSRF-TOKEN'] = csrfCookie.split('=')[1];
  const body = { username, password, rememberMe: 'false' };
  if (csrf) body.csrfToken = csrf;
  r = await request('POST', 'http://127.0.0.1:83/login', { headers, body, cookies });
  cookies = r.cookies;
  return cookies;
}

async function list(cookies) {
  const r = await request('POST', 'http://127.0.0.1:83/fire/checkIn/list', {
    body: { pageNum: 1, pageSize: 100 },
    cookies
  });
  return r;
}

async function detail(cookies, id) {
  return request('GET', `http://127.0.0.1:83/fire/checkIn/detail/${id}`, { cookies });
}

async function filterCompanies(cookies) {
  return request('POST', 'http://127.0.0.1:83/fire/checkIn/filterCompanies', { body: {}, cookies });
}

(async () => {
  const cases = [
    ['13413462481', '123456', 'lead-or-member'],
    ['admin', 'Test@123456', 'admin'],
    ['18782959011', 'Test@123456', 'project_manager']
  ];
  for (const [u, p, label] of cases) {
    const cookies = await login(u, p);
    const listRes = await list(cookies);
    let parsed;
    try { parsed = JSON.parse(listRes.body); } catch (e) { parsed = { raw: listRes.body.slice(0, 200) }; }
    const companies = await filterCompanies(cookies);
    let cParsed; try { cParsed = JSON.parse(companies.body); } catch (e) { cParsed = {}; }
    console.log(JSON.stringify({
      label, user: u, listCode: parsed.code, total: parsed.total, rows: (parsed.rows || []).length,
      companyCount: (cParsed.data || []).length,
      sampleUsers: (parsed.rows || []).slice(0, 3).map((x) => ({ id: x.checkInId, userId: x.userId, companyId: x.companyId }))
    }));
    if (label === 'lead-or-member') {
      const d137 = await detail(cookies, 137);
      const d144 = await detail(cookies, 144);
      console.log(JSON.stringify({
        detail137: { status: d137.status, denied: /403|无权限|unauth|无权/.test(d137.body) },
        detail144: { status: d144.status, ok: /签到|checkIn|维保/.test(d144.body) && !/403|无权限|unauth/.test(d144.body) }
      }));
      const forged = await request('POST', 'http://127.0.0.1:83/fire/checkIn/list', {
        body: { pageNum: 1, pageSize: 100, userId: 1 },
        cookies
      });
      const fj = JSON.parse(forged.body);
      console.log(JSON.stringify({ forgeUserId1: { total: fj.total, rows: (fj.rows || []).length } }));
    }
  }
})().catch((e) => { console.error(e); process.exit(1); });
