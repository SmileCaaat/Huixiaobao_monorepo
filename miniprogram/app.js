const TAB_PAGES = [
  "/pages/index/index",
  "/pages/message/index",
  "/pages/scan/index",
  "/pages/mine/index"
];

App({
  onLaunch() {
    // 勿在 onLaunch 里 reLaunch：页面栈为空时重复打开登录页会在开发者工具刷成白屏。
  },
  onShow() {},
  globalData: {
    tabPages: TAB_PAGES,
    authRedirecting: false
  }
});
