"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
const utils_request = require("../../utils/request.js");
if (!Math) {
  TabBar();
}
const TabBar = () => "../../components/TabBar.js";
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const userInfo = common_vendor.ref({
      userName: "",
      loginName: "",
      phonenumber: ""
    });
    const goPage = (url) => {
      common_vendor.index.showToast({ title: "功能开发中...", icon: "none" });
    };
    const handleLogout = () => {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要退出登录吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              await api_index.api.logout();
            } catch (e) {
              common_vendor.index.__f__("error", "at pages/mine/index.vue:72", "Logout API failed:", e);
            }
            utils_request.removeToken();
            common_vendor.index.reLaunch({ url: "/pages/login/index" });
          }
        }
      });
    };
    const loadUserInfo = async () => {
      try {
        const res = await api_index.api.getUserInfo();
        if (res.data) {
          userInfo.value = res.data;
        }
      } catch (e) {
      }
    };
    common_vendor.onMounted(() => {
      loadUserInfo();
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(userInfo.value.userName ? userInfo.value.userName.charAt(0) : "用"),
        b: common_vendor.t(userInfo.value.userName || "未登录"),
        c: common_vendor.t(userInfo.value.loginName || ""),
        d: common_vendor.o(($event) => goPage()),
        e: common_vendor.o(($event) => goPage()),
        f: common_vendor.o(($event) => goPage()),
        g: common_vendor.o(handleLogout),
        h: common_vendor.p({
          currentTab: 3
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-9023ef44"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/mine/index.js.map
