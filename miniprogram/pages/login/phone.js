"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_request = require("../../utils/request.js");
const _sfc_main = {
  __name: "phone",
  setup(__props) {
    const phone = common_vendor.ref("");
    const code = common_vendor.ref("");
    const countdown = common_vendor.ref(0);
    let timer = null;
    const canSubmit = common_vendor.computed(() => {
      return phone.value.length === 11 && code.value.length >= 4;
    });
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const sendCode = () => {
      if (countdown.value > 0)
        return;
      if (phone.value.length !== 11) {
        common_vendor.index.showToast({
          title: "请输入正确的手机号",
          icon: "none"
        });
        return;
      }
      common_vendor.index.showToast({
        title: "验证码已发送",
        icon: "success"
      });
      countdown.value = 60;
      timer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
          clearInterval(timer);
        }
      }, 1e3);
    };
    const handleLogin = async () => {
      if (!canSubmit.value)
        return;
      common_vendor.index.showLoading({ title: "登录中...", mask: true });
      try {
        const res = await api.login({
          username: phone.value,
          password: code.value
          // 将验证码作为密码尝试，或提示用户使用密码登录
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
        common_vendor.index.__f__("error", "at pages/login/phone.vue:137", "Phone Login Error:", e);
        common_vendor.index.showToast({
          title: e.msg || e.errMsg || "网络连接错误",
          icon: "none"
        });
      }
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: phone.value,
        c: common_vendor.o(($event) => phone.value = $event.detail.value),
        d: phone.value
      }, phone.value ? {
        e: common_vendor.o(($event) => phone.value = "")
      } : {}, {
        f: code.value,
        g: common_vendor.o(($event) => code.value = $event.detail.value),
        h: common_vendor.t(countdown.value > 0 ? `${countdown.value}s后重发` : "获取验证码"),
        i: countdown.value > 0 ? 1 : "",
        j: common_vendor.o(sendCode),
        k: canSubmit.value ? 1 : "",
        l: !canSubmit.value,
        m: common_vendor.o(handleLogin)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-ca029e96"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login/phone.js.map
