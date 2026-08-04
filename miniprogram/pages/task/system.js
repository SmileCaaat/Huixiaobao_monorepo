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
    taskInfo: null,
    customerAddress: "",
    loading: false,
    marking: false
  },

  onLoad(options) {
    const taskId = options.taskId;
    const categoryKey = options.categoryKey ? decodeURIComponent(options.categoryKey) : "";
    if (!taskId || !categoryKey) {
      wx.showToast({ title: "\u53c2\u6570\u7f3a\u5931", icon: "none" });
      return;
    }
    this.setData({ taskId, categoryKey });
    wx.setNavigationBarTitle({ title: "\u7cfb\u7edf\u8bbe\u5907" });
    this.loadSystem();
  },

  async loadSystem() {
    this.setData({ loading: true });
    try {
      const results = await Promise.all([
        api.getInspectionTestSystem(this.data.taskId, this.data.categoryKey),
        api.getInspectionTestDetail(this.data.taskId)
      ]);
      const res = results[0];
      const data = res.data || {};
      const detail = (results[1] && results[1].data) || {};
      const taskInfo = detail.taskInfo || detail.task || null;
      let customerAddress = "";
      if (taskInfo && taskInfo.companyId) {
        try {
          const companyRes = await api.getCompanyDetail(taskInfo.companyId);
          customerAddress = (companyRes.data && companyRes.data.address) || "";
        } catch (ignore) {}
      }
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
        equipments,
        taskInfo,
        customerAddress
      });
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },

  onMarkAllNormal() {
    if (this.data.marking) return;
    wx.showModal({
      title: "\u662f\u5426\u5168\u90e8\u6b63\u5e38\uff1f",
      content: "\u8bbe\u7f6e\u7684\u9879\u4e0d\u5305\u62ec\u6d4b\u8bd5\u548c\u5df2\u4fdd\u5b58\u8fc7\u7684\u9879",
      confirmText: "\u786e\u5b9a",
      cancelText: "\u53d6\u6d88",
      confirmColor: "#1565C0",
      success: (res) => {
        if (res.confirm) {
          this.doMarkAllNormal();
        }
      }
    });
  },

  async doMarkAllNormal() {
    if (this.data.marking) return;
    this.setData({ marking: true });
    wx.showLoading({ title: "\u5904\u7406\u4e2d" });
    try {
      const res = await api.markCategoryAllNormal(this.data.taskId, this.data.categoryKey);
      wx.showToast({ title: (res && res.msg) || "\u64cd\u4f5c\u6210\u529f", icon: "success" });
      this.loadSystem();
    } catch (e) {
      // toast handled by request
    } finally {
      wx.hideLoading();
      this.setData({ marking: false });
    }
  },

  goDevice(e) {
    const equipmentKey = e.currentTarget.dataset.key;
    if (!equipmentKey) return;
    const equipment = this.data.equipments.find((item) => item.equipmentKey === equipmentKey);
    if (equipment && (String(equipment.recordType) === "1" || equipment.recordTypeLabel === "\u6d4b\u8bd5")) {
      this.askWhetherEquipmentExists(equipment);
      return;
    }
    wx.navigateTo({
      url:
        "/pages/task/device?taskId=" +
        this.data.taskId +
        "&categoryKey=" +
        encodeURIComponent(this.data.categoryKey) +
        "&equipmentKey=" +
        encodeURIComponent(equipmentKey)
    });
  },

  askWhetherEquipmentExists(equipment) {
    wx.showActionSheet({
      itemList: ["\u6709", "\u65e0"],
      success: async (res) => {
        if (res.tapIndex === 1) {
          wx.showLoading({ title: "\u6b63\u5728\u66f4\u65b0" });
          try {
            await api.markInspectionTestNoDevice(this.data.taskId, this.data.categoryKey, equipment.equipmentKey);
            wx.showToast({ title: "\u5df2\u8bbe\u4e3a\u65e0\u6b64\u8bbe\u5907", icon: "success" });
            this.loadSystem();
          } finally {
            wx.hideLoading();
          }
          return;
        }
        const task = this.data.taskInfo || {};
        const params = {
          linked: "1",
          companyId: task.companyId || "",
          companyName: task.companyName || "",
          categoryKey: this.data.categoryKey,
          systemName: this.data.categoryName,
          equipmentKey: equipment.equipmentKey,
          equipmentName: equipment.equipmentName || equipment.displayName || "",
          customerAddress: this.data.customerAddress || ""
        };
        const query = Object.keys(params).map((key) => key + "=" + encodeURIComponent(params[key])).join("&");
        wx.navigateTo({ url: "/pages/inspection/form?" + query });
      }
    });
  }
});
