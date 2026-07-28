"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
const _sfc_main = {
  __name: "register",
  setup(__props) {
    const phone = common_vendor.ref("");
    const nickName = common_vendor.ref("");
    const password = common_vendor.ref("");
    const confirmPwd = common_vendor.ref("");
    const smsCode = common_vendor.ref("");
    const inviteCode = common_vendor.ref("");
    const inviteToken = common_vendor.ref("");
    const orgCompany = common_vendor.ref("");
    const orgDept = common_vendor.ref("");
    const inviteHint = common_vendor.ref("请填写信息完成注册；扫码进入将自动锁定部门");
    const showPwd = common_vendor.ref(false);
    const showPwd2 = common_vendor.ref(false);
    const smsSending = common_vendor.ref(false);
    const smsLeft = common_vendor.ref(0);
    common_vendor.onLoad((options) => {
      if (options && options.inviteToken) {
        inviteToken.value = options.inviteToken;
        resolveInvite(options.inviteToken, "");
      } else if (options && options.inviteCode) {
        inviteCode.value = options.inviteCode;
        resolveInvite("", options.inviteCode);
      }
    });
    const resolveInvite = async (token, code) => {
      try {
        const res = await api_index.api.resolveRegisterInvite({ inviteToken: token, inviteCode: code });
        if ((res.code === 200 || res.code === 0) && res.data) {
          if (res.data.valid) {
            orgCompany.value = res.data.companyName || "";
            orgDept.value = res.data.deptName || "";
            const modeText = res.data.registerMode === "0" ? "自动通过" : "需人工审核";
            inviteHint.value = "邀请有效（" + modeText + "），公司/部门已锁定";
          } else {
            inviteHint.value = res.data.message || "邀请无效，将进入人工审核";
          }
        }
      } catch (e) {
        inviteHint.value = "邀请解析失败，可手动填写邀请码";
      }
    };
    const canSubmit = common_vendor.computed(() => {
      return phone.value.length === 11 && nickName.value.length > 0 && password.value.length >= 6 && confirmPwd.value.length >= 6 && smsCode.value.length >= 4;
    });
    const goLogin = () => {
      common_vendor.index.navigateTo({ url: "/pages/login/password" });
    };
    const sendSms = async () => {
      if (smsLeft.value > 0 || smsSending.value)
        return;
      if (!/^1\d{10}$/.test(phone.value)) {
        common_vendor.index.showToast({ title: "请输入正确手机号", icon: "none" });
        return;
      }
      smsSending.value = true;
      try {
        const res = await api_index.api.sendRegisterSms({ phonenumber: phone.value });
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({ title: res.msg || "验证码已发送", icon: "none" });
          smsLeft.value = 60;
          const timer = setInterval(() => {
            smsLeft.value -= 1;
            if (smsLeft.value <= 0)
              clearInterval(timer);
          }, 1e3);
        } else {
          common_vendor.index.showToast({ title: res.msg || "发送失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.showToast({ title: e.msg || "发送失败", icon: "none" });
      } finally {
        smsSending.value = false;
      }
    };
    const onInviteBlur = () => {
      if (inviteCode.value && !inviteToken.value) {
        resolveInvite("", inviteCode.value);
      }
    };
    const handleRegister = async () => {
      if (!canSubmit.value) {
        common_vendor.index.showToast({ title: "请填写完整信息", icon: "none" });
        return;
      }
      if (password.value !== confirmPwd.value) {
        common_vendor.index.showToast({ title: "两次输入的密码不一致", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "注册中...", mask: true });
      try {
        const res = await api_index.api.register({
          loginName: phone.value,
          userName: nickName.value,
          phonenumber: phone.value,
          password: password.value,
          smsCode: smsCode.value,
          inviteToken: inviteToken.value || undefined,
          inviteCode: inviteCode.value || undefined
        });
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.hideLoading();
          const tip = res.msg || (res.autoPassed ? "注册成功" : "注册成功，请等待审核");
          common_vendor.index.showToast({ title: tip, icon: "none", duration: 2e3 });
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1800);
        } else {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({ title: res.msg || "注册失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: e.msg || e.errMsg || "注册失败，请检查网络",
          icon: "none"
        });
      }
    };
    return (_ctx, _cache) => {
      return {
        a: phone.value,
        b: common_vendor.o(($event) => phone.value = $event.detail.value),
        c: nickName.value,
        d: common_vendor.o(($event) => nickName.value = $event.detail.value),
        e: inviteHint.value,
        f: orgCompany.value,
        g: orgDept.value,
        h: orgCompany.value || orgDept.value,
        i: smsCode.value,
        j: common_vendor.o(($event) => smsCode.value = $event.detail.value),
        k: smsLeft.value > 0 ? smsLeft.value + "s" : "获取验证码",
        l: common_vendor.o(sendSms),
        m: showPwd.value ? "text" : "password",
        n: password.value,
        o: common_vendor.o(($event) => password.value = $event.detail.value),
        p: common_vendor.t(showPwd.value ? "👁" : "👁‍🗨"),
        q: common_vendor.o(($event) => showPwd.value = !showPwd.value),
        r: showPwd2.value ? "text" : "password",
        s: confirmPwd.value,
        t: common_vendor.o(($event) => confirmPwd.value = $event.detail.value),
        u: common_vendor.t(showPwd2.value ? "👁" : "👁‍🗨"),
        v: common_vendor.o(($event) => showPwd2.value = !showPwd2.value),
        w: inviteCode.value,
        x: common_vendor.o(($event) => inviteCode.value = $event.detail.value),
        y: common_vendor.o(onInviteBlur),
        z: canSubmit.value ? 1 : "",
        A: common_vendor.o(handleRegister),
        B: common_vendor.o(goLogin)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-12565c11"]]);
wx.createPage(MiniProgramPage);
