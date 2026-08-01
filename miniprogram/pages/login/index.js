const auth = require("../../services/auth.js");

Page({
  data: {
    ready: true
  },
  onShow() {
    // 已登录则进首页；避免与 App.onLaunch 叠加重定向
    if (auth.isLoggedIn()) {
      wx.reLaunch({ url: "/pages/index/index" });
    }
  },
  goPassword() {
    wx.navigateTo({ url: "/pages/login/password" });
  },
  goRegister() {
    wx.navigateTo({ url: "/pages/login/register" });
  }
});
