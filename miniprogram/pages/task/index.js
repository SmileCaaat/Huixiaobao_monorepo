const { api } = require("../../api/index.js");

const STATUS_MAP = {
  "0": { text: "\u5f85\u5f00\u59cb", cls: "badge-muted" },
  "1": { text: "\u8fdb\u884c\u4e2d", cls: "badge-warn" },
  "2": { text: "\u5df2\u5b8c\u6210", cls: "badge-ok" },
  "3": { text: "\u5df2\u903e\u671f", cls: "badge-bad" }
};

Page({
  data: {
    activeTab: "periodic",
    taskType: "0",
    list: [],
    loading: false,
    noMore: false,
    pageNum: 1,
    pageSize: 10,
    companyId: null
  },

  onShow() {
    this.refresh();
  },

  onPullDownRefresh() {
    this.refresh().finally(() => wx.stopPullDownRefresh());
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (tab === this.data.activeTab) return;
    this.setData({
      activeTab: tab,
      taskType: tab === "periodic" ? "0" : "1"
    });
    this.refresh();
  },

  async ensureCompany() {
    if (this.data.companyId) return this.data.companyId;
    const res = await api.getCurrentCompany();
    const companyId = res && res.data ? res.data.companyId : null;
    this.setData({ companyId });
    return companyId;
  },

  async refresh() {
    this.setData({ pageNum: 1, noMore: false, list: [] });
    await this.loadList();
  },

  async loadList() {
    if (this.data.loading || this.data.noMore) return;
    this.setData({ loading: true });
    try {
      const companyId = await this.ensureCompany();
      if (!companyId) {
        wx.showToast({ title: "\u8bf7\u5148\u9009\u62e9\u516c\u53f8", icon: "none" });
        return;
      }
      const res = await api.getMyTaskList({
        companyId,
        taskType: this.data.taskType,
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize
      });
      const rows = (res.rows || (res.data && res.data.rows) || []).map((item) => {
        const st = STATUS_MAP[String(item.taskStatus)] || STATUS_MAP["0"];
        return Object.assign({}, item, {
          statusText: st.text,
          statusClass: st.cls
        });
      });
      this.setData({
        list: this.data.pageNum === 1 ? rows : this.data.list.concat(rows),
        noMore: rows.length < this.data.pageSize
      });
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },

  loadMore() {
    if (this.data.noMore || this.data.loading) return;
    this.setData({ pageNum: this.data.pageNum + 1 });
    this.loadList();
  },

  goDetail(e) {
    const taskId = e.currentTarget.dataset.id;
    wx.navigateTo({ url: "/pages/task/detail?taskId=" + taskId });
  }
});
