const { api } = require("../../api/index.js");
const { BASE_URL } = require("../../services/request.js");

function fullImageUrl(url) {
  if (!url) return "";
  return /^https?:\/\//i.test(url) ? url : BASE_URL + (url.charAt(0) === "/" ? url : "/" + url);
}

Page({
  data: {
    detail: null,
    buildingId: null,
    autoFireText: "-",
    imageUrl: ""
  },

  onLoad(options) {
    if (options && options.id) {
      this.setData({ buildingId: options.id });
      this.loadDetail(options.id);
    }
  },

  onShow() {
    if (this.data.buildingId) {
      this.loadDetail(this.data.buildingId);
    }
  },

  async loadDetail(id) {
    try {
      const res = await api.getBuildingDetail(id);
      const detail = res.data || {};
      const auto = String(detail.autoFireSystem == null ? "" : detail.autoFireSystem);
      let autoFireText = "-";
      if (auto === "1") autoFireText = "有";
      else if (auto === "0") autoFireText = "无";
      this.setData({
        detail: detail,
        autoFireText: autoFireText,
        imageUrl: fullImageUrl(detail.image)
      });
    } catch (e) {
      wx.showToast({ title: "加载失败", icon: "none" });
    }
  },

  previewImage() {
    if (!this.data.imageUrl) return;
    wx.previewImage({ urls: [this.data.imageUrl], current: this.data.imageUrl });
  },

  goEdit() {
    wx.navigateTo({ url: "/pages/building/form?id=" + this.data.buildingId });
  }
});
