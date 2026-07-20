"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  _easycom_uni_nav_bar2();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
if (!Math) {
  _easycom_uni_nav_bar();
}
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const equipmentList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const currentCompanyId = common_vendor.ref(null);
    const groupedEquipment = common_vendor.computed(() => {
      const groups = {};
      equipmentList.value.forEach((item) => {
        const type = item.systemName || item.equipmentType || item.systemType || "其他";
        const typeName = type;
        if (!groups[type]) {
          groups[type] = {
            type,
            typeName,
            count: 0,
            items: []
          };
        }
        groups[type].count++;
        groups[type].items.push(item);
      });
      return Object.values(groups).sort((a, b) => b.count - a.count);
    });
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goAdd = () => {
      common_vendor.index.navigateTo({
        url: "/pages/equipment/form"
      });
    };
    const goTypeList = (group) => {
      common_vendor.index.setStorageSync("currentTypeEquipments", group.items);
      common_vendor.index.navigateTo({
        url: `/pages/equipment/list?type=${encodeURIComponent(
          group.type
        )}&typeName=${encodeURIComponent(group.typeName)}`
      });
    };
    const loadCurrentCompany = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          currentCompanyId.value = res.data.companyId;
          return res.data.companyId;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/equipment/index.vue:120", "获取当前公司失败:", e);
      }
      return null;
    };
    const loadEquipmentList = async () => {
      try {
        loading.value = true;
        if (!currentCompanyId.value) {
          const companyId = await loadCurrentCompany();
          if (!companyId) {
            common_vendor.index.showToast({ title: "请先选择公司", icon: "none" });
            return;
          }
        }
        const res = await api_index.api.getEquipmentList({
          companyId: currentCompanyId.value,
          buildingId: "",
          equipmentType: "",
          status: "",
          pageNum: 1,
          pageSize: 500
        });
        if (res.code === 200 || res.code === 0) {
          const data = res.rows || res.data || [];
          equipmentList.value = Array.isArray(data) ? data : data.rows || data.list || [];
        } else {
          equipmentList.value = [];
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/equipment/index.vue:156", "获取设备列表失败:", e);
        common_vendor.index.showToast({ title: "获取设备列表失败", icon: "none" });
        equipmentList.value = [];
      } finally {
        loading.value = false;
      }
    };
    const handleRefresh = () => {
      loadEquipmentList();
    };
    common_vendor.onShow(async () => {
      await loadCurrentCompany();
      loadEquipmentList();
    });
    common_vendor.onMounted(() => {
      common_vendor.index.$on("refreshEquipmentList", handleRefresh);
    });
    common_vendor.onUnmounted(() => {
      common_vendor.index.$off("refreshEquipmentList", handleRefresh);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "设备管理",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.f(groupedEquipment.value, (group, k0, i0) => {
          return {
            a: common_vendor.t(group.typeName || group.type),
            b: common_vendor.t(group.count),
            c: group.type,
            d: common_vendor.o(($event) => goTypeList(group), group.type)
          };
        }),
        d: groupedEquipment.value.length === 0 && !loading.value
      }, groupedEquipment.value.length === 0 && !loading.value ? {} : {}, {
        e: common_vendor.o(goAdd)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-a961c2a9"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/equipment/index.js.map
