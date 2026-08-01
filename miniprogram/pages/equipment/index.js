const { api } = require("../../api/index.js");

Page({
  data: {
    categories: []
  },

  onShow() {
    this.loadCategories();
  },

  async loadCategories() {
    try {
      const res = await api.getDictEquipmentCategories();
      let rows = res.data || [];
      if (!rows.length) {
        rows = [{ label: "\u5168\u90e8\u8bbe\u5907", value: "" }];
      } else {
        rows = rows.map((item) => {
          if (typeof item === "string") return { label: item, value: item };
          return {
            label: item.label || item.name || item.categoryName || "",
            value: item.value || item.key || item.label || item.name || ""
          };
        });
      }
      this.setData({ categories: rows });
    } catch (e) {
      this.setData({
        categories: [{ label: "\u5168\u90e8\u8bbe\u5907", value: "" }]
      });
    }
  },

  goList(e) {
    const category = e.currentTarget.dataset.category || "";
    wx.navigateTo({ url: "/pages/equipment/list?category=" + encodeURIComponent(category) });
  }
});
