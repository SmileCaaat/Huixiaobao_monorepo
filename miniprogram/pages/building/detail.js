const { api } = require("../../api/index.js");

Page({
  data: {
    detail: null,
    buildingId: null
  },

  onLoad(options) {
    if (options && options.id) {
      this.setData({ buildingId: options.id });
      this.loadDetail(options.id);
    }
  },

  async loadDetail(id) {
    try {
      const res = await api.getBuildingDetail(id);
      this.setData({ detail: res.data || {} });
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    }
  },

  goEdit() {
    wx.navigateTo({ url: "/pages/building/form?id=" + this.data.buildingId });
  }
});
