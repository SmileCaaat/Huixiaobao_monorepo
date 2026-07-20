"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
const utils_request = require("../../utils/request.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  const _easycom_uni_easyinput2 = common_vendor.resolveComponent("uni-easyinput");
  const _easycom_uni_forms_item2 = common_vendor.resolveComponent("uni-forms-item");
  const _easycom_uni_forms2 = common_vendor.resolveComponent("uni-forms");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  (_easycom_uni_nav_bar2 + _easycom_uni_easyinput2 + _easycom_uni_forms_item2 + _easycom_uni_forms2 + _easycom_uni_icons2)();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
const _easycom_uni_easyinput = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-easyinput/uni-easyinput.js";
const _easycom_uni_forms_item = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-forms-item/uni-forms-item.js";
const _easycom_uni_forms = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-forms/uni-forms.js";
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  (_easycom_uni_nav_bar + _easycom_uni_easyinput + _easycom_uni_forms_item + _easycom_uni_forms + _easycom_uni_icons)();
}
const defaultAddress = "广东省阳江市阳春市迎宾";
const _sfc_main = {
  __name: "form",
  setup(__props) {
    const formRef = common_vendor.ref(null);
    const isEdit = common_vendor.ref(false);
    const showTypePicker = common_vendor.ref(false);
    const typeIndex = common_vendor.ref(0);
    const imageList = common_vendor.ref([]);
    const buildingTypes = [
      "单、多层民用建筑",
      "高层民用建筑",
      "工业建筑",
      "其他建筑"
    ];
    const formData = common_vendor.ref({
      buildingId: null,
      buildingCode: "",
      buildingName: "",
      buildingType: "",
      floors: "",
      undergroundFloors: "",
      area: "",
      buildingHeight: "",
      structureType: "",
      fireResistanceLevel: "",
      fireZoneCount: "",
      evacuationExitCount: "",
      evacuationStairCount: "",
      fireElevatorCount: "",
      refugeFloorLocation: "",
      completionDate: "",
      address: "",
      longitude: "",
      latitude: "",
      remark: "",
      hasFireFacilities: false,
      landArea: "",
      aboveGroundFloors: ""
    });
    const rules = {
      buildingName: {
        rules: [{ required: true, errorMessage: "请输入建筑名称" }]
      },
      buildingHeight: {
        rules: [{ required: true, errorMessage: "请输入建筑高度" }]
      },
      floors: {
        rules: [{ required: true, errorMessage: "请输入建筑层数" }]
      },
      aboveGroundFloors: {
        rules: [{ required: true, errorMessage: "请输入地上层数" }]
      },
      undergroundFloors: {
        rules: [{ required: true, errorMessage: "请输入地下层数" }]
      }
    };
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const onFireFacilitiesChange = (e) => {
      formData.value.hasFireFacilities = e.detail.value === "true";
    };
    const onTypeChange = (e) => {
      typeIndex.value = e.detail.value[0];
    };
    const confirmType = () => {
      formData.value.buildingType = buildingTypes[typeIndex.value];
      showTypePicker.value = false;
    };
    const chooseImage = () => {
      common_vendor.index.chooseImage({
        count: 4 - imageList.value.length,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          res.tempFilePaths.forEach((path) => {
            uploadImg(path);
          });
        }
      });
    };
    const uploadImg = (tempPath) => {
      const imgObj = { tempPath, serverUrl: "", uploading: true };
      imageList.value.push(imgObj);
      const currentIndex = imageList.value.length - 1;
      const token = common_vendor.index.getStorageSync("token");
      common_vendor.index.uploadFile({
        url: utils_request.BASE_URL + "/api/common/upload",
        filePath: tempPath,
        name: "file",
        formData: {},
        header: {
          Authorization: token ? `Bearer ${token}` : ""
        },
        success: (uploadRes) => {
          try {
            const res = JSON.parse(uploadRes.data);
            const url = res.url || res.fileName || res.filePath || res.data && res.data.url;
            if (url) {
              imageList.value[currentIndex].serverUrl = url;
              imageList.value[currentIndex].uploading = false;
            } else {
              imageList.value.splice(currentIndex, 1);
              common_vendor.index.showToast({ title: res.msg || "上传失败", icon: "none" });
            }
          } catch (e) {
            imageList.value.splice(currentIndex, 1);
            common_vendor.index.showToast({ title: "上传失败", icon: "none" });
          }
        },
        fail: () => {
          imageList.value.splice(currentIndex, 1);
          common_vendor.index.showToast({ title: "上传失败", icon: "none" });
        }
      });
    };
    const removeImage = (index) => {
      imageList.value.splice(index, 1);
    };
    const previewImage = (index) => {
      common_vendor.index.previewImage({
        urls: imageList.value.map((i) => i.tempPath),
        current: index
      });
    };
    const validateForm = async () => {
      try {
        await formRef.value.validate();
        return true;
      } catch (e) {
        return false;
      }
    };
    const getCurrentCompanyId = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          return res.data.companyId;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/building/form.vue:395", "获取当前公司失败:", e);
      }
      return null;
    };
    const handleSave = async () => {
      if (!await validateForm())
        return;
      const pendingImages = imageList.value.filter((img) => img.uploading);
      if (pendingImages.length > 0) {
        return common_vendor.index.showToast({ title: "图片上传中，请稍候", icon: "none" });
      }
      try {
        common_vendor.index.showLoading({ title: "正在保存..." });
        const companyId = await getCurrentCompanyId();
        if (!companyId) {
          common_vendor.index.showToast({ title: "未获取到公司信息", icon: "none" });
          return;
        }
        const payload = {
          ...formData.value,
          companyId: Number(companyId),
          floors: Number(formData.value.floors),
          undergroundFloors: Number(formData.value.undergroundFloors || 0),
          aboveGroundFloors: Number(formData.value.aboveGroundFloors || 0),
          area: Number(formData.value.area || 0),
          landArea: Number(formData.value.landArea || 0),
          buildingHeight: Number(formData.value.buildingHeight),
          fireZoneCount: Number(formData.value.fireZoneCount || 0),
          evacuationExitCount: Number(formData.value.evacuationExitCount || 0),
          evacuationStairCount: Number(formData.value.evacuationStairCount || 0),
          fireElevatorCount: Number(formData.value.fireElevatorCount || 0),
          imageUrls: imageList.value.map((img) => img.serverUrl).join(","),
          longitude: formData.value.longitude ? Number(formData.value.longitude) : null,
          latitude: formData.value.latitude ? Number(formData.value.latitude) : null
        };
        let res;
        if (isEdit.value) {
          res = await api_index.api.updateBuilding(payload);
        } else {
          res = await api_index.api.addBuilding(payload);
        }
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({
            title: isEdit.value ? "编辑成功" : "添加成功",
            icon: "success"
          });
          common_vendor.index.$emit("refreshBuildingList");
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        } else {
          common_vendor.index.showToast({ title: res.msg || "保存失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/building/form.vue:466", "保存失败:", e);
        common_vendor.index.showToast({ title: "网络请求失败", icon: "none" });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    common_vendor.onMounted(async () => {
      const building = common_vendor.index.getStorageSync("editBuilding");
      if (building) {
        isEdit.value = true;
        formData.value = {
          ...building,
          buildingName: building.buildingName || building.name,
          buildingHeight: building.buildingHeight || building.height,
          floors: building.floors || building.floorCount,
          area: building.area || building.buildingArea,
          autoFireSystem: building.autoFireSystem || (building.hasFireFacilities ? "1" : "0")
        };
        common_vendor.index.removeStorageSync("editBuilding");
        const idx = buildingTypes.indexOf(building.buildingType);
        if (idx >= 0) {
          typeIndex.value = idx;
        }
      } else {
        try {
          const res = await api_index.api.getCurrentCompany();
          if ((res.code === 200 || res.code === 0) && res.data && res.data.address) {
            formData.value.address = res.data.address;
          }
        } catch (e) {
          common_vendor.index.__f__("error", "at pages/building/form.vue:508", "获取公司地址失败:", e);
        }
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: isEdit.value ? "编辑建筑" : "建筑登记",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.o(($event) => formData.value.buildingName = $event),
        d: common_vendor.p({
          placeholder: "必填",
          modelValue: formData.value.buildingName
        }),
        e: common_vendor.p({
          label: "建筑名称",
          name: "buildingName",
          required: true
        }),
        f: formData.value.hasFireFacilities === true,
        g: formData.value.hasFireFacilities === false,
        h: common_vendor.o(onFireFacilitiesChange),
        i: common_vendor.p({
          label: "自动消防设施",
          name: "hasFireFacilities"
        }),
        j: common_vendor.o(($event) => formData.value.address = $event),
        k: common_vendor.p({
          placeholder: defaultAddress,
          modelValue: formData.value.address
        }),
        l: common_vendor.p({
          label: "建筑地址",
          name: "address"
        }),
        m: !formData.value.buildingType
      }, !formData.value.buildingType ? {} : {
        n: common_vendor.t(formData.value.buildingType)
      }, {
        o: common_vendor.o(($event) => showTypePicker.value = true),
        p: common_vendor.p({
          label: "建筑类别",
          name: "buildingType"
        }),
        q: common_vendor.o(($event) => formData.value.landArea = $event),
        r: common_vendor.p({
          type: "digit",
          placeholder: "选填",
          modelValue: formData.value.landArea
        }),
        s: common_vendor.p({
          label: "占地面积(m²)",
          name: "landArea"
        }),
        t: common_vendor.o(($event) => formData.value.area = $event),
        v: common_vendor.p({
          type: "digit",
          placeholder: "选填",
          modelValue: formData.value.area
        }),
        w: common_vendor.p({
          label: "建筑面积(m²)",
          name: "area"
        }),
        x: common_vendor.o(($event) => formData.value.buildingHeight = $event),
        y: common_vendor.p({
          type: "digit",
          placeholder: "必填",
          modelValue: formData.value.buildingHeight
        }),
        z: common_vendor.p({
          label: "建筑高度(m)",
          name: "buildingHeight",
          required: true
        }),
        A: common_vendor.o(($event) => formData.value.floors = $event),
        B: common_vendor.p({
          type: "number",
          placeholder: "必填",
          modelValue: formData.value.floors
        }),
        C: common_vendor.p({
          label: "建筑层数",
          name: "floors",
          required: true
        }),
        D: common_vendor.o(($event) => formData.value.aboveGroundFloors = $event),
        E: common_vendor.p({
          type: "number",
          placeholder: "必填",
          modelValue: formData.value.aboveGroundFloors
        }),
        F: common_vendor.p({
          label: "地上层数",
          name: "aboveGroundFloors",
          required: true
        }),
        G: common_vendor.o(($event) => formData.value.undergroundFloors = $event),
        H: common_vendor.p({
          type: "number",
          placeholder: "必填",
          modelValue: formData.value.undergroundFloors
        }),
        I: common_vendor.p({
          label: "地下层数",
          name: "undergroundFloors",
          required: true
        }),
        J: common_vendor.o(($event) => formData.value.evacuationExitCount = $event),
        K: common_vendor.p({
          type: "number",
          placeholder: "选填",
          modelValue: formData.value.evacuationExitCount
        }),
        L: common_vendor.p({
          label: "安全出口(个)",
          name: "evacuationExitCount"
        }),
        M: common_vendor.o(($event) => formData.value.evacuationStairCount = $event),
        N: common_vendor.p({
          type: "number",
          placeholder: "选填",
          modelValue: formData.value.evacuationStairCount
        }),
        O: common_vendor.p({
          label: "疏散楼梯数(个)",
          name: "evacuationStairCount"
        }),
        P: common_vendor.o(($event) => formData.value.fireElevatorCount = $event),
        Q: common_vendor.p({
          type: "number",
          placeholder: "选填",
          modelValue: formData.value.fireElevatorCount
        }),
        R: common_vendor.p({
          label: "消防电梯数(个)",
          name: "fireElevatorCount"
        }),
        S: common_vendor.o(($event) => formData.value.refugeFloorLocation = $event),
        T: common_vendor.p({
          placeholder: "选填",
          modelValue: formData.value.refugeFloorLocation
        }),
        U: common_vendor.p({
          label: "避难层位置",
          name: "refugeFloorLocation"
        }),
        V: common_vendor.sr(formRef, "c7bcce41-1", {
          "k": "formRef"
        }),
        W: common_vendor.p({
          model: formData.value,
          rules,
          ["label-width"]: "200rpx"
        }),
        X: common_vendor.f(imageList.value, (img, idx, i0) => {
          return {
            a: img.tempPath,
            b: common_vendor.o(($event) => previewImage(idx), idx),
            c: common_vendor.o(($event) => removeImage(idx), idx),
            d: idx
          };
        }),
        Y: imageList.value.length < 4
      }, imageList.value.length < 4 ? {
        Z: common_vendor.p({
          type: "camera",
          size: "32",
          color: "#999"
        }),
        aa: common_vendor.o(chooseImage)
      } : {}, {
        ab: common_vendor.o(handleSave),
        ac: showTypePicker.value
      }, showTypePicker.value ? {
        ad: common_vendor.o(($event) => showTypePicker.value = false),
        ae: common_vendor.o(confirmType),
        af: common_vendor.f(buildingTypes, (item, index, i0) => {
          return {
            a: common_vendor.t(item),
            b: index
          };
        }),
        ag: [typeIndex.value],
        ah: common_vendor.o(onTypeChange),
        ai: common_vendor.o(() => {
        }),
        aj: common_vendor.o(($event) => showTypePicker.value = false)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-c7bcce41"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/building/form.js.map
