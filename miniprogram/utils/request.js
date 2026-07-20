"use strict";
const common_vendor = require("../common/vendor.js");
let BASE_URL = "https://huixiaobao-admin.site";
const TOKEN_KEY = "token";
const getToken = () => {
  return common_vendor.index.getStorageSync(TOKEN_KEY);
};
const setToken = (token) => {
  common_vendor.index.setStorageSync(TOKEN_KEY, token);
};
const removeToken = () => {
  common_vendor.index.removeStorageSync(TOKEN_KEY);
};
const request = (options) => {
  return new Promise((resolve, reject) => {
    if (options.loading !== false) {
      common_vendor.index.showLoading({
        title: "加载中...",
        mask: true
      });
    }
    const token = getToken();
    common_vendor.index.request({
      url: BASE_URL + options.url,
      method: options.method || "GET",
      data: options.data || {},
      header: {
        "Content-Type": options.contentType || "application/json",
        Authorization: token ? `Bearer ${token}` : ""
      },
      success: (res) => {
        if (options.loading !== false) {
          common_vendor.index.hideLoading();
        }
        if (res.statusCode === 200) {
          const data = res.data;
          if (data.code === 0 || data.code === 200) {
            resolve(data);
          } else if (data.code === 401) {
            removeToken();
            common_vendor.index.showToast({
              title: "登录已过期，请重新登录",
              icon: "none"
            });
            setTimeout(() => {
              common_vendor.index.reLaunch({
                url: "/pages/login/index"
              });
            }, 1500);
            reject(data);
          } else {
            if (options.showError !== false) {
              common_vendor.index.showToast({
                title: data.msg || "请求失败",
                icon: "none"
              });
            }
            reject(data);
          }
        } else {
          common_vendor.index.showToast({
            title: "网络请求失败",
            icon: "none"
          });
          reject(res);
        }
      },
      fail: (err) => {
        if (options.loading !== false) {
          common_vendor.index.hideLoading();
        }
        common_vendor.index.showToast({
          title: "网络连接失败",
          icon: "none"
        });
        reject(err);
      }
    });
  });
};
const get = (url, data, options = {}) => {
  return request({
    url,
    method: "GET",
    data,
    ...options
  });
};
const post = (url, data, options = {}) => {
  return request({
    url,
    method: "POST",
    data,
    ...options
  });
};
const upload = (filePath, formData = {}) => {
  return new Promise((resolve, reject) => {
    common_vendor.index.showLoading({
      title: "上传中...",
      mask: true
    });
    common_vendor.index.uploadFile({
      url: BASE_URL + "/api/common/upload",
      filePath,
      name: "file",
      formData,
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ""
      },
      success: (res) => {
        common_vendor.index.hideLoading();
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data);
          if (data.code === 0 || data.code === 200) {
            const normalizedData = data.data && typeof data.data === "object" ? { ...data.data } : {};
            if (!normalizedData.url && data.url) {
              normalizedData.url = data.url;
            }
            if (!normalizedData.fileName && data.fileName) {
              normalizedData.fileName = data.fileName;
            }
            if (!normalizedData.newFileName && data.newFileName) {
              normalizedData.newFileName = data.newFileName;
            }
            if (!normalizedData.originalFilename && data.originalFilename) {
              normalizedData.originalFilename = data.originalFilename;
            }
            resolve({
              ...data,
              data: normalizedData
            });
          } else {
            common_vendor.index.showToast({
              title: data.msg || "上传失败",
              icon: "none"
            });
            reject(data);
          }
        } else {
          common_vendor.index.showToast({
            title: "上传失败",
            icon: "none"
          });
          reject(res);
        }
      },
      fail: (err) => {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "上传失败",
          icon: "none"
        });
        reject(err);
      }
    });
  });
};
exports.BASE_URL = BASE_URL;
exports.get = get;
exports.getToken = getToken;
exports.post = post;
exports.removeToken = removeToken;
exports.setToken = setToken;
exports.upload = upload;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/request.js.map
