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
  __name: "index",
  setup(__props) {
    const activeTab = common_vendor.ref("periodic");
    const taskList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const pageNum = common_vendor.ref(1);
    const pageSize = common_vendor.ref(10);
    const hasMore = common_vendor.ref(true);
    const currentCompanyId = common_vendor.ref(null);
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const switchTab = (tab) => {
      if (activeTab.value === tab)
        return;
      activeTab.value = tab;
      pageNum.value = 1;
      taskList.value = [];
      hasMore.value = true;
      loadTaskList();
    };
    const formatDateYMD = (date) => {
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, "0");
      const d = String(date.getDate()).padStart(2, "0");
      return `${y}-${m}-${d}`;
    };
    const getTaskQueryTimeRange = () => {
      const now = /* @__PURE__ */ new Date();
      const firstDay = new Date(now.getFullYear(), now.getMonth() - 12, 1);
      const lastDay = new Date(now.getFullYear(), now.getMonth() + 13, 0);
      return {
        beginTime: formatDateYMD(firstDay),
        endTime: formatDateYMD(lastDay)
      };
    };
    const loadCurrentCompany = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          currentCompanyId.value = res.data.companyId;
          return res.data.companyId;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/index.vue:141", "获取当前公司失败:", e);
      }
      return null;
    };
    const loadTaskList = async () => {
      var _a, _b;
      if (loading.value || !hasMore.value)
        return;
      try {
        loading.value = true;
        const companyId = await loadCurrentCompany();
        if (!companyId) {
          common_vendor.index.showToast({ title: "请先选择公司", icon: "none" });
          return;
        }
        const { beginTime, endTime } = getTaskQueryTimeRange();
        const res = await api_index.api.getMyTaskList({
          companyId: currentCompanyId.value,
          taskStatus: "",
          taskType: activeTab.value === "periodic" ? "0" : "1",
          // 0: 周期任务, 1: 临时任务
          params: {
            beginTime,
            endTime
          },
          pageNum: pageNum.value,
          pageSize: pageSize.value
        });
        if (res.code === 200 || res.code === 0) {
          const rawRows = res.rows || ((_a = res.data) == null ? void 0 : _a.rows) || [];
          const targetType = activeTab.value === "periodic" ? "0" : "1";
          const rows = rawRows.filter(
            (item) => String(item.taskType ?? "") === targetType
          );
          if (pageNum.value === 1) {
            taskList.value = rows;
          } else {
            taskList.value = [...taskList.value, ...rows];
          }
          hasMore.value = rawRows.length >= pageSize.value;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/task/index.vue:194", "获取任务列表失败:", e);
        common_vendor.index.showToast({ title: "获取任务列表失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const loadMore = () => {
      if (hasMore.value && !loading.value) {
        pageNum.value++;
        loadTaskList();
      }
    };
    const goTaskDetail = (item) => {
      common_vendor.index.setStorageSync("currentTask", item);
      common_vendor.index.navigateTo({
        url: `/pages/task/action?id=${item.taskId}`
      });
    };
    const getStatusClass = (status) => {
      const statusMap = {
        0: "status-pending",
        // 待执行
        1: "status-running",
        // 执行中
        2: "status-completed",
        // 已完成
        3: "status-overdue"
        // 已逾期
      };
      return statusMap[status] || "status-pending";
    };
    const getStatusText = (status) => {
      const statusMap = {
        0: "待执行",
        1: "执行中",
        2: "已完成",
        3: "已逾期"
      };
      return statusMap[status] || "待执行";
    };
    common_vendor.onShow(async () => {
      pageNum.value = 1;
      taskList.value = [];
      hasMore.value = true;
      await loadTaskList();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "维保任务",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: activeTab.value === "periodic"
      }, activeTab.value === "periodic" ? {} : {}, {
        d: activeTab.value === "periodic" ? 1 : "",
        e: common_vendor.o(($event) => switchTab("periodic")),
        f: activeTab.value === "temporary"
      }, activeTab.value === "temporary" ? {} : {}, {
        g: activeTab.value === "temporary" ? 1 : "",
        h: common_vendor.o(($event) => switchTab("temporary")),
        i: common_vendor.f(taskList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.taskName || "维保任务"),
            b: common_vendor.t(item.managerName || "-"),
            c: common_vendor.t(item.managerPhone || ""),
            d: common_vendor.t(item.operatorNames || "-"),
            e: common_vendor.t(getStatusText(item.taskStatus)),
            f: common_vendor.n(getStatusClass(item.taskStatus)),
            g: item.taskId,
            h: common_vendor.o(($event) => goTaskDetail(item), item.taskId)
          };
        }),
        j: loading.value
      }, loading.value ? {} : {}, {
        k: taskList.value.length === 0 && !loading.value
      }, taskList.value.length === 0 && !loading.value ? {} : {}, {
        l: common_vendor.o(loadMore)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-0fef721e"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/task/index.js.map
