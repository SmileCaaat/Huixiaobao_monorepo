"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
if (!Math) {
  TabBar();
}
const TabBar = () => "../../components/TabBar.js";
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const extractEquipmentCode = (scanResult) => {
      if (scanResult.startsWith("http://") || scanResult.startsWith("https://")) {
        const parts = scanResult.split("/");
        return parts[parts.length - 1];
      }
      return scanResult;
    };
    const handleScan = () => {
      common_vendor.index.scanCode({
        success: async (res) => {
          try {
            const equipmentCode = extractEquipmentCode(res.result);
            common_vendor.index.__f__("log", "at pages/scan/index.vue:41", "扫码结果:", res.result, "提取的设备编码:", equipmentCode);
            common_vendor.index.showLoading({ title: "查询中...", mask: true });
            const scanRes = await api_index.api.scanEquipment(equipmentCode);
            common_vendor.index.hideLoading();
            if ((scanRes.code === 200 || scanRes.code === 0) && scanRes.data) {
              const data = scanRes.data;
              const equipmentData = {
                equipmentCode: data.equipmentCode || "",
                equipmentName: data.equipmentName || "",
                buildingId: data.buildingId || "",
                buildingName: data.buildingName || "",
                floor: data.floorNo || "",
                equipmentType: data.systemName || data.equipmentType || "",
                brand: data.manufacturer || "",
                warrantyEndDate: data.expireDate || "",
                quantity: data.quantity || 1,
                location: data.location || "",
                specifications: data.model || ""
              };
              common_vendor.index.setStorageSync("scanEquipmentData", equipmentData);
              common_vendor.index.showToast({ title: "扫码成功", icon: "success" });
              setTimeout(() => {
                common_vendor.index.navigateTo({
                  url: "/pages/equipment/form?fromScan=true"
                });
              }, 1e3);
            } else {
              common_vendor.index.showToast({
                title: scanRes.msg || "未找到设备信息",
                icon: "none"
              });
            }
          } catch (e) {
            common_vendor.index.hideLoading();
            common_vendor.index.__f__("error", "at pages/scan/index.vue:85", "扫码失败:", e);
            common_vendor.index.showToast({ title: "扫码失败", icon: "none" });
          }
        },
        fail: () => {
          common_vendor.index.showToast({ title: "扫码取消", icon: "none" });
        }
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(handleScan),
        b: common_vendor.p({
          currentTab: 2
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-99526857"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/scan/index.js.map
