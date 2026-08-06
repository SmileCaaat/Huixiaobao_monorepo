const { api } = require("../../api/index.js");
const auth = require("../../services/auth.js");

Page({
  data: {
    projectName: "",
    projectAddr: "",
    showCompanyDrawer: false,
    selectedCompany: null,
    monthPlan: { done: 0, total: 0 },
    deviceStats: { normal: 0, abnormal: 0 },
    homeError: "",
    appList: [
      { name: "维保签到", icon: "/static/appIcon/sign_in.png", color: "#e53935", url: "/pages/checkin/index" },
      { name: "维保任务", icon: "/static/appIcon/task.png", color: "#fb8c00", url: "/pages/task/index" },
      { name: "维保客户", icon: "/static/appIcon/customer.png", color: "#3f51b5", url: "/pages/customer/index" },
      { name: "巡检测试", icon: "/static/appIcon/test.png", color: "#43a047", url: "/pages/inspection/index" },
      { name: "设备信息", icon: "/static/appIcon/equipment.png", color: "#1e88e5", url: "/pages/equipment/index" },
      { name: "报告查询", icon: "/static/appIcon/report.png", color: "#8e24aa", url: "/pages/report/index" },
      { name: "建筑信息", icon: "/static/appIcon/architecture.png", color: "#00897b", url: "/pages/building/index" },
      { name: "故障上报", icon: "/static/appIcon/repair_add.png", color: "#f44336", url: "/pages/repair/form" },
      { name: "报修工单", icon: "/static/appIcon/repair_list.png", color: "#ff9800", url: "/pages/repair/index" }
    ]
  },

  onShow() {
    if (!auth.isLoggedIn()) {
      const app = getApp();
      if (app.globalData && app.globalData.authRedirecting) return;
      const pages = getCurrentPages() || [];
      const cur = pages.length ? pages[pages.length - 1] : null;
      const route = (cur && (cur.route || cur.__route__)) || "";
      if (route.indexOf("pages/login/") === 0) return;
      if (app.globalData) app.globalData.authRedirecting = true;
      wx.reLaunch({
        url: "/pages/login/index",
        complete() {
          if (app.globalData) app.globalData.authRedirecting = false;
        }
      });
      return;
    }
    this.loadCurrentCompany();
    this.loadHome();
  },

  goPage(e) {
    const url = e.currentTarget.dataset.url;
    if (url) wx.navigateTo({ url });
  },

  switchProject() {
    this.setData({ showCompanyDrawer: true });
  },

  onDrawerClose() {
    this.setData({ showCompanyDrawer: false });
  },

  async onCompanySelect(e) {
    const company = e.detail && e.detail.company;
    if (!company) return;
    try {
      wx.showLoading({ title: "切换中..." });
      const detailRes = await api.getCompanyDetail(company.companyId);
      const data = (detailRes && detailRes.data) || company;
      await api.switchCompany({ companyId: data.companyId });
      this.setData({
        selectedCompany: data,
        projectName: data.companyName || company.companyName,
        projectAddr: data.address || company.address || "",
        showCompanyDrawer: false
      });
      await this.loadHome();
    } catch (err) {
      // request 已提示
    } finally {
      wx.hideLoading();
    }
  },

  async loadCurrentCompany() {
    try {
      const res = await api.getCurrentCompany({}, { loading: false, showError: false });
      if (res && res.data) {
        this.setData({
          selectedCompany: res.data,
          projectName: res.data.companyName || "暂无项目",
          projectAddr: res.data.address || "请选择项目"
        });
      }
    } catch (e) {
      this.setData({
        projectName: "暂无项目",
        projectAddr: "请选择项目"
      });
    }
  },

  async loadHome() {
    try {
      const res = await api.getHomeStats({}, { loading: false, showError: false });
      const data = (res && res.data) || {};
      const patch = {
        homeError: "",
        monthPlan: data.monthPlan || { done: 0, total: 0 },
        deviceStats: data.deviceStats || { normal: 0, abnormal: 0 }
      };
      if (!this.data.selectedCompany) {
        patch.projectName = data.projectName || this.data.projectName || "暂无项目";
        patch.projectAddr = data.projectAddr || this.data.projectAddr || "请选择项目";
      }
      this.setData(patch);
    } catch (e) {
      this.setData({
        homeError: (e && e.msg) || "首页统计加载失败",
        monthPlan: { done: 0, total: 0 },
        deviceStats: { normal: 0, abnormal: 0 }
      });
    }
  }
});
