const { api } = require("../../api/index.js");
const { uploadFile } = require("../../services/upload.js");
const { BASE_URL } = require("../../services/request.js");

function splitImages(value) {
  if (!value) return [];
  return String(value)
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

function fullImageUrl(url) {
  if (!url) return "";
  return /^https?:\/\//i.test(url) ? url : BASE_URL + (url.charAt(0) === "/" ? url : "/" + url);
}

Page({
  data: {
    taskId: null,
    maintenanceSummary: "",
    patrolSummaryRemark: "",
    testSummaryRemark: "",
    upkeepSummaryRemark: "",
    otherPatrolContent: "",
    otherTestContent: "",
    images: [],
    saving: false,
    previousCache: null
  },

  onLoad(options) {
    const taskId = options.taskId || options.id;
    if (!taskId) {
      wx.showToast({ title: "\u4efb\u52a1\u4e0d\u5b58\u5728", icon: "none" });
      return;
    }
    this.setData({ taskId });
    this.loadConclusion();
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    if (!field) return;
    this.setData({ [field]: e.detail.value });
  },

  async loadConclusion() {
    try {
      const res = await api.getTaskConclusion(this.data.taskId);
      const data = res.data || {};
      const images = splitImages(data.alarmHostVoucher).map((url) => ({
        serverUrl: url,
        previewUrl: fullImageUrl(url)
      }));
      this.setData({
        maintenanceSummary: data.maintenanceSummary || "",
        patrolSummaryRemark: data.patrolSummaryRemark || "",
        testSummaryRemark: data.testSummaryRemark || "",
        upkeepSummaryRemark: data.upkeepSummaryRemark || "",
        otherPatrolContent: data.otherPatrolContent || "",
        otherTestContent: data.otherTestContent || "",
        images
      });
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    }
  },

  async loadPrevious() {
    if (this.data.previousCache) {
      return this.data.previousCache;
    }
    const res = await api.getPreviousTaskConclusion(this.data.taskId);
    const data = res.data || {};
    this.setData({ previousCache: data });
    return data;
  },

  async quoteField(field, emptyTip) {
    try {
      const data = await this.loadPrevious();
      const value = data[field] || "";
      if (!value) {
        wx.showToast({ title: emptyTip || "\u4e0a\u6708\u8be5\u5b57\u6bb5\u4e3a\u7a7a", icon: "none" });
        return;
      }
      this.setData({ [field]: value });
      wx.showToast({ title: "\u5df2\u5f15\u7528", icon: "success" });
    } catch (e) {
      wx.showToast({ title: (e && e.msg) || "\u6682\u65e0\u4e0a\u6708\u53ef\u5f15\u7528\u5185\u5bb9", icon: "none" });
    }
  },

  quoteSummary() {
    this.quoteField("maintenanceSummary");
  },

  quotePatrol() {
    this.quoteField("otherPatrolContent", "\u4e0a\u6708\u5de1\u67e5\u5185\u5bb9\u4e3a\u7a7a");
  },

  quoteTest() {
    this.quoteField("otherTestContent", "\u4e0a\u6708\u6d4b\u8bd5\u5185\u5bb9\u4e3a\u7a7a");
  },

  chooseImage() {
    const remain = 5 - this.data.images.length;
    if (remain <= 0) return;
    wx.chooseImage({
      count: remain,
      sizeType: ["compressed"],
      sourceType: ["album", "camera"],
      success: (res) => {
        (res.tempFilePaths || []).forEach((p) => this.uploadImg(p));
      }
    });
  },

  async uploadImg(tempPath) {
    const images = this.data.images.concat([{ tempPath, serverUrl: "", previewUrl: tempPath, uploading: true }]);
    this.setData({ images });
    const index = images.length - 1;
    try {
      const body = await uploadFile(tempPath);
      const serverUrl = body.fileName || body.url || (body.data && (body.data.fileName || body.data.url));
      if (!serverUrl) {
        throw new Error("empty url");
      }
      this.setData({
        ["images[" + index + "].serverUrl"]: serverUrl,
        ["images[" + index + "].previewUrl"]: fullImageUrl(serverUrl),
        ["images[" + index + "].uploading"]: false
      });
    } catch (e) {
      const next = this.data.images.slice();
      next.splice(index, 1);
      this.setData({ images: next });
    }
  },

  removeImage(e) {
    const index = e.currentTarget.dataset.index;
    const images = this.data.images.slice();
    images.splice(index, 1);
    this.setData({ images });
  },

  previewImage(e) {
    const index = e.currentTarget.dataset.index;
    const urls = this.data.images.map((item) => item.previewUrl || item.tempPath || item.serverUrl).filter(Boolean);
    wx.previewImage({ current: urls[index], urls });
  },

  async handleSave() {
    if (this.data.saving) return;
    const summary = (this.data.maintenanceSummary || "").trim();
    if (!summary) {
      wx.showToast({ title: "\u8bf7\u586b\u5199\u7ef4\u4fdd\u60c5\u51b5\u7b80\u8ff0", icon: "none" });
      return;
    }
    if (this.data.images.some((img) => img.uploading)) {
      wx.showToast({ title: "\u56fe\u7247\u4e0a\u4f20\u4e2d", icon: "none" });
      return;
    }
    this.setData({ saving: true });
    try {
      await api.saveTaskConclusion({
        taskId: this.data.taskId,
        maintenanceSummary: summary,
        patrolSummaryRemark: this.data.patrolSummaryRemark || "",
        testSummaryRemark: this.data.testSummaryRemark || "",
        upkeepSummaryRemark: this.data.upkeepSummaryRemark || "",
        otherPatrolContent: this.data.otherPatrolContent || "",
        otherTestContent: this.data.otherTestContent || "",
        alarmHostVoucher: this.data.images.map((img) => img.serverUrl).filter(Boolean).join(",")
      });
      wx.showToast({ title: "\u4fdd\u5b58\u6210\u529f", icon: "success" });
      setTimeout(() => wx.navigateBack({ delta: 1 }), 500);
    } catch (e) {
      wx.showToast({ title: (e && e.msg) || "\u4fdd\u5b58\u5931\u8d25", icon: "none" });
    } finally {
      this.setData({ saving: false });
    }
  }
});
