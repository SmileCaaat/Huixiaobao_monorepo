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
    const buildingList = common_vendor.ref([]);
    const currentCompanyId = common_vendor.ref(null);
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goAdd = () => {
      common_vendor.index.navigateTo({
        url: "/pages/building/form"
      });
    };
    const goDetail = (item) => {
      common_vendor.index.setStorageSync("currentBuilding", item);
      common_vendor.index.navigateTo({
        url: `/pages/building/detail?id=${item.buildingId}`
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
        common_vendor.index.__f__("error", "at pages/building/index.vue:112", "获取当前公司失败:", e);
      }
      return null;
    };
    const loadBuildingList = async () => {
      try {
        if (!currentCompanyId.value) {
          const companyId = await loadCurrentCompany();
          if (!companyId) {
            common_vendor.index.showToast({ title: "请先选择公司", icon: "none" });
            return;
          }
        }
        const res = await api_index.api.getBuildingList({
          companyId: currentCompanyId.value,
          buildingName: "",
          pageNum: 1,
          pageSize: 50
        });
        if (res.code === 200 || res.code === 0) {
          const data = res.rows || res.data || [];
          buildingList.value = Array.isArray(data) ? data : data.rows || data.list || [];
        } else {
          buildingList.value = [];
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/building/index.vue:144", "获取建筑列表失败:", e);
        common_vendor.index.showToast({ title: "获取建筑列表失败", icon: "none" });
        buildingList.value = [];
      }
    };
    const handleRefresh = () => {
      loadBuildingList();
    };
    common_vendor.onMounted(async () => {
      await loadCurrentCompany();
      loadBuildingList();
      common_vendor.index.$on("refreshBuildingList", handleRefresh);
    });
    common_vendor.onUnmounted(() => {
      common_vendor.index.$off("refreshBuildingList", handleRefresh);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "建筑列表",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.f(buildingList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.buildingName),
            b: common_vendor.t(item.buildingHeight),
            c: common_vendor.t(item.floors || item.floorCount),
            d: common_vendor.t(item.aboveGroundFloors),
            e: common_vendor.t(item.undergroundFloors),
            f: common_vendor.t(item.landArea),
            g: common_vendor.t(item.area),
            h: item.buildingId,
            i: common_vendor.o(($event) => goDetail(item), item.buildingId)
          };
        }),
        d: buildingList.value.length === 0
      }, buildingList.value.length === 0 ? {} : {}, {
        e: common_vendor.o(goAdd)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-6705acfc"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/building/index.js.map
