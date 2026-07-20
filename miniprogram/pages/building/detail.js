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
  __name: "detail",
  setup(__props) {
    const isEditing = common_vendor.ref(false);
    const buildingData = common_vendor.ref({
      buildingId: null,
      buildingCode: "",
      buildingName: "",
      address: "",
      buildingTypeText: "",
      area: "",
      buildingHeight: "",
      floors: "",
      floorCount: "",
      undergroundFloors: "",
      structureType: "",
      fireResistanceLevel: "",
      fireZoneCount: "",
      evacuationExitCount: "",
      emergencyExits: "",
      safetyExits: "",
      completionDate: "",
      remark: "",
      companyId: null
    });
    const goBack = () => {
      if (isEditing.value) {
        isEditing.value = false;
      } else {
        common_vendor.index.navigateBack();
      }
    };
    const toggleEdit = () => {
      isEditing.value = true;
    };
    const handleSave = async () => {
      try {
        common_vendor.index.showLoading({ title: "正在保存..." });
        let companyId = buildingData.value.companyId;
        if (!companyId) {
          try {
            const companyRes = await api_index.api.getCurrentCompany();
            if ((companyRes.code === 200 || companyRes.code === 0) && companyRes.data) {
              companyId = companyRes.data.companyId;
            }
          } catch (e) {
            common_vendor.index.__f__("error", "at pages/building/detail.vue:300", "获取当前公司失败:", e);
          }
        }
        const payload = {
          ...buildingData.value,
          buildingId: buildingData.value.buildingId,
          companyId,
          floors: Number(
            buildingData.value.floors || buildingData.value.floorCount || 0
          ),
          undergroundFloors: Number(buildingData.value.undergroundFloors || 0),
          area: Number(buildingData.value.area || 0),
          buildingHeight: Number(buildingData.value.buildingHeight || 0),
          fireZoneCount: Number(buildingData.value.fireZoneCount || 0),
          evacuationExitCount: Number(buildingData.value.evacuationExitCount || 0)
        };
        const res = await api_index.api.updateBuilding(payload);
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({ title: "保存成功", icon: "success" });
          common_vendor.index.$emit("refreshBuildingList");
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        } else {
          common_vendor.index.showToast({ title: res.msg || "保存失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/building/detail.vue:332", "保存失败:", e);
        common_vendor.index.showToast({ title: "网络请求失败", icon: "none" });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    const loadBuildingDetail = async (id) => {
      try {
        common_vendor.index.showLoading({ title: "获取详情..." });
        const res = await api_index.api.getBuildingDetail(id);
        if ((res.code === 200 || res.code === 0) && res.data) {
          buildingData.value = res.data;
        } else {
          const building = common_vendor.index.getStorageSync("currentBuilding");
          if (building && (building.buildingId == id || building.id == id)) {
            buildingData.value = building;
          } else {
            common_vendor.index.showToast({ title: "获取详情失败", icon: "none" });
          }
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/building/detail.vue:356", "获取建筑详情失败:", e);
        const building = common_vendor.index.getStorageSync("currentBuilding");
        if (building) {
          buildingData.value = building;
        } else {
          common_vendor.index.showToast({ title: "获取详情出错", icon: "none" });
        }
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    common_vendor.onMounted(() => {
      var _a;
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const id = (_a = currentPage.options) == null ? void 0 : _a.id;
      if (id) {
        loadBuildingDetail(id);
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: isEditing.value ? "编辑建筑" : "建筑信息",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: isEditing.value
      }, isEditing.value ? {
        d: buildingData.value.buildingCode,
        e: common_vendor.o(($event) => buildingData.value.buildingCode = $event.detail.value)
      } : {
        f: common_vendor.t(buildingData.value.buildingCode)
      }, {
        g: isEditing.value
      }, isEditing.value ? {
        h: buildingData.value.buildingName,
        i: common_vendor.o(($event) => buildingData.value.buildingName = $event.detail.value)
      } : {
        j: common_vendor.t(buildingData.value.buildingName)
      }, {
        k: isEditing.value
      }, isEditing.value ? {
        l: buildingData.value.address,
        m: common_vendor.o(($event) => buildingData.value.address = $event.detail.value)
      } : {
        n: common_vendor.t(buildingData.value.address)
      }, {
        o: isEditing.value
      }, isEditing.value ? {
        p: buildingData.value.buildingTypeText,
        q: common_vendor.o(($event) => buildingData.value.buildingTypeText = $event.detail.value)
      } : {
        r: common_vendor.t(buildingData.value.buildingTypeText)
      }, {
        s: isEditing.value
      }, isEditing.value ? {
        t: buildingData.value.area,
        v: common_vendor.o(($event) => buildingData.value.area = $event.detail.value)
      } : {
        w: common_vendor.t(buildingData.value.area)
      }, {
        x: isEditing.value
      }, isEditing.value ? {
        y: buildingData.value.buildingHeight,
        z: common_vendor.o(($event) => buildingData.value.buildingHeight = $event.detail.value)
      } : {
        A: common_vendor.t(buildingData.value.buildingHeight)
      }, {
        B: isEditing.value
      }, isEditing.value ? {
        C: buildingData.value.floors,
        D: common_vendor.o(($event) => buildingData.value.floors = $event.detail.value)
      } : {
        E: common_vendor.t(buildingData.value.floors || buildingData.value.floorCount)
      }, {
        F: isEditing.value
      }, isEditing.value ? {
        G: buildingData.value.undergroundFloors,
        H: common_vendor.o(($event) => buildingData.value.undergroundFloors = $event.detail.value)
      } : {
        I: common_vendor.t(buildingData.value.undergroundFloors)
      }, {
        J: isEditing.value
      }, isEditing.value ? {
        K: buildingData.value.structureType,
        L: common_vendor.o(($event) => buildingData.value.structureType = $event.detail.value)
      } : {
        M: common_vendor.t(buildingData.value.structureType)
      }, {
        N: isEditing.value
      }, isEditing.value ? {
        O: buildingData.value.fireResistanceLevel,
        P: common_vendor.o(($event) => buildingData.value.fireResistanceLevel = $event.detail.value)
      } : {
        Q: common_vendor.t(buildingData.value.fireResistanceLevel)
      }, {
        R: isEditing.value
      }, isEditing.value ? {
        S: buildingData.value.fireZoneCount,
        T: common_vendor.o(($event) => buildingData.value.fireZoneCount = $event.detail.value)
      } : {
        U: common_vendor.t(buildingData.value.fireZoneCount)
      }, {
        V: isEditing.value
      }, isEditing.value ? {
        W: buildingData.value.evacuationExitCount,
        X: common_vendor.o(($event) => buildingData.value.evacuationExitCount = $event.detail.value)
      } : {
        Y: common_vendor.t(buildingData.value.evacuationExitCount || buildingData.value.emergencyExits || buildingData.value.safetyExits)
      }, {
        Z: isEditing.value
      }, isEditing.value ? {
        aa: common_vendor.t(buildingData.value.completionDate || "请选择"),
        ab: common_vendor.o((e) => buildingData.value.completionDate = e.detail.value)
      } : {
        ac: common_vendor.t(buildingData.value.completionDate)
      }, {
        ad: isEditing.value
      }, isEditing.value ? {
        ae: buildingData.value.remark,
        af: common_vendor.o(($event) => buildingData.value.remark = $event.detail.value)
      } : {
        ag: common_vendor.t(buildingData.value.remark || "-")
      }, {
        ah: isEditing.value
      }, isEditing.value ? {
        ai: common_vendor.o(handleSave)
      } : {
        aj: common_vendor.o(toggleEdit)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-ff35c7d6"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/building/detail.js.map
