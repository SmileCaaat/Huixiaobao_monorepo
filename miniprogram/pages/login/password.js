"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
const utils_request = require("../../utils/request.js");
const _sfc_main = {
  __name: "password",
  setup(__props) {
    const phone = common_vendor.ref("");
    const password = common_vendor.ref("");
    const showPwd = common_vendor.ref(false);
    const canSubmit = common_vendor.computed(
      () => phone.value.length === 11 && password.value.length >= 6
    );
    const handleForgot = () => {
      common_vendor.index.showToast({ title: "请联系管理员重置密码", icon: "none" });
    };
    const goRegister = () => {
      common_vendor.index.navigateTo({ url: "/pages/login/register" });
    };
    const handleLogin = async () => {
      if (!canSubmit.value) {
        common_vendor.index.showToast({ title: "请输入完整信息", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "登录中...", mask: true });
      try {
        const res = await api_index.api.login({
          username: phone.value,
          password: password.value
        });
        common_vendor.index.hideLoading();
        if ((res.code === 200 || res.code === 0) && res.data && res.data.token) {
          utils_request.setToken(res.data.token);
          common_vendor.index.showToast({ title: "登录成功", icon: "success" });
          setTimeout(() => {
            common_vendor.index.reLaunch({ url: "/pages/index/index" });
          }, 1500);
        } else {
          common_vendor.index.showToast({ title: res.msg || "登录失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.hideLoading();
        common_vendor.index.__f__("error", "at pages/login/password.vue:112", "Login Error:", e);
        common_vendor.index.showToast({
          title: e.msg || e.errMsg || "网络连接错误",
          icon: "none"
        });
      }
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: phone.value,
        b: common_vendor.o(($event) => phone.value = $event.detail.value),
        c: phone.value
      }, phone.value ? {
        d: common_vendor.o(($event) => phone.value = "")
      } : {}, {
        e: showPwd.value ? "text" : "password",
        f: password.value,
        g: common_vendor.o(($event) => password.value = $event.detail.value),
        h: common_vendor.t(showPwd.value ? "👁" : "👁‍🗨"),
        i: common_vendor.o(($event) => showPwd.value = !showPwd.value),
        j: common_vendor.o(handleForgot),
        k: canSubmit.value ? 1 : "",
        l: common_vendor.o(handleLogin),
        m: common_vendor.o(goRegister)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-f5737803"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login/password.js.map
