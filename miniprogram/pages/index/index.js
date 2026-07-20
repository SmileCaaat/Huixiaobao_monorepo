"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_index = require("../../api/index.js");
if (!Math) {
  CompanyDrawer();
}
const CompanyDrawer = () => "../../components/CompanyDrawer.js";
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const projectName = common_vendor.ref("");
    const projectAddr = common_vendor.ref("");
    const showCompanyDrawer = common_vendor.ref(false);
    const selectedCompany = common_vendor.ref(null);
    const monthPlan = common_vendor.ref({
      done: 0,
      total: 0
    });
    const deviceStats = common_vendor.ref({
      normal: 0,
      abnormal: 0
    });
    const bannerList = common_vendor.ref([
      {
        title: "消防安全月活动",
        desc: "全面提升消防安全意识",
        color: "linear-gradient(135deg, #ff6b6b, #ee5a5a)"
      },
      {
        title: "设备巡检提醒",
        desc: "本月已完成 80% 巡检任务",
        color: "linear-gradient(135deg, #4facfe, #00f2fe)"
      },
      {
        title: "培训通知",
        desc: "新员工消防培训即将开始",
        color: "linear-gradient(135deg, #43e97b, #38f9d7)"
      }
    ]);
    const appList = common_vendor.ref([
      {
        name: "维保签到",
        icon: "/static/appIcon/sign_in.png",
        color: "#e53935",
        url: "/pages/checkin/index"
      },
      {
        name: "维保任务",
        icon: "/static/appIcon/task.png",
        color: "#fb8c00",
        url: "/pages/task/index"
      },
      {
        name: "维保客户",
        icon: "/static/appIcon/customer.png",
        color: "#3f51b5",
        url: "/pages/customer/index"
      },
      // {
      //   name: "巡检测试",
      //   icon: "/static/appIcon/test.png",
      //   color: "#43a047",
      //   url: "/pages/inspection/index",
      // },
      {
        name: "设备信息",
        icon: "/static/appIcon/equipment.png",
        color: "#1e88e5",
        url: "/pages/equipment/index"
      },
      {
        name: "报告查询",
        icon: "/static/appIcon/report.png",
        color: "#8e24aa",
        url: "/pages/report/index"
      },
      {
        name: "建筑信息",
        icon: "/static/appIcon/architecture.png",
        color: "#00897b",
        url: "/pages/building/index"
      },
      {
        name: "故障上报",
        icon: "/static/appIcon/repair_add.png",
        color: "#f44336",
        url: "/pages/repair/form"
      },
      {
        name: "报修工单",
        icon: "/static/appIcon/repair_list.png",
        color: "#ff9800",
        url: "/pages/repair/index"
      }
    ]);
    const goPage = (url) => {
      const enabledPages = [
        "/pages/building/index",
        "/pages/checkin/index",
        "/pages/equipment/index",
        "/pages/inspection/index",
        "/pages/task/index",
        "/pages/report/index",
        "/pages/repair/form",
        "/pages/repair/index",
        "/pages/customer/index"
      ];
      if (enabledPages.includes(url)) {
        common_vendor.index.navigateTo({ url });
      } else {
        common_vendor.index.showToast({ title: "功能开发中...", icon: "none" });
      }
    };
    const switchProject = () => {
      showCompanyDrawer.value = true;
    };
    const handleCompanySelect = async (company) => {
      try {
        common_vendor.index.showLoading({ title: "获取详情..." });
        const res = await api_index.api.getCompanyDetail(company.companyId);
        if ((res.code === 200 || res.code === 0) && res.data) {
          const data = res.data;
          selectedCompany.value = data;
          projectName.value = data.companyName || company.companyName;
          projectAddr.value = data.address || company.address;
          api_index.api.switchCompany({ companyId: data.companyId });
        } else {
          selectedCompany.value = company;
          projectName.value = company.companyName;
          projectAddr.value = company.address;
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/index/index.vue:251", "获取公司详情失败:", e);
        selectedCompany.value = company;
        projectName.value = company.companyName;
        projectAddr.value = company.address;
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    const loadData = async () => {
      try {
        const res = await api_index.api.getHomeStats(void 0, { showError: false });
        if (res.data) {
          if (!selectedCompany.value) {
            projectName.value = res.data.projectName || "消防设备管理项目";
            projectAddr.value = res.data.projectAddr || "默认地址";
          }
          monthPlan.value = res.data.monthPlan || { done: 0, total: 0 };
          deviceStats.value = res.data.deviceStats || { normal: 0, abnormal: 0 };
        }
      } catch (e) {
        if (!selectedCompany.value) {
          projectName.value = "消防设备管理项目";
          projectAddr.value = "项目地址";
        }
        monthPlan.value = { done: 0, total: 16 };
        deviceStats.value = { normal: 28, abnormal: 2 };
      }
    };
    const loadCurrentCompany = async () => {
      try {
        const res = await api_index.api.getCurrentCompany();
        if ((res.code === 200 || res.code === 0) && res.data) {
          const data = res.data;
          selectedCompany.value = data;
          projectName.value = data.companyName || "暂无项目";
          projectAddr.value = data.address || "请选择项目";
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/index/index.vue:296", "获取当前公司失败:", e);
      }
    };
    common_vendor.onMounted(() => {
      loadCurrentCompany();
      loadData();
    });
    return (_ctx, _cache) => {
      var _a;
      return {
        a: common_assets._imports_0,
        b: common_vendor.f(bannerList.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.title),
            b: common_vendor.t(item.desc),
            c: item.color,
            d: index
          };
        }),
        c: common_vendor.t(projectName.value || "暂无项目"),
        d: common_vendor.t(projectAddr.value || "请选择项目"),
        e: common_vendor.o(switchProject),
        f: common_vendor.f(appList.value, (item, index, i0) => {
          return {
            a: item.icon,
            b: item.color,
            c: common_vendor.t(item.name),
            d: index,
            e: common_vendor.o(($event) => goPage(item.url), index)
          };
        }),
        g: common_vendor.t(monthPlan.value.done),
        h: common_vendor.t(monthPlan.value.total),
        i: common_vendor.t(deviceStats.value.normal),
        j: common_vendor.o(($event) => showCompanyDrawer.value = false),
        k: common_vendor.o(handleCompanySelect),
        l: common_vendor.p({
          visible: showCompanyDrawer.value,
          currentCompanyId: (_a = selectedCompany.value) == null ? void 0 : _a.companyId
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-83a5a03c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/index/index.js.map
