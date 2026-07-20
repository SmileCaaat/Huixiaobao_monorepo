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
    const recordType = common_vendor.ref("0");
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const loadTaskDetail = async () => {
      if (!taskId.value)
        return;
      try {
        loading.value = true;
        const res = await api_index.api.getTaskDetail(taskId.value, recordType.value);
        if (res.code === 200 || res.code === 0) {
          const data = res.data || {};
          taskInfo.value = data;
          if (data.systems && Array.isArray(data.systems)) {
            systemList.value = data.systems.filter(
              (item) => item.recordType === recordType.value
            );
          } else {
            loadSystemList();
          }
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/detail.vue:122", "获取任务详情失败:", e);
        const cached = common_vendor.index.getStorageSync("currentTask");
        if (cached) {
          taskInfo.value = cached;
        }
        loadSystemList();
      } finally {
        loading.value = false;
      }
    };
    const loadSystemList = async () => {
      if (!taskId.value)
        return;
      try {
        loading.value = true;
        const res = await api_index.api.getTaskDetail(taskId.value, recordType.value);
        if (res.code === 200 || res.code === 0) {
          const rows = res.data || res.rows || [];
          systemList.value = rows.filter(
            (item) => item.recordType === recordType.value
          );
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/detail.vue:149", "获取系统列表失败:", e);
      } finally {
        loading.value = false;
      }
    };
    const goSystemDetail = (item) => {
      common_vendor.index.setStorageSync("currentSystem", item);
      common_vendor.index.navigateTo({
        url: `/pages/task/system?recordId=${item.recordId}&recordType=${recordType.value}&taskId=${taskId.value}`
      });
    };
    common_vendor.onShow(() => {
      var _a, _b;
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const id = (_a = currentPage.options) == null ? void 0 : _a.id;
      const type = (_b = currentPage.options) == null ? void 0 : _b.recordType;
      if (type !== void 0) {
        recordType.value = type;
      }
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
          title: recordType.value === "1" ? "消防设施测试" : "系统设备",
          ["background-color"]: recordType.value === "1" ? "#ff9800" : "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.t(taskInfo.value.taskName || "维保任务"),
        d: common_vendor.f(systemList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.itemName || item.systemName),
            b: common_vendor.t(item.totalItems || 0),
            c: common_vendor.t(item.completedItems || 0),
            d: common_vendor.t(item.uncompletedItems || 0),
            e: common_vendor.t(item.completedItems >= item.totalItems && item.totalItems > 0 ? "已完成" : "未完成"),
            f: common_vendor.n(item.completedItems >= item.totalItems && item.totalItems > 0 ? "completed" : "pending"),
            g: item.recordId,
            h: common_vendor.o(($event) => goSystemDetail(item), item.recordId)
          };
        }),
        e: loading.value
      }, loading.value ? {} : {}, {
        f: systemList.value.length === 0 && !loading.value
      }, systemList.value.length === 0 && !loading.value ? {} : {}, {
        g: recordType.value === "1" ? 1 : ""
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-2eddac49"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/task/detail.js.map
