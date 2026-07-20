"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  const _easycom_uni_popup2 = common_vendor.resolveComponent("uni-popup");
  (_easycom_uni_nav_bar2 + _easycom_uni_icons2 + _easycom_uni_popup2)();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
const _easycom_uni_popup = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-popup/uni-popup.js";
if (!Math) {
  (_easycom_uni_nav_bar + _easycom_uni_icons + _easycom_uni_popup)();
}
const _sfc_main = {
  __name: "system",
  setup(__props) {
    const recordId = common_vendor.ref(null);
    const taskId = common_vendor.ref(null);
    const recordType = common_vendor.ref("0");
    const loading = common_vendor.ref(false);
    const systemInfo = common_vendor.ref({});
    const deviceList = common_vendor.ref([]);
    const maintenancePopup = common_vendor.ref(null);
    const submitting = common_vendor.ref(false);
    const maintenanceForm = common_vendor.ref({
      recordId: null,
      deviceLocation: "",
      testSituation: "",
      testTime: "",
      testResult: "",
      sitePhotosList: []
    });
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const fullImageUrl = (path) => {
      if (!path || typeof path !== "string")
        return "";
      const p = path.trim();
      if (!p)
        return "";
      if (p.indexOf("http://") === 0 || p.indexOf("https://") === 0 || p.indexOf("wxfile://") === 0)
        return p;
      const base = String(api_index.api.BASE_URL || "").replace(/\/$/, "");
      return p.startsWith("/") ? base + p : `${base}/${p}`;
    };
    const pickUploadPath = (uploadRes) => {
      var _a, _b, _c, _d;
      return uploadRes.url || ((_a = uploadRes.data) == null ? void 0 : _a.url) || uploadRes.fileName || ((_b = uploadRes.data) == null ? void 0 : _b.fileName) || ((_c = uploadRes.data) == null ? void 0 : _c.newFileName) || ((_d = uploadRes.data) == null ? void 0 : _d.url);
    };
    const loadDeviceList = async () => {
      if (!recordId.value)
        return;
      try {
        loading.value = true;
        const res = await api_index.api.getSystemDetail(recordId.value);
        if (res.code === 200 || res.code === 0) {
          const data = res.data || {};
          if (data.system) {
            systemInfo.value = data.system;
          }
          deviceList.value = (data.equipments || []).filter(
            (item) => item.recordType === recordType.value
          );
          if (!taskId.value) {
            const sys = data.system || {};
            const eqs = data.equipments || [];
            const resolved = sys.taskId || data.taskId || eqs[0] && eqs[0].taskId;
            if (resolved)
              taskId.value = String(resolved);
          }
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/system.vue:195", "获取设备列表失败:", e);
        common_vendor.index.showToast({ title: "获取设备列表失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const goDeviceDetail = (item) => {
      common_vendor.index.setStorageSync("currentDevice", item);
      const tid = taskId.value || "";
      common_vendor.index.navigateTo({
        url: `/pages/task/device?recordId=${item.recordId}&recordType=${recordType.value}&taskId=${tid}`
      });
    };
    const openMaintenance = (item) => {
      maintenanceForm.value = {
        recordId: item.recordId,
        deviceLocation: item.deviceLocation || "",
        testSituation: item.testSituation || "",
        testTime: item.testTime || "",
        testResult: item.testResult || "",
        sitePhotosList: item.sitePhotos ? item.sitePhotos.split(",").filter((s) => s) : []
      };
      maintenancePopup.value.open();
    };
    const closeMaintenance = () => {
      maintenancePopup.value.close();
    };
    const previewImage = (url) => {
      const urls = maintenanceForm.value.sitePhotosList.map((u) => fullImageUrl(u));
      common_vendor.index.previewImage({
        urls,
        current: fullImageUrl(url)
      });
    };
    const deleteImage = (index) => {
      maintenanceForm.value.sitePhotosList.splice(index, 1);
    };
    const chooseImage = () => {
      common_vendor.index.chooseImage({
        count: 5 - maintenanceForm.value.sitePhotosList.length,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: async (res) => {
          const tempFilePaths = res.tempFilePaths;
          for (const path of tempFilePaths) {
            try {
              const uploadRes = await api_index.api.uploadFile(path);
              const stored = pickUploadPath(uploadRes);
              if ((uploadRes.code === 200 || uploadRes.code === 0) && stored) {
                maintenanceForm.value.sitePhotosList.push(stored);
              } else if (uploadRes.code === 200 || uploadRes.code === 0) {
                common_vendor.index.showToast({ title: "上传成功但未返回文件地址", icon: "none" });
              }
            } catch (e) {
              common_vendor.index.__f__("error", "at pages/task/system.vue:258", "图片上传失败:", e);
            }
          }
        }
      });
    };
    const submitMaintenance = async () => {
      if (!taskId.value) {
        common_vendor.index.showToast({ title: "缺少任务ID，请从任务列表进入", icon: "none" });
        return;
      }
      try {
        submitting.value = true;
        const now = /* @__PURE__ */ new Date();
        const formattedDate = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")} ${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}:${String(now.getSeconds()).padStart(2, "0")}`;
        const data = {
          recordId: maintenanceForm.value.recordId,
          deviceLocation: maintenanceForm.value.deviceLocation,
          testSituation: maintenanceForm.value.testSituation,
          testResult: maintenanceForm.value.testResult,
          sitePhotos: maintenanceForm.value.sitePhotosList.join(","),
          testTime: maintenanceForm.value.testTime || formattedDate,
          taskId: taskId.value
        };
        const res = await api_index.api.updateMaintenance(data);
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({ title: "保存成功" });
          closeMaintenance();
          loadDeviceList();
        } else {
          common_vendor.index.showToast({ title: res.msg || "保存失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/system.vue:290", "保存维护信息失败:", e);
        common_vendor.index.showToast({ title: "保存失败", icon: "none" });
      } finally {
        submitting.value = false;
      }
    };
    common_vendor.onShow(() => {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const options = currentPage.options || {};
      recordId.value = options.recordId;
      recordType.value = options.recordType || "0";
      const cachedTask = common_vendor.index.getStorageSync("currentTask");
      taskId.value = options.taskId || (cachedTask && cachedTask.taskId) || null;
      const cached = common_vendor.index.getStorageSync("currentSystem");
      if (cached) {
        systemInfo.value = cached;
      }
      loadDeviceList();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "设备列表",
          ["background-color"]: recordType.value === "1" ? "#ff9800" : "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.t(systemInfo.value.itemName || "系统"),
        d: common_vendor.f(deviceList.value, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.itemName),
            b: common_vendor.t(item.totalItems || 0),
            c: common_vendor.t(item.completedItems || 0),
            d: common_vendor.t(item.uncompletedItems || 0),
            e: common_vendor.t(item.completedItems >= item.totalItems && item.totalItems > 0 ? "已完成" : "未完成"),
            f: common_vendor.n(item.completedItems >= item.totalItems && item.totalItems > 0 ? "completed" : "pending")
          }, recordType.value === "1" ? {
            g: common_vendor.o(($event) => openMaintenance(item), item.recordId)
          } : {}, {
            h: item.recordId,
            i: common_vendor.o(($event) => goDeviceDetail(item), item.recordId)
          });
        }),
        e: recordType.value === "1",
        f: loading.value
      }, loading.value ? {} : {}, {
        g: deviceList.value.length === 0 && !loading.value
      }, deviceList.value.length === 0 && !loading.value ? {} : {}, {
        h: common_vendor.o(closeMaintenance),
        i: maintenanceForm.value.deviceLocation,
        j: common_vendor.o(($event) => maintenanceForm.value.deviceLocation = $event.detail.value),
        k: maintenanceForm.value.testSituation,
        l: common_vendor.o(($event) => maintenanceForm.value.testSituation = $event.detail.value),
        m: maintenanceForm.value.testResult,
        n: common_vendor.o(($event) => maintenanceForm.value.testResult = $event.detail.value),
        o: common_vendor.f(maintenanceForm.value.sitePhotosList, (img, index, i0) => {
          return {
            a: fullImageUrl(img),
            b: common_vendor.o(($event) => previewImage(img), index),
            c: common_vendor.o(($event) => deleteImage(index), index),
            d: index
          };
        }),
        p: maintenanceForm.value.sitePhotosList.length < 5
      }, maintenanceForm.value.sitePhotosList.length < 5 ? {
        q: common_vendor.p({
          type: "plusempty",
          size: "30",
          color: "#999"
        }),
        r: common_vendor.o(chooseImage)
      } : {}, {
        s: common_vendor.o(submitMaintenance),
        t: submitting.value,
        v: common_vendor.sr(maintenancePopup, "2ecdcd3f-1", {
          "k": "maintenancePopup"
        }),
        w: common_vendor.p({
          type: "bottom",
          ["background-color"]: "#fff"
        }),
        x: recordType.value === "1" ? 1 : ""
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-2ecdcd3f"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/task/system.js.map
