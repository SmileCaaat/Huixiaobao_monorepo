const { BASE_URL } = require("../config/env.js");
const auth = require("./auth.js");

let redirecting401 = false;
let loadingCount = 0;
const DEFAULT_TIMEOUT_MS = 12000;

function showLoadingSafe(title, mask) {
  loadingCount += 1;
  if (loadingCount === 1) {
    wx.showLoading({ title: title || "加载中...", mask: !!mask });
  }
}

function hideLoadingSafe() {
  if (loadingCount <= 0) {
    loadingCount = 0;
    return;
  }
  loadingCount -= 1;
  if (loadingCount === 0) {
    try {
      wx.hideLoading();
    } catch (e) {
      // ignore
    }
  }
}

function handleUnauthorized() {
  if (redirecting401) return;
  redirecting401 = true;
  auth.clearSession();
  try {
    const pages = getCurrentPages() || [];
    const cur = pages.length ? pages[pages.length - 1] : null;
    const route = (cur && (cur.route || cur.__route__)) || "";
    if (route.indexOf("pages/login/") === 0) {
      redirecting401 = false;
      return;
    }
  } catch (e) {
    // ignore
  }
  try {
    const app = getApp();
    if (app && app.globalData) app.globalData.authRedirecting = true;
  } catch (e) {
    // ignore
  }
  wx.showToast({ title: "登录已过期，请重新登录", icon: "none" });
  setTimeout(() => {
    wx.reLaunch({
      url: "/pages/login/index",
      complete() {
        redirecting401 = false;
        try {
          const app = getApp();
          if (app && app.globalData) app.globalData.authRedirecting = false;
        } catch (e) {
          // ignore
        }
      }
    });
  }, 400);
}

function buildUrl(url, query) {
  let full = url.startsWith("http") ? url : BASE_URL + url;
  if (!query || typeof query !== "object") return full;
  const parts = [];
  Object.keys(query).forEach((k) => {
    const v = query[k];
    if (v === undefined || v === null || v === "") return;
    parts.push(encodeURIComponent(k) + "=" + encodeURIComponent(v));
  });
  if (!parts.length) return full;
  return full + (full.indexOf("?") >= 0 ? "&" : "?") + parts.join("&");
}

function shortErr(err) {
  const msg = (err && (err.errMsg || err.message)) || "";
  if (!msg) return "网络连接失败";
  if (msg.indexOf("url not in domain list") >= 0) {
    return "域名未校验：请在详情里勾选不校验合法域名";
  }
  if (msg.indexOf("timeout") >= 0 || msg.indexOf("TIME_OUT") >= 0) {
    return "请求超时，请确认后端已启动 " + BASE_URL;
  }
  if (msg.indexOf("connect fail") >= 0 || msg.indexOf("CONNECTION_REFUSED") >= 0) {
    return "连不上后端 " + BASE_URL;
  }
  return msg.length > 36 ? msg.slice(0, 36) + "..." : msg;
}

function request(options) {
  const opts = options || {};
  const loading = opts.loading !== false;
  const showError = opts.showError !== false;
  const mask = opts.mask === true;
  const fullUrl = buildUrl(opts.url, opts.query);
  let settled = false;

  return new Promise((resolve, reject) => {
    if (loading) showLoadingSafe("加载中...", mask);

    const finish = () => {
      if (settled) return;
      settled = true;
      if (loading) hideLoadingSafe();
    };

    const token = auth.getToken();
    wx.request({
      url: fullUrl,
      method: opts.method || "GET",
      data: opts.data !== undefined ? opts.data : {},
      timeout: opts.timeout || DEFAULT_TIMEOUT_MS,
      header: Object.assign(
        {
          "Content-Type": opts.contentType || "application/json"
        },
        token ? { Authorization: "Bearer " + token } : {},
        opts.header || {}
      ),
      success(res) {
        finish();
        if (res.statusCode === 401) {
          handleUnauthorized();
          reject({ code: 401, msg: "未授权", statusCode: 401 });
          return;
        }
        if (res.statusCode !== 200) {
          if (showError) {
            wx.showToast({ title: "网络请求失败(" + res.statusCode + ")", icon: "none" });
          }
          reject(res);
          return;
        }
        const data = res.data || {};
        if (data.code === 401) {
          handleUnauthorized();
          reject(data);
          return;
        }
        if (data.code === 0 || data.code === 200) {
          resolve(data);
          return;
        }
        if (showError) {
          wx.showToast({ title: data.msg || "请求失败", icon: "none" });
        }
        reject(data);
      },
      fail(err) {
        finish();
        console.error("[request fail]", fullUrl, err);
        if (showError) wx.showToast({ title: shortErr(err), icon: "none", duration: 3500 });
        reject(err);
      },
      complete() {
        // Safety net if success/fail path is interrupted.
        finish();
      }
    });
  });
}

function get(url, query, options) {
  return request(
    Object.assign({}, options, {
      url,
      method: "GET",
      query: query || {},
      data: {}
    })
  );
}

function post(url, data, options) {
  const opts = options || {};
  const body = data || {};
  let query = Object.assign({}, opts.query || {});
  if (body && !opts.keepPageInBody) {
    if (body.pageNum != null) query.pageNum = body.pageNum;
    if (body.pageSize != null) query.pageSize = body.pageSize;
  }
  return request(
    Object.assign({}, opts, {
      url,
      method: "POST",
      data: body,
      query
    })
  );
}

module.exports = {
  BASE_URL,
  request,
  get,
  post,
  handleUnauthorized
};
