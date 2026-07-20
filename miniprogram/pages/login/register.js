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
    const showPwd = common_vendor.ref(false);
    const showPwd2 = common_vendor.ref(false);
    const deptList = common_vendor.ref([]);
    const selectedDeptIndex = common_vendor.ref(-1);
    common_vendor.onMounted(() => {
      fetchDeptList();
    });
    const fetchDeptList = async () => {
      try {
        const res = await api_index.api.getDeptList();
        if (res.code === 200 || res.code === 0) {
          deptList.value = res.data || [];
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/login/register.vue:131", "Fetch Dept List Error:", e);
      }
    };
    const onDeptChange = (e) => {
      selectedDeptIndex.value = e.detail.value;
    };
    const canSubmit = common_vendor.computed(() => {
      return phone.value.length === 11 && nickName.value.length > 0 && password.value.length >= 6 && confirmPwd.value.length >= 6 && selectedDeptIndex.value !== -1;
    });
    const goLogin = () => {
      common_vendor.index.navigateTo({ url: "/pages/login/password" });
    };
    const handleRegister = async () => {
      if (!canSubmit.value) {
        common_vendor.index.__f__("log", "at pages/login/register.vue:155", canSubmit.value);
        common_vendor.index.showToast({ title: "请填写完整信息", icon: "none" });
        return;
      }
      if (password.value !== confirmPwd.value) {
        common_vendor.index.showToast({ title: "两次输入的密码不一致", icon: "none" });
        return;
      }
      if (selectedDeptIndex.value === -1) {
        common_vendor.index.showToast({ title: "请选择所属部门", icon: "none" });
        return;
      }
      common_vendor.index.showLoading({ title: "注册中...", mask: true });
      try {
        const res = await api_index.api.register({
          loginName: phone.value,
          // 登录账号（手机号）
          userName: nickName.value,
          // 用户昵称（姓名）
          phonenumber: phone.value,
          // 手机号码
          password: password.value,
          // 密码
          deptId: deptList.value[selectedDeptIndex.value].deptId
          // 部门ID
        });
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({ title: "注册成功", icon: "success" });
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        } else {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({ title: res.msg || "注册失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.hideLoading();
        common_vendor.index.__f__("error", "at pages/login/register.vue:196", "Register Error:", e);
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
        e: common_vendor.t(selectedDeptIndex.value === -1 ? "请选择所属部门" : deptList.value[selectedDeptIndex.value].deptName),
        f: selectedDeptIndex.value === -1 ? 1 : "",
        g: common_vendor.o(onDeptChange),
        h: selectedDeptIndex.value,
        i: deptList.value,
        j: showPwd.value ? "text" : "password",
        k: password.value,
        l: common_vendor.o(($event) => password.value = $event.detail.value),
        m: common_vendor.t(showPwd.value ? "👁" : "👁‍🗨"),
        n: common_vendor.o(($event) => showPwd.value = !showPwd.value),
        o: showPwd2.value ? "text" : "password",
        p: confirmPwd.value,
        q: common_vendor.o(($event) => confirmPwd.value = $event.detail.value),
        r: common_vendor.t(showPwd2.value ? "👁" : "👁‍🗨"),
        s: common_vendor.o(($event) => showPwd2.value = !showPwd2.value),
        t: canSubmit.value ? 1 : "",
        v: common_vendor.o(handleRegister),
        w: common_vendor.o(goLogin)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-12565c11"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login/register.js.map
