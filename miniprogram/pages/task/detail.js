const { api } = require("../../api/index.js");

function pickCategories(data) {
  if (!data) return [];
  if (Array.isArray(data.categories)) return data.categories;
  if (Array.isArray(data.list)) return data.list;
  if (Array.isArray(data)) return data;
  return [];
}

Page({
  data: {
    taskId: null,
    taskInfo: null,
    categories: [],
    loading: false
  },

  onLoad(options) {
    const taskId = options.taskId || options.id;
    if (!taskId) {
      wx.showToast({ title: "\u4efb\u52a1\u4e0d\u5b58\u5728", icon: "none" });
      return;
    }
    this.setData({ taskId });
    wx.setNavigationBarTitle({ title: "\u4efb\u52a1\u8be6\u60c5" });
    this.loadDetail();
  },

  async loadDetail() {
    this.setData({ loading: true });
    try {
      const res = await api.getInspectionTestDetail(this.data.taskId);
      const data = res.data || {};
      const categories = pickCategories(data).map((item) => {
        const total = item.totalItems || 0;
        const done = item.completedItems || 0;
        const progress = total > 0 ? done + "/" + total : "";
        return Object.assign({}, item, {
          displayName: item.categoryName || item.name || item.itemName || "-",
          progressText: progress
        });
      });
      this.setData({
        taskInfo: data.taskInfo || data.task || null,
        categories
      });
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },

  goSystem(e) {
    const categoryKey = e.currentTarget.dataset.key;
    if (!categoryKey) return;
    wx.navigateTo({
      url:
        "/pages/task/system?taskId=" +
        this.data.taskId +
        "&categoryKey=" +
        encodeURIComponent(categoryKey)
    });
  }
});
