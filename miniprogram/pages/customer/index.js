"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_index = require("../../api/index.js");
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
  __name: "index",
  setup(__props) {
    const searchKeyword = common_vendor.ref("");
    const customerList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const filteredList = common_vendor.computed(() => {
      if (!searchKeyword.value.trim())
        return customerList.value;
      const keyword = searchKeyword.value.trim().toLowerCase();
      return customerList.value.filter(
        (item) => item.companyName.toLowerCase().includes(keyword) || item.address && item.address.toLowerCase().includes(keyword)
      );
    });
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const handleSearch = () => {
    };
    const goDetail = (item) => {
      common_vendor.index.showModal({
        title: "客户详情",
        content: `名称: ${item.companyName}
联系人: ${item.contactPerson || "无"}
地址: ${item.address || "无"}`,
        showCancel: false
      });
    };
    const makeCall = (phone) => {
      common_vendor.index.makePhoneCall({
        phoneNumber: phone
      });
    };
    const loadData = async () => {
      if (loading.value)
        return;
      loading.value = true;
      try {
        const res = await api_index.api.getMyCompanyList();
        common_vendor.index.__f__("log", "at pages/customer/index.vue:116", "获取客户列表结果:", res);
        if (res.data) {
          customerList.value = res.data;
          common_vendor.index.__f__("log", "at pages/customer/index.vue:119", "客户数据已加载, 数量:", customerList.value.length);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/customer/index.vue:122", "加载客户数据异常:", e);
        common_vendor.index.showToast({ title: "加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const loadMore = () => {
      common_vendor.index.__f__("log", "at pages/customer/index.vue:131", "已到底部");
    };
    common_vendor.onMounted(() => {
      common_vendor.index.__f__("log", "at pages/customer/index.vue:135", "维保客户页面已挂载");
      loadData();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "维保客户",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.p({
          type: "search",
          size: "18",
          color: "#999"
        }),
        d: common_vendor.o([($event) => searchKeyword.value = $event.detail.value, handleSearch]),
        e: searchKeyword.value,
        f: common_vendor.f(filteredList.value, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.companyName),
            b: common_vendor.t(item.status === "0" ? "正常" : "停用"),
            c: common_vendor.n(item.status === "0" ? "active" : "inactive"),
            d: "cda5385a-2-" + i0,
            e: common_vendor.t(item.address || "暂无地址"),
            f: "cda5385a-3-" + i0,
            g: common_vendor.t(item.contactPerson || "暂无联系人"),
            h: item.contactPhone
          }, item.contactPhone ? {
            i: common_vendor.t(item.contactPhone),
            j: common_vendor.o(($event) => makeCall(item.contactPhone), item.companyId)
          } : {}, {
            k: "cda5385a-4-" + i0,
            l: item.companyId,
            m: common_vendor.o(($event) => goDetail(item), item.companyId)
          });
        }),
        g: common_vendor.p({
          type: "location",
          size: "14",
          color: "#999"
        }),
        h: common_vendor.p({
          type: "person",
          size: "14",
          color: "#999"
        }),
        i: common_vendor.p({
          type: "right",
          size: "16",
          color: "#ccc"
        }),
        j: filteredList.value.length === 0 && !loading.value
      }, filteredList.value.length === 0 && !loading.value ? {
        k: common_assets._imports_0
      } : {}, {
        l: loading.value
      }, loading.value ? {} : {}, {
        m: common_vendor.o(loadMore)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-cda5385a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/customer/index.js.map
