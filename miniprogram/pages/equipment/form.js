"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
const utils_request = require("../../utils/request.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  const _easycom_uni_forms_item2 = common_vendor.resolveComponent("uni-forms-item");
  const _easycom_uni_easyinput2 = common_vendor.resolveComponent("uni-easyinput");
  const _easycom_uni_datetime_picker2 = common_vendor.resolveComponent("uni-datetime-picker");
  const _easycom_uni_number_box2 = common_vendor.resolveComponent("uni-number-box");
  const _easycom_uni_forms2 = common_vendor.resolveComponent("uni-forms");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  const _easycom_uni_popup2 = common_vendor.resolveComponent("uni-popup");
  (_easycom_uni_nav_bar2 + _easycom_uni_forms_item2 + _easycom_uni_easyinput2 + _easycom_uni_datetime_picker2 + _easycom_uni_number_box2 + _easycom_uni_forms2 + _easycom_uni_icons2 + _easycom_uni_popup2)();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
const _easycom_uni_forms_item = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-forms-item/uni-forms-item.js";
const _easycom_uni_easyinput = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-easyinput/uni-easyinput.js";
const _easycom_uni_datetime_picker = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-datetime-picker/uni-datetime-picker.js";
const _easycom_uni_number_box = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-number-box/uni-number-box.js";
const _easycom_uni_forms = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-forms/uni-forms.js";
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
const _easycom_uni_popup = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-popup/uni-popup.js";
if (!Math) {
  (_easycom_uni_nav_bar + _easycom_uni_forms_item + _easycom_uni_easyinput + _easycom_uni_datetime_picker + _easycom_uni_number_box + _easycom_uni_forms + _easycom_uni_icons + _easycom_uni_popup)();
}
const _sfc_main = {
  __name: "form",
  setup(__props) {
    const systemNamePresets = [
      "火灾报警控制器",
      "烟感探测器",
      "温感探测器",
      "手动火灾报警按钮",
      "消火栓按钮",
      "声光警报器",
      "消防应急广播",
      "消防电话",
      "输入模块",
      "输出模块",
      "输入输出模块",
      "火灾显示盘",
      "可燃气体探测器",
      "喷淋头",
      "湿式报警阀",
      "干式报警阀",
      "预作用报警阀",
      "雨淋报警阀",
      "水流指示器",
      "信号阀",
      "末端试水装置",
      "喷淋泵",
      "稳压泵",
      "气压罐",
      "室内消火栓箱",
      "消火栓栓头",
      "消防水带",
      "消防水枪",
      "消火栓泵",
      "消防水箱",
      "室外消火栓",
      "水泵接合器",
      "七氟丙烷灭火系统",
      "IG541 混合气体灭火系统",
      "二氧化碳灭火系统",
      "气溶胶灭火系统",
      "气瓶",
      "瓶头阀",
      "选择阀",
      "喷嘴",
      "气体灭火控制器",
      "紧急启停按钮",
      "放气指示灯",
      "泡沫液储罐",
      "泡沫比例混合器",
      "泡沫产生器",
      "泡沫炮",
      "泡沫消火栓",
      "排烟风机",
      "正压送风机",
      "排烟口",
      "送风口",
      "排烟防火阀",
      "防火阀",
      "补风系统",
      "防火门",
      "防火卷帘",
      "防火窗",
      "防火封堵材料",
      "应急照明灯",
      "疏散指示标志灯",
      "应急照明控制器",
      "集中电源",
      "分配电装置",
      "消防水池",
      "消防水箱",
      "消防水泵",
      "稳压设备",
      "水泵接合器",
      "消防水管网",
      "细水雾灭火系统",
      "干粉灭火系统",
      "固定消防炮灭火系统",
      "厨房设备自动灭火装置",
      "电动汽车充电桩灭火系统",
      "隧道消防系统",
      "大空间智能灭火系统",
      "其他"
    ];
    const activeTab = common_vendor.ref("input");
    const formRef = common_vendor.ref(null);
    const isEdit = common_vendor.ref(false);
    const equipmentId = common_vendor.ref(null);
    const formData = common_vendor.ref({
      equipmentCode: "",
      // 设备编号（后台自动生成，但在前端这里可能保留用于显示或编辑）
      buildingId: "",
      buildingName: "",
      floorNo: "",
      // 所在楼层
      systemName: "",
      // 系统名称
      projectCategory: "",
      // 项目类别
      equipmentName: "",
      manufacturer: "",
      // 生产厂家
      expireDate: "",
      // 有效日期
      quantity: 1,
      location: "",
      model: ""
      // 规格型号
    });
    const imageList = common_vendor.ref([]);
    const rules = {
      location: { rules: [{ required: true, errorMessage: "请输入具体位置" }] }
    };
    const buildingList = common_vendor.ref([]);
    const projectCategoryList = common_vendor.ref([]);
    const showBuildingPicker = common_vendor.ref(false);
    const showProjectCategoryPicker = common_vendor.ref(false);
    const buildingIndex = common_vendor.ref(0);
    const projectCategoryIndex = common_vendor.ref(0);
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const getCurrentCompanyId = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          return res.data.companyId;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/equipment/form.vue:395", "获取当前公司失败:", e);
      }
      return null;
    };
    const loadBuildings = async () => {
      try {
        const companyId = await getCurrentCompanyId();
        if (!companyId)
          return;
        const res = await api_index.api.getBuildingList({
          companyId,
          pageNum: 1,
          pageSize: 100
        });
        if (res.code === 200 || res.code === 0) {
          buildingList.value = res.rows || res.data || [];
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/equipment/form.vue:416", "获取建筑列表失败", e);
      }
    };
    const loadProjectCategories = async () => {
      try {
        const res = await api_index.api.getSystemTypes();
        if (res.code === 200 || res.code === 0) {
          projectCategoryList.value = res.data || [];
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/equipment/form.vue:429", "获取项目类别失败", e);
      }
    };
    const onBuildingChange = (e) => {
      buildingIndex.value = e.detail.value[0];
    };
    const confirmBuilding = () => {
      if (buildingList.value.length > 0) {
        const selected = buildingList.value[buildingIndex.value];
        formData.value.buildingId = selected.buildingId;
        formData.value.buildingName = selected.buildingName;
      }
      showBuildingPicker.value = false;
    };
    const onProjectCategoryChange = (e) => {
      projectCategoryIndex.value = e.detail.value[0];
    };
    const confirmProjectCategory = () => {
      if (projectCategoryList.value.length > 0) {
        const selected = projectCategoryList.value[projectCategoryIndex.value];
        formData.value.projectCategory = selected.label;
      }
      showProjectCategoryPicker.value = false;
    };
    const onSystemPresetChange = (e) => {
      const index = e.detail.value;
      formData.value.systemName = systemNamePresets[index];
    };
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
            common_vendor.index.__f__("log", "at pages/equipment/form.vue:483", "扫码结果:", res.result, "提取的设备编码:", equipmentCode);
            const scanRes = await api_index.api.scanEquipment(equipmentCode);
            if ((scanRes.code === 200 || scanRes.code === 0) && scanRes.data) {
              const data = scanRes.data;
              formData.value.equipmentCode = data.equipmentCode || "";
              formData.value.equipmentName = data.equipmentName || "";
              formData.value.buildingId = data.buildingId || "";
              formData.value.buildingName = data.buildingName || "";
              formData.value.floorNo = data.floorNo || "";
              formData.value.systemName = data.systemName || "";
              formData.value.projectCategory = data.projectCategory || "";
              formData.value.manufacturer = data.manufacturer || "";
              formData.value.expireDate = data.expireDate || "";
              formData.value.quantity = data.quantity || 1;
              formData.value.location = data.location || "";
              formData.value.model = data.model || "";
              activeTab.value = "input";
              common_vendor.index.showToast({ title: "扫码成功", icon: "success" });
            } else {
              common_vendor.index.showToast({
                title: scanRes.msg || "未找到设备信息",
                icon: "none"
              });
            }
          } catch (e) {
            common_vendor.index.__f__("error", "at pages/equipment/form.vue:510", "扫码失败:", e);
            common_vendor.index.showToast({ title: "扫码失败", icon: "none" });
          }
        },
        fail: () => {
          common_vendor.index.showToast({ title: "扫码取消", icon: "none" });
        }
      });
    };
    const handleScanTab = () => {
      activeTab.value = "scan";
      handleScan();
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
    const handleSave = async () => {
      try {
        await formRef.value.validate();
      } catch (e) {
        return;
      }
      try {
        common_vendor.index.showLoading({ title: "保存中...", mask: true });
        const companyId = await getCurrentCompanyId();
        const payload = {
          ...formData.value,
          equipmentType: formData.value.systemName,
          // 增加设备类型字段 (别名)
          manufacturer: formData.value.manufacturer || formData.value.brand,
          brand: formData.value.manufacturer || formData.value.brand,
          expireDate: formData.value.expireDate || formData.value.warrantyEndDate,
          expiryDate: formData.value.expireDate || formData.value.warrantyEndDate,
          warrantyEndDate: formData.value.expireDate || formData.value.warrantyEndDate,
          specifications: formData.value.model || formData.value.specifications,
          specification: formData.value.model || formData.value.specifications,
          model: formData.value.model || formData.value.specifications,
          floor: formData.value.floorNo || formData.value.floor,
          companyId,
          image: imageList.value.map((img) => img.serverUrl).join(",")
        };
        let res;
        if (isEdit.value && equipmentId.value) {
          payload.equipmentId = equipmentId.value;
          res = await api_index.api.editEquipment(payload);
        } else {
          res = await api_index.api.addEquipment(payload);
        }
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({ title: "保存成功", icon: "success" });
          setTimeout(() => {
            common_vendor.index.$emit("refreshEquipmentList");
            const pages = getCurrentPages();
            let delta = 0;
            for (let i = pages.length - 1; i >= 0; i--) {
              const route = pages[i].route;
              if (route.includes("pages/equipment/index")) {
                delta = pages.length - 1 - i;
                break;
              }
            }
            if (delta > 0) {
              common_vendor.index.navigateBack({ delta });
            } else {
              common_vendor.index.navigateBack();
            }
          }, 1500);
        } else {
          common_vendor.index.showToast({ title: res.msg || "保存失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/equipment/form.vue:655", "保存失败", e);
        common_vendor.index.showToast({ title: "保存失败", icon: "none" });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    common_vendor.onMounted(() => {
      loadBuildings();
      loadProjectCategories();
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const options = currentPage.options || {};
      if (options.id) {
        isEdit.value = true;
        equipmentId.value = options.id;
        loadEquipmentDetail(options.id);
      } else if (options.fromScan === "true") {
        const scanData = common_vendor.index.getStorageSync("scanEquipmentData");
        if (scanData) {
          formData.value = {
            ...formData.value,
            ...scanData,
            // 确保字段一致性
            floorNo: scanData.floorNo || scanData.floor || "",
            systemName: scanData.systemName || scanData.equipmentType || "",
            manufacturer: scanData.manufacturer || scanData.brand || "",
            model: scanData.model || scanData.specifications || "",
            expireDate: scanData.expireDate || scanData.warrantyEndDate || ""
          };
          common_vendor.index.removeStorageSync("scanEquipmentData");
        }
      }
    });
    const loadEquipmentDetail = async (id) => {
      try {
        const res = await api_index.api.getEquipmentDetail(id);
        if (res.code === 200 && res.data) {
          const data = res.data;
          Object.assign(formData.value, data);
          if (data.image) {
            imageList.value = data.image.split(",").map((url) => ({
              tempPath: url,
              serverUrl: url,
              uploading: false
            }));
          }
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/equipment/form.vue:715", "获取设备详情失败", e);
      }
    };
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
        c: activeTab.value === "input" ? 1 : "",
        d: common_vendor.o(($event) => activeTab.value = "input"),
        e: activeTab.value === "scan" ? 1 : "",
        f: common_vendor.o(handleScanTab),
        g: activeTab.value === "input"
      }, activeTab.value === "input" ? common_vendor.e({
        h: !formData.value.buildingName
      }, !formData.value.buildingName ? {} : {
        i: common_vendor.t(formData.value.buildingName)
      }, {
        j: common_vendor.o(($event) => showBuildingPicker.value = true),
        k: common_vendor.p({
          label: "所在建筑",
          name: "buildingId"
        }),
        l: common_vendor.o(($event) => formData.value.floorNo = $event),
        m: common_vendor.p({
          placeholder: "请输入所在楼层",
          modelValue: formData.value.floorNo
        }),
        n: common_vendor.p({
          label: "所在楼层",
          name: "floorNo"
        }),
        o: common_vendor.o(($event) => formData.value.systemName = $event),
        p: common_vendor.p({
          placeholder: "请输入或从右侧列表选择",
          modelValue: formData.value.systemName
        }),
        q: systemNamePresets,
        r: common_vendor.o(onSystemPresetChange),
        s: common_vendor.p({
          label: "系统名称",
          name: "systemName",
          required: true
        }),
        t: !formData.value.projectCategory
      }, !formData.value.projectCategory ? {} : {
        v: common_vendor.t(formData.value.projectCategory)
      }, {
        w: common_vendor.o(($event) => showProjectCategoryPicker.value = true),
        x: common_vendor.p({
          label: "项目类别",
          name: "projectCategory"
        }),
        y: common_vendor.o(($event) => formData.value.equipmentName = $event),
        z: common_vendor.p({
          placeholder: "请输入设备名称",
          modelValue: formData.value.equipmentName
        }),
        A: common_vendor.p({
          label: "设备名称",
          name: "equipmentName",
          required: true
        }),
        B: common_vendor.o(($event) => formData.value.manufacturer = $event),
        C: common_vendor.p({
          placeholder: "请输入并选择生产厂家",
          modelValue: formData.value.manufacturer
        }),
        D: common_vendor.p({
          label: "生产厂家",
          name: "manufacturer"
        }),
        E: common_vendor.o(($event) => formData.value.expireDate = $event),
        F: common_vendor.p({
          type: "date",
          ["clear-icon"]: false,
          modelValue: formData.value.expireDate
        }),
        G: common_vendor.p({
          label: "有效日期",
          name: "expireDate"
        }),
        H: common_vendor.o(($event) => formData.value.quantity = $event),
        I: common_vendor.p({
          min: 1,
          max: 999,
          color: "#e53935",
          modelValue: formData.value.quantity
        }),
        J: common_vendor.p({
          label: "设备数量",
          name: "quantity"
        }),
        K: common_vendor.o(($event) => formData.value.location = $event),
        L: common_vendor.p({
          placeholder: "必填",
          modelValue: formData.value.location
        }),
        M: common_vendor.p({
          label: "具体位置",
          name: "location",
          required: true
        }),
        N: common_vendor.o(($event) => formData.value.model = $event),
        O: common_vendor.p({
          placeholder: "选填",
          modelValue: formData.value.model
        }),
        P: common_vendor.p({
          label: "规格型号",
          name: "model"
        }),
        Q: common_vendor.sr(formRef, "9daf322d-1", {
          "k": "formRef"
        }),
        R: common_vendor.p({
          model: formData.value,
          rules,
          ["label-width"]: "160rpx",
          ["label-align"]: "left"
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
        W: common_vendor.o(handleSave)
      }) : {}, {
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
        ad: common_vendor.sr("buildingPopup", "9daf322d-21"),
        ae: common_vendor.p({
          type: "bottom"
        })
      } : {}, {
        af: showBuildingPicker.value
      }, showBuildingPicker.value ? {
        ag: common_vendor.o(($event) => showBuildingPicker.value = false),
        ah: common_vendor.o(confirmBuilding),
        ai: common_vendor.f(buildingList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.buildingName),
            b: item.buildingId
          };
        }),
        aj: [buildingIndex.value],
        ak: common_vendor.o(onBuildingChange),
        al: common_vendor.o(() => {
        }),
        am: common_vendor.o(($event) => showBuildingPicker.value = false)
      } : {}, {
        an: showProjectCategoryPicker.value
      }, showProjectCategoryPicker.value ? {
        ao: common_vendor.o(($event) => showProjectCategoryPicker.value = false),
        ap: common_vendor.o(confirmProjectCategory),
        aq: common_vendor.f(projectCategoryList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.label),
            b: item.value
          };
        }),
        ar: [projectCategoryIndex.value],
        as: common_vendor.o(onProjectCategoryChange),
        at: common_vendor.o(() => {
        }),
        av: common_vendor.o(($event) => showProjectCategoryPicker.value = false)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-9daf322d"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/equipment/form.js.map
