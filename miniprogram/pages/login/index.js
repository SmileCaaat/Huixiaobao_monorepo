"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const isAgreed = common_vendor.ref(false);
    const acceptNotify = common_vendor.ref(false);
    const toggleAgreement = () => {
      isAgreed.value = !isAgreed.value;
    };
    const toggleNotify = () => {
      acceptNotify.value = !acceptNotify.value;
    };
    const handlePhoneLogin = () => {
      if (!isAgreed.value) {
        common_vendor.index.showToast({ title: "请先同意用户协议", icon: "none" });
        return;
      }
      common_vendor.index.navigateTo({ url: "/pages/login/register" });
    };
    const goPasswordLogin = () => {
      if (!isAgreed.value) {
        common_vendor.index.showToast({ title: "请先同意用户协议", icon: "none" });
        return;
      }
      common_vendor.index.navigateTo({ url: "/pages/login/password" });
    };
    const openUserAgreement = () => {
      common_vendor.index.navigateTo({ url: "/pages/agreement/user" });
    };
    const openPrivacyPolicy = () => {
      common_vendor.index.navigateTo({ url: "/pages/agreement/privacy" });
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_assets._imports_0,
        b: common_vendor.o(handlePhoneLogin),
        c: common_vendor.o(goPasswordLogin),
        d: isAgreed.value
      }, isAgreed.value ? {} : {}, {
        e: isAgreed.value ? 1 : "",
        f: common_vendor.o(openUserAgreement),
        g: common_vendor.o(openPrivacyPolicy),
        h: common_vendor.o(toggleAgreement),
        i: acceptNotify.value
      }, acceptNotify.value ? {} : {}, {
        j: acceptNotify.value ? 1 : "",
        k: common_vendor.o(toggleNotify)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-45258083"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login/index.js.map
