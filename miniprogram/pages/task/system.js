const { api } = require("../../api/index.js");

function pickEquipments(data) {
  if (!data) return [];
  if (Array.isArray(data.equipments)) return data.equipments;
  if (Array.isArray(data.list)) return data.list;
  return [];
}

function mapEquipment(item) {
  const total = Number(item.totalItems) || 0;
  const done = Number(item.completedItems) || 0;
  const pending = Math.max(total - done, 0);
  const finished = total > 0 && pending === 0;
  return Object.assign({}, item, {
    displayName: item.equipmentName || item.name || "-",
    typeLabel: item.recordTypeLabel || (String(item.recordType) === "1" ? "测试" : "巡查"),
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
    categoryKey: "",
    categoryName: "",
    equipments: [],
    taskInfo: null,
    customerAddress: "",
    loading: false,
    marking: false,
    showDeviceSheet: false,
    pendingEquipment: null
  },

  onLoad(options) {
    const taskId = options.taskId;
    const categoryKey = options.categoryKey ? decodeURIComponent(options.categoryKey) : "";
    if (!taskId || !categoryKey) {
      wx.showToast({ title: "参数缺失", icon: "none" });
      return;
    }
    this.setData({ taskId, categoryKey });
  },

  onShow() {
    if (this.data.taskId && this.data.categoryKey) {
      this.loadSystem();
    }
  },

  async loadSystem() {
    this.setData({ loading: true });
    try {
      const results = await Promise.all([
        api.getInspectionTestSystem(this.data.taskId, this.data.categoryKey),
        api.getInspectionTestDetail(this.data.taskId)
      ]);
      const data = (results[0] && results[0].data) || {};
      const detail = (results[1] && results[1].data) || {};
      const taskInfo = detail.taskInfo || detail.task || null;
      let customerAddress = "";
      if (taskInfo && taskInfo.companyId) {
        try {
          const companyRes = await api.getCompanyDetail(taskInfo.companyId);
          customerAddress = (companyRes.data && companyRes.data.address) || "";
        } catch (ignore) {}
      }
      this.setData({
        categoryName: data.categoryName || "",
        equipments: pickEquipments(data).map(mapEquipment),
        taskInfo,
        customerAddress
      });
    } catch (e) {
      wx.showToast({ title: "加载失败", icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },

  onMarkAllNormal() {
    if (this.data.marking) return;
    wx.showModal({
      title: "是否全部正常？",
      content: "设置的项不包括测试和已保存过的项",
      confirmText: "确定",
      cancelText: "取消",
      confirmColor: "#E53935",
      success: (res) => {
        if (res.confirm) this.doMarkAllNormal();
      }
    });
  },

  async doMarkAllNormal() {
    if (this.data.marking) return;
    this.setData({ marking: true });
    wx.showLoading({ title: "处理中" });
    try {
      const res = await api.markCategoryAllNormal(this.data.taskId, this.data.categoryKey);
      wx.showToast({ title: (res && res.msg) || "操作成功", icon: "success" });
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
    if (equipment && (String(equipment.recordType) === "1" || equipment.typeLabel === "测试")) {
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
    this.setData({
      showDeviceSheet: true,
      pendingEquipment: equipment || null
    });
  },

  closeDeviceSheet() {
    this.setData({ showDeviceSheet: false, pendingEquipment: null });
  },

  preventMove() {},

  async onDeviceSheetSelect(e) {
    const value = e.currentTarget.dataset.value;
    const equipment = this.data.pendingEquipment;
    this.closeDeviceSheet();
    if (!equipment || !value) return;

    if (value === "none") {
      wx.showLoading({ title: "正在更新" });
      try {
        await api.markInspectionTestNoDevice(
          this.data.taskId,
          this.data.categoryKey,
          equipment.equipmentKey
        );
        wx.showToast({ title: "已设为无此设备", icon: "success" });
        this.loadSystem();
      } finally {
        wx.hideLoading();
      }
      return;
    }

    const task = this.data.taskInfo || {};
    const params = {
      linked: "1",
      taskId: this.data.taskId,
      companyId: task.companyId || "",
      companyName: task.companyName || "",
      categoryKey: this.data.categoryKey,
      systemName: this.data.categoryName,
      equipmentKey: equipment.equipmentKey,
      equipmentName: equipment.equipmentName || equipment.displayName || "",
      customerAddress: this.data.customerAddress || ""
    };
    const query = Object.keys(params)
      .map((key) => key + "=" + encodeURIComponent(params[key]))
      .join("&");
    wx.navigateTo({ url: "/pages/inspection/form?" + query });
  }
});
