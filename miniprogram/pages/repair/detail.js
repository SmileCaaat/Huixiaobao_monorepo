const { api } = require("../../api/index.js");
const request = require("../../services/request.js");
const auth = require("../../services/auth.js");

const STATUS_MAP = {
  "0": { text: "待处理", cls: "badge-muted" },
  "1": { text: "处理中", cls: "badge-warn" },
  "2": { text: "已完成", cls: "badge-ok" }
};

const URGENCY_MAP = {
  "0": "一般",
  "1": "紧急",
  "2": "特急"
};

function formatDate(v) {
  if (!v) return "-";
  const s = String(v);
  return s.length >= 10 ? s.slice(0, 10) : s;
}

Page({
  data: {
    repairId: null,
    detail: null,
    activeTab: "info",
    statusText: "",
    statusClass: "badge-muted",
    urgencyText: "",
    reporterText: "",
    handlerText: "",
    foundTimeText: "",
    faultImages: [],
    logs: [],
    progress: { accept: false, started: false, done: false },
    isHandler: false,
    canEdit: false,
    canStart: false,
    canComplete: false,
    canTransfer: false,
    repairDescription: "",
    completeImages: [],
    submitting: false
  },

  onLoad(options) {
    const repairId = options.id || options.repairId;
    if (!repairId) {
      wx.showToast({ title: "参数缺失", icon: "none" });
      return;
    }
    this.setData({ repairId });
  },

  onShow() {
    if (this.data.repairId) {
      this.loadDetail();
      this.loadLogs();
    }
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab });
  },

  async loadDetail() {
    try {
      const res = await api.getRepairDetail(this.data.repairId);
      const detail = res.data || {};
      const st = STATUS_MAP[String(detail.repairStatus)] || STATUS_MAP["0"];
      const faultImages = detail.faultImages
        ? String(detail.faultImages).split(",").filter(Boolean)
        : [];
      const user = auth.getUser && auth.getUser();
      const userId = user && (user.userId || user.id);
      const isHandler =
        detail.repairUserId != null &&
        String(detail.repairUserId) === String(userId);
      const status = String(detail.repairStatus);
      const started = !!detail.startTime;
      const canStart = isHandler && status === "1" && !started;
      const canComplete = isHandler && status === "1" && started;
      const canTransfer = isHandler && status === "1";
      const canEdit =
        (status === "0" || (status === "1" && !started)) &&
        (isHandler ||
          (detail.reporterId != null &&
            String(detail.reporterId) === String(userId)));

      this.setData({
        detail,
        statusText: st.text,
        statusClass: st.cls,
        urgencyText: URGENCY_MAP[String(detail.urgencyLevel)] || "-",
        reporterText:
          (detail.reporterName || "-") +
          (detail.reporterPhone ? " " + detail.reporterPhone : ""),
        handlerText:
          (detail.repairPerson || detail.repairUserName || "-") +
          (detail.repairPhone ? " " + detail.repairPhone : ""),
        foundTimeText: formatDate(detail.foundTime),
        faultImages,
        isHandler,
        canEdit,
        canStart,
        canComplete,
        canTransfer,
        repairDescription: detail.repairDescription || "",
        progress: {
          accept: !!(detail.acceptTime || detail.dispatchTime || detail.repairUserId),
          started: started || status === "2",
          done: status === "2"
        }
      });
    } catch (e) {
      wx.showToast({ title: "加载失败", icon: "none" });
    }
  },

  async loadLogs() {
    try {
      const res = await api.getRepairLogs(this.data.repairId);
      this.setData({ logs: res.data || [] });
    } catch (e) {
      this.setData({ logs: [] });
    }
  },

  previewFaultImage(e) {
    wx.previewImage({
      urls: this.data.faultImages,
      current: this.data.faultImages[e.currentTarget.dataset.index]
    });
  },

  goEdit() {
    wx.navigateTo({
      url: "/pages/repair/form?id=" + this.data.repairId
    });
  },

  goTransfer() {
    wx.navigateTo({
      url: "/pages/repair/transfer?id=" + this.data.repairId
    });
  },

  async handleStart() {
    try {
      await api.startRepair(this.data.repairId);
      wx.showToast({ title: "已开始处理", icon: "success" });
      this.loadDetail();
      this.loadLogs();
    } catch (e) {
      // toast by request layer
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
    const completeImages = this.data.completeImages.concat([
      { tempPath, serverUrl: "", uploading: true }
    ]);
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
          const url =
            body.url || body.fileName || (body.data && body.data.url);
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
    if (!this.data.canComplete) {
      if (this.data.canStart) {
        return this.handleStart();
      }
      return;
    }
    const repairDescription = (this.data.repairDescription || "").trim();
    if (!repairDescription) {
      wx.showToast({ title: "请填写维修说明", icon: "none" });
      return;
    }
    if (this.data.completeImages.some((img) => img.uploading)) {
      wx.showToast({ title: "图片上传中", icon: "none" });
      return;
    }
    this.setData({ submitting: true });
    try {
      const repairImages = this.data.completeImages
        .map((i) => i.serverUrl)
        .filter(Boolean)
        .join(",");
      await api.completeRepair({
        repairId: this.data.repairId,
        repairDescription,
        repairImages
      });
      wx.showToast({ title: "已完成", icon: "success" });
      this.loadDetail();
      this.loadLogs();
    } catch (e) {
      // backend enforces
    } finally {
      this.setData({ submitting: false });
    }
  }
});
