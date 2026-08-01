const { BASE_URL } = require("../config/env.js");
const auth = require("./auth.js");

function uploadFile(filePath, options) {
  const opts = options || {};
  return new Promise((resolve, reject) => {
    const token = auth.getToken();
    wx.uploadFile({
      url: BASE_URL + (opts.url || "/api/common/upload"),
      filePath,
      name: opts.name || "file",
      formData: opts.formData || {},
      header: {
        Authorization: token ? "Bearer " + token : ""
      },
      success(res) {
        let data = res.data;
        try {
          data = typeof data === "string" ? JSON.parse(data) : data;
        } catch (e) {}
        if (res.statusCode === 401 || (data && data.code === 401)) {
          auth.clearSession();
          wx.reLaunch({ url: "/pages/login/index" });
          reject(data || { code: 401 });
          return;
        }
        if (data && (data.code === 200 || data.code === 0)) {
          resolve(data);
          return;
        }
        wx.showToast({ title: (data && data.msg) || "上传失败", icon: "none" });
        reject(data || res);
      },
      fail(err) {
        wx.showToast({ title: "上传失败", icon: "none" });
        reject(err);
      }
    });
  });
}

module.exports = { uploadFile };
