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
  __name: "detail",
  setup(__props) {
    const taskId = common_vendor.ref(null);
    const loading = common_vendor.ref(false);
    const taskInfo = common_vendor.ref({});
    const systemList = common_vendor.ref([]);
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const loadTaskDetail = async () => {
      if (!taskId.value)
        return;
      try {
        loading.value = true;
        const res = await api_index.api.getInspectionTestDetail(taskId.value);
        if (res.code === 200 || res.code === 0) {
          const data = res.data || {};
          taskInfo.value = data.taskInfo || data;
          systemList.value = data.categories || [];
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/detail.js", "获取消防维护详情失败:", e);
        const cached = common_vendor.index.getStorageSync("currentTask");
        if (cached) {
          taskInfo.value = cached;
        }
      } finally {
        loading.value = false;
      }
    };
    const goSystemDetail = (item) => {
      common_vendor.index.setStorageSync("currentSystem", item);
      common_vendor.index.navigateTo({
        url: `/pages/task/system?categoryKey=${encodeURIComponent(item.categoryKey)}&taskId=${taskId.value}`
      });
    };
    common_vendor.onShow(() => {
      var _a;
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const id = (_a = currentPage.options) == null ? void 0 : _a.id;
      if (id) {
        taskId.value = id;
        loadTaskDetail();
      } else {
        const cached = common_vendor.index.getStorageSync("currentTask");
        if (cached) {
          taskInfo.value = cached;
          taskId.value = cached.taskId;
          loadTaskDetail();
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
          title: "消防维护",
          ["background-color"]: "#1565c0",
          color: "#ffffff"
        }),
        c: common_vendor.t(taskInfo.value.taskName || "维保任务"),
        d: common_vendor.f(systemList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.categoryName || item.itemName || item.systemName),
            b: common_vendor.t(item.totalItems || 0),
            c: common_vendor.t(item.completedItems || 0),
            d: common_vendor.t(item.uncompletedItems || 0),
            e: common_vendor.t(item.status === "1" || item.completedItems >= item.totalItems && item.totalItems > 0 ? "已完成" : "未完成"),
            f: common_vendor.n(item.status === "1" || item.completedItems >= item.totalItems && item.totalItems > 0 ? "completed" : "pending"),
            g: item.categoryKey || item.recordId,
            h: common_vendor.o(($event) => goSystemDetail(item), item.categoryKey || item.recordId)
          };
        }),
        e: loading.value
      }, loading.value ? {} : {}, {
        f: systemList.value.length === 0 && !loading.value
      }, systemList.value.length === 0 && !loading.value ? {} : {}, {
        g: false
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-2eddac49"]]);
wx.createPage(MiniProgramPage);
