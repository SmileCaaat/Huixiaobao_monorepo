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
  __name: "list",
  setup(__props) {
    const typeName = common_vendor.ref("设备列表");
    const typeCode = common_vendor.ref("");
    const equipmentList = common_vendor.ref([]);
    const searchKeyword = common_vendor.ref("");
    const inspectionStatus = common_vendor.ref("");
    const dateStatus = common_vendor.ref("");
    const filteredList = common_vendor.computed(() => {
      let list = equipmentList.value;
      if (searchKeyword.value.trim()) {
        const keyword = searchKeyword.value.trim().toLowerCase();
        list = list.filter((item) => {
          return item.equipmentName && item.equipmentName.toLowerCase().includes(keyword) || item.equipmentCode && item.equipmentCode.toLowerCase().includes(keyword) || item.buildingName && item.buildingName.toLowerCase().includes(keyword) || item.location && item.location.toLowerCase().includes(keyword);
        });
      }
      if (inspectionStatus.value === "0") {
        list = list.filter((item) => !item.lastInspectionTime);
      } else if (inspectionStatus.value === "1") {
        list = list.filter((item) => item.lastInspectionTime);
      }
      if (dateStatus.value === "0") {
        list = list.filter((item) => {
          if (!item.expiryDate)
            return true;
          return new Date(item.expiryDate) >= /* @__PURE__ */ new Date();
        });
      } else if (dateStatus.value === "1") {
        list = list.filter((item) => {
          if (!item.expiryDate)
            return false;
          return new Date(item.expiryDate) < /* @__PURE__ */ new Date();
        });
      }
      return list;
    });
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goDetail = (item) => {
      common_vendor.index.setStorageSync("currentEquipment", item);
      common_vendor.index.navigateTo({
        url: `/pages/equipment/detail?id=${item.equipmentId}`
      });
    };
    const handleSearch = () => {
    };
    const setInspectionStatus = (status) => {
      if (inspectionStatus.value === status) {
        inspectionStatus.value = "";
      } else {
        inspectionStatus.value = status;
      }
    };
    const setDateStatus = (status) => {
      if (dateStatus.value === status) {
        dateStatus.value = "";
      } else {
        dateStatus.value = status;
      }
    };
    const formatDate = (dateStr) => {
      if (!dateStr)
        return "";
      return dateStr.substring(0, 16).replace("T", " ");
    };
    const getInspectionClass = (item) => {
      return item.lastInspectionTime ? "text-normal" : "text-warning";
    };
    const getCurrentCompanyId = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          return res.data.companyId;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/equipment/list.vue:275", "获取当前公司失败:", e);
      }
      return null;
    };
    const loadEquipmentList = async () => {
      try {
        const companyId = await getCurrentCompanyId();
        if (!companyId)
          return;
        const res = await api_index.api.getEquipmentList({
          companyId,
          equipmentType: "",
          // 获取全部，然后在前端根据 typeCode 过滤，保证与 index.vue 逻辑一致
          pageNum: 1,
          pageSize: 500
        });
        if (res.code === 200 || res.code === 0) {
          const data = res.rows || res.data || [];
          if (typeCode.value) {
            equipmentList.value = data.filter(
              (item) => item.systemName === typeCode.value || item.equipmentType === typeCode.value || item.systemType === typeCode.value
            );
          } else {
            equipmentList.value = data;
          }
          common_vendor.index.setStorageSync("currentTypeEquipments", equipmentList.value);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/equipment/list.vue:310", "加载设备列表失败", e);
      }
    };
    const refreshList = () => {
      loadEquipmentList();
    };
    common_vendor.onLoad((options) => {
      if (options.typeName) {
        typeName.value = decodeURIComponent(options.typeName);
      }
      if (options.type) {
        typeCode.value = decodeURIComponent(options.type);
      }
      const items = common_vendor.index.getStorageSync("currentTypeEquipments");
      if (items && Array.isArray(items)) {
        equipmentList.value = items;
      }
      common_vendor.index.$on("refreshEquipmentList", refreshList);
    });
    common_vendor.onMounted(() => {
    });
    common_vendor.onShow(() => {
      loadEquipmentList();
    });
    common_vendor.onUnmounted(() => {
      common_vendor.index.$off("refreshEquipmentList", refreshList);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: typeName.value,
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.o([($event) => searchKeyword.value = $event.detail.value, handleSearch]),
        d: searchKeyword.value,
        e: inspectionStatus.value === "0"
      }, inspectionStatus.value === "0" ? {} : {}, {
        f: inspectionStatus.value === "0" ? 1 : "",
        g: inspectionStatus.value === "0" ? 1 : "",
        h: common_vendor.o(($event) => setInspectionStatus("0")),
        i: inspectionStatus.value === "1"
      }, inspectionStatus.value === "1" ? {} : {}, {
        j: inspectionStatus.value === "1" ? 1 : "",
        k: inspectionStatus.value === "1" ? 1 : "",
        l: common_vendor.o(($event) => setInspectionStatus("1")),
        m: dateStatus.value === "0" ? 1 : "",
        n: dateStatus.value === "0" ? 1 : "",
        o: common_vendor.o(($event) => setDateStatus("0")),
        p: dateStatus.value === "1" ? 1 : "",
        q: dateStatus.value === "1" ? 1 : "",
        r: common_vendor.o(($event) => setDateStatus("1")),
        s: common_vendor.f(filteredList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.equipmentName),
            b: common_vendor.t(formatDate(item.createTime)),
            c: common_vendor.t(item.equipmentCode || "-"),
            d: common_vendor.t(item.quantity || 1),
            e: common_vendor.t(item.buildingName || "-"),
            f: common_vendor.t(item.floorNo || item.floor || "-"),
            g: common_vendor.t(item.location || "-"),
            h: common_vendor.t(item.lastInspectionTime || "未巡查"),
            i: common_vendor.n(getInspectionClass(item)),
            j: item.equipmentId,
            k: common_vendor.o(($event) => goDetail(item), item.equipmentId)
          };
        }),
        t: filteredList.value.length === 0
      }, filteredList.value.length === 0 ? {} : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-71d9c6ae"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/equipment/list.js.map
