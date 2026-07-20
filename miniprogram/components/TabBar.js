"use strict";
const common_vendor = require("../common/vendor.js");
const common_assets = require("../common/assets.js");
const _sfc_main = {
  __name: "TabBar",
  props: {
    currentTab: {
      type: Number,
      default: 0
    }
  },
  setup(__props) {
    const props = __props;
    const tabPages = [
      "/pages/index/index",
      "/pages/message/index",
      "/pages/scan/index",
      "/pages/mine/index"
    ];
    const switchTab = (index) => {
      if (index === props.currentTab)
        return;
      common_vendor.index.redirectTo({ url: tabPages[index] });
    };
    return (_ctx, _cache) => {
      return {
        a: common_assets._imports_0$1,
        b: __props.currentTab === 0 ? 1 : "",
        c: common_vendor.o(($event) => switchTab(0)),
        d: common_assets._imports_1,
        e: __props.currentTab === 1 ? 1 : "",
        f: common_vendor.o(($event) => switchTab(1)),
        g: common_assets._imports_2,
        h: __props.currentTab === 2 ? 1 : "",
        i: common_vendor.o(($event) => switchTab(2)),
        j: common_assets._imports_3,
        k: __props.currentTab === 3 ? 1 : "",
        l: common_vendor.o(($event) => switchTab(3))
      };
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-7d9a6b19"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../.sourcemap/mp-weixin/components/TabBar.js.map
