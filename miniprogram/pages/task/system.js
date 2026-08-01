const { api } = require("../../api/index.js");

function pickEquipments(data) {
  if (!data) return [];
  if (Array.isArray(data.equipments)) return data.equipments;
  if (Array.isArray(data.list)) return data.list;
  return [];
}

Page({
  data: {
    taskId: null,
    categoryKey: "",
    categoryName: "",
    equipments: [],
    loading: false
  },

  onLoad(options) {
    const taskId = options.taskId;
    const categoryKey = options.categoryKey ? decodeURIComponent(options.categoryKey) : "";
    if (!taskId || !categoryKey) {
      wx.showToast({ title: "\u53c2\u6570\u7f3a\u5931", icon: "none" });
      return;
    }
    this.setData({ taskId, categoryKey });
    wx.setNavigationBarTitle({ title: "\u8bbe\u5907\u5217\u8868" });
    this.loadSystem();
  },

  async loadSystem() {
    this.setData({ loading: true });
    try {
      const res = await api.getInspectionTestSystem(this.data.taskId, this.data.categoryKey);
      const data = res.data || {};
      const equipments = pickEquipments(data).map((item) => {
        const total = item.totalItems || 0;
        const done = item.completedItems || 0;
        return Object.assign({}, item, {
          displayName: item.equipmentName || item.name || "-",
          progressText: total > 0 ? done + "/" + total : ""
        });
      });
      this.setData({
        categoryName: data.categoryName || "",
        equipments
      });
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },

  goDevice(e) {
    const equipmentKey = e.currentTarget.dataset.key;
    if (!equipmentKey) return;
    wx.navigateTo({
      url:
        "/pages/task/device?taskId=" +
        this.data.taskId +
        "&categoryKey=" +
        encodeURIComponent(this.data.categoryKey) +
        "&equipmentKey=" +
        encodeURIComponent(equipmentKey)
    });
  }
});
