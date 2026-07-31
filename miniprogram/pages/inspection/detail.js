const api = require("../../api/index.js").api;

Page({
  data: {
    detail: null,
    images: [],
    typeText: ""
  },

  onLoad(options) {
    if (options && options.id) {
      this.loadDetail(options.id);
    }
  },

  async loadDetail(id) {
    try {
      const res = await api.getInspectionDetail(id);
      const detail = res.data || {};
      const typeMap = { "0": "测试", "1": "巡查", "2": "保养" };
      let images = detail.images || [];
      if ((!images || !images.length) && detail.imageUrls) {
        images = String(detail.imageUrls).split(",").map((s) => s.trim()).filter(Boolean);
      }
      this.setData({
        detail,
        images,
        typeText: typeMap[String(detail.inspectionType)] || "-"
      });
    } catch (e) {
      wx.showToast({ title: "加载失败", icon: "none" });
    }
  },

  preview(e) {
    wx.previewImage({
      current: e.currentTarget.dataset.url,
      urls: this.data.images
    });
  }
});
