const { api } = require("../../api/index.js");

Page({
  data: {
    userName: "",
    phonenumber: "",
    password: "",
    inviteCode: "",
    showPwd: false
  },
  onUserName(e) {
    this.setData({ userName: e.detail.value });
  },
  onPhone(e) {
    this.setData({ phonenumber: e.detail.value });
  },
  onPassword(e) {
    this.setData({ password: e.detail.value });
  },
  onInvite(e) {
    this.setData({ inviteCode: e.detail.value });
  },
  togglePwd() {
    this.setData({ showPwd: !this.data.showPwd });
  },
  async handleRegister() {
    const userName = (this.data.userName || "").trim();
    const phonenumber = (this.data.phonenumber || "").trim();
    const password = this.data.password || "";
    const inviteCode = (this.data.inviteCode || "").trim().toUpperCase();
    if (!userName) {
      wx.showToast({ title: "请填写姓名", icon: "none" });
      return;
    }
    if (!/^1\d{10}$/.test(phonenumber)) {
      wx.showToast({ title: "请输入正确手机号", icon: "none" });
      return;
    }
    if (password.length < 6 || password.length > 20) {
      wx.showToast({ title: "密码长度需为 6-20 位", icon: "none" });
      return;
    }
    if (!inviteCode) {
      wx.showToast({ title: "请输入部门邀请码", icon: "none" });
      return;
    }
    try {
      const res = await api.register({
        userName,
        phonenumber,
        password,
        inviteCode
      });
      wx.showToast({ title: (res && res.msg) || "注册成功", icon: "success" });
      setTimeout(() => {
        wx.redirectTo({ url: "/pages/login/password" });
      }, 1000);
    } catch (e) {}
  }
});
