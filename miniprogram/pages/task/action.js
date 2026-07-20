"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  (_easycom_uni_nav_bar2 + _easycom_uni_icons2)();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  (_easycom_uni_nav_bar + _easycom_uni_icons)();
}
const _sfc_main = {
  __name: "action",
  setup(__props) {
    const taskId = common_vendor.ref(null);
    const taskInfo = common_vendor.ref({});
    common_vendor.onLoad((options) => {
      taskId.value = options.id;
      const currentTask = common_vendor.index.getStorageSync("currentTask");
      if (currentTask && currentTask.taskId == taskId.value) {
        taskInfo.value = currentTask;
      }
    });
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goToMaintenance = () => {
      common_vendor.index.navigateTo({
        url: `/pages/task/detail?id=${taskId.value}&recordType=0`
      });
    };
    const goToFireTest = () => {
      common_vendor.index.navigateTo({
        url: `/pages/task/detail?id=${taskId.value}&recordType=1`
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "选择操作",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.t(taskInfo.value.taskName || "维保任务"),
        d: common_vendor.t(taskInfo.value.companyName || "所属公司"),
        e: common_vendor.p({
          type: "wrench-filled",
          size: "40",
          color: "#ffffff"
        }),
        f: common_vendor.o(goToMaintenance),
        g: common_vendor.p({
          type: "fire-filled",
          size: "40",
          color: "#ffffff"
        }),
        h: common_vendor.o(goToFireTest)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-23c4102b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/task/action.js.map
