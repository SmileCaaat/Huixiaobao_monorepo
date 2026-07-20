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
  __name: "detail",
  setup(__props) {
    const formRef = common_vendor.ref(null);
    const inspectionId = common_vendor.ref(null);
    const isEditing = common_vendor.ref(false);
    const loading = common_vendor.ref(false);
    const formData = common_vendor.ref({
      inspectionId: null,
      companyId: null,
      companyName: "",
      inspectionType: "1",
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
      remark: ""
    });
    const originalData = common_vendor.ref(null);
    const originalImages = common_vendor.ref([]);
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
      if (isEditing.value) {
        common_vendor.index.showModal({
          title: "提示",
          content: "确定放弃编辑吗？",
          success: (res) => {
            if (res.confirm) {
              common_vendor.index.navigateBack();
            }
          }
        });
      } else {
        common_vendor.index.navigateBack();
      }
    };
    const getTypeName = (type) => {
      const item = typeOptions.find((t) => t.value === type);
      return item ? item.label : "-";
    };
    const getImageUrl = (img) => {
      if (typeof img === "string") {
        if (img.startsWith("http")) {
          return img;
        }
        return utils_request.BASE_URL + img;
      }
      return img.tempPath || (img.serverUrl ? img.serverUrl.startsWith("http") ? img.serverUrl : utils_request.BASE_URL + img.serverUrl : "");
    };
    const loadDetail = async () => {
      if (!inspectionId.value)
        return;
      try {
        loading.value = true;
        common_vendor.index.showLoading({ title: "加载中...", mask: true });
        const res = await api_index.api.getInspectionDetail(inspectionId.value);
        if (res.code === 200 || res.code === 0) {
          const data = res.data || res;
          formData.value = {
            inspectionId: data.inspectionId,
            companyId: data.companyId,
            companyName: data.companyName || "",
            inspectionType: String(data.inspectionType || "1"),
            buildingId: data.buildingId,
            buildingName: data.buildingName || "",
            floor: data.floor || "",
            systemType: data.systemType || "",
            systemName: data.systemName || "",
            equipmentName: data.equipmentName || "",
            equipmentCount: data.equipmentCount || 1,
            location: data.location || "",
            inspectionTime: data.inspectionTime || data.inspectionDate || "",
            equipmentStatus: String(data.equipmentStatus || data.status || "0"),
            remark: data.remark || ""
          };
          let urls = [];
          common_vendor.index.__f__("log", "at pages/inspection/detail.vue:547", "=== 图片数据调试 ===");
          common_vendor.index.__f__("log", "at pages/inspection/detail.vue:548", "data.images:", data.images);
          common_vendor.index.__f__("log", "at pages/inspection/detail.vue:549", "data.imageUrls:", data.imageUrls);
          if (data.images && Array.isArray(data.images) && data.images.length > 0) {
            urls = data.images;
          } else if (data.imageUrls) {
            urls = data.imageUrls.split(",").filter((url) => url.trim());
          }
          common_vendor.index.__f__("log", "at pages/inspection/detail.vue:557", "处理后的urls:", urls);
          if (urls.length > 0) {
            imageList.value = urls.map((url) => ({
              tempPath: url.startsWith("http") ? url : utils_request.BASE_URL + url,
              serverUrl: url,
              uploading: false
            }));
            common_vendor.index.__f__("log", "at pages/inspection/detail.vue:565", "imageList:", imageList.value);
          }
          originalData.value = JSON.parse(JSON.stringify(formData.value));
          originalImages.value = JSON.parse(JSON.stringify(imageList.value));
        } else {
          common_vendor.index.showToast({ title: res.msg || "获取详情失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/inspection/detail.vue:575", "获取详情失败", e);
        common_vendor.index.showToast({ title: "获取详情失败", icon: "none" });
      } finally {
        loading.value = false;
        common_vendor.index.hideLoading();
      }
    };
    const getCurrentCompanyId = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          return res.data.companyId;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/inspection/detail.vue:591", "获取当前公司失败:", e);
      }
      return null;
    };
    const loadBuildings = async () => {
      try {
        const companyId = formData.value.companyId || await getCurrentCompanyId();
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
        common_vendor.index.__f__("error", "at pages/inspection/detail.vue:612", "获取建筑列表失败", e);
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
    const startEdit = () => {
      isEditing.value = true;
      loadBuildings();
      generateFloors();
    };
    const cancelEdit = () => {
      formData.value = JSON.parse(JSON.stringify(originalData.value));
      imageList.value = JSON.parse(JSON.stringify(originalImages.value));
      isEditing.value = false;
    };
    const onTypeChange = (e) => {
      formData.value.inspectionType = e.detail.value;
    };
    const onStatusChange = (e) => {
      formData.value.equipmentStatus = e.detail.value ? "0" : "1";
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
            common_vendor.index.__f__("error", "at pages/inspection/detail.vue:748", "解析上传响应失败", e);
            imageList.value.splice(currentIndex, 1);
            common_vendor.index.showToast({ title: "上传失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/inspection/detail.vue:754", "上传图片失败", err);
          imageList.value.splice(currentIndex, 1);
          common_vendor.index.showToast({ title: "上传失败", icon: "none" });
        }
      });
    };
    const removeImage = (index) => {
      imageList.value.splice(index, 1);
    };
    const previewImage = (index) => {
      const urls = imageList.value.map((img) => getImageUrl(img));
      common_vendor.index.previewImage({
        urls,
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
          inspectionId: formData.value.inspectionId,
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
          imageUrls: imageList.value.map((img) => img.serverUrl || img).join(","),
          remark: formData.value.remark
        };
        const res = await api_index.api.editInspection(payload);
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
        common_vendor.index.__f__("error", "at pages/inspection/detail.vue:820", "保存失败", e);
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
        inspectionId.value = id;
        loadDetail();
      }
    });
    return (_ctx, _cache) => {
      var _a, _b;
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: isEditing.value ? "编辑巡检" : "巡检详情",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.t(formData.value.companyName || "未选择公司"),
        d: common_vendor.p({
          label: "单位名称",
          name: "companyName"
        }),
        e: isEditing.value
      }, isEditing.value ? {
        f: common_vendor.f(typeOptions, (item, k0, i0) => {
          return {
            a: item.value,
            b: formData.value.inspectionType === item.value,
            c: common_vendor.t(item.label),
            d: item.value
          };
        }),
        g: common_vendor.o(onTypeChange)
      } : {
        h: common_vendor.t(getTypeName(formData.value.inspectionType))
      }, {
        i: common_vendor.p({
          label: "维保类型",
          name: "inspectionType"
        }),
        j: isEditing.value
      }, isEditing.value ? common_vendor.e({
        k: !formData.value.buildingName
      }, !formData.value.buildingName ? {} : {
        l: common_vendor.t(formData.value.buildingName)
      }, {
        m: common_vendor.o(($event) => showBuildingPicker.value = true)
      }) : {
        n: common_vendor.t(formData.value.buildingName || "-")
      }, {
        o: common_vendor.p({
          label: "建筑名称",
          name: "buildingId"
        }),
        p: isEditing.value
      }, isEditing.value ? common_vendor.e({
        q: !formData.value.floor
      }, !formData.value.floor ? {} : {
        r: common_vendor.t(formData.value.floor)
      }, {
        s: common_vendor.o(($event) => showFloorPicker.value = true)
      }) : {
        t: common_vendor.t(formData.value.floor || "-")
      }, {
        v: common_vendor.p({
          label: "所在楼层",
          name: "floor"
        }),
        w: isEditing.value
      }, isEditing.value ? common_vendor.e({
        x: !formData.value.systemName
      }, !formData.value.systemName ? {} : {
        y: common_vendor.t(formData.value.systemName)
      }, {
        z: common_vendor.o(($event) => showSystemPicker.value = true)
      }) : {
        A: common_vendor.t(formData.value.systemName || "-")
      }, {
        B: common_vendor.p({
          label: "系统名称",
          name: "systemType"
        }),
        C: isEditing.value
      }, isEditing.value ? common_vendor.e({
        D: !formData.value.equipmentName
      }, !formData.value.equipmentName ? {} : {
        E: common_vendor.t(formData.value.equipmentName)
      }, {
        F: common_vendor.o(($event) => showEquipmentPicker.value = true)
      }) : {
        G: common_vendor.t(formData.value.equipmentName || "-")
      }, {
        H: common_vendor.p({
          label: "设备名称",
          name: "equipmentName"
        }),
        I: isEditing.value
      }, isEditing.value ? {
        J: common_vendor.o(($event) => formData.value.equipmentCount = $event),
        K: common_vendor.p({
          min: 1,
          max: 999,
          color: "#e53935",
          modelValue: formData.value.equipmentCount
        })
      } : {
        L: common_vendor.t(formData.value.equipmentCount || 1)
      }, {
        M: common_vendor.p({
          label: "设备数量",
          name: "equipmentCount"
        }),
        N: isEditing.value
      }, isEditing.value ? {
        O: common_vendor.o(($event) => formData.value.location = $event),
        P: common_vendor.p({
          placeholder: "必填",
          maxlength: "200",
          modelValue: formData.value.location
        }),
        Q: common_vendor.t(((_a = formData.value.location) == null ? void 0 : _a.length) || 0)
      } : {
        R: common_vendor.t(formData.value.location || "-")
      }, {
        S: common_vendor.p({
          label: "具体位置",
          name: "location",
          required: isEditing.value
        }),
        T: isEditing.value
      }, isEditing.value ? {
        U: common_vendor.o(($event) => formData.value.inspectionTime = $event),
        V: common_vendor.p({
          type: "datetime",
          ["clear-icon"]: false,
          modelValue: formData.value.inspectionTime
        })
      } : {
        W: common_vendor.t(formData.value.inspectionTime || "-")
      }, {
        X: common_vendor.p({
          label: "巡检时间",
          name: "inspectionTime"
        }),
        Y: isEditing.value
      }, isEditing.value ? {
        Z: common_vendor.t(formData.value.equipmentStatus === "0" ? "正常" : "异常"),
        aa: formData.value.equipmentStatus === "0",
        ab: common_vendor.o(onStatusChange)
      } : {
        ac: common_vendor.t(formData.value.equipmentStatus === "0" ? "正常" : "异常"),
        ad: common_vendor.n(formData.value.equipmentStatus === "0" ? "status-normal" : "status-error")
      }, {
        ae: common_vendor.p({
          label: "设备状态",
          name: "equipmentStatus"
        }),
        af: isEditing.value
      }, isEditing.value ? {
        ag: formData.value.remark,
        ah: common_vendor.o(($event) => formData.value.remark = $event.detail.value),
        ai: common_vendor.t(((_b = formData.value.remark) == null ? void 0 : _b.length) || 0)
      } : {
        aj: common_vendor.t(formData.value.remark || "-")
      }, {
        ak: common_vendor.p({
          label: "详细备注",
          name: "remark"
        }),
        al: common_vendor.sr(formRef, "7bc71f0a-1", {
          "k": "formRef"
        }),
        am: common_vendor.p({
          model: formData.value,
          rules,
          ["label-width"]: "160rpx"
        }),
        an: common_vendor.f(imageList.value, (img, idx, i0) => {
          return common_vendor.e({
            a: getImageUrl(img),
            b: common_vendor.o(($event) => previewImage(idx), idx)
          }, isEditing.value ? {
            c: common_vendor.o(($event) => removeImage(idx), idx)
          } : {}, {
            d: idx
          });
        }),
        ao: isEditing.value,
        ap: isEditing.value && imageList.value.length < 4
      }, isEditing.value && imageList.value.length < 4 ? {
        aq: common_vendor.p({
          type: "camera",
          size: "32",
          color: "#999"
        }),
        ar: common_vendor.o(chooseImage)
      } : {}, {
        as: !isEditing.value && imageList.value.length === 0
      }, !isEditing.value && imageList.value.length === 0 ? {} : {}, {
        at: isEditing.value
      }, isEditing.value ? {
        av: common_vendor.o(cancelEdit),
        aw: common_vendor.o(handleSave)
      } : {
        ax: common_vendor.o(startEdit)
      }, {
        ay: showBuildingPicker.value
      }, showBuildingPicker.value ? {
        az: common_vendor.o(($event) => showBuildingPicker.value = false),
        aA: common_vendor.o(confirmBuilding),
        aB: common_vendor.f(buildingList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.buildingName),
            b: item.buildingId
          };
        }),
        aC: [buildingIndex.value],
        aD: common_vendor.o(onBuildingChange),
        aE: common_vendor.o(() => {
        }),
        aF: common_vendor.o(($event) => showBuildingPicker.value = false)
      } : {}, {
        aG: showFloorPicker.value
      }, showFloorPicker.value ? {
        aH: common_vendor.o(($event) => showFloorPicker.value = false),
        aI: common_vendor.o(confirmFloor),
        aJ: common_vendor.f(floorList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item),
            b: item
          };
        }),
        aK: [floorIndex.value],
        aL: common_vendor.o(onFloorChange),
        aM: common_vendor.o(() => {
        }),
        aN: common_vendor.o(($event) => showFloorPicker.value = false)
      } : {}, {
        aO: showSystemPicker.value
      }, showSystemPicker.value ? {
        aP: common_vendor.o(($event) => showSystemPicker.value = false),
        aQ: common_vendor.o(confirmSystem),
        aR: common_vendor.f(systemList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.label),
            b: item.value
          };
        }),
        aS: [systemIndex.value],
        aT: common_vendor.o(onSystemChange),
        aU: common_vendor.o(() => {
        }),
        aV: common_vendor.o(($event) => showSystemPicker.value = false)
      } : {}, {
        aW: showEquipmentPicker.value
      }, showEquipmentPicker.value ? {
        aX: common_vendor.o(($event) => showEquipmentPicker.value = false),
        aY: common_vendor.o(confirmEquipment),
        aZ: common_vendor.f(equipmentList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item),
            b: item
          };
        }),
        ba: [equipmentIndex.value],
        bb: common_vendor.o(onEquipmentChange),
        bc: common_vendor.o(() => {
        }),
        bd: common_vendor.o(($event) => showEquipmentPicker.value = false)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-7bc71f0a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/inspection/detail.js.map
