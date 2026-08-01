const { api } = require("../../api/index.js");

Page({
  data: {
    category: "",
    list: [],
    loading: false,
    noMore: false,
    pageNum: 1,
    pageSize: 10,
    companyId: null
  },

  onLoad(options) {
    const category = options && options.category ? decodeURIComponent(options.category) : "";
    this.setData({ category });
    const title = category || "\u8bbe\u5907\u5217\u8868";
    wx.setNavigationBarTitle({ title });
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
      const payload = {
        companyId,
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize
      };
      if (this.data.category) payload.projectCategory = this.data.category;
      const res = await api.getEquipmentList(payload);
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

  goDetail(e) {
    wx.navigateTo({ url: "/pages/equipment/detail?id=" + e.currentTarget.dataset.id });
  },

  goForm() {
    const q = this.data.category ? "?category=" + encodeURIComponent(this.data.category) : "";
    wx.navigateTo({ url: "/pages/equipment/form" + q });
  }
});
