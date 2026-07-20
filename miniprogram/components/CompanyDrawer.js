"use strict";
const common_vendor = require("../common/vendor.js");
const api_index = require("../api/index.js");
const _sfc_main = {
  __name: "CompanyDrawer",
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    currentCompanyId: {
      type: [Number, String],
      default: null
    }
  },
  emits: ["close", "select"],
  setup(__props, { emit: __emit }) {
    const props = __props;
    const emit = __emit;
    const searchKeyword = common_vendor.ref("");
    const companyList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const filteredCompanyList = common_vendor.computed(() => {
      if (!searchKeyword.value.trim()) {
        return companyList.value || [];
      }
      const keyword = searchKeyword.value.trim().toLowerCase();
      return (companyList.value || []).filter((item) => {
        const name = (item.companyName || "").toLowerCase();
        const shortName = (item.shortName || "").toLowerCase();
        const address = (item.address || "").toLowerCase();
        return name.includes(keyword) || shortName.includes(keyword) || address.includes(keyword);
      });
    });
    const handleSearch = () => {
    };
    const selectCompany = async (company) => {
      loading.value = true;
      try {
        const res = await api_index.switchCompany({ companyId: company.companyId });
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({ title: "切换成功", icon: "success" });
          emit("select", company);
          closeDrawer();
        } else {
          common_vendor.index.showToast({ title: res.msg || "切换失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at components/CompanyDrawer.vue:112", "切换公司失败:", e);
        common_vendor.index.showToast({ title: "切换公司失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const closeDrawer = () => {
      emit("close");
    };
    const loadCompanyList = async () => {
      loading.value = true;
      try {
        const res = await api_index.getMyCompanyList();
        if ((res.code === 200 || res.code === 0) && res.data) {
          companyList.value = res.data.map((item) => ({
            companyId: item.companyId,
            companyName: item.companyName || "未命名公司",
            shortName: item.shortName || "",
            address: item.address || "暂无地址",
            contactPerson: item.contactPerson || "",
            contactPhone: item.contactPhone || "",
            checkInAddress: item.checkInAddress || "",
            checkInLongitude: item.checkInLongitude,
            checkInLatitude: item.checkInLatitude,
            checkInRadius: item.checkInRadius,
            status: item.status
          }));
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at components/CompanyDrawer.vue:146", "获取公司列表失败:", e);
        common_vendor.index.showToast({ title: "获取公司列表失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    common_vendor.watch(
      () => props.visible,
      (newVal) => {
        if (newVal) {
          searchKeyword.value = "";
          loadCompanyList();
        }
      }
    );
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: __props.visible
      }, __props.visible ? common_vendor.e({
        b: common_vendor.o([($event) => searchKeyword.value = $event.detail.value, handleSearch]),
        c: searchKeyword.value,
        d: common_vendor.f(filteredCompanyList.value, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.companyName),
            b: item.companyId === __props.currentCompanyId
          }, item.companyId === __props.currentCompanyId ? {} : {}, {
            c: common_vendor.t(item.address),
            d: item.companyId === __props.currentCompanyId ? 1 : "",
            e: item.companyId,
            f: common_vendor.o(($event) => selectCompany(item), item.companyId)
          });
        }),
        e: filteredCompanyList.value.length === 0
      }, filteredCompanyList.value.length === 0 ? {} : {}, {
        f: loading.value
      }, loading.value ? {} : {}, {
        g: common_vendor.o(() => {
        }),
        h: common_vendor.o(closeDrawer)
      }) : {});
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-8688cdd1"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../.sourcemap/mp-weixin/components/CompanyDrawer.js.map
