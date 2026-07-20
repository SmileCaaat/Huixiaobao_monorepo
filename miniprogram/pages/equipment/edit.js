"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
const utils_request = require("../../utils/request.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  const _easycom_uni_easyinput2 = common_vendor.resolveComponent("uni-easyinput");
  const _easycom_uni_forms_item2 = common_vendor.resolveComponent("uni-forms-item");
  const _easycom_uni_datetime_picker2 = common_vendor.resolveComponent("uni-datetime-picker");
  const _easycom_uni_number_box2 = common_vendor.resolveComponent("uni-number-box");
  const _easycom_uni_forms2 = common_vendor.resolveComponent("uni-forms");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  (_easycom_uni_nav_bar2 + _easycom_uni_easyinput2 + _easycom_uni_forms_item2 + _easycom_uni_datetime_picker2 + _easycom_uni_number_box2 + _easycom_uni_forms2 + _easycom_uni_icons2)();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
const _easycom_uni_easyinput = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-easyinput/uni-easyinput.js";
const _easycom_uni_forms_item = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-forms-item/uni-forms-item.js";
const _easycom_uni_datetime_picker = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-datetime-picker/uni-datetime-picker.js";
const _easycom_uni_number_box = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-number-box/uni-number-box.js";
const _easycom_uni_forms = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-forms/uni-forms.js";
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
if (!Math) {
  (_easycom_uni_nav_bar + _easycom_uni_easyinput + _easycom_uni_forms_item + _easycom_uni_datetime_picker + _easycom_uni_number_box + _easycom_uni_forms + _easycom_uni_icons)();
}
const _sfc_main = {
  __name: "edit",
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
    const formRef = common_vendor.ref(null);
    const equipmentId = common_vendor.ref(null);
    const formData = common_vendor.ref({
      equipmentCode: "",
      buildingId: "",
      buildingName: "",
      floor: "",
      equipmentType: "",
      equipmentName: "",
      brand: "",
      warrantyEndDate: "",
      quantity: 1,
      location: "",
      specifications: "",
      remark: "",
      imageUrls: ""
    });
    const imageList = common_vendor.ref([]);
    const rules = {
      equipmentCode: {
        rules: [{ required: true, errorMessage: "请输入设备编号" }]
      },
      location: { rules: [{ required: true, errorMessage: "请输入具体位置" }] }
    };
    const buildingList = common_vendor.ref([]);
    const floorList = common_vendor.ref([]);
    const systemList = common_vendor.ref([
      "消火栓系统",
      "自动喷水灭火系统",
      "火灾自动报警系统",
      "防排烟系统",
      "应急照明系统",
      "灭火器"
    ]);
    const showBuildingPicker = common_vendor.ref(false);
    const showFloorPicker = common_vendor.ref(false);
    const showSystemPicker = common_vendor.ref(false);
    const buildingIndex = common_vendor.ref(0);
    const floorIndex = common_vendor.ref(0);
    const systemIndex = common_vendor.ref(0);
    const goBack = () => {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定放弃编辑吗？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.navigateBack();
          }
        }
      });
    };
    const loadData = () => {
      const cached = common_vendor.index.getStorageSync("currentEquipment");
      if (cached) {
        formData.value = {
          equipmentCode: cached.equipmentCode || "",
          buildingId: cached.buildingId || "",
          buildingName: cached.buildingName || "",
          floor: cached.floor || "",
          equipmentType: cached.equipmentType || cached.systemName || "",
          equipmentName: cached.equipmentName || "",
          brand: cached.brand || cached.manufacturer || "",
          warrantyEndDate: cached.warrantyEndDate || cached.expiryDate || "",
          quantity: cached.quantity || 1,
          location: cached.location || "",
          specifications: cached.specifications || cached.specification || "",
          remark: cached.remark || "",
          imageUrls: cached.imageUrls || ""
        };
        if (cached.imageUrls) {
          const urls = cached.imageUrls.split(",").filter((url) => url);
          imageList.value = urls.map((url) => ({
            tempPath: url,
            serverUrl: url,
            uploading: false
          }));
        }
      }
    };
    const getCurrentCompanyId = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          return res.data.companyId;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/equipment/edit.vue:436", "获取当前公司失败:", e);
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
        common_vendor.index.__f__("error", "at pages/equipment/edit.vue:457", "获取建筑列表失败", e);
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
      formData.value.equipmentType = systemList.value[systemIndex.value];
      showSystemPicker.value = false;
    };
    const onSystemPresetChange = (e) => {
      const index = e.detail.value;
      formData.value.equipmentType = systemNamePresets[index];
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
          equipmentId: equipmentId.value,
          companyId,
          equipmentCode: formData.value.equipmentCode,
          buildingId: formData.value.buildingId,
          floor: formData.value.floor,
          equipmentType: formData.value.equipmentType,
          systemName: formData.value.equipmentType,
          // 增加系统名称字段
          equipmentName: formData.value.equipmentName,
          brand: formData.value.brand,
          manufacturer: formData.value.brand,
          // 增加生产厂家字段
          warrantyEndDate: formData.value.warrantyEndDate,
          expiryDate: formData.value.warrantyEndDate,
          // 增加有效日期字段
          expireDate: formData.value.warrantyEndDate,
          // 增加有效日期字段 (常用别名)
          quantity: Number(formData.value.quantity) || 1,
          location: formData.value.location,
          specifications: formData.value.specifications,
          specification: formData.value.specifications,
          // 增加规格型号字段
          model: formData.value.specifications,
          // 增加规格型号字段
          remark: formData.value.remark,
          imageUrls: imageList.value.map((img) => img.serverUrl).join(",")
        };
        const res = await api_index.api.editEquipment(payload);
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
        common_vendor.index.__f__("error", "at pages/equipment/edit.vue:646", "保存失败", e);
        common_vendor.index.showToast({ title: "保存失败", icon: "none" });
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
        equipmentId.value = id;
      }
      loadData();
      loadBuildings();
      generateFloors();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "编辑设备",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.o(($event) => formData.value.equipmentCode = $event),
        d: common_vendor.p({
          placeholder: "请输入设备编号",
          modelValue: formData.value.equipmentCode
        }),
        e: common_vendor.p({
          label: "设备编号",
          name: "equipmentCode",
          required: true
        }),
        f: !formData.value.buildingName
      }, !formData.value.buildingName ? {} : {
        g: common_vendor.t(formData.value.buildingName)
      }, {
        h: common_vendor.o(($event) => showBuildingPicker.value = true),
        i: common_vendor.p({
          label: "所在建筑",
          name: "buildingId"
        }),
        j: !formData.value.floor
      }, !formData.value.floor ? {} : {
        k: common_vendor.t(formData.value.floor)
      }, {
        l: common_vendor.o(($event) => showFloorPicker.value = true),
        m: common_vendor.p({
          label: "所在楼层",
          name: "floor"
        }),
        n: common_vendor.o(($event) => formData.value.equipmentType = $event),
        o: common_vendor.p({
          placeholder: "请输入或从右侧列表选择",
          modelValue: formData.value.equipmentType
        }),
        p: systemNamePresets,
        q: common_vendor.o(onSystemPresetChange),
        r: common_vendor.p({
          label: "系统名称",
          name: "equipmentType"
        }),
        s: common_vendor.o(($event) => formData.value.equipmentName = $event),
        t: common_vendor.p({
          placeholder: "请输入设备名称",
          modelValue: formData.value.equipmentName
        }),
        v: common_vendor.p({
          label: "设备名称",
          name: "equipmentName"
        }),
        w: common_vendor.o(($event) => formData.value.brand = $event),
        x: common_vendor.p({
          placeholder: "请输入生产厂家",
          modelValue: formData.value.brand
        }),
        y: common_vendor.p({
          label: "生产厂家",
          name: "brand"
        }),
        z: common_vendor.o(($event) => formData.value.warrantyEndDate = $event),
        A: common_vendor.p({
          type: "date",
          ["clear-icon"]: false,
          modelValue: formData.value.warrantyEndDate
        }),
        B: common_vendor.p({
          label: "有效日期",
          name: "warrantyEndDate"
        }),
        C: common_vendor.o(($event) => formData.value.quantity = $event),
        D: common_vendor.p({
          min: 1,
          max: 999,
          color: "#e53935",
          modelValue: formData.value.quantity
        }),
        E: common_vendor.p({
          label: "设备数量",
          name: "quantity"
        }),
        F: common_vendor.o(($event) => formData.value.location = $event),
        G: common_vendor.p({
          placeholder: "请输入具体位置",
          modelValue: formData.value.location
        }),
        H: common_vendor.p({
          label: "具体位置",
          name: "location",
          required: true
        }),
        I: common_vendor.o(($event) => formData.value.specifications = $event),
        J: common_vendor.p({
          placeholder: "选填",
          modelValue: formData.value.specifications
        }),
        K: common_vendor.p({
          label: "规格型号",
          name: "specifications"
        }),
        L: formData.value.remark,
        M: common_vendor.o(($event) => formData.value.remark = $event.detail.value),
        N: common_vendor.p({
          label: "备注",
          name: "remark"
        }),
        O: common_vendor.sr(formRef, "0233bbef-1", {
          "k": "formRef"
        }),
        P: common_vendor.p({
          model: formData.value,
          rules,
          ["label-width"]: "160rpx",
          ["label-align"]: "left"
        }),
        Q: common_vendor.f(imageList.value, (img, idx, i0) => {
          return {
            a: img.tempPath,
            b: common_vendor.o(($event) => previewImage(idx), idx),
            c: common_vendor.o(($event) => removeImage(idx), idx),
            d: idx
          };
        }),
        R: imageList.value.length < 4
      }, imageList.value.length < 4 ? {
        S: common_vendor.p({
          type: "camera",
          size: "32",
          color: "#999"
        }),
        T: common_vendor.o(chooseImage)
      } : {}, {
        U: common_vendor.o(handleSave),
        V: showBuildingPicker.value
      }, showBuildingPicker.value ? {
        W: common_vendor.o(($event) => showBuildingPicker.value = false),
        X: common_vendor.o(confirmBuilding),
        Y: common_vendor.f(buildingList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.buildingName),
            b: item.buildingId
          };
        }),
        Z: [buildingIndex.value],
        aa: common_vendor.o(onBuildingChange),
        ab: common_vendor.o(() => {
        }),
        ac: common_vendor.o(($event) => showBuildingPicker.value = false)
      } : {}, {
        ad: showFloorPicker.value
      }, showFloorPicker.value ? {
        ae: common_vendor.o(($event) => showFloorPicker.value = false),
        af: common_vendor.o(confirmFloor),
        ag: common_vendor.f(floorList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item),
            b: item
          };
        }),
        ah: [floorIndex.value],
        ai: common_vendor.o(onFloorChange),
        aj: common_vendor.o(() => {
        }),
        ak: common_vendor.o(($event) => showFloorPicker.value = false)
      } : {}, {
        al: showSystemPicker.value
      }, showSystemPicker.value ? {
        am: common_vendor.o(($event) => showSystemPicker.value = false),
        an: common_vendor.o(confirmSystem),
        ao: common_vendor.f(systemList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item),
            b: item
          };
        }),
        ap: [systemIndex.value],
        aq: common_vendor.o(onSystemChange),
        ar: common_vendor.o(() => {
        }),
        as: common_vendor.o(($event) => showSystemPicker.value = false)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-0233bbef"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/equipment/edit.js.map
