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
  __name: "device",
  setup(__props) {
    const recordId = common_vendor.ref(null);
    const taskId = common_vendor.ref(null);
    const categoryKey = common_vendor.ref(null);
    const equipmentKey = common_vendor.ref(null);
    const loading = common_vendor.ref(false);
    const deviceInfo = common_vendor.ref({});
    const itemList = common_vendor.ref([]);
    const showModal = common_vendor.ref(false);
    const currentItem = common_vendor.ref({});
    const savingMap = common_vendor.reactive({});
    const statusSeqMap = common_vendor.reactive({});
    const modalForm = common_vendor.reactive({
      otherNotes: "",
      faultImages: ""
    });
    const normalizeImageUrl = (url) => {
      if (!url)
        return "";
      return url.startsWith("http") ? url : `${api_index.api.BASE_URL}${url}`;
    };
    const splitImages = (images) => {
      if (!images)
        return [];
      return String(images).split(",").map((s) => s.trim()).filter(Boolean);
    };
    const getActiveLabel = (val, target, text) => {
      return val === target ? `✓ ${text}` : text;
    };
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const loadItemList = async () => {
      if (!taskId.value || !categoryKey.value || !equipmentKey.value) {
        if (recordId.value) {
          // 兼容旧入口：仍按 recordId 拉取
          try {
            loading.value = true;
            const legacy = await api_index.api.getDeviceDetail(recordId.value);
            if (legacy.code === 200 || legacy.code === 0) {
              const data = legacy.data || {};
              deviceInfo.value = data.equipment || {};
              itemList.value = (data.checkItems || data.items || []).map((item) => ({
                ...item,
                faultDescription: item.faultDescription || "",
                otherNotes: item.otherNotes || "",
                faultImages: item.faultImages || "",
                checkResult: item.checkResult || "0"
              }));
            }
          } catch (e) {
            common_vendor.index.showToast({ title: "获取检查项列表失败", icon: "none" });
          } finally {
            loading.value = false;
          }
        }
        return;
      }
      try {
        loading.value = true;
        const res = await api_index.api.getInspectionTestEquipment(
          taskId.value,
          categoryKey.value,
          equipmentKey.value
        );
        if (res.code === 200 || res.code === 0) {
          const data = res.data || {};
          deviceInfo.value = data;
          itemList.value = (data.checkItems || []).map((item) => ({
            ...item,
            faultDescription: item.faultDescription || "",
            otherNotes: item.otherNotes || "",
            faultImages: item.faultImages || "",
            checkResult: item.checkResult || "0"
          }));
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/device.js:loadItemList", "获取检查项列表失败:", e);
        common_vendor.index.showToast({ title: "获取检查项列表失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const handleQuickAction = async (item, result) => {
      if (!item || !item.recordId)
        return;
      if (item.checkResult === result)
        return;
      if (savingMap[item.recordId])
        return;
      if (!taskId.value) {
        common_vendor.index.showToast({ title: "缺少任务ID，请从任务列表进入", icon: "none" });
        return;
      }
      const originalResult = item.checkResult;
      const originalDesc = item.faultDescription;
      savingMap[item.recordId] = true;
      statusSeqMap[item.recordId] = (statusSeqMap[item.recordId] || 0) + 1;
      const seq = statusSeqMap[item.recordId];
      item.checkResult = result;
      if (result !== "2") {
        item.faultDescription = "";
      }
      try {
        common_vendor.index.showLoading({ mask: true });
        const res = await api_index.api.updateCheckResult({
          recordId: item.recordId,
          checkResult: result,
          taskId: taskId.value
        });
        if (seq !== statusSeqMap[item.recordId])
          return;
        if (res.code !== 200 && res.code !== 0) {
          item.checkResult = originalResult;
          item.faultDescription = originalDesc;
          common_vendor.index.showToast({ title: res.msg || "操作失败", icon: "none" });
        }
      } catch (e) {
        if (seq === statusSeqMap[item.recordId]) {
          item.checkResult = originalResult;
          item.faultDescription = originalDesc;
        }
        common_vendor.index.__f__("error", "at pages/task/device.vue:handleQuickAction", e);
        common_vendor.index.showToast({ title: "操作失败", icon: "none" });
      } finally {
        if (seq === statusSeqMap[item.recordId]) {
          savingMap[item.recordId] = false;
        }
        common_vendor.index.hideLoading();
      }
    };
    const saveFaultDesc = async (item) => {
      if (!item.faultDescription || !item.faultDescription.trim()) {
        common_vendor.index.showToast({ title: "请输入故障描述", icon: "none" });
        return;
      }
      if (!taskId.value) {
        common_vendor.index.showToast({ title: "缺少任务ID，请从任务列表进入", icon: "none" });
        return;
      }
      try {
        common_vendor.index.showLoading({ mask: true });
        const res = await api_index.api.updateFaultDesc({
          recordId: item.recordId,
          faultDescription: item.faultDescription,
          taskId: taskId.value
        });
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({ title: "保存成功", icon: "success" });
        } else {
          common_vendor.index.showToast({ title: res.msg || "保存失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/device.vue:saveFaultDesc", e);
        common_vendor.index.showToast({ title: "保存失败", icon: "none" });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    const openOtherNotes = (item) => {
      currentItem.value = { ...item };
      modalForm.otherNotes = item.otherNotes || "";
      modalForm.faultImages = item.faultImages || "";
      showModal.value = true;
    };
    const closeModal = () => {
      showModal.value = false;
    };
    const chooseImage = () => {
      common_vendor.index.chooseImage({
        count: 3,
        sizeType: ["compressed"],
        sourceType: ["camera", "album"],
        success: async (res) => {
          const tempFilePaths = res.tempFilePaths || [];
          common_vendor.index.showLoading({ title: "上传中..." });
          let failed = false;
          for (const path of tempFilePaths) {
            try {
              const uploadRes = await api_index.api.uploadFile(path);
              if (uploadRes.code === 200 || uploadRes.code === 0) {
                const currentImages = splitImages(modalForm.faultImages);
                const url = uploadRes.fileName || uploadRes.url || uploadRes.data && (uploadRes.data.url || uploadRes.data.fileName);
                if (url && currentImages.length < 5) {
                  currentImages.push(url);
                  modalForm.faultImages = currentImages.join(",");
                }
              } else {
                failed = true;
              }
            } catch (e) {
              failed = true;
              common_vendor.index.__f__("error", "at pages/task/device.vue:chooseImage", e);
            }
          }
          common_vendor.index.hideLoading();
          if (failed) {
            common_vendor.index.showToast({ title: "部分上传失败，可重试", icon: "none" });
          }
        }
      });
    };
    const previewModalImages = (index) => {
      const urls = splitImages(modalForm.faultImages).map(normalizeImageUrl);
      if (!urls.length)
        return;
      common_vendor.index.previewImage({
        urls,
        current: index
      });
    };
    const removeModalImage = (index) => {
      const images = splitImages(modalForm.faultImages);
      images.splice(index, 1);
      modalForm.faultImages = images.join(",");
    };
    const saveDetail = async () => {
      if (!taskId.value) {
        common_vendor.index.showToast({ title: "缺少任务ID，请从任务列表进入", icon: "none" });
        return;
      }
      try {
        common_vendor.index.showLoading({ mask: true });
        // 不传 checkResult，避免覆盖外层状态按钮结果
        const res = await api_index.api.updateCheckDetail({
          recordId: currentItem.value.recordId,
          otherNotes: modalForm.otherNotes == null ? "" : modalForm.otherNotes,
          faultImages: modalForm.faultImages == null ? "" : modalForm.faultImages,
          taskId: taskId.value
        });
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({ title: "保存成功", icon: "success" });
          const index = itemList.value.findIndex((i) => i.recordId === currentItem.value.recordId);
          if (index > -1) {
            itemList.value[index] = {
              ...itemList.value[index],
              otherNotes: modalForm.otherNotes == null ? "" : modalForm.otherNotes,
              faultImages: modalForm.faultImages == null ? "" : modalForm.faultImages
            };
          }
          closeModal();
        } else {
          common_vendor.index.showToast({ title: res.msg || "保存失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/device.vue:saveDetail", e);
        common_vendor.index.showToast({ title: "保存失败", icon: "none" });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    common_vendor.onShow(() => {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const options = currentPage.options || {};
      recordId.value = options.recordId || null;
      categoryKey.value = options.categoryKey ? decodeURIComponent(options.categoryKey) : null;
      equipmentKey.value = options.equipmentKey ? decodeURIComponent(options.equipmentKey) : null;
      const cachedTask = common_vendor.index.getStorageSync("currentTask");
      taskId.value = options.taskId || cachedTask && cachedTask.taskId || null;
      const cached = common_vendor.index.getStorageSync("currentDevice");
      if (cached) {
        deviceInfo.value = cached;
        if (!categoryKey.value && cached.categoryKey)
          categoryKey.value = cached.categoryKey;
        if (!equipmentKey.value && cached.equipmentKey)
          equipmentKey.value = cached.equipmentKey;
      }
      loadItemList();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: deviceInfo.value.equipmentName || deviceInfo.value.itemName || "设备详情",
          ["background-color"]: "#1565c0",
          color: "#ffffff"
        }),
        c: common_vendor.f(itemList.value, (item, k0, i0) => {
          const count = splitImages(item.faultImages).length;
          return common_vendor.e({
            a: common_vendor.t(item.itemName),
            b: common_vendor.t(item.itemCode),
            c: item.checkResult === "1",
            d: common_vendor.o(($event) => handleQuickAction(item, "1"), item.recordId),
            e: common_vendor.t(getActiveLabel(item.checkResult, "1", "正常")),
            f: item.checkResult === "2",
            g: common_vendor.o(($event) => handleQuickAction(item, "2"), item.recordId),
            h: common_vendor.t(getActiveLabel(item.checkResult, "2", "故障")),
            i: item.checkResult === "3",
            j: common_vendor.o(($event) => handleQuickAction(item, "3"), item.recordId),
            k: common_vendor.t(getActiveLabel(item.checkResult, "3", "无此设备")),
            l: item.checkResult === "2"
          }, item.checkResult === "2" ? {
            m: item.faultDescription,
            n: common_vendor.o(($event) => item.faultDescription = $event.detail.value, item.recordId),
            o: common_vendor.o(($event) => saveFaultDesc(item), item.recordId),
            p: common_vendor.o(() => {
            }, item.recordId)
          } : {}, {
            q: common_vendor.o(($event) => openOtherNotes(item), item.recordId),
            r: common_vendor.t("其他说明"),
            s: count > 0,
            t: common_vendor.t(count),
            z: item.recordId
          });
        }),
        d: loading.value
      }, loading.value ? {} : {}, {
        e: itemList.value.length === 0 && !loading.value
      }, itemList.value.length === 0 && !loading.value ? {} : {}, {
        f: showModal.value
      }, showModal.value ? common_vendor.e({
        g: common_vendor.o(closeModal),
        h: modalForm.otherNotes,
        i: common_vendor.o(($event) => modalForm.otherNotes = $event.detail.value),
        j: common_vendor.o(chooseImage),
        k: !modalForm.faultImages
      }, !modalForm.faultImages ? {} : {
        l: common_vendor.t(splitImages(modalForm.faultImages).length)
      }, {
        m: !!modalForm.faultImages
      }, modalForm.faultImages ? {
        n: common_vendor.f(splitImages(modalForm.faultImages), (img, index, i0) => {
          return {
            a: normalizeImageUrl(img),
            b: common_vendor.o(($event) => previewModalImages(index), index),
            c: common_vendor.o(($event) => removeModalImage(index), "del-" + index),
            d: index
          };
        })
      } : {}, {
        o: common_vendor.o(closeModal),
        p: common_vendor.o(saveDetail),
        z: common_vendor.o(() => {
        }),
        A: common_vendor.o(closeModal)
      }) : {}, {
        B: ""
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-588b9861"]]);
wx.createPage(MiniProgramPage);
