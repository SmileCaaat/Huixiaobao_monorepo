"use strict";
const common_vendor = require("../../common/vendor.js");
const api_index = require("../../api/index.js");
if (!Array) {
  const _easycom_uni_nav_bar2 = common_vendor.resolveComponent("uni-nav-bar");
  const _easycom_uni_icons2 = common_vendor.resolveComponent("uni-icons");
  const _easycom_uni_load_more2 = common_vendor.resolveComponent("uni-load-more");
  (_easycom_uni_nav_bar2 + _easycom_uni_icons2 + _easycom_uni_load_more2)();
}
const _easycom_uni_nav_bar = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-nav-bar/uni-nav-bar.js";
const _easycom_uni_icons = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-icons/uni-icons.js";
const _easycom_uni_load_more = () => "../../node-modules/@dcloudio/uni-ui/lib/uni-load-more/uni-load-more.js";
if (!Math) {
  (_easycom_uni_nav_bar + _easycom_uni_icons + _easycom_uni_load_more)();
}
const pageSize = 10;
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const currentTab = common_vendor.ref(0);
    const list = common_vendor.ref([]);
    const stats = common_vendor.ref({
      pending: 0,
      processing: 0,
      completed: 0,
      reportedTotal: 0,
      assignedTotal: 0
    });
    const loading = common_vendor.ref(false);
    const refreshing = common_vendor.ref(false);
    const loadStatus = common_vendor.ref("more");
    const pageNum = common_vendor.ref(1);
    const goBack = () => {
      common_vendor.index.navigateBack({
        fail: () => {
          common_vendor.index.reLaunch({ url: "/pages/index/index" });
        }
      });
    };
    const switchTab = (index) => {
      if (currentTab.value === index)
        return;
      currentTab.value = index;
      onRefresh();
    };
    const goReport = () => {
      common_vendor.index.navigateTo({ url: "/pages/repair/form" });
    };
    const goDetail = (repairId) => {
      common_vendor.index.navigateTo({ url: `/pages/repair/detail?id=${repairId}` });
    };
    const getStatusText = (status) => {
      const map = { 0: "待处理", 1: "处理中", 2: "已完成" };
      return map[status] || "未知";
    };
    const getUrgencyText = (level) => {
      const map = { 0: "一般", 1: "紧急", 2: "特急" };
      return map[level] || "一般";
    };
    const formatTime = (timeStr) => {
      if (!timeStr)
        return "";
      return timeStr.substring(0, 16);
    };
    const fetchStats = async () => {
      try {
        const res = await api_index.api.getRepairStats();
        if (res.code === 200 || res.code === 0) {
          stats.value = res.data;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/repair/index.vue:195", "获取统计失败", e);
      }
    };
    const fetchData = async () => {
      if (loading.value)
        return;
      loading.value = true;
      loadStatus.value = "loading";
      try {
        const params = {
          pageNum: pageNum.value,
          pageSize
        };
        let res;
        if (currentTab.value === 0) {
          res = await api_index.api.getMyAssignedRepairs(params);
        } else {
          res = await api_index.api.getMyReportedRepairs(params);
        }
        if (res.code === 200 || res.code === 0) {
          const newList = res.rows || res.data || [];
          if (pageNum.value === 1) {
            list.value = newList;
          } else {
            list.value = list.value.concat(newList);
          }
          if (newList.length < pageSize) {
            loadStatus.value = "noMore";
          } else {
            loadStatus.value = "more";
          }
        } else {
          common_vendor.index.showToast({ title: "加载失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/repair/index.vue:234", "获取列表失败", e);
        common_vendor.index.showToast({ title: "网络错误", icon: "none" });
      } finally {
        loading.value = false;
        refreshing.value = false;
      }
    };
    const onRefresh = () => {
      refreshing.value = true;
      pageNum.value = 1;
      fetchStats();
      fetchData();
    };
    const loadMore = () => {
      if (loadStatus.value === "more") {
        pageNum.value++;
        fetchData();
      }
    };
    common_vendor.onShow(() => {
      onRefresh();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.p({
          fixed: true,
          ["status-bar"]: true,
          ["left-icon"]: "back",
          title: "报修工单",
          ["background-color"]: "#e53935",
          color: "#ffffff"
        }),
        c: common_vendor.t(stats.value.pending || 0),
        d: common_vendor.t(stats.value.processing || 0),
        e: common_vendor.t(stats.value.completed || 0),
        f: stats.value.assignedTotal > 0
      }, stats.value.assignedTotal > 0 ? {
        g: common_vendor.t(stats.value.assignedTotal)
      } : {}, {
        h: currentTab.value === 0 ? 1 : "",
        i: common_vendor.o(($event) => switchTab(0)),
        j: stats.value.reportedTotal > 0
      }, stats.value.reportedTotal > 0 ? {
        k: common_vendor.t(stats.value.reportedTotal)
      } : {}, {
        l: currentTab.value === 1 ? 1 : "",
        m: common_vendor.o(($event) => switchTab(1)),
        n: list.value.length > 0
      }, list.value.length > 0 ? {
        o: common_vendor.f(list.value, (item, index, i0) => {
          return {
            a: common_vendor.t(getUrgencyText(item.urgencyLevel)),
            b: common_vendor.n("level-" + item.urgencyLevel),
            c: common_vendor.t(getStatusText(item.repairStatus)),
            d: common_vendor.n("status-" + item.repairStatus),
            e: common_vendor.t(item.companyName || "未知单位"),
            f: common_vendor.t(item.equipmentName || "未指定设备"),
            g: common_vendor.t(item.faultDescription),
            h: common_vendor.t(formatTime(item.createTime)),
            i: common_vendor.t(item.reporterName || "系统上报"),
            j: "3ef94d39-1-" + i0,
            k: index,
            l: common_vendor.o(($event) => goDetail(item.repairId), index)
          };
        }),
        p: common_vendor.p({
          type: "right",
          size: "14",
          color: "#ccc"
        }),
        q: common_vendor.p({
          status: loadStatus.value
        })
      } : !loading.value ? {
        s: common_vendor.p({
          type: "paperplane",
          size: "60",
          color: "#ddd"
        })
      } : {}, {
        r: !loading.value,
        t: common_vendor.o(loadMore),
        v: refreshing.value,
        w: common_vendor.o(onRefresh),
        x: common_vendor.p({
          type: "plusempty",
          size: "30",
          color: "#fff"
        }),
        y: common_vendor.o(goReport)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-3ef94d39"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/repair/index.js.map
