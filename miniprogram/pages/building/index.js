const { api } = require("../../api/index.js");

function displayNum(value, suffix) {
  if (value === null || value === undefined || value === "") return "-";
  return String(value) + (suffix || "");
}

function mapBuildingRow(row) {
  return Object.assign({}, row, {
    heightText: displayNum(row.buildingHeight, "m"),
    floorText: displayNum(row.floorCount, "\u5c42"),
    aboveText: displayNum(row.aboveGroundFloors, "\u5c42"),
    underText: displayNum(row.undergroundFloors, "\u5c42"),
    landAreaText: displayNum(row.landArea, "m\u00b2"),
    areaText: displayNum(row.area, "m\u00b2")
  });
}

Page({
  data: {
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

  async ensureCompany() {
    if (this.data.companyId) return this.data.companyId;
    const res = await api.getCurrentCompany();
    const companyId = res && res.data ? res.data.companyId : null;
    if (!companyId) {
      wx.showToast({ title: "\u8bf7\u5148\u9009\u62e9\u516c\u53f8", icon: "none" });
    }
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
      if (!companyId) return;
      const res = await api.getBuildingList({
        companyId,
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize
      });
      const rows = (res.rows || res.data || []).map(mapBuildingRow);
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
    wx.navigateTo({ url: "/pages/building/detail?id=" + e.currentTarget.dataset.id });
  },

  goForm() {
    wx.navigateTo({ url: "/pages/building/form" });
  }
});
