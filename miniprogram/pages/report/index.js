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
    this.setData({ keyword: e.detail.value || "" });
  },

  handleSearch() {
    const keyword = String(this.data.keyword || "").trim();
    this.setData({ keyword });
    this.refresh();
  },

  async refresh() {
    this._listSeq = (this._listSeq || 0) + 1;
    const seq = this._listSeq;
    this.setData({ pageNum: 1, noMore: false, list: [], loading: false });
    await this.loadList(seq);
  },

  async loadList(seq) {
    if (this.data.loading) return;
    if (this.data.noMore && this.data.pageNum > 1) return;
    const requestSeq = seq || this._listSeq || 0;
    this.setData({ loading: true });
    try {
      const keyword = String(this.data.keyword || "").trim();
      const res = await api.getReportList(
        {
          pageNum: this.data.pageNum,
          pageSize: this.data.pageSize,
          reportName: keyword
        },
        { showError: false }
      );
      if (requestSeq !== this._listSeq) return;
      const rows = (res.rows || res.data || []).map(mapReportRow);
      this.setData({
        list: this.data.pageNum === 1 ? rows : this.data.list.concat(rows),
        noMore: rows.length < this.data.pageSize
      });
    } catch (e) {
      if (requestSeq !== this._listSeq) return;
      const msg =
        (e && (e.msg || e.errMsg || e.message)) ||
        "加载失败";
      wx.showToast({
        title: String(msg).indexOf("timeout") >= 0 ? "请求超时" : "加载失败",
        icon: "none"
      });
    } finally {
      if (requestSeq === this._listSeq) {
        this.setData({ loading: false });
      }
    }
  },

  loadMore() {
    if (this.data.noMore || this.data.loading) return;
    this.setData({ pageNum: this.data.pageNum + 1 });
    this.loadList(this._listSeq);
  },

  openReport(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({
      url: "/pages/report/preview?id=" + encodeURIComponent(id)
    });
  }
});
