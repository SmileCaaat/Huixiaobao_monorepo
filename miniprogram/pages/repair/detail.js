const { api } = require("../../api/index.js");
const request = require("../../services/request.js");
const auth = require("../../services/auth.js");

const STATUS_MAP = {
  "0": { text: "\u5f85\u5904\u7406", cls: "badge-muted" },
  "1": { text: "\u5904\u7406\u4e2d", cls: "badge-warn" },
  "2": { text: "\u5df2\u5b8c\u6210", cls: "badge-ok" }
};

const URGENCY_MAP = {
  "0": "\u4e00\u822c",
  "1": "\u7d27\u6025",
  "2": "\u7279\u6025"
};

Page({
  data: {
    repairId: null,
    detail: null,
    statusText: "",
    statusClass: "badge-muted",
    urgencyText: "",
    faultImages: [],
    canStart: false,
    canComplete: false,
    repairDescription: "",
    completeImages: [],
    submitting: false
  },

  onLoad(options) {
    const repairId = options.id || options.repairId;
    if (!repairId) {
      wx.showToast({ title: "\u53c2\u6570\u7f3a\u5931", icon: "none" });
      return;
    }
    this.setData({ repairId });
    this.loadDetail();
  },

  async loadDetail() {
    try {
      const res = await api.getRepairDetail(this.data.repairId);
      const detail = res.data || {};
      const st = STATUS_MAP[String(detail.repairStatus)] || STATUS_MAP["0"];
      const faultImages = detail.faultImages
        ? detail.faultImages.split(",").filter(Boolean)
        : [];
      this.setData({
        detail,
        statusText: st.text,
        statusClass: st.cls,
        urgencyText: URGENCY_MAP[String(detail.urgencyLevel)] || "-",
        faultImages,
        canStart: String(detail.repairStatus) === "0",
        canComplete: String(detail.repairStatus) === "1"
      });
    } catch (e) {
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    }
  },

  previewFaultImage(e) {
    wx.previewImage({ urls: this.data.faultImages, current: this.data.faultImages[e.currentTarget.dataset.index] });
  },

  async handleStart() {
    try {
      await api.startRepair(this.data.repairId);
      wx.showToast({ title: "\u5df2\u5f00\u59cb\u5904\u7406", icon: "success" });
      this.loadDetail();
    } catch (e) {
      // backend enforces
    }
  },

  onRepairDescInput(e) {
    this.setData({ repairDescription: e.detail.value });
  },

  chooseCompleteImage() {
    const remain = 4 - this.data.completeImages.length;
    if (remain <= 0) return;
    wx.chooseImage({
      count: remain,
      sizeType: ["compressed"],
      sourceType: ["album", "camera"],
      success: (res) => {
        (res.tempFilePaths || []).forEach((p) => this.uploadCompleteImg(p));
      }
    });
  },

  uploadCompleteImg(tempPath) {
    const completeImages = this.data.completeImages.concat([{ tempPath, serverUrl: "", uploading: true }]);
    const index = completeImages.length - 1;
    this.setData({ completeImages });
    const token = auth.getToken();
    wx.uploadFile({
      url: request.BASE_URL + "/api/common/upload",
      filePath: tempPath,
      name: "file",
      header: token ? { Authorization: "Bearer " + token } : {},
      success: (uploadRes) => {
        try {
          const body = JSON.parse(uploadRes.data);
          const url = body.url || body.fileName || (body.data && body.data.url);
          if (url) {
            this.setData({
              ["completeImages[" + index + "].serverUrl"]: url,
              ["completeImages[" + index + "].uploading"]: false
            });
          } else {
            this.removeCompleteImage(index);
          }
        } catch (err) {
          this.removeCompleteImage(index);
        }
      },
      fail: () => this.removeCompleteImage(index)
    });
  },

  removeCompleteImage(index) {
    const completeImages = this.data.completeImages.slice();
    completeImages.splice(index, 1);
    this.setData({ completeImages });
  },

  async handleComplete() {
    if (this.data.submitting) return;
    const repairDescription = (this.data.repairDescription || "").trim();
    if (!repairDescription) {
      wx.showToast({ title: "\u8bf7\u586b\u5199\u5904\u7406\u8bf4\u660e", icon: "none" });
      return;
    }
    if (this.data.completeImages.some((img) => img.uploading)) {
      wx.showToast({ title: "\u56fe\u7247\u4e0a\u4f20\u4e2d", icon: "none" });
      return;
    }
    this.setData({ submitting: true });
    try {
      const repairImages = this.data.completeImages.map((i) => i.serverUrl).filter(Boolean).join(",");
      await api.completeRepair({
        repairId: this.data.repairId,
        repairDescription,
        repairImages
      });
      wx.showToast({ title: "\u5df2\u5b8c\u6210", icon: "success" });
      this.loadDetail();
    } catch (e) {
      // backend enforces
    } finally {
      this.setData({ submitting: false });
    }
  }
});
