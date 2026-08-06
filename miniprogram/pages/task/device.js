const { api } = require("../../api/index.js");

const RESULT_MAP = {
  "0": "执行中",
  "1": "正常",
  "2": "故障",
  "3": "无此设备"
};

function pickCheckItems(data) {
  if (!data) return [];
  if (Array.isArray(data.checkItems)) return data.checkItems;
  if (Array.isArray(data.list)) return data.list;
  return [];
}

function mapItem(item) {
  const code = String(item.checkResult != null ? item.checkResult : "0");
  return Object.assign({}, item, {
    displayName: item.checkItemName || item.itemName || item.name || "-",
    checkResult: code,
    statusText: RESULT_MAP[code] || RESULT_MAP["0"],
    isPending: code === "0"
  });
}

Page({
  data: {
    taskId: null,
    categoryKey: "",
    equipmentKey: "",
    equipmentName: "",
    checkItems: [],
    loading: false,
    marking: false,
    saving: false
  },

  onLoad(options) {
    const taskId = options.taskId;
    const categoryKey = options.categoryKey ? decodeURIComponent(options.categoryKey) : "";
    const equipmentKey = options.equipmentKey ? decodeURIComponent(options.equipmentKey) : "";
    if (!taskId || !categoryKey || !equipmentKey) {
      wx.showToast({ title: "参数缺失", icon: "none" });
      return;
    }
    this.setData({ taskId, categoryKey, equipmentKey });
  },

  onShow() {
    if (this.data.taskId && this.data.categoryKey && this.data.equipmentKey) {
      this.loadEquipment();
    }
  },

  async loadEquipment() {
    this.setData({ loading: true });
    try {
      const res = await api.getInspectionTestEquipment(
        this.data.taskId,
        this.data.categoryKey,
        this.data.equipmentKey
      );
      const data = res.data || {};
      this.setData({
        equipmentName: data.equipmentName || "",
        checkItems: pickCheckItems(data).map(mapItem)
      });
    } catch (e) {
      wx.showToast({ title: "加载失败", icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },

  async setResult(e) {
    const recordId = e.currentTarget.dataset.id;
    const checkResult = String(e.currentTarget.dataset.result || "");
    if (!recordId || !checkResult) return;
    try {
      await api.updateCheckResult({
        taskId: this.data.taskId,
        recordId,
        checkResult
      });
      if (checkResult === "2") {
        this.promptFaultDesc(recordId);
      } else {
        wx.showToast({ title: "已更新", icon: "success" });
        this.loadEquipment();
      }
    } catch (err) {
      // toast handled by request
    }
  },

  onMarkAllNormal() {
    if (this.data.marking) return;
    const pending = this.data.checkItems.filter((item) => String(item.checkResult) === "0");
    if (!pending.length) {
      wx.showToast({ title: "没有可设置的项", icon: "none" });
      return;
    }
    wx.showModal({
      title: "是否全部正常？",
      content: "将未检查项设为正常（已保存项不改动）",
      confirmText: "确定",
      cancelText: "取消",
      confirmColor: "#E53935",
      success: (res) => {
        if (res.confirm) this.doMarkAllNormal(pending);
      }
    });
  },

  async doMarkAllNormal(pending) {
    if (this.data.marking) return;
    this.setData({ marking: true });
    wx.showLoading({ title: "处理中" });
    try {
      for (const item of pending) {
        await api.updateCheckResult({
          taskId: this.data.taskId,
          recordId: item.recordId,
          checkResult: "1"
        });
      }
      wx.showToast({ title: "已全部正常", icon: "success" });
      this.loadEquipment();
    } catch (e) {
      // toast handled by request
    } finally {
      wx.hideLoading();
      this.setData({ marking: false });
    }
  },

  openCheckDetail(e) {
    const recordId = e.currentTarget.dataset.id;
    if (!recordId) return;
    wx.navigateTo({
      url:
        "/pages/task/check-detail?taskId=" +
        this.data.taskId +
        "&categoryKey=" +
        encodeURIComponent(this.data.categoryKey) +
        "&equipmentKey=" +
        encodeURIComponent(this.data.equipmentKey) +
        "&recordId=" +
        recordId
    });
  },

  promptFaultDesc(recordId) {
    wx.showModal({
      title: "故障描述",
      editable: true,
      placeholderText: "请输入故障描述",
      success: async (res) => {
        if (!res.confirm) {
          this.loadEquipment();
          return;
        }
        const faultDescription = (res.content || "").trim();
        if (!faultDescription) {
          wx.showToast({ title: "请填写故障描述", icon: "none" });
          return;
        }
        try {
          await api.updateFaultDesc({
            taskId: this.data.taskId,
            recordId,
            checkResult: "2",
            faultDescription
          });
          wx.showToast({ title: "已保存", icon: "success" });
          this.loadEquipment();
        } catch (err) {
          // toast handled by request
        }
      }
    });
  },

  onSave() {
    if (this.data.saving) return;
    this.setData({ saving: true });
    wx.showToast({ title: "已保存", icon: "success" });
    setTimeout(() => {
      this.setData({ saving: false });
      wx.navigateBack();
    }, 500);
  }
});
