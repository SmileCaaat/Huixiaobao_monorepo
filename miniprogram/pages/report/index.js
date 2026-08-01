const { api } = require("../../api/index.js");

Page({
  data: {
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

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
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
      const res = await api.getReportList({
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize,
        reportName: this.data.keyword
      });
      const rows = res.rows || res.data || [];
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

  openReport(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    api.previewReport(id).catch(() => {});
  }
});
