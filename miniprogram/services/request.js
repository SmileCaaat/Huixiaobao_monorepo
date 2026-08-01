const { BASE_URL } = require("../config/env.js");
const auth = require("./auth.js");

let redirecting401 = false;

function handleUnauthorized() {
  if (redirecting401) return;
  redirecting401 = true;
  auth.clearSession();
  wx.showToast({ title: "登录已过期，请重新登录", icon: "none" });
  setTimeout(() => {
    redirecting401 = false;
    wx.reLaunch({ url: "/pages/login/index" });
  }, 1200);
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
  // Keep toast short; full detail goes to console
  if (msg.indexOf("url not in domain list") >= 0) {
    return "域名未校验：请在详情里勾选不校验合法域名";
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
  const fullUrl = buildUrl(opts.url, opts.query);
  return new Promise((resolve, reject) => {
    if (loading) wx.showLoading({ title: "加载中...", mask: true });
    const token = auth.getToken();
    wx.request({
      url: fullUrl,
      method: opts.method || "GET",
      data: opts.data !== undefined ? opts.data : {},
      header: Object.assign(
        {
          "Content-Type": opts.contentType || "application/json"
        },
        token ? { Authorization: "Bearer " + token } : {},
        opts.header || {}
      ),
      success(res) {
        if (loading) wx.hideLoading();
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
        if (loading) wx.hideLoading();
        console.error("[request fail]", fullUrl, err);
        if (showError) wx.showToast({ title: shortErr(err), icon: "none", duration: 3500 });
        reject(err);
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
