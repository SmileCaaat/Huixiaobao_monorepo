const { api } = require("../../api/index.js");

function formatDatePart(value) {
  if (!value) return "";
  const text = String(value).replace("T", " ");
  return text.length >= 10 ? text.slice(0, 10) : text;
}

function formatDateTime(value) {
  if (!value) return "-";
  const text = String(value).replace("T", " ");
  return text.length >= 19 ? text.slice(0, 19) : text;
}

function formatPlanTime(start, end) {
  const s = formatDatePart(start);
  const e = formatDatePart(end);
  if (s && e) return s + " 至 " + e;
  if (s) return s;
  if (e) return e;
  return "-";
}

function mapReportRow(item) {
  const row = item || {};
  return Object.assign({}, row, {
    taskTitle: row.taskName || row.reportName || "-",
    planTimeText: formatPlanTime(row.planStartTime, row.planEndTime),
    managerText: row.managerName || "-",
    operatorText: row.operatorNames || "-",
    createTimeText: formatDateTime(row.createTime)
  });
}

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
      const rows = (res.rows || res.data || []).map(mapReportRow);
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

  openReport(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    api.previewReport(id).catch(() => {});
  }
});
