const { api } = require("../../api/index.js");
const { uploadFile } = require("../../services/upload.js");

const ADDR_LOCATING = "\u6b63\u5728\u83b7\u53d6\u4f4d\u7f6e...";
const ADDR_FAIL_LOCATE = "\u83b7\u53d6\u4f4d\u7f6e\u5931\u8d25\uff0c\u8bf7\u70b9\u51fb\u5237\u65b0\u91cd\u8bd5";
const ADDR_FAIL_GEO = "\u81ea\u52a8\u5730\u5740\u89e3\u6790\u4e0d\u53ef\u7528\uff0c\u8bf7\u9009\u62e9\u4f4d\u7f6e";
const ADDR_RESOLVING = "\u6b63\u5728\u89e3\u6790\u5730\u5740...";

Page({
  data: {
    companyId: null,
    companyName: "",
    latitude: null,
    longitude: null,
    addressText: ADDR_LOCATING,
    addressMode: "auto",
    addressFailed: false,
    locating: false,
    checkInType: "0",
    taskList: [],
    taskIndex: -1,
    selectedTaskId: null,
    selectedTaskName: "",
    images: [],
    remark: "\u6b63\u5e38\u6253\u5361",
    submitting: false
  },

  onLoad(options) {
    if (options && options.taskId) {
      this.setData({ selectedTaskId: options.taskId });
    }
  },

  onShow() {
    if (this._pageInitialized) return;
    this._pageInitialized = true;
    this.initPage();
  },

  async initPage() {
    await this.loadCompany();
    this.getLocation();
    if (this.data.companyId) {
      await this.fetchTasks();
    }
    if (this.data.selectedTaskId && this.data.taskList.length) {
      this.syncTaskSelection(this.data.selectedTaskId);
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
        return;
      }
    } catch (e) {}
    this.setData({
      companyId: null,
      companyName: "\u672a\u9009\u62e9\u516c\u53f8"
    });
  },

  getLocation() {
    if (this.data.locating) return;
    this.setData({
      locating: true,
      addressMode: "auto",
      addressFailed: false,
      addressText: ADDR_LOCATING
    });
    wx.getLocation({
      type: "gcj02",
      success: (res) => {
        const lat = res.latitude;
        const lng = res.longitude;
        this.setData({
          latitude: Number(lat.toFixed(6)),
          longitude: Number(lng.toFixed(6)),
          addressText: ADDR_RESOLVING,
          addressFailed: false
        });
        this.resolveAddress(lng, lat);
      },
      fail: () => {
        this.setData({
          locating: false,
          addressFailed: true,
          addressText: ADDR_FAIL_LOCATE,
          latitude: null,
          longitude: null
        });
        wx.showToast({ title: "\u83b7\u53d6\u4f4d\u7f6e\u5931\u8d25", icon: "none" });
      }
    });
  },

  async resolveAddress(longitude, latitude) {
    try {
      const res = await api.reverseGeocode({ longitude: longitude, latitude: latitude }, { loading: false, showError: false });
      const address = res && res.data && res.data.address;
      if (!address || this.isCoordText(address)) {
        this.handleAddressResolveFailure();
        return;
      }
      this.setData({
        locating: false,
        addressMode: "auto",
        addressFailed: false,
        addressText: address,
        latitude: res.data.latitude != null ? res.data.latitude : this.data.latitude,
        longitude: res.data.longitude != null ? res.data.longitude : this.data.longitude
      });
    } catch (e) {
      this.handleAddressResolveFailure();
    }
  },

  handleAddressResolveFailure() {
    this.setData({
      locating: false,
      addressMode: "auto",
      addressFailed: true,
      addressText: ADDR_FAIL_GEO
    });
    if (this._mapSelectionPrompted) return;
    this._mapSelectionPrompted = true;
    wx.showModal({
      title: "\u9009\u62e9\u7b7e\u5230\u4f4d\u7f6e",
      content: "\u81ea\u52a8\u5730\u5740\u89e3\u6790\u6682\u4e0d\u53ef\u7528\uff0c\u8bf7\u5728\u5730\u56fe\u4e2d\u9009\u62e9\u5f53\u524d\u7b7e\u5230\u4f4d\u7f6e\u3002",
      confirmText: "\u9009\u62e9\u4f4d\u7f6e",
      success: (result) => {
        if (result.confirm) this.chooseMapLocation();
      }
    });
  },

  onLocationAction() {
    if (this.data.locating) return;
    if (this.data.addressFailed) {
      this.chooseMapLocation();
      return;
    }
    this.getLocation();
  },

  chooseMapLocation() {
    if (this.data.locating) return;
    this.setData({ locating: true });
    wx.chooseLocation({
      success: (result) => {
        const baseAddress = String(result.address || "").trim();
        const placeName = String(result.name || "").trim();
        const address = baseAddress && placeName && baseAddress.indexOf(placeName) < 0
          ? baseAddress + placeName
          : (baseAddress || placeName);
        if (!this.isChineseAddress(address)) {
          this.setData({
            locating: false,
            addressMode: "auto",
            addressFailed: true,
            addressText: ADDR_FAIL_GEO
          });
          wx.showToast({ title: "\u672a\u83b7\u53d6\u5230\u4e2d\u6587\u5730\u5740", icon: "none" });
          return;
        }
        this.setData({
          latitude: Number(Number(result.latitude).toFixed(6)),
          longitude: Number(Number(result.longitude).toFixed(6)),
          addressText: address,
          addressMode: "map",
          addressFailed: false,
          locating: false
        });
      },
      fail: (error) => {
        this.setData({ locating: false });
        const message = String(error && error.errMsg || "");
        if (message.indexOf("cancel") < 0) {
          wx.showToast({ title: "\u9009\u62e9\u4f4d\u7f6e\u5931\u8d25", icon: "none" });
        }
      }
    });
  },

  isChineseAddress(text) {
    const value = String(text || "").trim();
    return value.length >= 2
      && value.length <= 255
      && /[\u4e00-\u9fff]/.test(value)
      && !this.isCoordText(value)
      && value.indexOf("\u89e3\u6790\u5931\u8d25") < 0;
  },

  isCoordText(text) {
    return /^-?\d+(\.\d+)?\s*,\s*-?\d+(\.\d+)?$/.test(String(text || "").trim());
  },

  async fetchTasks() {
    if (!this.data.companyId) return;
    try {
      const res = await api.listTasksByCompany({ companyId: this.data.companyId });
      const taskList = (res && (res.rows || res.data)) || [];
      this.setData({ taskList: taskList });
      if (this.data.selectedTaskId) {
        this.syncTaskSelection(this.data.selectedTaskId);
      }
    } catch (e) {
      this.setData({ taskList: [] });
    }
  },

  syncTaskSelection(taskId) {
    const idx = this.data.taskList.findIndex((t) => String(t.taskId) === String(taskId));
    if (idx >= 0) {
      const task = this.data.taskList[idx];
      this.setData({
        taskIndex: idx,
        selectedTaskId: task.taskId,
        selectedTaskName: task.taskName || ""
      });
    } else {
      this.setData({ taskIndex: -1, selectedTaskId: null, selectedTaskName: "" });
    }
  },

  onTypeTap(e) {
    const type = e.currentTarget.dataset.type;
    if (type === "0" || type === "1") {
      this.setData({ checkInType: type });
    }
  },

  onTaskChange(e) {
    const idx = Number(e.detail.value);
    const task = this.data.taskList[idx];
    if (!task) return;
    this.setData({
      taskIndex: idx,
      selectedTaskId: task.taskId,
      selectedTaskName: task.taskName || ""
    });
  },

  chooseImage() {
    const remain = 2 - this.data.images.length;
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
    const images = this.data.images.concat([{ tempPath: tempPath, serverUrl: "", uploading: true }]);
    const index = images.length - 1;
    this.setData({ images: images });
    try {
      const body = await uploadFile(tempPath);
      const url = body.fileName || body.url || (body.data && (body.data.fileName || body.data.url));
      if (!url) {
        this.removeImageByIndex(index);
        return;
      }
      this.setData({
        ["images[" + index + "].serverUrl"]: url,
        ["images[" + index + "].uploading"]: false
      });
    } catch (e) {
      this.removeImageByIndex(index);
    }
  },

  removeImageByIndex(index) {
    const images = this.data.images.slice();
    images.splice(index, 1);
    this.setData({ images: images });
  },

  removeImage(e) {
    this.removeImageByIndex(e.currentTarget.dataset.index);
  },

  previewImage(e) {
    const idx = e.currentTarget.dataset.index;
    const urls = this.data.images.map((i) => i.tempPath || i.serverUrl);
    wx.previewImage({ urls: urls, current: urls[idx] });
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value || "" });
  },

  goHistory() {
    wx.navigateTo({ url: "/pages/checkin/history" });
  },

  async handleSubmit() {
    if (this.data.submitting) return;
    if (!this.data.companyId) {
      wx.showToast({ title: "\u8bf7\u5148\u9009\u62e9\u516c\u53f8", icon: "none" });
      return;
    }
    if (!this.data.selectedTaskId) {
      wx.showToast({ title: "\u8bf7\u9009\u62e9\u7ef4\u4fdd\u4efb\u52a1", icon: "none" });
      return;
    }
    if (this.data.latitude == null || this.data.longitude == null) {
      wx.showToast({ title: "\u8bf7\u5148\u83b7\u53d6\u5b9a\u4f4d", icon: "none" });
      return;
    }
    if (this.data.addressFailed || !this.data.addressText || this.isCoordText(this.data.addressText)) {
      wx.showToast({ title: "\u8bf7\u5148\u89e3\u6790\u4e2d\u6587\u5730\u5740", icon: "none" });
      return;
    }
    if (!this.data.images.length) {
      wx.showToast({ title: "\u8bf7\u81f3\u5c11\u4e0a\u4f20\u4e00\u5f20\u7167\u7247", icon: "none" });
      return;
    }
    if (this.data.images.some((img) => img.uploading || !img.serverUrl)) {
      wx.showToast({ title: "\u56fe\u7247\u4e0a\u4f20\u4e2d\uff0c\u8bf7\u7a0d\u5019", icon: "none" });
      return;
    }

    const typeLabel = this.data.checkInType === "0" ? "\u7b7e\u5230" : "\u7b7e\u9000";
    const payload = {
      companyId: this.data.companyId,
      companyName: this.data.companyName,
      taskId: this.data.selectedTaskId,
      checkInType: this.data.checkInType,
      address: this.data.addressText,
      locatedAddress: this.data.addressText,
      addressMode: this.data.addressMode,
      latitude: this.data.latitude,
      longitude: this.data.longitude,
      remark: this.data.remark,
      images: this.data.images.map((img, index) => ({
        imageUrl: img.serverUrl,
        imageName: typeLabel + "\u56fe\u7247" + (index + 1),
        sortOrder: index + 1
      }))
    };

    this.setData({ submitting: true });
    wx.showLoading({ title: "\u63d0\u4ea4\u4e2d...", mask: true });
    try {
      await api.addCheckIn(payload);
      wx.showToast({ title: typeLabel + "\u6210\u529f", icon: "success" });
      this.setData({
        images: [],
        remark: "\u6b63\u5e38\u6253\u5361",
        checkInType: "0"
      });
    } catch (e) {
      wx.showToast({ title: (e && e.msg) || "\u64cd\u4f5c\u5931\u8d25", icon: "none" });
    } finally {
      wx.hideLoading();
      this.setData({ submitting: false });
    }
  }
});
