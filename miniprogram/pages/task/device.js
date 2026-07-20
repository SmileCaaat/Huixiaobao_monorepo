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
    const recordType = common_vendor.ref("0");
    const loading = common_vendor.ref(false);
    const deviceInfo = common_vendor.ref({});
    const itemList = common_vendor.ref([]);
    const showModal = common_vendor.ref(false);
    const currentItem = common_vendor.ref({});
    const modalForm = common_vendor.reactive({
      otherNotes: "",
      faultImages: ""
    });
    const statusOptions = [
      { label: "正常", value: "1" },
      { label: "故障", value: "2" },
      { label: "无此设备", value: "3" }
    ];
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const loadItemList = async () => {
      if (!recordId.value)
        return;
      try {
        loading.value = true;
        const res = await api_index.api.getDeviceDetail(recordId.value);
        if (res.code === 200 || res.code === 0) {
          const data = res.data || {};
          if (data.equipment) {
            deviceInfo.value = data.equipment;
          }
          itemList.value = (data.checkItems || data.items || []).filter((item) => item.recordType === recordType.value).map((item) => ({
            ...item,
            faultDescription: item.faultDescription || "",
            checkResult: item.checkResult || "0"
            // 默认为0未检查
          }));
          if (!taskId.value) {
            const eq = data.equipment || {};
            const rows = data.checkItems || data.items || [];
            const fromEq = eq.taskId;
            const fromRow = rows[0] && rows[0].taskId;
            const resolved = fromEq || fromRow || data.taskId;
            if (resolved)
              taskId.value = String(resolved);
          }
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/device.vue:239", "获取检查项列表失败:", e);
        common_vendor.index.showToast({ title: "获取检查项列表失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const handleQuickAction = async (item, result) => {
      if (item.checkResult === result)
        return;
      if (!taskId.value) {
        common_vendor.index.showToast({ title: "缺少任务ID，请从任务列表进入", icon: "none" });
        return;
      }
      const originalResult = item.checkResult;
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
        if (res.code !== 200 && res.code !== 0) {
          item.checkResult = originalResult;
          common_vendor.index.showToast({ title: res.msg || "操作失败", icon: "none" });
        }
      } catch (e) {
        item.checkResult = originalResult;
        common_vendor.index.__f__("error", "at pages/task/device.vue:272", e);
        common_vendor.index.showToast({ title: "操作失败", icon: "none" });
      } finally {
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
        common_vendor.index.__f__("error", "at pages/task/device.vue:299", e);
        common_vendor.index.showToast({ title: "保存失败", icon: "none" });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    const openDetailModal = (item) => {
      currentItem.value = { ...item };
      modalForm.otherNotes = item.otherNotes || "";
      modalForm.faultImages = item.faultImages || "";
      showModal.value = true;
    };
    const closeModal = () => {
      showModal.value = false;
    };
    const updateModalResult = (result) => {
      currentItem.value.checkResult = result;
    };
    const onStatusChange = (e) => {
      const index = e.detail.value;
      currentItem.value.checkResult = statusOptions[index].value;
    };
    const getStatusLabel = (val) => {
      const option = statusOptions.find((opt) => opt.value === val);
      return option ? option.label : "请选择";
    };
    const chooseImage = () => {
      common_vendor.index.chooseImage({
        count: 3,
        sizeType: ["compressed"],
        success: async (res) => {
          const tempFilePaths = res.tempFilePaths;
          common_vendor.index.showLoading({ title: "上传中..." });
          for (const path of tempFilePaths) {
            try {
              const uploadRes = await api_index.api.uploadFile(path);
              if (uploadRes.code === 200 || uploadRes.code === 0) {
                const currentImages = modalForm.faultImages ? modalForm.faultImages.split(",").filter((s) => s) : [];
                if (currentImages.length < 5) {
                  currentImages.push(uploadRes.fileName || uploadRes.data.url);
                  modalForm.faultImages = currentImages.join(",");
                }
              }
            } catch (e) {
              common_vendor.index.__f__("error", "at pages/task/device.vue:358", "图片上传失败:", e);
            }
          }
          common_vendor.index.hideLoading();
        }
      });
    };
    const saveDetail = async () => {
      if (!taskId.value) {
        common_vendor.index.showToast({ title: "缺少任务ID，请从任务列表进入", icon: "none" });
        return;
      }
      try {
        common_vendor.index.showLoading({ mask: true });
        const res = await api_index.api.updateCheckDetail({
          recordId: currentItem.value.recordId,
          checkResult: currentItem.value.checkResult,
          faultDescription: currentItem.value.faultDescription,
          // 从 currentItem 取
          otherNotes: modalForm.otherNotes,
          faultImages: modalForm.faultImages,
          taskId: taskId.value
        });
        if (res.code === 200 || res.code === 0) {
          common_vendor.index.showToast({ title: "保存成功", icon: "success" });
          closeModal();
          const index = itemList.value.findIndex(
            (i) => i.recordId === currentItem.value.recordId
          );
          if (index > -1) {
            itemList.value[index] = {
              ...itemList.value[index],
              checkResult: currentItem.value.checkResult,
              otherNotes: modalForm.otherNotes,
              faultImages: modalForm.faultImages
            };
          }
        } else {
          common_vendor.index.showToast({ title: res.msg || "保存失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/device.vue:397", e);
        common_vendor.index.showToast({ title: "保存失败", icon: "none" });
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    common_vendor.onShow(() => {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const options = currentPage.options || {};
      recordId.value = options.recordId;
      const cachedTask = common_vendor.index.getStorageSync("currentTask");
      taskId.value = options.taskId || (cachedTask && cachedTask.taskId) || null;
      recordType.value = options.recordType || "0";
      const cached = common_vendor.index.getStorageSync("currentDevice");
      if (cached) {
        deviceInfo.value = cached;
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
          title: deviceInfo.value.itemName || "设备详情",
          ["background-color"]: recordType.value === "1" ? "#ff9800" : "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.f(itemList.value, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.itemName),
            b: common_vendor.t(item.itemCode),
            c: item.checkResult === "1" ? 1 : "",
            d: common_vendor.o(($event) => handleQuickAction(item, "1"), item.recordId),
            e: item.checkResult === "2" ? 1 : "",
            f: common_vendor.o(($event) => handleQuickAction(item, "2"), item.recordId),
            g: item.checkResult === "3" ? 1 : "",
            h: common_vendor.o(($event) => handleQuickAction(item, "3"), item.recordId),
            i: item.checkResult === "2"
          }, item.checkResult === "2" ? {
            j: item.faultDescription,
            k: common_vendor.o(($event) => item.faultDescription = $event.detail.value, item.recordId),
            l: common_vendor.o(($event) => saveFaultDesc(item), item.recordId),
            m: common_vendor.o(() => {
            }, item.recordId)
          } : {}, {
            n: item.recordId,
            o: common_vendor.o(($event) => openDetailModal(item), item.recordId)
          });
        }),
        d: loading.value
      }, loading.value ? {} : {}, {
        e: itemList.value.length === 0 && !loading.value
      }, itemList.value.length === 0 && !loading.value ? {} : {}, {
        f: showModal.value
      }, showModal.value ? common_vendor.e({
        g: common_vendor.o(closeModal),
        h: common_vendor.t(currentItem.value.itemName),
        i: currentItem.value.checkResult === "1"
      }, currentItem.value.checkResult === "1" ? {} : {}, {
        j: currentItem.value.checkResult === "1" ? 1 : "",
        k: common_vendor.o(($event) => updateModalResult("1")),
        l: currentItem.value.checkResult === "2"
      }, currentItem.value.checkResult === "2" ? {} : {}, {
        m: currentItem.value.checkResult === "2" ? 1 : "",
        n: common_vendor.o(($event) => updateModalResult("2")),
        o: common_vendor.t(getStatusLabel(currentItem.value.checkResult)),
        p: statusOptions,
        q: common_vendor.o(onStatusChange),
        r: modalForm.otherNotes,
        s: common_vendor.o(($event) => modalForm.otherNotes = $event.detail.value),
        t: common_vendor.o(chooseImage),
        v: !modalForm.faultImages
      }, !modalForm.faultImages ? {} : {
        w: common_vendor.t(modalForm.faultImages.split(",").length)
      }, {
        x: common_vendor.o(closeModal),
        y: common_vendor.o(saveDetail),
        z: common_vendor.o(() => {
        }),
        A: common_vendor.o(closeModal)
      }) : {}, {
        B: recordType.value === "1" ? 1 : ""
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-588b9861"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/task/device.js.map
