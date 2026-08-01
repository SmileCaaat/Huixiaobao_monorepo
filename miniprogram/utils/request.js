/** @deprecated 请使用 services/request.js；本文件仅兼容旧引用 */
const requestSvc = require("../services/request.js");
const auth = require("../services/auth.js");

module.exports = {
  BASE_URL: requestSvc.BASE_URL,
  request: requestSvc.request,
  get: requestSvc.get,
  post: requestSvc.post,
  getToken: auth.getToken,
  setToken: auth.setToken,
  removeToken: auth.removeToken
};
