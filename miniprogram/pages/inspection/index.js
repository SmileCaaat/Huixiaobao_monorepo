const api = require("../../api/index.js").api;

const TYPE_MAP = {
  "0": "\u6d4b\u8bd5",
  "1": "\u5de1\u67e5",
  "2": "\u4fdd\u517b"
};

function formatDateText(value) {
  if (!value) return "";
  const text = String(value).replace("T", " ");
  return text.length >= 10 ? text.slice(0, 10) : text;
}

function mapInspectionRow(item) {
  const row = item || {};
  const statusFault = String(row.equipmentStatus) === "1";
  return Object.assign({}, row, {
    typeTag: TYPE_MAP[String(row.inspectionType)] || "",
    dateText: formatDateText(row.inspectionTime || row.createTime),
    statusText: statusFault ? "\u6545\u969c" : "\u6b63\u5e38",
    statusClass: statusFault ? "bad" : "ok"
  });
}

Page({
  data: {
    keyword: "",
    selectedDate: "",
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

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onDateChange(e) {
    this.setData({ selectedDate: e.detail.value });
    this.refresh();
  },

  handleSearch() {
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
      const res = await api.getInspectionList({
        companyId,
        keyword: this.data.keyword,
        inspectionDate: this.data.selectedDate,
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize
      });
      const rows = (res.rows || res.data || []).map(mapInspectionRow);
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
    wx.navigateTo({ url: "/pages/inspection/detail?id=" + e.currentTarget.dataset.id });
  },

  goAdd() {
    wx.navigateTo({ url: "/pages/inspection/form" });
  }
});
