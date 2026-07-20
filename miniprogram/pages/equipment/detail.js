"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
const utils_request = require("../../utils/request.js");
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
    const getFullImageUrl = (url) => {
      if (!url)
        return "";
      if (url.startsWith("http://") || url.startsWith("https://")) {
        return url;
      }
      return utils_request.BASE_URL + url;
    };
    const equipmentId = common_vendor.ref(null);
    const loading = common_vendor.ref(false);
    const formData = common_vendor.ref({
      equipmentId: null,
      companyId: null,
      buildingId: null,
      buildingName: "",
      floor: "",
      systemType: "",
      systemName: "",
      equipmentName: "",
      equipmentCode: "",
      manufacturer: "",
      expiryDate: "",
      quantity: 1,
      location: "",
      specification: "",
      imageUrls: ""
    });
    const imageList = common_vendor.ref([]);
    const previewImage = (index) => {
      common_vendor.index.previewImage({
        urls: imageList.value,
        current: index
      });
    };
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const loadDetail = async () => {
      if (!equipmentId.value)
        return;
      try {
        loading.value = true;
        common_vendor.index.showLoading({ title: "加载中...", mask: true });
        const res = await api_index.api.getEquipmentDetail(equipmentId.value);
        if (res.code === 200 || res.code === 0) {
          const data = res.data || res;
          formData.value = {
            equipmentId: data.equipmentId,
            companyId: data.companyId,
            buildingId: data.buildingId,
            buildingName: data.buildingName || "",
            floor: data.floorNo || data.floor || "",
            // API 返回 floorNo
            equipmentType: data.systemName || data.equipmentType || "",
            // API 返回 systemName
            systemName: data.systemName || data.equipmentTypeName || "",
            equipmentName: data.equipmentName || "",
            equipmentCode: data.equipmentCode || data.equipmentId || "",
            manufacturer: data.manufacturer || data.producer || "",
            expiryDate: data.expireDate || data.expiryDate || data.validDate || "",
            // API 返回 expireDate
            quantity: data.quantity || data.equipmentCount || 1,
            location: data.location || "",
            specification: data.model || data.specification || "",
            // API 返回 model
            imageUrls: data.image || ""
          };
          if (data.image) {
            imageList.value = data.image.split(",").filter((url) => url).map((url) => getFullImageUrl(url.trim()));
          }
          common_vendor.index.setStorageSync("currentEquipment", formData.value);
        } else {
          const cached = common_vendor.index.getStorageSync("currentEquipment");
          if (cached) {
            formData.value = cached;
          }
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/equipment/detail.vue:204", "获取详情失败", e);
        const cached = common_vendor.index.getStorageSync("currentEquipment");
        if (cached) {
          formData.value = cached;
        }
      } finally {
        loading.value = false;
        common_vendor.index.hideLoading();
      }
    };
    const goEdit = () => {
      common_vendor.index.navigateTo({
        url: `/pages/equipment/edit?id=${formData.value.equipmentId}`
      });
    };
    common_vendor.onMounted(() => {
      var _a;
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const id = (_a = currentPage.options) == null ? void 0 : _a.id;
      if (id) {
        equipmentId.value = id;
        loadDetail();
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "设备信息",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.t(formData.value.buildingName || "-"),
        d: common_vendor.t(formData.value.floor || "-"),
        e: common_vendor.t(formData.value.equipmentType || "-"),
        f: common_vendor.t(formData.value.equipmentName || "-"),
        g: common_vendor.t(formData.value.manufacturer || "-"),
        h: common_vendor.t(formData.value.expiryDate || "-"),
        i: common_vendor.t(formData.value.quantity || "-"),
        j: common_vendor.t(formData.value.location || "-"),
        k: common_vendor.t(formData.value.specification || "-"),
        l: common_vendor.t(formData.value.equipmentCode || "-"),
        m: imageList.value.length === 0 ? 1 : "",
        n: imageList.value.length > 0
      }, imageList.value.length > 0 ? {
        o: common_vendor.f(imageList.value, (img, idx, i0) => {
          return {
            a: idx,
            b: img,
            c: common_vendor.o(($event) => previewImage(idx), idx)
          };
        })
      } : {}, {
        p: common_vendor.o(goEdit)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-49d12978"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/equipment/detail.js.map
