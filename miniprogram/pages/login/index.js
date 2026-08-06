Page({
  data: {},
  goPassword() {
    wx.navigateTo({ url: "/pages/login/password" });
  },
  goRegister() {
    wx.navigateTo({ url: "/pages/login/register" });
  }
});
