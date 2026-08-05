const { api } = require("../../api/index.js");
const request = require("../../services/request.js");
const auth = require("../../services/auth.js");

Page({
  data: {
    companyId: null,
    taskId: null,
    companyName: "",
    linked: false,
    systemTypeName: "",
    equipmentName: "",
    customerAddress: "",
    isReported: "0",
    urgencyLevel: "0",
    urgencyOptions: [
      { label: "\u4e00\u822c", value: "0" },
      { label: "\u7d27\u6025", value: "1" },
      { label: "\u7279\u6025", value: "2" }
    ],
    urgencyIndex: 0,
    urgencyText: "\u4e00\u822c",
    faultDescription: "",
    images: [],
    saving: false
  },

  onLoad(options) {
    if (options && options.linked === "1") {
      const decode = (value) => value ? decodeURIComponent(value) : "";
      this.setData({
        linked: true,
        taskId: options.taskId || null,
        companyId: options.companyId || null,
        companyName: decode(options.companyName),
        systemTypeName: decode(options.systemTypeName),
        equipmentName: decode(options.equipmentName),
        customerAddress: decode(options.customerAddress),
        isReported: "1",
        faultDescription: decode(options.faultDescription)
      });
    } else {
      this.loadCompany();
    }
  },

  async loadCompany() {
    try {
      const res = await api.getCurrentCompany();
      if (res && res.data) {
        this.setData({
          companyId: res.data.companyId,
          companyName: res.data.companyName || "\u5f53\u524d\u516c\u53f8"
        });
      }
    } catch (e) {
      this.setData({ companyName: "\u672a\u9009\u62e9\u516c\u53f8" });
    }
  },

  onUrgencyChange(e) {
    const idx = Number(e.detail.value) || 0;
    const opt = this.data.urgencyOptions[idx];
    this.setData({
      urgencyIndex: idx,
      urgencyLevel: opt.value,
      urgencyText: opt.label
    });
  },

  onDescInput(e) {
    this.setData({ faultDescription: e.detail.value });
  },

  chooseImage() {
    const remain = 4 - this.data.images.length;
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

  uploadImg(tempPath) {
    const images = this.data.images.concat([{ tempPath, serverUrl: "", uploading: true }]);
    const index = images.length - 1;
    this.setData({ images });
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
              ["images[" + index + "].serverUrl"]: url,
              ["images[" + index + "].uploading"]: false
            });
          } else {
            this.removeImage(index);
            wx.showToast({ title: body.msg || "\u4e0a\u4f20\u5931\u8d25", icon: "none" });
          }
        } catch (err) {
          this.removeImage(index);
          wx.showToast({ title: "\u4e0a\u4f20\u5931\u8d25", icon: "none" });
        }
      },
      fail: () => {
        this.removeImage(index);
        wx.showToast({ title: "\u4e0a\u4f20\u5931\u8d25", icon: "none" });
      }
    });
  },

  removeImage(e) {
    const index = typeof e === "number" ? e : e.currentTarget.dataset.index;
    const images = this.data.images.slice();
    images.splice(index, 1);
    this.setData({ images });
  },

  previewImage(e) {
    const urls = this.data.images.map((i) => i.tempPath || i.serverUrl);
    wx.previewImage({ urls, current: urls[e.currentTarget.dataset.index] });
  },

  async submit() {
    if (this.data.saving) return;
    if (!this.data.companyId) {
      wx.showToast({ title: "\u8bf7\u5148\u9009\u62e9\u516c\u53f8", icon: "none" });
      return;
    }
    const faultDescription = (this.data.faultDescription || "").trim();
    if (!faultDescription) {
      wx.showToast({ title: "\u8bf7\u586b\u5199\u6545\u969c\u63cf\u8ff0", icon: "none" });
      return;
    }
    if (this.data.images.some((img) => img.uploading)) {
      wx.showToast({ title: "\u56fe\u7247\u4e0a\u4f20\u4e2d", icon: "none" });
      return;
    }
    this.setData({ saving: true });
    try {
      const faultImages = this.data.images.map((i) => i.serverUrl).filter(Boolean).join(",");
      await api.addRepair({
        taskId: this.data.taskId,
        companyId: this.data.companyId,
        companyName: this.data.companyName,
        systemTypeName: this.data.systemTypeName,
        equipmentName: this.data.equipmentName,
        customerAddress: this.data.customerAddress,
        isReported: this.data.linked ? "1" : this.data.isReported,
        urgencyLevel: this.data.urgencyLevel,
        faultDescription,
        faultImages
      });
      wx.showToast({ title: "\u63d0\u4ea4\u6210\u529f", icon: "success" });
      setTimeout(() => wx.navigateBack(), 1200);
    } catch (e) {
      // toast handled by request
    } finally {
      this.setData({ saving: false });
    }
  }
});
