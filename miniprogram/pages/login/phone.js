Page({
  data: {
    title: "手机号登录",
    tip: "本轮使用账号密码登录；手机号登录后续接入。"
  },
  goBack() {
    wx.navigateBack({ fail: () => wx.redirectTo({ url: "/pages/login/index" }) });
  }
});
