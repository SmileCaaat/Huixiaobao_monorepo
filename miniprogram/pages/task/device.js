const { api } = require("../../api/index.js");

const RESULT_MAP = {
  "0": "\u5f85\u68c0\u67e5",
  "1": "\u6b63\u5e38",
  "2": "\u6545\u969c",
  "3": "\u65e0\u6b64\u8bbe\u5907"
};

function pickCheckItems(data) {
  if (!data) return [];
  if (Array.isArray(data.checkItems)) return data.checkItems;
  if (Array.isArray(data.list)) return data.list;
  return [];
}

Page({
  data: {
    taskId: null,
    categoryKey: "",
    equipmentKey: "",
    equipmentName: "",
    checkItems: [],
    loading: false
  },

  onLoad(options) {
    const taskId = options.taskId;
    const categoryKey = options.categoryKey ? decodeURIComponent(options.categoryKey) : "";
    const equipmentKey = options.equipmentKey ? decodeURIComponent(options.equipmentKey) : "";
    if (!taskId || !categoryKey || !equipmentKey) {
      wx.showToast({ title: "\u53c2\u6570\u7f3a\u5931", icon: "none" });
      return;
    }
    this.setData({ taskId, categoryKey, equipmentKey });
    wx.setNavigationBarTitle({ title: "\u68c0\u67e5\u9879" });
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
      const checkItems = pickCheckItems(data).map((item) => {
        const code = String(item.checkResult != null ? item.checkResult : "0");
        return Object.assign({}, item, {
          displayName: item.checkItemName || item.itemName || item.name || "-",
          resultText: RESULT_MAP[code] || RESULT_MAP["0"]
        });
      });
      this.setData({
        equipmentName: data.equipmentName || "",
        checkItems
      });
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },

  async setResult(e) {
    const recordId = e.currentTarget.dataset.id;
    const checkResult = e.currentTarget.dataset.result;
    if (!recordId) return;
    try {
      await api.updateCheckResult({
        taskId: this.data.taskId,
        recordId,
        checkResult
      });
      if (checkResult === "2") {
        this.promptFaultDesc(recordId);
      } else {
        wx.showToast({ title: "\u5df2\u66f4\u65b0", icon: "success" });
        this.loadEquipment();
      }
    } catch (err) {
      // toast handled by request
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
      title: "\u6545\u969c\u63cf\u8ff0",
      editable: true,
      placeholderText: "\u8bf7\u8f93\u5165\u6545\u969c\u63cf\u8ff0",
      success: async (res) => {
        if (!res.confirm) {
          this.loadEquipment();
          return;
        }
        const faultDescription = (res.content || "").trim();
        if (!faultDescription) {
          wx.showToast({ title: "\u8bf7\u586b\u5199\u6545\u969c\u63cf\u8ff0", icon: "none" });
          return;
        }
        try {
          await api.updateFaultDesc({
            taskId: this.data.taskId,
            recordId,
            checkResult: "2",
            faultDescription
          });
          wx.showToast({ title: "\u5df2\u4fdd\u5b58", icon: "success" });
          this.loadEquipment();
        } catch (err) {
          // toast handled by request
        }
      }
    });
  }
});
