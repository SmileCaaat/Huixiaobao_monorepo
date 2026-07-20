"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
const utils_request = require("../../utils/request.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  const _easycom_uni_forms_item2 = common_vendor.resolveComponent("uni-forms-item");
  const _easycom_uni_number_box2 = common_vendor.resolveComponent("uni-number-box");
  const _easycom_uni_easyinput2 = common_vendor.resolveComponent("uni-easyinput");
  const _easycom_uni_datetime_picker2 = common_vendor.resolveComponent("uni-datetime-picker");
  const _easycom_uni_forms2 = common_vendor.resolveComponent("uni-forms");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  (_easycom_uni_nav_bar2 + _easycom_uni_forms_item2 + _easycom_uni_number_box2 + _easycom_uni_easyinput2 + _easycom_uni_datetime_picker2 + _easycom_uni_forms2 + _easycom_uni_icons2)();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
const _easycom_uni_forms_item = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-forms-item/uni-forms-item.js";
const _easycom_uni_number_box = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-number-box/uni-number-box.js";
const _easycom_uni_easyinput = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-easyinput/uni-easyinput.js";
const _easycom_uni_datetime_picker = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-datetime-picker/uni-datetime-picker.js";
const _easycom_uni_forms = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-forms/uni-forms.js";
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  (_easycom_uni_nav_bar + _easycom_uni_forms_item + _easycom_uni_number_box + _easycom_uni_easyinput + _easycom_uni_datetime_picker + _easycom_uni_forms + _easycom_uni_icons)();
}
const _sfc_main = {
  __name: "form",
  setup(__props) {
    const formRef = common_vendor.ref(null);
    const formData = common_vendor.ref({
      companyId: null,
      companyName: "",
      inspectionType: "1",
      // 默认巡查
      buildingId: null,
      buildingName: "",
      floor: "",
      systemType: "",
      systemName: "",
      equipmentName: "",
      equipmentCount: 1,
      location: "",
      inspectionTime: "",
      equipmentStatus: "0",
      // 默认正常
      remark: ""
    });
    const rules = {
      location: { rules: [{ required: true, errorMessage: "请输入具体位置" }] }
    };
    const typeOptions = [
      { label: "测试", value: "0" },
      { label: "巡查", value: "1" },
      { label: "保养", value: "2" }
    ];
    const systemList = common_vendor.ref([
      { label: "消火栓系统", value: "01" },
      { label: "自动喷水灭火系统", value: "02" },
      { label: "火灾自动报警系统", value: "03" },
      { label: "消防供水设施", value: "04" },
      { label: "防排烟系统", value: "05" },
      { label: "应急照明系统", value: "06" },
      { label: "灭火器", value: "07" }
    ]);
    const equipmentList = common_vendor.ref([
      "消火栓",
      "消防水泵及控制柜",
      "喷淋头",
      "烟感探测器",
      "手动报警按钮",
      "消防水箱",
      "排烟风机",
      "应急灯",
      "灭火器"
    ]);
    const buildingList = common_vendor.ref([]);
    const floorList = common_vendor.ref([]);
    const imageList = common_vendor.ref([]);
    const showBuildingPicker = common_vendor.ref(false);
    const showFloorPicker = common_vendor.ref(false);
    const showSystemPicker = common_vendor.ref(false);
    const showEquipmentPicker = common_vendor.ref(false);
    const buildingIndex = common_vendor.ref(0);
    const floorIndex = common_vendor.ref(0);
    const systemIndex = common_vendor.ref(0);
    const equipmentIndex = common_vendor.ref(0);
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const getCurrentTime = () => {
      const now = /* @__PURE__ */ new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, "0");
      const day = String(now.getDate()).padStart(2, "0");
      const hour = String(now.getHours()).padStart(2, "0");
      const minute = String(now.getMinutes()).padStart(2, "0");
      const second = String(now.getSeconds()).padStart(2, "0");
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
    };
    const onTypeChange = (e) => {
      formData.value.inspectionType = e.detail.value;
    };
    const onStatusChange = (e) => {
      formData.value.equipmentStatus = e.detail.value ? "0" : "1";
    };
    const getCurrentCompany = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          return res.data;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/inspection/form.vue:404", "获取当前公司失败:", e);
      }
      return null;
    };
    const loadBuildings = async () => {
      try {
        if (!formData.value.companyId) {
          const company = await getCurrentCompany();
          if (company) {
            formData.value.companyId = company.companyId;
            formData.value.companyName = company.companyName || "当前公司";
          }
        }
        if (!formData.value.companyId)
          return;
        const res = await api_index.api.getBuildingList({
          companyId: formData.value.companyId,
          pageNum: 1,
          pageSize: 100
        });
        if (res.code === 200 || res.code === 0) {
          buildingList.value = res.rows || res.data || [];
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/inspection/form.vue:432", "获取建筑列表失败", e);
      }
    };
    const generateFloors = (aboveGround = 10, underground = 2) => {
      const floors = [];
      for (let i = underground; i >= 1; i--) {
        floors.push(`B${i}`);
      }
      for (let i = 1; i <= aboveGround; i++) {
        floors.push(`${i}F`);
      }
      floorList.value = floors;
    };
    const onBuildingChange = (e) => {
      buildingIndex.value = e.detail.value[0];
    };
    const confirmBuilding = () => {
      if (buildingList.value.length > 0) {
        const selected = buildingList.value[buildingIndex.value];
        formData.value.buildingId = selected.buildingId;
        formData.value.buildingName = selected.buildingName;
        generateFloors(
          selected.aboveGroundFloors || 10,
          selected.undergroundFloors || 2
        );
      }
      showBuildingPicker.value = false;
    };
    const onFloorChange = (e) => {
      floorIndex.value = e.detail.value[0];
    };
    const confirmFloor = () => {
      if (floorList.value.length > 0) {
        formData.value.floor = floorList.value[floorIndex.value];
      }
      showFloorPicker.value = false;
    };
    const onSystemChange = (e) => {
      systemIndex.value = e.detail.value[0];
    };
    const confirmSystem = () => {
      const selected = systemList.value[systemIndex.value];
      formData.value.systemType = selected.value;
      formData.value.systemName = selected.label;
      showSystemPicker.value = false;
    };
    const onEquipmentChange = (e) => {
      equipmentIndex.value = e.detail.value[0];
    };
    const confirmEquipment = () => {
      formData.value.equipmentName = equipmentList.value[equipmentIndex.value];
      showEquipmentPicker.value = false;
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
            common_vendor.index.__f__("log", "at pages/inspection/form.vue:532", "上传响应:", uploadRes.data);
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
            common_vendor.index.__f__("error", "at pages/inspection/form.vue:544", "解析上传响应失败", e);
            imageList.value.splice(currentIndex, 1);
            common_vendor.index.showToast({ title: "上传失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/inspection/form.vue:550", "上传图片失败", err);
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
    const handleSave = async () => {
      try {
        await formRef.value.validate();
      } catch (e) {
        return;
      }
      const pendingImages = imageList.value.filter((img) => img.uploading);
      if (pendingImages.length > 0) {
        return common_vendor.index.showToast({ title: "图片上传中，请稍候", icon: "none" });
      }
      try {
        common_vendor.index.showLoading({ title: "保存中...", mask: true });
        const payload = {
          companyId: formData.value.companyId,
          inspectionType: formData.value.inspectionType,
          buildingId: formData.value.buildingId,
          floor: formData.value.floor,
          systemType: formData.value.systemType,
          systemName: formData.value.systemName,
          equipmentName: formData.value.equipmentName,
          equipmentCount: formData.value.equipmentCount,
          location: formData.value.location,
          inspectionTime: formData.value.inspectionTime,
          equipmentStatus: formData.value.equipmentStatus,
          imageUrls: imageList.value.map((img) => img.serverUrl).join(","),
          remark: formData.value.remark
        };
        const res = await api_index.api.addInspection(payload);
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({ title: "保存成功", icon: "success" });
          setTimeout(() => {
            common_vendor.index.$emit("refreshInspectionList");
            common_vendor.index.navigateBack();
          }, 1500);
        } else {
          common_vendor.index.showToast({ title: res.msg || "保存失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/inspection/form.vue:614", "保存失败", e);
        common_vendor.index.showToast({ title: "保存失败", icon: "none" });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    common_vendor.onMounted(async () => {
      formData.value.inspectionTime = getCurrentTime();
      try {
        const company = await getCurrentCompany();
        if (company) {
          formData.value.companyId = company.companyId;
          formData.value.companyName = company.companyName || "当前公司";
        } else {
          formData.value.companyName = "未选择公司";
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/inspection/form.vue:635", "获取公司信息失败", e);
        formData.value.companyName = "未选择公司";
      }
      loadBuildings();
      generateFloors();
    });
    return (_ctx, _cache) => {
      var _a, _b;
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "巡检登记",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.t(formData.value.companyName || "未选择公司"),
        d: common_vendor.p({
          label: "单位名称",
          name: "companyName"
        }),
        e: common_vendor.f(typeOptions, (item, k0, i0) => {
          return {
            a: item.value,
            b: formData.value.inspectionType === item.value,
            c: common_vendor.t(item.label),
            d: item.value
          };
        }),
        f: common_vendor.o(onTypeChange),
        g: common_vendor.p({
          label: "维保类型",
          name: "inspectionType"
        }),
        h: !formData.value.buildingName
      }, !formData.value.buildingName ? {} : {
        i: common_vendor.t(formData.value.buildingName)
      }, {
        j: common_vendor.o(($event) => showBuildingPicker.value = true),
        k: common_vendor.p({
          label: "建筑名称",
          name: "buildingId"
        }),
        l: !formData.value.floor
      }, !formData.value.floor ? {} : {
        m: common_vendor.t(formData.value.floor)
      }, {
        n: common_vendor.o(($event) => showFloorPicker.value = true),
        o: common_vendor.p({
          label: "所在楼层",
          name: "floor"
        }),
        p: !formData.value.systemName
      }, !formData.value.systemName ? {} : {
        q: common_vendor.t(formData.value.systemName)
      }, {
        r: common_vendor.o(($event) => showSystemPicker.value = true),
        s: common_vendor.p({
          label: "系统名称",
          name: "systemType"
        }),
        t: !formData.value.equipmentName
      }, !formData.value.equipmentName ? {} : {
        v: common_vendor.t(formData.value.equipmentName)
      }, {
        w: common_vendor.o(($event) => showEquipmentPicker.value = true),
        x: common_vendor.p({
          label: "设备名称",
          name: "equipmentName"
        }),
        y: common_vendor.o(($event) => formData.value.equipmentCount = $event),
        z: common_vendor.p({
          min: 1,
          max: 999,
          color: "#e53935",
          modelValue: formData.value.equipmentCount
        }),
        A: common_vendor.p({
          label: "设备数量",
          name: "equipmentCount"
        }),
        B: common_vendor.o(($event) => formData.value.location = $event),
        C: common_vendor.p({
          placeholder: "必填",
          maxlength: "200",
          modelValue: formData.value.location
        }),
        D: common_vendor.t(((_a = formData.value.location) == null ? void 0 : _a.length) || 0),
        E: common_vendor.p({
          label: "具体位置",
          name: "location",
          required: true
        }),
        F: common_vendor.o(($event) => formData.value.inspectionTime = $event),
        G: common_vendor.p({
          type: "datetime",
          ["clear-icon"]: false,
          modelValue: formData.value.inspectionTime
        }),
        H: common_vendor.p({
          label: "巡检时间",
          name: "inspectionTime"
        }),
        I: common_vendor.t(formData.value.equipmentStatus === "0" ? "正常" : "异常"),
        J: formData.value.equipmentStatus === "0",
        K: common_vendor.o(onStatusChange),
        L: common_vendor.p({
          label: "设备状态",
          name: "equipmentStatus"
        }),
        M: formData.value.remark,
        N: common_vendor.o(($event) => formData.value.remark = $event.detail.value),
        O: common_vendor.t(((_b = formData.value.remark) == null ? void 0 : _b.length) || 0),
        P: common_vendor.p({
          label: "详细备注",
          name: "remark"
        }),
        Q: common_vendor.sr(formRef, "b69fb204-1", {
          "k": "formRef"
        }),
        R: common_vendor.p({
          model: formData.value,
          rules,
          ["label-width"]: "160rpx"
        }),
        S: common_vendor.f(imageList.value, (img, idx, i0) => {
          return {
            a: img.tempPath,
            b: common_vendor.o(($event) => previewImage(idx), idx),
            c: common_vendor.o(($event) => removeImage(idx), idx),
            d: idx
          };
        }),
        T: imageList.value.length < 4
      }, imageList.value.length < 4 ? {
        U: common_vendor.p({
          type: "camera",
          size: "32",
          color: "#999"
        }),
        V: common_vendor.o(chooseImage)
      } : {}, {
        W: common_vendor.o(handleSave),
        X: showBuildingPicker.value
      }, showBuildingPicker.value ? {
        Y: common_vendor.o(($event) => showBuildingPicker.value = false),
        Z: common_vendor.o(confirmBuilding),
        aa: common_vendor.f(buildingList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.buildingName),
            b: item.buildingId
          };
        }),
        ab: [buildingIndex.value],
        ac: common_vendor.o(onBuildingChange),
        ad: common_vendor.o(() => {
        }),
        ae: common_vendor.o(($event) => showBuildingPicker.value = false)
      } : {}, {
        af: showFloorPicker.value
      }, showFloorPicker.value ? {
        ag: common_vendor.o(($event) => showFloorPicker.value = false),
        ah: common_vendor.o(confirmFloor),
        ai: common_vendor.f(floorList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item),
            b: item
          };
        }),
        aj: [floorIndex.value],
        ak: common_vendor.o(onFloorChange),
        al: common_vendor.o(() => {
        }),
        am: common_vendor.o(($event) => showFloorPicker.value = false)
      } : {}, {
        an: showSystemPicker.value
      }, showSystemPicker.value ? {
        ao: common_vendor.o(($event) => showSystemPicker.value = false),
        ap: common_vendor.o(confirmSystem),
        aq: common_vendor.f(systemList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.label),
            b: item.value
          };
        }),
        ar: [systemIndex.value],
        as: common_vendor.o(onSystemChange),
        at: common_vendor.o(() => {
        }),
        av: common_vendor.o(($event) => showSystemPicker.value = false)
      } : {}, {
        aw: showEquipmentPicker.value
      }, showEquipmentPicker.value ? {
        ax: common_vendor.o(($event) => showEquipmentPicker.value = false),
        ay: common_vendor.o(confirmEquipment),
        az: common_vendor.f(equipmentList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item),
            b: item
          };
        }),
        aA: [equipmentIndex.value],
        aB: common_vendor.o(onEquipmentChange),
        aC: common_vendor.o(() => {
        }),
        aD: common_vendor.o(($event) => showEquipmentPicker.value = false)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-b69fb204"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/inspection/form.js.map
