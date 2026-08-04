const { api } = require("../../api/index.js");
const { uploadFile } = require("../../services/upload.js");
const { BASE_URL } = require("../../services/request.js");

function pickCheckItems(data) {
  if (!data) return [];
  if (Array.isArray(data.checkItems)) return data.checkItems;
  if (Array.isArray(data.list)) return data.list;
  return [];
}

function splitImages(value) {
  if (!value) return [];
  return String(value).split(",").map((item) => item.trim()).filter(Boolean);
}

function fullImageUrl(url) {
  if (!url) return "";
  return /^https?:\/\//i.test(url) ? url : BASE_URL + (url.charAt(0) === "/" ? url : "/" + url);
}

Page({
  data: {
    taskId: "",
    categoryKey: "",
    equipmentKey: "",
    recordId: "",
    displayName: "检查项",
    checkResult: "0",
    intact: "",
    otherNotes: "",
    notesLength: 0,
    images: [],
    loading: false,
    saving: false
  },

  onLoad(options) {
    const taskId = options.taskId || "";
    const categoryKey = options.categoryKey ? decodeURIComponent(options.categoryKey) : "";
    const equipmentKey = options.equipmentKey ? decodeURIComponent(options.equipmentKey) : "";
    const recordId = options.recordId || "";
    if (!taskId || !categoryKey || !equipmentKey || !recordId) {
      wx.showToast({ title: "参数缺失", icon: "none" });
      return;
    }
    this.setData({ taskId, categoryKey, equipmentKey, recordId });
    this.loadDetail();
  },

  async loadDetail() {
    this.setData({ loading: true });
    try {
      const res = await api.getInspectionTestEquipment(
        this.data.taskId,
        this.data.categoryKey,
        this.data.equipmentKey
      );
      const rows = pickCheckItems(res.data || {});
      const item = rows.find((row) => String(row.recordId) === String(this.data.recordId));
      if (!item) {
        wx.showToast({ title: "未找到检查项", icon: "none" });
        return;
      }
      const result = String(item.checkResult == null ? "0" : item.checkResult);
      const rawImages = splitImages(item.faultImages);
      this.setData({
        displayName: item.checkItemName || item.itemName || item.name || "检查项",
        checkResult: result,
        intact: result === "1" ? "yes" : (result === "2" || result === "3" ? "no" : ""),
        otherNotes: item.otherNotes || "",
        notesLength: String(item.otherNotes || "").length,
        images: rawImages.map((url) => ({ serverUrl: url, previewUrl: fullImageUrl(url) }))
      });
    } catch (err) {
      wx.showToast({ title: "加载失败", icon: "none" });
    } finally {
      this.setData({ loading: false });
    }
  },

  goBack() {
    wx.navigateBack();
  },

  setIntact(e) {
    const intact = e.currentTarget.dataset.value;
    this.setData({ intact, checkResult: intact === "yes" ? "1" : "2" });
  },

  selectStatus(e) {
    const checkResult = String(e.currentTarget.dataset.value || "0");
    this.setData({ checkResult, intact: checkResult === "1" ? "yes" : "no" });
  },

  onNotesInput(e) {
    const value = e.detail.value || "";
    this.setData({ otherNotes: value, notesLength: value.length });
  },

  chooseAttachment() {
    const remaining = 5 - this.data.images.length;
    if (remaining <= 0) return;
    wx.chooseImage({
      count: remaining,
      sizeType: ["compressed"],
      sourceType: ["camera", "album"],
      success: async (res) => {
        const paths = res.tempFilePaths || [];
        wx.showLoading({ title: "上传中...", mask: true });
        const next = this.data.images.slice();
        try {
          for (const filePath of paths) {
            const body = await uploadFile(filePath);
            const serverUrl = body.fileName || body.url || (body.data && (body.data.fileName || body.data.url));
            if (serverUrl && next.length < 5) {
              next.push({ serverUrl, previewUrl: fullImageUrl(serverUrl) });
            }
          }
          this.setData({ images: next });
        } catch (err) {
          wx.showToast({ title: "部分附件上传失败", icon: "none" });
        } finally {
          wx.hideLoading();
        }
      }
    });
  },

  previewImage(e) {
    const index = Number(e.currentTarget.dataset.index || 0);
    const urls = this.data.images.map((item) => item.previewUrl);
    if (!urls.length) return;
    wx.previewImage({ current: urls[index], urls });
  },

  removeImage(e) {
    const index = Number(e.currentTarget.dataset.index);
    const images = this.data.images.slice();
    images.splice(index, 1);
    this.setData({ images });
  },

  async saveDetail() {
    if (this.data.saving || this.data.loading) return;
    if (this.data.checkResult === "0") {
      wx.showToast({ title: "请选择设备状态", icon: "none" });
      return;
    }
    this.setData({ saving: true });
    try {
      await api.updateCheckDetail({
        taskId: this.data.taskId,
        recordId: this.data.recordId,
        checkResult: this.data.checkResult,
        otherNotes: this.data.otherNotes,
        faultImages: this.data.images.map((item) => item.serverUrl).join(",")
      });
      wx.showToast({ title: "保存成功", icon: "success" });
      setTimeout(() => wx.navigateBack(), 500);
    } catch (err) {
      // request layer shows the server error
    } finally {
      this.setData({ saving: false });
    }
  }
});
