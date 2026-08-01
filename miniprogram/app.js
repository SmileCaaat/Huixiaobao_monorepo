const auth = require("./services/auth.js");

const TAB_PAGES = [
  "/pages/index/index",
  "/pages/message/index",
  "/pages/scan/index",
  "/pages/mine/index"
];

App({
  onLaunch() {
    // 勿在 onLaunch 里对首屏 reLaunch：此时 getCurrentPages() 常为空，
    // 重复打开 pages/login/index 会在开发者工具刷成白屏。
  },
  onShow() {},
  globalData: {
    tabPages: TAB_PAGES
  }
});
