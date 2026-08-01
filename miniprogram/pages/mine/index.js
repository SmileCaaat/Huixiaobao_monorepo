const { api } = require("../../api/index.js");
const auth = require("../../services/auth.js");

Page({
  data: {
    userName: "",
    loginName: "",
    phonenumber: ""
  },

  onShow() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: "/pages/login/index" });
      return;
    }
    this.loadUser();
  },

  async loadUser() {
    try {
      const res = await api.getUserInfo();
      const u = (res && res.data) || {};
      auth.setUser(u);
      this.setData({
        userName: u.userName || u.nickName || "",
        loginName: u.loginName || "",
        phonenumber: u.phonenumber || ""
      });
    } catch (e) {
      const cached = auth.getUser() || {};
      this.setData({
        userName: cached.userName || "",
        loginName: cached.loginName || "",
        phonenumber: cached.phonenumber || ""
      });
    }
  },

  async handleLogout() {
    try {
      await api.logout();
    } catch (e) {}
    auth.clearSession();
    wx.reLaunch({ url: "/pages/login/index" });
  }
});
