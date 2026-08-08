const { api } = require("../../api/index.js");

function formatDate(v) {
  if (!v) return "-";
  const s = String(v);
  return s.length >= 10 ? s.slice(0, 10) : s;
}

function mapRow(item) {
  const status = String(item.repairStatus);
  let statusText = "待处理";
  let statusTone = "muted";
  if (status === "1") {
    statusText = "维修中";
    statusTone = "warn";
  } else if (status === "2") {
    statusText = "已完成";
    statusTone = "ok";
  }
  const urgency = String(item.urgencyLevel == null ? "0" : item.urgencyLevel);
  const urgencyMap = { "0": "一般", "1": "紧急", "2": "特急" };
  const desc = item.faultDescription || "";
  return Object.assign({}, item, {
    statusText: statusText,
    statusTone: statusTone,
    urgencyText: urgencyMap[urgency] || "一般",
    urgencyClass: "u" + (urgencyMap[urgency] ? urgency : "0"),
    foundTimeText: formatDate(item.foundTime || item.createTime),
    completeTimeText: formatDate(item.completeTime),
    descSnippet: desc.length > 40 ? desc.slice(0, 40) + "..." : desc
  });
}

Page({
  data: {
    activeTab: "pending",
    keyword: "",
    list: [],
    loading: false,
    noMore: false,
    pageNum: 1,
    pageSize: 10
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
    this.setData({ activeTab: tab });
    this.refresh();
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value || "" });
  },

  handleSearch() {
    this.refresh();
  },

  async refresh() {
    this.setData({ pageNum: 1, noMore: false, list: [] });
    await this.loadList();
  },

  async loadList() {
    if (this.data.loading || this.data.noMore) return;
    this.setData({ loading: true });
    try {
      const keyword = (this.data.keyword || "").trim();
      const params = {
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize,
        params: {}
      };
      if (this.data.activeTab === "pending") {
        params.params.pendingOnly = true;
      } else {
        params.repairStatus = "2";
      }
      if (keyword) {
        params.params.keyword = keyword;
      }
      const res = await api.getMyRepairList(params);
      const rows = (res.rows || res.data || []).map(mapRow);
      this.setData({
        list: this.data.pageNum === 1 ? rows : this.data.list.concat(rows),
        noMore: rows.length < this.data.pageSize
      });
    } catch (e) {
      wx.showToast({ title: "加载失败", icon: "none" });
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
