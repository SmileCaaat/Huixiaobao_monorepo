const { api } = require("../../api/index.js");

function pickCategories(data) {
  if (!data) return [];
  if (Array.isArray(data.categories)) return data.categories;
  if (Array.isArray(data.list)) return data.list;
  if (Array.isArray(data)) return data;
  return [];
}

function mapCategory(item) {
  const total = Number(item.totalItems) || 0;
  const done = Number(item.completedItems) || 0;
  const pending = Math.max(total - done, 0);
  const finished = total > 0 && pending === 0;
  return Object.assign({}, item, {
    displayName: item.categoryName || item.name || item.itemName || "-",
    totalItems: total,
    completedItems: done,
    pendingItems: pending,
    statusText: finished ? "已完成" : "未完成",
    statusClass: finished ? "done" : "todo"
  });
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
      wx.showToast({ title: "任务不存在", icon: "none" });
      return;
    }
    this.setData({ taskId });
  },

  onShow() {
    if (this.data.taskId) this.loadDetail();
  },

  async loadDetail() {
    this.setData({ loading: true });
    try {
      const res = await api.getInspectionTestDetail(this.data.taskId);
      const data = res.data || {};
      this.setData({
        taskInfo: data.taskInfo || data.task || null,
        categories: pickCategories(data).map(mapCategory)
      });
    } catch (e) {
      wx.showToast({ title: "加载失败", icon: "none" });
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
  },

  goConclusion() {
    if (!this.data.taskId) return;
    wx.navigateTo({
      url: "/pages/task/conclusion?taskId=" + this.data.taskId
    });
  }
});
