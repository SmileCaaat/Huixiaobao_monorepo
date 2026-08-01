const { api } = require("../../api/index.js");

const STATUS_MAP = {
  "0": { text: "\u5f85\u5904\u7406", cls: "badge-muted" },
  "1": { text: "\u5904\u7406\u4e2d", cls: "badge-warn" },
  "2": { text: "\u5df2\u5b8c\u6210", cls: "badge-ok" }
};

const URGENCY_MAP = {
  "0": "\u4e00\u822c",
  "1": "\u7d27\u6025",
  "2": "\u7279\u6025"
};

Page({
  data: {
    activeTab: "assigned",
    list: [],
    stats: null,
    loading: false,
    noMore: false,
    pageNum: 1,
    pageSize: 10
  },

  onShow() {
    this.loadStats();
    this.refresh();
  },

  onPullDownRefresh() {
    Promise.all([this.loadStats(), this.refresh()]).finally(() => wx.stopPullDownRefresh());
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (tab === this.data.activeTab) return;
    this.setData({ activeTab: tab });
    this.refresh();
  },

  async loadStats() {
    try {
      const res = await api.getRepairStats();
      this.setData({ stats: res.data || null });
    } catch (e) {
      // optional header counts
    }
  },

  async refresh() {
    this.setData({ pageNum: 1, noMore: false, list: [] });
    await this.loadList();
  },

  async loadList() {
    if (this.data.loading || this.data.noMore) return;
    this.setData({ loading: true });
    try {
      const params = { pageNum: this.data.pageNum, pageSize: this.data.pageSize };
      const fetcher =
        this.data.activeTab === "assigned"
          ? api.getMyAssignedRepairList
          : api.getMyReportedRepairList;
      const res = await fetcher(params);
      const rows = (res.rows || res.data || []).map((item) => {
        const st = STATUS_MAP[String(item.repairStatus)] || STATUS_MAP["0"];
        const desc = item.faultDescription || "";
        return Object.assign({}, item, {
          statusText: st.text,
          statusClass: st.cls,
          urgencyText: URGENCY_MAP[String(item.urgencyLevel)] || "-",
          descSnippet: desc.length > 40 ? desc.slice(0, 40) + "..." : desc
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
    wx.navigateTo({ url: "/pages/repair/detail?id=" + e.currentTarget.dataset.id });
  },

  goForm() {
    wx.navigateTo({ url: "/pages/repair/form" });
  }
});
