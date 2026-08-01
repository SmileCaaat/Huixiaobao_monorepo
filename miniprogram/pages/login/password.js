const { api } = require("../../api/index.js");
const auth = require("../../services/auth.js");

Page({
  data: {
    username: "",
    password: "",
    showPwd: false
  },
  onUsername(e) {
    this.setData({ username: e.detail.value });
  },
  onPassword(e) {
    this.setData({ password: e.detail.value });
  },
  togglePwd() {
    this.setData({ showPwd: !this.data.showPwd });
  },
  handleForgot() {
    wx.showToast({ title: "请联系管理员重置密码", icon: "none" });
  },
  goRegister() {
    wx.navigateTo({ url: "/pages/login/register" });
  },
  async handleLogin() {
    const username = (this.data.username || "").trim();
    const password = this.data.password || "";
    if (!username || password.length < 6) {
      wx.showToast({ title: "请输入完整信息", icon: "none" });
      return;
    }
    try {
      const res = await api.login({ username, password });
      if (res.data && res.data.token) {
        auth.setToken(res.data.token);
        if (res.data.user) auth.setUser(res.data.user);
        wx.showToast({ title: "登录成功", icon: "success" });
        setTimeout(() => {
          wx.reLaunch({ url: "/pages/index/index" });
        }, 800);
      } else {
        wx.showToast({ title: res.msg || "登录失败", icon: "none" });
      }
    } catch (e) {
      // toast 已由 request 处理
    }
  }
});
